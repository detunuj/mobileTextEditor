/**
 * File: EditorFile.kt
 * Purpose: Domain models defining active editor file state, encodings (UTF-8, UTF-16, ASCII, ISO-8859-1),
 *          and in-memory snapshot state for undo/redo.
 * Group Member: Member 1 — Editor Engine & File Management
 */
package com.example.mobiletexteditor.editor.model

/**
 * Character encodings supported by the text editor.
 */
enum class FileEncoding(val displayName: String, val charsetName: String) {
    UTF_8("UTF-8", "UTF-8"),
    UTF_16("UTF-16", "UTF-16"),
    US_ASCII("US-ASCII", "US-ASCII"),
    ISO_8859_1("ISO-8859-1 (Latin-1)", "ISO-8859-1")
}

/**
 * State of the currently active file loaded in the editor.
 */
data class EditorFile(
    val path: String,
    val name: String,
    val encoding: FileEncoding = FileEncoding.UTF_8,
    val isModified: Boolean = false,
    val isReadOnly: Boolean = false,
    val lastModifiedTimestamp: Long = System.currentTimeMillis()
) {
    val isKotlin: Boolean
        get() = name.endsWith(".kt", ignoreCase = true) || name.endsWith(".kts", ignoreCase = true)

    val isMarkdown: Boolean
        get() = name.endsWith(".md", ignoreCase = true) || name.endsWith(".markdown", ignoreCase = true)

    val isPlainText: Boolean
        get() = !isKotlin && !isMarkdown
}

/**
 * Snapshot of editor content for in-memory undo/redo history.
 */
data class UndoRedoState(
    val text: String,
    val cursorPosition: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
