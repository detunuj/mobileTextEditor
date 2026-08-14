package com.example.mobiletexteditor.version.model

/**
 * Type of line change in a diff comparison.
 */
enum class DiffType {
    /** Line added in the new version (displayed in green) */
    ADDED,
    /** Line deleted from the old version (displayed in red) */
    DELETED,
    /** Line modified between versions */
    MODIFIED,
    /** Line unchanged between versions */
    UNCHANGED
}

/**
 * Represents a single line in a diff view.
 *
 * @param type The type of change (ADDED, DELETED, MODIFIED, UNCHANGED)
 * @param text The textual content of the line
 * @param oldLineNumber Line number in the older version (null if line was added)
 * @param newLineNumber Line number in the newer version (null if line was deleted)
 */
data class DiffLine(
    val type: DiffType,
    val text: String,
    val oldLineNumber: Int? = null,
    val newLineNumber: Int? = null
)

/**
 * Summary result of comparing two text revisions.
 *
 * @param lines Complete line-by-line diff breakdown
 * @param linesAdded Total count of added lines
 * @param linesDeleted Total count of deleted lines
 * @param linesUnchanged Total count of unchanged lines
 */
data class DiffResult(
    val lines: List<DiffLine>,
    val linesAdded: Int,
    val linesDeleted: Int,
    val linesUnchanged: Int
) {
    val totalChanges: Int get() = linesAdded + linesDeleted
}
