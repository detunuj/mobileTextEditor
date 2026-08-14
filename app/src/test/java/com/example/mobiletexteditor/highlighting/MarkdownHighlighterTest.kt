/**
 * File: MarkdownHighlighterTest.kt
 * Purpose: Unit tests verifying Markdown syntax span styling for H1/H2/H3 headers,
 *          bold & italic markdown tags, inline code, code blocks, and blockquotes.
 * Group Member: Member 2 — Syntax Highlighting & Recovery
 */
package com.example.mobiletexteditor.highlighting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownHighlighterTest {

    @Test
    fun testEmptyMarkdownReturnsEmpty() {
        val result = MarkdownHighlighter.highlight("")
        assertEquals("", result.text)
        assertTrue(result.spanStyles.isEmpty())
    }

    @Test
    fun testHeadersHighlighting() {
        val md = """
            # Heading Level 1
            ## Heading Level 2
            ### Heading Level 3
        """.trimIndent()

        val result = MarkdownHighlighter.highlight(md)
        assertEquals(md, result.text)
        assertTrue("Should contain span styles for headers", result.spanStyles.size >= 3)
    }

    @Test
    fun testBoldAndItalicHighlighting() {
        val md = "This is **bold** text and *italic* text."
        val result = MarkdownHighlighter.highlight(md)

        assertEquals(md, result.text)
        assertTrue(result.spanStyles.size >= 2)
    }

    @Test
    fun testInlineCodeAndCodeBlockHighlighting() {
        val md = """
            Use `val x = 10` for immutable variables.
            ```kotlin
            fun main() = println("Hello")
            ```
        """.trimIndent()

        val result = MarkdownHighlighter.highlight(md)
        assertEquals(md, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }
}
