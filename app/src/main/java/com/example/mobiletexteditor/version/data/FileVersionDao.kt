package com.example.mobiletexteditor.version.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for version control snapshots.
 */
@Dao
interface FileVersionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: FileVersionEntity): Long

    /**
     * Get all versions for a file in ascending order (v1 -> v2 -> ... -> vN).
     * Critical for sequential delta patch reconstruction.
     */
    @Query("SELECT * FROM file_versions WHERE filePath = :filePath ORDER BY versionNumber ASC")
    suspend fun getVersionsForFileAsc(filePath: String): List<FileVersionEntity>

    /**
     * Get all versions for a file in descending order (latest first) as a reactive Flow.
     * Ideal for UI timeline / history listings.
     */
    @Query("SELECT * FROM file_versions WHERE filePath = :filePath ORDER BY versionNumber DESC")
    fun getVersionsForFileFlow(filePath: String): Flow<List<FileVersionEntity>>

    /**
     * Get all versions for a file in descending order (latest first).
     */
    @Query("SELECT * FROM file_versions WHERE filePath = :filePath ORDER BY versionNumber DESC")
    suspend fun getVersionsForFileDesc(filePath: String): List<FileVersionEntity>

    /**
     * Get the latest snapshot for a specific file.
     */
    @Query("SELECT * FROM file_versions WHERE filePath = :filePath ORDER BY versionNumber DESC LIMIT 1")
    suspend fun getLatestVersion(filePath: String): FileVersionEntity?

    /**
     * Get a specific version snapshot by version number.
     */
    @Query("SELECT * FROM file_versions WHERE filePath = :filePath AND versionNumber = :versionNumber LIMIT 1")
    suspend fun getVersionByNumber(filePath: String, versionNumber: Int): FileVersionEntity?

    /**
     * Get total count of saved versions for a file.
     */
    @Query("SELECT COUNT(*) FROM file_versions WHERE filePath = :filePath")
    suspend fun getVersionCount(filePath: String): Int

    /**
     * Get distinct list of all file paths tracked by the version control system.
     */
    @Query("SELECT DISTINCT filePath FROM file_versions ORDER BY timestamp DESC")
    fun getAllTrackedFilesFlow(): Flow<List<String>>

    @Delete
    suspend fun deleteVersion(version: FileVersionEntity)

    @Query("DELETE FROM file_versions WHERE id = :id")
    suspend fun deleteVersionById(id: Long)

    @Query("DELETE FROM file_versions WHERE filePath = :filePath")
    suspend fun deleteVersionsForFile(filePath: String)
}
