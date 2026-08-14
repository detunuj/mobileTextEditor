/**
 * File: SearchResult.kt
 * Purpose: Domain models for search matches, character index boundaries, and match query result state.
 * Group Member: Member 1 — Editor Engine & File Management
 */
package com.example.mobiletexteditor.editor.model

/**
 * A single match occurrence within the text.
 */
data class SearchMatch(
    val startIndex: Int,
    val endIndex: Int,
    val matchedText: String
)

/**
 * Complete state of an active text search operation.
 */
data class SearchResult(
    val query: String = "",
    val matches: List<SearchMatch> = emptyList(),
    val currentMatchIndex: Int = -1,
    val isCaseSensitive: Boolean = false,
    val isMatchWholeWord: Boolean = false
) {
    val totalMatches: Int get() = matches.size
    val hasMatches: Boolean get() = matches.isNotEmpty()
    val activeMatch: SearchMatch? get() = if (currentMatchIndex in matches.indices) matches[currentMatchIndex] else null
}
