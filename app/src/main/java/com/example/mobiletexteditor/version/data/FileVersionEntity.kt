/**
 * File: FileVersionEntity.kt
 * Purpose: Room database entity representing an incremental snapshot. Stores baseline text for v1
 *          and compact unified diff deltas for subsequent versions, eliminating storage duplication.
 * Group Member: Member 3 — Version Control & Database
 */
package com.example.mobiletexteditor.version.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a snapshot in the incremental version control system.
 *
 * Incremental Delta Storage:
 * - When [versionNumber] is 1 (or [isBaseVersion] is true), [patchData] stores the full baseline file content.
 * - For all subsequent versions (v2, v3, ...), [patchData] stores only the unified diff delta patch
 *   relative to the previous version. This prevents file duplication and keeps storage minimal.
 */
@Entity(
    tableName = "file_versions",
    indices = [
        Index(value = ["filePath"]),
        Index(value = ["filePath", "versionNumber"], unique = true)
    ]
)
data class FileVersionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Absolute path or unique identifier of the file */
    val filePath: String,

    /** Display name of the file (e.g. "Main.kt", "Notes.md") */
    val fileName: String,

    /** Sequential version number: 1, 2, 3, ... */
    val versionNumber: Int,

    /** Human-readable tag / title for this version (e.g. "v1.0 - Initial commit") */
    val versionName: String,

    /** Optional description or commit message describing the changes */
    val description: String = "",

    /** Epoch timestamp in milliseconds when snapshot was captured */
    val timestamp: Long = System.currentTimeMillis(),

    /**
     * Stored content:
     * - Full text for the base version (versionNumber == 1)
     * - Delta unified patch string for incremental versions (versionNumber > 1)
     */
    val patchData: String,

    /** True if this is the base snapshot; false if this is an incremental delta patch */
    val isBaseVersion: Boolean = false,

    /** Count of lines added relative to the prior version */
    val linesAdded: Int = 0,

    /** Count of lines deleted relative to the prior version */
    val linesDeleted: Int = 0,

    /** Total line count of the file at this version */
    val totalLines: Int = 0,

    /** Size of the file in bytes at this version state */
    val fileSizeBytes: Long = 0L
)
