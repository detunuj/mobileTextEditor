/**
 * File: KotlinHighlighterTest.kt
 * Purpose: Unit tests verifying correct tokenizer span generation for Kotlin keywords, types,
 *          string literals, single & multi-line comments, and annotations.
 * Group Member: Member 2 — Syntax Highlighting & Recovery
 */
package com.example.mobiletexteditor.highlighting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KotlinHighlighterTest {

    @Test
    fun testEmptyStringProducesEmptyAnnotatedString() {
        val result = KotlinHighlighter.highlight("")
        assertEquals("", result.text)
        assertTrue(result.spanStyles.isEmpty())
    }

    @Test
    fun testKeywordAndTypeHighlighting() {
        val code = "class UserService(val id: Int, var name: String)"
        val result = KotlinHighlighter.highlight(code)

        assertEquals(code, result.text)
        assertTrue("Should have span styles applied for keywords and types", result.spanStyles.isNotEmpty())
    }

    @Test
    fun testAnnotationAndCommentHighlighting() {
        val code = """
            @Composable
            fun MainScreen() {
                // This is a line comment
                /* Block comment */
            }
        """.trimIndent()

        val result = KotlinHighlighter.highlight(code)
        assertEquals(code, result.text)
        assertTrue(result.spanStyles.size >= 3)
    }

    @Test
    fun testStringLiteralHighlighting() {
        val code = "val greeting = \"Hello, Mobile Editor!\""
        val result = KotlinHighlighter.highlight(code)

        assertEquals(code, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }
}
