package com.example.mobiletexteditor.editor

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.mobiletexteditor.editor.model.EditorFile
import com.example.mobiletexteditor.editor.model.FileEncoding
import com.example.mobiletexteditor.editor.model.SearchResult
import com.example.mobiletexteditor.editor.model.UndoRedoState
import java.io.File

/**
 * Language syntax modes supported by the editor.
 */
enum class EditorLanguage(val displayName: String) {
    AUTO("Auto-Detect"),
    KOTLIN("Kotlin"),
    MARKDOWN("Markdown"),
    PLAIN_TEXT("Plain Text")
}

/**
 * Central state controller for Member 1 (Editor Engine & File Management).
 */
class EditorManager(
    val fileManager: FileManager,
    val undoRedoManager: UndoRedoManager = UndoRedoManager()
) {
    constructor(context: Context) : this(FileManager(context))

    var activeFile by mutableStateOf(fileManager.createNewFile())
        private set

    var textContent by mutableStateOf("")
        private set

    var isWordWrapEnabled by mutableStateOf(true)
    var isReadOnly by mutableStateOf(false)
    var fontSizeSp by mutableFloatStateOf(14f)

    var languageOverride by mutableStateOf(EditorLanguage.AUTO)

    var searchResult by mutableStateOf(SearchResult())
        private set

    val canUndo: Boolean get() = undoRedoManager.canUndo
    val canRedo: Boolean get() = undoRedoManager.canRedo

    /**
     * Resolves whether Kotlin syntax highlighting should be active.
     */
    val isKotlinLanguage: Boolean get() = when (languageOverride) {
        EditorLanguage.KOTLIN -> true
        EditorLanguage.MARKDOWN -> false
        EditorLanguage.PLAIN_TEXT -> false
        EditorLanguage.AUTO -> activeFile.isKotlin
    }

    /**
     * Resolves whether Markdown syntax highlighting should be active.
     */
    val isMarkdownLanguage: Boolean get() = when (languageOverride) {
        EditorLanguage.MARKDOWN -> true
        EditorLanguage.KOTLIN -> false
        EditorLanguage.PLAIN_TEXT -> false
        EditorLanguage.AUTO -> activeFile.isMarkdown
    }

    /**
     * Updates the editor text buffer and pushes state to undo stack.
     */
    fun updateContent(newText: String, pushToUndo: Boolean = true) {
        if (isReadOnly) return

        if (pushToUndo && newText != textContent) {
            undoRedoManager.pushState(UndoRedoState(text = textContent))
        }

        textContent = newText
        if (!activeFile.isModified) {
            activeFile = activeFile.copy(isModified = true)
        }

        // Re-run active search query if any
        if (searchResult.query.isNotEmpty()) {
            searchResult = SearchManager.search(
                text = newText,
                query = searchResult.query,
                isCaseSensitive = searchResult.isCaseSensitive,
                isMatchWholeWord = searchResult.isMatchWholeWord
            )
        }
    }

    /**
     * Directly sets editor content without marking as user modification (e.g., on open / rollback).
     */
    fun setContentDirectly(newText: String, markModified: Boolean = false) {
        textContent = newText
        undoRedoManager.clear()
        activeFile = activeFile.copy(isModified = markModified)
    }

    fun undo() {
        if (isReadOnly || !canUndo) return
        val previousState = undoRedoManager.undo(UndoRedoState(text = textContent))
        if (previousState != null) {
            textContent = previousState.text
            activeFile = activeFile.copy(isModified = true)
            if (searchResult.query.isNotEmpty()) {
                performSearch(searchResult.query, searchResult.isCaseSensitive, searchResult.isMatchWholeWord)
            }
        }
    }

    fun redo() {
        if (isReadOnly || !canRedo) return
        val nextState = undoRedoManager.redo(UndoRedoState(text = textContent))
        if (nextState != null) {
            textContent = nextState.text
            activeFile = activeFile.copy(isModified = true)
            if (searchResult.query.isNotEmpty()) {
                performSearch(searchResult.query, searchResult.isCaseSensitive, searchResult.isMatchWholeWord)
            }
        }
    }

    fun increaseFontSize() {
        if (fontSizeSp < 32f) {
            fontSizeSp += 2f
        }
    }

    fun decreaseFontSize() {
        if (fontSizeSp > 10f) {
            fontSizeSp -= 2f
        }
    }

    fun newFile(name: String = "Untitled.kt") {
        activeFile = fileManager.createNewFile(name)
        textContent = ""
        undoRedoManager.clear()
        isReadOnly = false
        languageOverride = EditorLanguage.AUTO
    }

    suspend fun openFile(file: File, encoding: FileEncoding = FileEncoding.UTF_8): Result<Unit> {
        val result = fileManager.openFile(file, encoding)
        return result.map { (openedFile, content) ->
            activeFile = openedFile
            textContent = content
            isReadOnly = openedFile.isReadOnly
            languageOverride = EditorLanguage.AUTO
            undoRedoManager.clear()
        }
    }

    suspend fun saveCurrentFile(): Result<EditorFile> {
        val result = fileManager.saveFile(activeFile, textContent)
        return result.onSuccess { saved ->
            activeFile = saved
        }
    }

    suspend fun saveFileAs(fileName: String, encoding: FileEncoding): Result<EditorFile> {
        val result = fileManager.saveFileAs(
            fileName = fileName,
            content = textContent,
            encoding = encoding
        )
        return result.onSuccess { saved ->
            activeFile = saved
        }
    }

    fun performSearch(query: String, isCaseSensitive: Boolean = false, isMatchWholeWord: Boolean = false) {
        searchResult = SearchManager.search(
            text = textContent,
            query = query,
            isCaseSensitive = isCaseSensitive,
            isMatchWholeWord = isMatchWholeWord
        )
    }

    fun findNext() {
        searchResult = SearchManager.findNext(searchResult)
    }

    fun findPrevious() {
        searchResult = SearchManager.findPrevious(searchResult)
    }

    fun replaceCurrent(replacement: String) {
        if (isReadOnly) return
        val (newText, updatedResult) = SearchManager.replaceCurrent(textContent, searchResult, replacement)
        updateContent(newText)
        searchResult = updatedResult
    }

    fun replaceAll(replacement: String): Int {
        if (isReadOnly) return 0
        val (newText, count) = SearchManager.replaceAll(
            text = textContent,
            query = searchResult.query,
            replacement = replacement,
            isCaseSensitive = searchResult.isCaseSensitive,
            isMatchWholeWord = searchResult.isMatchWholeWord
        )
        if (count > 0) {
            updateContent(newText)
        }
        return count
    }

    fun clearSearch() {
        searchResult = SearchResult()
    }
}
