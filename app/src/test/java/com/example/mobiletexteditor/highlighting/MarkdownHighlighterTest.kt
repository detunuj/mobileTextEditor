package com.example.mobiletexteditor.highlighting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownHighlighterTest {

    @Test
    fun testMarkdownHighlighting() {
        val markdown = """
            # Header 1
            ## Header 2
            
            This is **bold** text and *italic* text.
            Here is `inline code` and [Google](https://google.com).
            
            > A blockquote
            
            ```kotlin
            fun test() = true
            ```
        """.trimIndent()

        val annotated = MarkdownHighlighter.highlight(markdown)
        assertEquals(markdown, annotated.text)
        assertTrue("Markdown span styles should be attached", annotated.spanStyles.isNotEmpty())
    }
}
