package com.example.mobiletexteditor.version

import com.example.mobiletexteditor.version.data.FileVersionEntity
import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import com.github.difflib.patch.Patch

/**
 * DeltaManager handles incremental delta generation and text reconstruction without file duplication.
 *
 * Core Principles:
 * 1. Base Snapshot (v1): Stored as raw baseline text.
 * 2. Incremental Snapshots (v2..vN): Stored as unified diff patch strings relative to the previous version.
 * 3. Reconstruction: Sequential patching from Base (v1) -> v2 -> ... -> target version (vK).
 */
object DeltaManager {

    private const val UNIFIED_DIFF_CONTEXT_SIZE = 3

    /**
     * Splits a text block into individual lines while preserving empty lines.
     */
    fun splitLines(text: String): List<String> {
        if (text.isEmpty()) return emptyList()
        return text.split("\r\n", "\n", "\r")
    }

    /**
     * Reconstructs a full multi-line string from a list of lines.
     */
    fun joinLines(lines: List<String>): String {
        return lines.joinToString("\n")
    }

    /**
     * Generates a unified diff patch string representing changes from [baseText] to [revisedText].
     *
     * @return Compact unified diff string representing the delta patch.
     */
    fun createDelta(baseText: String, revisedText: String): String {
        val baseLines = splitLines(baseText)
        val revisedLines = splitLines(revisedText)

        val patch: Patch<String> = DiffUtils.diff(baseLines, revisedLines)
        val diffLines: List<String> = UnifiedDiffUtils.generateUnifiedDiff(
            "original",
            "revised",
            baseLines,
            patch,
            UNIFIED_DIFF_CONTEXT_SIZE
        )

        return joinLines(diffLines)
    }

    /**
     * Applies a unified diff patch string onto [baseText] to produce the revised text.
     *
     * @throws Exception if the patch cannot be applied to the given base text.
     */
    fun applyDelta(baseText: String, patchString: String): String {
        if (patchString.isBlank()) {
            return baseText
        }

        val baseLines = splitLines(baseText)
        val patchLines = splitLines(patchString)

        val patch: Patch<String> = UnifiedDiffUtils.parseUnifiedDiff(patchLines)
        val patchedLines: List<String> = DiffUtils.patch(baseLines, patch)

        return joinLines(patchedLines)
    }

    /**
     * Reconstructs the full file text at [targetVersionNumber] by sequentially applying
     * delta patches starting from the base version (v1).
     *
     * @param versionsAsc List of [FileVersionEntity] sorted in ascending order (v1, v2, ...).
     * @param targetVersionNumber The version number to reconstruct.
     * @return Reconstructed full text of the file at the target version.
     */
    fun reconstructVersion(versionsAsc: List<FileVersionEntity>, targetVersionNumber: Int): String {
        require(versionsAsc.isNotEmpty()) { "Version list cannot be empty for reconstruction" }

        val baseVersion = versionsAsc.firstOrNull { it.versionNumber == 1 || it.isBaseVersion }
            ?: versionsAsc.first()

        var currentText = baseVersion.patchData

        if (targetVersionNumber <= baseVersion.versionNumber) {
            return currentText
        }

        // Apply subsequent delta patches sequentially up to targetVersionNumber
        for (version in versionsAsc) {
            if (version.versionNumber <= baseVersion.versionNumber) continue
            if (version.versionNumber > targetVersionNumber) break

            currentText = applyDelta(currentText, version.patchData)
        }

        return currentText
    }

    /**
     * Calculates the count of added lines and deleted lines between two texts.
     *
     * @return Pair(linesAdded, linesDeleted)
     */
    fun calculateLineChanges(baseText: String, revisedText: String): Pair<Int, Int> {
        val baseLines = splitLines(baseText)
        val revisedLines = splitLines(revisedText)

        val patch = DiffUtils.diff(baseLines, revisedLines)
        var added = 0
        var deleted = 0

        for (delta in patch.deltas) {
            when (delta.type) {
                com.github.difflib.patch.DeltaType.INSERT -> {
                    added += delta.target.lines.size
                }
                com.github.difflib.patch.DeltaType.DELETE -> {
                    deleted += delta.source.lines.size
                }
                com.github.difflib.patch.DeltaType.CHANGE -> {
                    deleted += delta.source.lines.size
                    added += delta.target.lines.size
                }
                else -> Unit
            }
        }

        return Pair(added, deleted)
    }
}
