package com.example.mobiletexteditor.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.mobiletexteditor.editor.model.UndoRedoState

/**
 * Dedicated system memory stack tracking granular edits during the active session.
 * Exposes reactive Compose state for canUndo and canRedo.
 */
class UndoRedoManager(private val maxStackSize: Int = 100) {

    private val undoStack = ArrayDeque<UndoRedoState>()
    private val redoStack = ArrayDeque<UndoRedoState>()

    var canUndo by mutableStateOf(false)
        private set

    var canRedo by mutableStateOf(false)
        private set

    private fun updateState() {
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
    }

    /**
     * Pushes a new state to the undo history stack.
     * Clears the redo stack when a new edit occurs.
     */
    fun pushState(state: UndoRedoState) {
        if (undoStack.isNotEmpty() && undoStack.last() == state) {
            return
        }

        if (undoStack.size >= maxStackSize) {
            undoStack.removeFirst()
        }

        undoStack.addLast(state)
        redoStack.clear()
        updateState()
    }

    /**
     * Reverts to the previous edit state.
     *
     * @param currentState The active state before undoing (pushed onto redo stack).
     * @return The previous state, or null if undo stack is empty.
     */
    fun undo(currentState: UndoRedoState): UndoRedoState? {
        if (undoStack.isEmpty()) return null

        val previousState = undoStack.removeLast()
        redoStack.addLast(currentState)
        updateState()
        return previousState
    }

    /**
     * Re-applies the next edit state from the redo stack.
     *
     * @param currentState The active state before redoing (pushed onto undo stack).
     * @return The next state, or null if redo stack is empty.
     */
    fun redo(currentState: UndoRedoState): UndoRedoState? {
        if (redoStack.isEmpty()) return null

        val nextState = redoStack.removeLast()
        undoStack.addLast(currentState)
        updateState()
        return nextState
    }

    /**
     * Clears both undo and redo stacks (e.g. when opening a new file).
     */
    fun clear() {
        undoStack.clear()
        redoStack.clear()
        updateState()
    }

    val undoCount: Int get() = undoStack.size
    val redoCount: Int get() = redoStack.size
}
