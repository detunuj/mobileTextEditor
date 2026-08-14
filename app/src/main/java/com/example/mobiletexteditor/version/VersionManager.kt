/**
 * File: VersionManager.kt
 * Purpose: Central coordinator for the version control system. Orchestrates snapshot creation,
 *          Room SQLite persistence, historical diff comparisons, and file rollback restorations.
 * Group Member: Member 3 — Version Control & Database
 */
package com.example.mobiletexteditor.version

import android.content.Context
import com.example.mobiletexteditor.version.data.FileVersionDao
import com.example.mobiletexteditor.version.data.FileVersionEntity
import com.example.mobiletexteditor.version.data.VersionDatabase
import com.example.mobiletexteditor.version.model.DiffResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets

/**
 * VersionManager coordinates the Incremental Version Control system:
 * - Creates explicit named/numbered snapshots without file duplication (deltas)
 * - Persists snapshots to SQLite via Room
 * - Computes diffs between versions and active editor text
 * - Reconstructs and rolls back files to historical states
 */
class VersionManager(
    private val fileVersionDao: FileVersionDao
) {

    constructor(context: Context) : this(
        VersionDatabase.getInstance(context).fileVersionDao()
    )

    /**
     * Creates a new version snapshot for the specified file.
     *
     * - If this is the first snapshot for the file, it is saved as the Base Version (v1) with full content.
     * - If subsequent snapshots exist, a delta patch is generated relative to the previous version
     *   and stored without duplicating the entire file.
     *
     * @param filePath Absolute path or identifier of the file
     * @param fileName File name for display (e.g. "MainActivity.kt")
     * @param currentContent Current text buffer in the editor
     * @param versionName Optional name/title for the version (defaults to "Version N")
     * @param description Optional commit message or description of changes
     */
    suspend fun createSnapshot(
        filePath: String,
        fileName: String,
        currentContent: String,
        versionName: String = "",
        description: String = ""
    ): Result<FileVersionEntity> = withContext(Dispatchers.IO) {
        try {
            val existingVersions = fileVersionDao.getVersionsForFileAsc(filePath)
            val currentLines = DeltaManager.splitLines(currentContent)
            val totalLines = currentLines.size
            val fileSizeBytes = currentContent.toByteArray(StandardCharsets.UTF_8).size.toLong()

            val newEntity: FileVersionEntity

            if (existingVersions.isEmpty()) {
                // First version -> BASE VERSION (stores full content)
                val vName = versionName.ifBlank { "Version 1 (Initial Base)" }
                newEntity = FileVersionEntity(
                    filePath = filePath,
                    fileName = fileName,
                    versionNumber = 1,
                    versionName = vName,
                    description = description,
                    timestamp = System.currentTimeMillis(),
                    patchData = currentContent,
                    isBaseVersion = true,
                    linesAdded = totalLines,
                    linesDeleted = 0,
                    totalLines = totalLines,
                    fileSizeBytes = fileSizeBytes
                )
            } else {
                // Subsequent version -> INCREMENTAL DELTA PATCH
                val latest = existingVersions.last()
                val latestContent = DeltaManager.reconstructVersion(existingVersions, latest.versionNumber)
                val deltaPatch = DeltaManager.createDelta(latestContent, currentContent)
                val (added, deleted) = DeltaManager.calculateLineChanges(latestContent, currentContent)

                val nextVersionNumber = latest.versionNumber + 1
                val vName = versionName.ifBlank { "Version $nextVersionNumber" }

                newEntity = FileVersionEntity(
                    filePath = filePath,
                    fileName = fileName,
                    versionNumber = nextVersionNumber,
                    versionName = vName,
                    description = description,
                    timestamp = System.currentTimeMillis(),
                    patchData = deltaPatch,
                    isBaseVersion = false,
                    linesAdded = added,
                    linesDeleted = deleted,
                    totalLines = totalLines,
                    fileSizeBytes = fileSizeBytes
                )
            }

            val insertedId = fileVersionDao.insertVersion(newEntity)
            Result.success(newEntity.copy(id = insertedId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Returns a reactive stream of all saved snapshots for a file (newest first).
     */
    fun getVersionHistoryFlow(filePath: String): Flow<List<FileVersionEntity>> {
        return fileVersionDao.getVersionsForFileFlow(filePath)
    }

    /**
     * Retrieves the list of versions for a file (newest first).
     */
    suspend fun getVersionHistory(filePath: String): List<FileVersionEntity> = withContext(Dispatchers.IO) {
        fileVersionDao.getVersionsForFileDesc(filePath)
    }

    /**
     * Reconstructs the exact full text of a file at a specific historical version.
     */
    suspend fun reconstructVersionContent(
        filePath: String,
        targetVersionNumber: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val versionsAsc = fileVersionDao.getVersionsForFileAsc(filePath)
            if (versionsAsc.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("No snapshots found for file: $filePath"))
            }

            val reconstructed = DeltaManager.reconstructVersion(versionsAsc, targetVersionNumber)
            Result.success(reconstructed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Compares two historical versions and generates line-by-line diff information.
     */
    suspend fun compareVersions(
        filePath: String,
        versionNumberA: Int,
        versionNumberB: Int
    ): Result<DiffResult> = withContext(Dispatchers.IO) {
        try {
            val versionsAsc = fileVersionDao.getVersionsForFileAsc(filePath)
            val textA = DeltaManager.reconstructVersion(versionsAsc, versionNumberA)
            val textB = DeltaManager.reconstructVersion(versionsAsc, versionNumberB)

            val diffResult = DiffManager.compare(textA, textB)
            Result.success(diffResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Compares the current editor text with a historical version.
     */
    suspend fun compareWithCurrent(
        filePath: String,
        currentContent: String,
        historicalVersionNumber: Int
    ): Result<DiffResult> = withContext(Dispatchers.IO) {
        try {
            val versionsAsc = fileVersionDao.getVersionsForFileAsc(filePath)
            val historicalText = DeltaManager.reconstructVersion(versionsAsc, historicalVersionNumber)

            val diffResult = DiffManager.compare(historicalText, currentContent)
            Result.success(diffResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Rolls back the file to a target historical version.
     * Reconstructs the target version text and returns it so the editor can load it.
     */
    suspend fun rollbackToVersion(
        filePath: String,
        targetVersionNumber: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        reconstructVersionContent(filePath, targetVersionNumber)
    }

    /**
     * Deletes a specific version snapshot.
     */
    suspend fun deleteVersion(version: FileVersionEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            fileVersionDao.deleteVersion(version)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clears all version history for a given file.
     */
    suspend fun clearHistory(filePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            fileVersionDao.deleteVersionsForFile(filePath)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
