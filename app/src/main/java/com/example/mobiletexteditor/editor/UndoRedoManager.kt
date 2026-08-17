package com.example.mobiletexteditor.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.mobiletexteditor.editor.model.UndoRedoState

/*
 Dedicated system memory stack tracking granular edits during the active session.
 Exposes observable Compose states for canUndo and canRedo.
 */
class UndoRedoManager(private val maxStackSize: Int = 100) {

    private val undoStack = ArrayDeque<UndoRedoState>()
    private val redoStack = ArrayDeque<UndoRedoState>()

    var canUndo by mutableStateOf(false)
        private set

    var canRedo by mutableStateOf(false)
        private set

    var undoCount by mutableIntStateOf(0)
        private set

    var redoCount by mutableIntStateOf(0)
        private set

    private fun updateStateFlags() {
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
        undoCount = undoStack.size
        redoCount = redoStack.size
    }

    /*
      Pushes a new state to the undo history stack.
      Clears the redo stack when a new user edit occurs.
     */
    fun pushState(state: UndoRedoState) {
        if (undoStack.isNotEmpty() && undoStack.last().text == state.text) {
            return
        }

        if (undoStack.size >= maxStackSize) {
            undoStack.removeFirst()
        }

        undoStack.addLast(state)
        redoStack.clear()
        updateStateFlags()
    }

    /*
     Reverts to the previous edit state.

     @param currentState The active state before undoing (pushed onto redo stack).
     @return The previous state, or null if undo stack is empty.
     */
    fun undo(currentState: UndoRedoState): UndoRedoState? {
        if (undoStack.isEmpty()) return null

        val previousState = undoStack.removeLast()
        redoStack.addLast(currentState)
        updateStateFlags()
        return previousState
    }

    /*
      Re-applies the next edit state from the redo stack.

      @param currentState The active state before redoing (pushed onto undo stack).
      @return The next state, or null if redo stack is empty.
     */
    fun redo(currentState: UndoRedoState): UndoRedoState? {
        if (redoStack.isEmpty()) return null

        val nextState = redoStack.removeLast()
        undoStack.addLast(currentState)
        updateStateFlags()
        return nextState
    }

    /*
      Clears both undo and redo stacks (e.g. when opening a new file).
     */
    fun clear() {
        undoStack.clear()
        redoStack.clear()
        updateStateFlags()
    }
}
