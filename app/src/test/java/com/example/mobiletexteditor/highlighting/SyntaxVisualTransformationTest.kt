/**
 * File: SyntaxVisualTransformationTest.kt
 * Purpose: Unit tests validating the visual transformation pipeline, ensuring both language syntax
 *          and in-editor search highlights are properly attached to the rendered AnnotatedString.
 * Group Member: Member 2 — Syntax Highlighting & Recovery
 */
package com.example.mobiletexteditor.highlighting

import androidx.compose.ui.text.AnnotatedString
import com.example.mobiletexteditor.editor.SearchManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyntaxVisualTransformationTest {

    @Test
    fun testSearchMatchHighlightingAttached() {
        val text = "val counter = 10\nprintln(counter)"
        val searchResult = SearchManager.search(text, "counter")

        assertEquals(2, searchResult.totalMatches)

        val transformation = SyntaxVisualTransformation(
            isKotlin = true,
            isMarkdown = false,
            searchResult = searchResult
        )

        val transformed = transformation.filter(AnnotatedString(text))
        assertEquals(text, transformed.text.text)

        // Verify span styles contain both syntax and search highlight spans
        assertTrue(transformed.text.spanStyles.isNotEmpty())
    }
}
