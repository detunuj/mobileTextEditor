package com.example.mobiletexteditor.highlighting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KotlinHighlighterTest {

    @Test
    fun testKeywordsPresent() {
        assertTrue(KotlinHighlighter.KEYWORDS.contains("fun"))
        assertTrue(KotlinHighlighter.KEYWORDS.contains("val"))
        assertTrue(KotlinHighlighter.KEYWORDS.contains("class"))
        assertTrue(KotlinHighlighter.KEYWORDS.contains("override"))
        assertTrue(KotlinHighlighter.KEYWORDS.contains("suspend"))
    }

    @Test
    fun testHighlightNonEmpty() {
        val code = """
            package com.example
            
            // This is a comment
            fun main() {
                val greeting = "Hello"
                println(greeting)
            }
        """.trimIndent()

        val annotated = KotlinHighlighter.highlight(code)
        assertEquals(code, annotated.text)
        assertTrue("Span styles should be attached for syntax elements", annotated.spanStyles.isNotEmpty())
    }
}
