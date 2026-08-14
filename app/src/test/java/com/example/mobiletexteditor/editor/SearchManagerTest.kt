package com.example.mobiletexteditor.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchManagerTest {

    @Test
    fun testSearchQueryAndMatchNavigation() {
        val sampleText = """
            fun calculateSum(a: Int, b: Int): Int {
                val sum = a + b
                return sum
            }
        """.trimIndent()

        val searchResult = SearchManager.search(
            text = sampleText,
            query = "Int",
            isCaseSensitive = true
        )

        assertEquals("Should find 3 occurrences of 'Int'", 3, searchResult.totalMatches)
        assertEquals(0, searchResult.currentMatchIndex)

        // Find next
        val next = SearchManager.findNext(searchResult)
        assertEquals(1, next.currentMatchIndex)

        // Find prev
        val prev = SearchManager.findPrevious(next)
        assertEquals(0, prev.currentMatchIndex)
    }

    @Test
    fun testReplaceAll() {
        val sample = "val apple = 1\nval appleCount = apple + 5"
        val (replaced, count) = SearchManager.replaceAll(
            text = sample,
            query = "apple",
            replacement = "orange"
        )

        assertEquals(3, count)
        assertEquals("val orange = 1\nval orangeCount = orange + 5", replaced)
    }

    @Test
    fun testReplaceCurrentMatch() {
        val sample = "cat dog cat bird"
        val result = SearchManager.search(sample, "cat")
        assertEquals(2, result.totalMatches)

        val (replaced, updatedResult) = SearchManager.replaceCurrent(sample, result, "tiger")
        assertEquals("tiger dog cat bird", replaced)
        assertEquals(1, updatedResult.totalMatches)
    }
}
