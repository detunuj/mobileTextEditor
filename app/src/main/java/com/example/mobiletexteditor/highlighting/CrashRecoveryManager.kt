package com.example.mobiletexteditor.highlighting

import android.content.Context
import com.example.mobiletexteditor.editor.model.EditorFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Data container for an unsaved recovery draft discovered upon launch.
 */
data class RecoveryDraft(
    val filePath: String,
    val fileName: String,
    val timestamp: Long,
    val recoveredContent: String
)

/**
 * CrashRecoveryManager provides automated fault tolerance:
 * 1. Background Auto-Backup: Periodically writes active editor buffer to temporary cache (every 10s).
 * 2. Crash Detection & Recovery: Detects uncommitted buffer caches upon restart and restores unsaved work.
 */
class CrashRecoveryManager(private val context: Context) {

    private val backupDir = File(context.cacheDir, "crash_backups").apply {
        if (!exists()) mkdirs()
    }

    private var autoBackupJob: Job? = null
    private var lastSavedHash: String = ""

    /**
     * Computes a safe file key for backup mapping based on file path.
     */
    private fun getBackupFile(filePath: String): File {
        val safeName = if (filePath.isBlank()) {
            "untitled_backup.draft"
        } else {
            val md5 = MessageDigest.getInstance("MD5").digest(filePath.toByteArray())
                .joinToString("") { "%02x".format(it) }
            "backup_$md5.draft"
        }
        return File(backupDir, safeName)
    }

    /**
     * Saves a recovery draft cache file immediately to disk.
     */
    suspend fun saveBackup(
        filePath: String,
        fileName: String,
        content: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupFile = getBackupFile(filePath)
            val currentHash = content.hashCode().toString()
            if (currentHash == lastSavedHash) return@withContext false

            FileOutputStream(backupFile).use { stream ->
                stream.bufferedWriter(Charsets.UTF_8).use { writer ->
                    writer.write("FILE_PATH:$filePath\n")
                    writer.write("FILE_NAME:$fileName\n")
                    writer.write("TIMESTAMP:${System.currentTimeMillis()}\n")
                    writer.write("---CONTENT_START---\n")
                    writer.write(content)
                    writer.flush()
                }
            }
            lastSavedHash = currentHash
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if an unsaved recovery draft exists for the given file.
     *
     * @param filePath The file to check.
     * @param originalFileLastModified The modification timestamp of the original file.
     */
    suspend fun checkForRecoveryDraft(
        filePath: String,
        originalFileLastModified: Long
    ): RecoveryDraft? = withContext(Dispatchers.IO) {
        try {
            val backupFile = getBackupFile(filePath)
            if (!backupFile.exists() || backupFile.length() == 0L) return@withContext null

            val lines = FileInputStream(backupFile).bufferedReader(Charsets.UTF_8).readLines()
            if (lines.size < 4) return@withContext null

            val draftPath = lines.firstOrNull { it.startsWith("FILE_PATH:") }?.substringAfter("FILE_PATH:") ?: ""
            val draftName = lines.firstOrNull { it.startsWith("FILE_NAME:") }?.substringAfter("FILE_NAME:") ?: ""
            val draftTimestamp = lines.firstOrNull { it.startsWith("TIMESTAMP:") }?.substringAfter("TIMESTAMP:")?.toLongOrNull() ?: 0L

            val contentStartIndex = lines.indexOf("---CONTENT_START---")
            if (contentStartIndex == -1 || contentStartIndex >= lines.size - 1) return@withContext null

            val recoveredContent = lines.subList(contentStartIndex + 1, lines.size).joinToString("\n")

            // Only report recovery if draft is newer than the saved file or file was unsaved
            if (draftTimestamp > originalFileLastModified || originalFileLastModified == 0L) {
                RecoveryDraft(
                    filePath = draftPath,
                    fileName = draftName,
                    timestamp = draftTimestamp,
                    recoveredContent = recoveredContent
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Clears and deletes the backup cache when user explicitly saves or discards.
     */
    suspend fun clearBackup(filePath: String) = withContext(Dispatchers.IO) {
        val backupFile = getBackupFile(filePath)
        if (backupFile.exists()) {
            backupFile.delete()
        }
        lastSavedHash = ""
    }

    /**
     * Starts the automated 10-second background backup loop.
     */
    fun startAutoBackup(
        scope: CoroutineScope,
        getActiveFile: () -> EditorFile,
        getActiveText: () -> String,
        intervalMs: Long = 10000L
    ) {
        autoBackupJob?.cancel()
        autoBackupJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(intervalMs)
                val file = getActiveFile()
                val text = getActiveText()

                if (file.isModified && text.isNotEmpty()) {
                    saveBackup(file.path, file.name, text)
                }
            }
        }
    }

    /**
     * Stops the background auto-backup loop.
     */
    fun stopAutoBackup() {
        autoBackupJob?.cancel()
        autoBackupJob = null
    }
}
