package com.example.mobiletexteditor.highlighting

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * High-performance Compose VisualTransformation that dynamically applies syntax highlighting
 * in real-time without modifying underlying editor buffer state or cursor offsets.
 */
class SyntaxVisualTransformation(
    private val isKotlin: Boolean,
    private val isMarkdown: Boolean
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val highlighted = when {
            isKotlin -> KotlinHighlighter.highlight(text.text)
            isMarkdown -> MarkdownHighlighter.highlight(text.text)
            else -> text
        }

        return TransformedText(
            text = highlighted,
            offsetMapping = OffsetMapping.Identity
        )
    }
}
