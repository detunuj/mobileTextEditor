package com.example.mobiletexteditor.editor

import com.example.mobiletexteditor.editor.model.UndoRedoState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UndoRedoManagerTest {

    @Test
    fun testUndoRedoStackOperations() {
        val manager = UndoRedoManager(maxStackSize = 10)
        assertFalse(manager.canUndo)
        assertFalse(manager.canRedo)

        val state1 = UndoRedoState(text = "Hello")
        val state2 = UndoRedoState(text = "Hello World")
        val state3 = UndoRedoState(text = "Hello World!")

        manager.pushState(state1)
        manager.pushState(state2)

        assertTrue(manager.canUndo)
        assertFalse(manager.canRedo)

        // Undo from state3 -> state2
        val undone = manager.undo(state3)
        assertEquals(state2, undone)
        assertTrue(manager.canRedo)

        // Redo back to state3
        val redone = manager.redo(state2)
        assertEquals(state3, redone)
    }

    @Test
    fun testClearResetsStacks() {
        val manager = UndoRedoManager()
        manager.pushState(UndoRedoState(text = "abc"))
        assertTrue(manager.canUndo)

        manager.clear()
        assertFalse(manager.canUndo)
        assertFalse(manager.canRedo)
    }
}
