/**
 * File: SearchManagerTest.kt
 * Purpose: Unit tests validating search match detection, whole-word and case-sensitive filters,
 *          forward/backward match navigation, and single & bulk text replacement.
 * Group Member: Member 1 — Editor Engine & File Management
 */
package com.example.mobiletexteditor.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchManagerTest {

    private val sampleCode = """
        fun calculateTotal(price: Double, tax: Double): Double {
            val total = price + (price * tax)
            return total
        }
    """.trimIndent()

    @Test
    fun testBasicSearchFindsAllOccurrences() {
        val result = SearchManager.search(sampleCode, "Double")
        assertEquals(3, result.totalMatches)
        assertEquals(0, result.currentMatchIndex)
        assertTrue(result.hasMatches)
    }

    @Test
    fun testCaseSensitiveSearch() {
        val caseSensitiveResult = SearchManager.search(sampleCode, "double", isCaseSensitive = true)
        assertEquals(0, caseSensitiveResult.totalMatches)

        val caseInsensitiveResult = SearchManager.search(sampleCode, "double", isCaseSensitive = false)
        assertEquals(3, caseInsensitiveResult.totalMatches)
    }

    @Test
    fun testWholeWordSearch() {
        val partialResult = SearchManager.search(sampleCode, "tot", isMatchWholeWord = false)
        assertEquals(3, partialResult.totalMatches) // calculateTotal, total, total

        val wholeWordResult = SearchManager.search(sampleCode, "tot", isMatchWholeWord = true)
        assertEquals(0, wholeWordResult.totalMatches)

        val wholeWordTotal = SearchManager.search(sampleCode, "total", isMatchWholeWord = true)
        assertEquals(2, wholeWordTotal.totalMatches) // matches variable names, ignores calculateTotal
    }

    @Test
    fun testMatchNavigationNextAndPrevious() {
        val search = SearchManager.search(sampleCode, "Double")
        assertEquals(0, search.currentMatchIndex)

        val next1 = SearchManager.findNext(search)
        assertEquals(1, next1.currentMatchIndex)

        val next2 = SearchManager.findNext(next1)
        assertEquals(2, next2.currentMatchIndex)

        // Wraps around to 0
        val wrappedNext = SearchManager.findNext(next2)
        assertEquals(0, wrappedNext.currentMatchIndex)

        // Wraps backward to last index
        val wrappedPrev = SearchManager.findPrevious(wrappedNext)
        assertEquals(2, wrappedPrev.currentMatchIndex)
    }

    @Test
    fun testReplaceCurrentMatch() {
        val search = SearchManager.search(sampleCode, "Double")
        val (replacedText, newSearch) = SearchManager.replaceCurrent(sampleCode, search, "Float")

        assertTrue(replacedText.startsWith("fun calculateTotal(price: Float"))
        assertEquals(2, newSearch.totalMatches)
    }

    @Test
    fun testReplaceAllMatches() {
        val (replacedText, count) = SearchManager.replaceAll(sampleCode, "Double", "BigDecimal")
        assertEquals(3, count)
        assertFalse(replacedText.contains("Double"))
        assertTrue(replacedText.contains("BigDecimal"))
    }
}
