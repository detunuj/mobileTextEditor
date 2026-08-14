/**
 * File: UndoRedoManagerTest.kt
 * Purpose: Unit tests verifying in-memory undo/redo stack state management, boundary conditions,
 *          redo clearing on new edits, and max stack capacity constraints.
 * Group Member: Member 1 — Editor Engine & File Management
 */
package com.example.mobiletexteditor.editor

import com.example.mobiletexteditor.editor.model.UndoRedoState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UndoRedoManagerTest {

    private lateinit var undoRedoManager: UndoRedoManager

    @Before
    fun setUp() {
        undoRedoManager = UndoRedoManager(maxStackSize = 10)
    }

    @Test
    fun testInitialStateIsEmpty() {
        assertFalse(undoRedoManager.canUndo)
        assertFalse(undoRedoManager.canRedo)
        assertEquals(0, undoRedoManager.undoCount)
        assertEquals(0, undoRedoManager.redoCount)
    }

    @Test
    fun testPushStateEnablesUndo() {
        undoRedoManager.pushState(UndoRedoState("Hello"))
        assertTrue(undoRedoManager.canUndo)
        assertFalse(undoRedoManager.canRedo)
        assertEquals(1, undoRedoManager.undoCount)
    }

    @Test
    fun testUndoAndRedoCycle() {
        undoRedoManager.pushState(UndoRedoState("State 1"))
        undoRedoManager.pushState(UndoRedoState("State 2"))

        // Undo from Current State 3
        val previousState = undoRedoManager.undo(UndoRedoState("State 3"))
        assertNotNull(previousState)
        assertEquals("State 2", previousState?.text)
        assertTrue(undoRedoManager.canRedo)

        // Redo back to State 3
        val redoneState = undoRedoManager.redo(UndoRedoState("State 2"))
        assertNotNull(redoneState)
        assertEquals("State 3", redoneState?.text)
    }

    @Test
    fun testNewEditClearsRedoStack() {
        undoRedoManager.pushState(UndoRedoState("A"))
        undoRedoManager.pushState(UndoRedoState("B"))

        // Undo B
        undoRedoManager.undo(UndoRedoState("C"))
        assertTrue(undoRedoManager.canRedo)

        // New typing occurs -> Redo must clear
        undoRedoManager.pushState(UndoRedoState("D"))
        assertFalse(undoRedoManager.canRedo)
    }

    @Test
    fun testMaxStackSizeBoundary() {
        val manager = UndoRedoManager(maxStackSize = 3)
        manager.pushState(UndoRedoState("1"))
        manager.pushState(UndoRedoState("2"))
        manager.pushState(UndoRedoState("3"))
        manager.pushState(UndoRedoState("4"))

        assertEquals(3, manager.undoCount)
    }

    @Test
    fun testClearRemovesAllStates() {
        undoRedoManager.pushState(UndoRedoState("1"))
        undoRedoManager.pushState(UndoRedoState("2"))
        undoRedoManager.clear()

        assertFalse(undoRedoManager.canUndo)
        assertFalse(undoRedoManager.canRedo)
        assertNull(undoRedoManager.undo(UndoRedoState("Current")))
    }
}
