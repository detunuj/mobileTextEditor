/**
 * File: DiffManagerTest.kt
 * Purpose: Unit tests validating the visual diff classification engine, ensuring correct assignment of
 *          ADDED, DELETED, and UNCHANGED lines, accurate line number gutter indexes, and summary formatting.
 * Group Member: Member 3 — Version Control & Database
 */
package com.example.mobiletexteditor.version

import com.example.mobiletexteditor.version.model.DiffType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiffManagerTest {

    @Test
    fun testIdenticalTextsProduceNoChanges() {
        val text = "Line 1\nLine 2\nLine 3"
        val result = DiffManager.compare(text, text)

        assertEquals(0, result.linesAdded)
        assertEquals(0, result.linesDeleted)
        assertEquals(3, result.linesUnchanged)
        assertTrue(result.lines.all { it.type == DiffType.UNCHANGED })
    }

    @Test
    fun testAddedLinesDetection() {
        val oldText = "Line 1\nLine 2"
        val newText = "Line 1\nLine 2\nLine 3 (New)"

        val result = DiffManager.compare(oldText, newText)

        assertEquals(1, result.linesAdded)
        assertEquals(0, result.linesDeleted)
        assertEquals(2, result.linesUnchanged)

        val addedLine = result.lines.find { it.type == DiffType.ADDED }
        assertEquals("Line 3 (New)", addedLine?.text)
        assertEquals(3, addedLine?.newLineNumber)
        assertEquals(null, addedLine?.oldLineNumber)
    }

    @Test
    fun testDeletedLinesDetection() {
        val oldText = "Line 1\nLine 2 (To Delete)\nLine 3"
        val newText = "Line 1\nLine 3"

        val result = DiffManager.compare(oldText, newText)

        assertEquals(0, result.linesAdded)
        assertEquals(1, result.linesDeleted)
        assertEquals(2, result.linesUnchanged)

        val deletedLine = result.lines.find { it.type == DiffType.DELETED }
        assertEquals("Line 2 (To Delete)", deletedLine?.text)
        assertEquals(2, deletedLine?.oldLineNumber)
        assertEquals(null, deletedLine?.newLineNumber)
    }

    @Test
    fun testFormatChangeSummary() {
        assertEquals("No changes", DiffManager.formatChangeSummary(0, 0))
        assertEquals("+5 lines", DiffManager.formatChangeSummary(5, 0))
        assertEquals("-3 lines", DiffManager.formatChangeSummary(0, 3))
        assertEquals("+10, -4 lines", DiffManager.formatChangeSummary(10, 4))
    }
}
