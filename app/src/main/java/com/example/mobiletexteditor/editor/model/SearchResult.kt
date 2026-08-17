package com.example.mobiletexteditor.editor.model

/*
  Snapshot of text and cursor selection in the undo/redo stack.
 */
data class UndoRedoState(
    val text: String,
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0
)

/*
 Represents a single search match in the editor text.
 */
data class SearchMatch(
    val startIndex: Int,
    val endIndex: Int,
    val lineNumber: Int,
    val lineText: String
)

/*
  Result container for active search query state.
 */
data class SearchResult(
    val query: String = "",
    val matches: List<SearchMatch> = emptyList(),
    val currentMatchIndex: Int = -1,
    val isCaseSensitive: Boolean = false,
    val isMatchWholeWord: Boolean = false
) {
    val hasMatches: Boolean get() = matches.isNotEmpty()
    val totalMatches: Int get() = matches.size
    val currentMatchNumber: Int get() = if (currentMatchIndex in matches.indices) currentMatchIndex + 1 else 0
    val activeMatch: SearchMatch? get() = if (currentMatchIndex in matches.indices) matches[currentMatchIndex] else null
}
