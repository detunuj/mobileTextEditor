package com.example.mobiletexteditor.version

import com.example.mobiletexteditor.version.model.DiffType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiffManagerTest {

    @Test
    fun testDiffAddedLines() {
        val oldText = "Line A\nLine B"
        val newText = "Line A\nLine B\nLine C"

        val diffResult = DiffManager.compare(oldText, newText)

        assertEquals("Should have 1 added line", 1, diffResult.linesAdded)
        assertEquals("Should have 0 deleted lines", 0, diffResult.linesDeleted)
        assertEquals("Should have 2 unchanged lines", 2, diffResult.linesUnchanged)

        val addedLine = diffResult.lines.first { it.type == DiffType.ADDED }
        assertEquals("Line C", addedLine.text)
        assertEquals(3, addedLine.newLineNumber)
    }

    @Test
    fun testDiffDeletedLines() {
        val oldText = "Line 1\nLine 2\nLine 3"
        val newText = "Line 1\nLine 3"

        val diffResult = DiffManager.compare(oldText, newText)

        assertEquals("Should have 0 added lines", 0, diffResult.linesAdded)
        assertEquals("Should have 1 deleted line", 1, diffResult.linesDeleted)

        val deletedLine = diffResult.lines.first { it.type == DiffType.DELETED }
        assertEquals("Line 2", deletedLine.text)
        assertEquals(2, deletedLine.oldLineNumber)
    }

    @Test
    fun testFormatChangeSummary() {
        assertEquals("No changes", DiffManager.formatChangeSummary(0, 0))
        assertEquals("+5 lines", DiffManager.formatChangeSummary(5, 0))
        assertEquals("-3 lines", DiffManager.formatChangeSummary(0, 3))
        assertEquals("+4, -2 lines", DiffManager.formatChangeSummary(4, 2))
    }
}
