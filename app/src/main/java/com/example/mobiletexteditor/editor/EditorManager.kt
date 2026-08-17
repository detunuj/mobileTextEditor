package com.example.mobiletexteditor.editor

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.mobiletexteditor.editor.model.EditorFile
import com.example.mobiletexteditor.editor.model.FileEncoding
import com.example.mobiletexteditor.editor.model.SearchResult
import com.example.mobiletexteditor.editor.model.UndoRedoState
import java.io.File

/*
  Editor Engine & File Management
 */
class EditorManager(
    val fileManager: FileManager,
    val undoRedoManager: UndoRedoManager = UndoRedoManager()
) {
    constructor(context: Context) : this(FileManager(context))

    var activeFile by mutableStateOf(fileManager.createNewFile())
        private set

    var textFieldValue by mutableStateOf(TextFieldValue(""))
        private set

    val textContent: String get() = textFieldValue.text

    var isWordWrapEnabled by mutableStateOf(true)
    var isReadOnly by mutableStateOf(false)
    var fontSizeSp by mutableFloatStateOf(14f)

    var searchResult by mutableStateOf(SearchResult())
        private set

    val canUndo: Boolean get() = undoRedoManager.canUndo
    val canRedo: Boolean get() = undoRedoManager.canRedo

    /*
    Increases  font size.
     */
    fun zoomIn() {
        if (fontSizeSp < 32f) {
            fontSizeSp = (fontSizeSp + 2f).coerceAtMost(32f)
        }
    }

    /*
     Decreases font size.
     */
    fun zoomOut() {
        if (fontSizeSp > 10f) {
            fontSizeSp = (fontSizeSp - 2f).coerceAtLeast(10f)
        }
    }

    /*
      Updates the editor text buffer and pushes state to undo stack.
     */
    fun updateContent(newValue: TextFieldValue, pushToUndo: Boolean = true) {
        if (isReadOnly) return

        if (pushToUndo && newValue.text != textFieldValue.text) {
            undoRedoManager.pushState(
                UndoRedoState(
                    text = textFieldValue.text,
                    selectionStart = textFieldValue.selection.start,
                    selectionEnd = textFieldValue.selection.end
                )
            )
        }

        textFieldValue = newValue
        if (!activeFile.isModified) {
            activeFile = activeFile.copy(isModified = true)
        }

        // Re-run active search query if any
        if (searchResult.query.isNotEmpty()) {
            searchResult = SearchManager.search(
                text = newValue.text,
                query = searchResult.query,
                isCaseSensitive = searchResult.isCaseSensitive,
                isMatchWholeWord = searchResult.isMatchWholeWord
            )
        }
    }

    fun updateContent(newText: String, pushToUndo: Boolean = true) {
        updateContent(
            TextFieldValue(
                text = newText,
                selection = TextRange(newText.length)
            ),
            pushToUndo = pushToUndo
        )
    }

    /*
      Directly sets editor content without pushing to undo.
     */
    fun setContentDirectly(newText: String, markModified: Boolean = false) {
        textFieldValue = TextFieldValue(text = newText, selection = TextRange(0))
        undoRedoManager.clear()
        activeFile = activeFile.copy(isModified = markModified)
    }

    fun undo() {
        if (isReadOnly) return
        val previousState = undoRedoManager.undo(
            UndoRedoState(
                text = textFieldValue.text,
                selectionStart = textFieldValue.selection.start,
                selectionEnd = textFieldValue.selection.end
            )
        )
        if (previousState != null) {
            val safeStart = previousState.selectionStart.coerceIn(0, previousState.text.length)
            val safeEnd = previousState.selectionEnd.coerceIn(safeStart, previousState.text.length)
            textFieldValue = TextFieldValue(
                text = previousState.text,
                selection = TextRange(safeStart, safeEnd)
            )
            activeFile = activeFile.copy(isModified = true)

            // Re-run search matches on new text
            if (searchResult.query.isNotEmpty()) {
                searchResult = SearchManager.search(
                    text = previousState.text,
                    query = searchResult.query,
                    isCaseSensitive = searchResult.isCaseSensitive,
                    isMatchWholeWord = searchResult.isMatchWholeWord
                )
            }
        }
    }

    fun redo() {
        if (isReadOnly) return
        val nextState = undoRedoManager.redo(
            UndoRedoState(
                text = textFieldValue.text,
                selectionStart = textFieldValue.selection.start,
                selectionEnd = textFieldValue.selection.end
            )
        )
        if (nextState != null) {
            val safeStart = nextState.selectionStart.coerceIn(0, nextState.text.length)
            val safeEnd = nextState.selectionEnd.coerceIn(safeStart, nextState.text.length)
            textFieldValue = TextFieldValue(
                text = nextState.text,
                selection = TextRange(safeStart, safeEnd)
            )
            activeFile = activeFile.copy(isModified = true)

            // Re-run search matches on new text
            if (searchResult.query.isNotEmpty()) {
                searchResult = SearchManager.search(
                    text = nextState.text,
                    query = searchResult.query,
                    isCaseSensitive = searchResult.isCaseSensitive,
                    isMatchWholeWord = searchResult.isMatchWholeWord
                )
            }
        }
    }

    fun newFile(name: String = "Untitled.kt") {
        activeFile = fileManager.createNewFile(name)
        textFieldValue = TextFieldValue("")
        undoRedoManager.clear()
        isReadOnly = false
    }

    suspend fun openFile(file: File, encoding: FileEncoding = FileEncoding.UTF_8): Result<Unit> {
        val result = fileManager.openFile(file, encoding)
        return result.map { (openedFile, content) ->
            activeFile = openedFile
            textFieldValue = TextFieldValue(content)
            isReadOnly = openedFile.isReadOnly
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
        syncCursorWithActiveMatch()
    }

    fun findNext() {
        searchResult = SearchManager.findNext(searchResult)
        syncCursorWithActiveMatch()
    }

    fun findPrevious() {
        searchResult = SearchManager.findPrevious(searchResult)
        syncCursorWithActiveMatch()
    }

    private fun syncCursorWithActiveMatch() {
        val match = searchResult.activeMatch
        if (match != null && match.startIndex <= textContent.length && match.endIndex <= textContent.length) {
            textFieldValue = textFieldValue.copy(
                selection = TextRange(match.startIndex, match.endIndex)
            )
        }
    }

    fun replaceCurrent(replacement: String) {
        if (isReadOnly) return
        val (newText, updatedResult) = SearchManager.replaceCurrent(textContent, searchResult, replacement)
        updateContent(newText)
        searchResult = updatedResult
        syncCursorWithActiveMatch()
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
            searchResult = SearchManager.search(
                text = newText,
                query = searchResult.query,
                isCaseSensitive = searchResult.isCaseSensitive,
                isMatchWholeWord = searchResult.isMatchWholeWord
            )
        }
        return count
    }

    fun clearSearch() {
        searchResult = SearchResult()
    }
}
