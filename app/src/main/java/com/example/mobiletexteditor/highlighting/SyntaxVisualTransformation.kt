package com.example.mobiletexteditor.highlighting

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.mobiletexteditor.editor.model.SearchResult

/**
 * High-performance Compose VisualTransformation that:
 * 1. Dynamically applies syntax highlighting (Kotlin, Markdown, or Plain Text).
 * 2. Highlights active Search and Replace matches in the editor buffer (Yellow for matches, Orange for active match).
 */
class SyntaxVisualTransformation(
    private val isKotlin: Boolean,
    private val isMarkdown: Boolean,
    private val searchResult: SearchResult = SearchResult()
) : VisualTransformation {

    companion object {
        val SearchMatchBgColor = Color(0x99FFEB3B)  // Translucent Yellow
        val SearchMatchTextColor = Color(0xFF000000) // Black text for maximum readability
        val ActiveMatchBgColor = Color(0xFFFF9800)   // Vibrant Amber/Orange
        val ActiveMatchTextColor = Color(0xFF000000)
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        if (rawText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // Step 1: Base syntax highlighting
        val baseHighlighted: AnnotatedString = when {
            isKotlin -> KotlinHighlighter.highlight(rawText)
            isMarkdown -> MarkdownHighlighter.highlight(rawText)
            else -> text
        }

        // Step 2: Overlay search highlights if search is active
        if (!searchResult.hasMatches) {
            return TransformedText(baseHighlighted, OffsetMapping.Identity)
        }

        val combined = buildAnnotatedString {
            append(baseHighlighted)

            val activeMatch = searchResult.activeMatch

            for (match in searchResult.matches) {
                if (match.startIndex in 0..rawText.length && match.endIndex in 0..rawText.length && match.startIndex < match.endIndex) {
                    val isActive = (match == activeMatch)
                    val bgColor = if (isActive) ActiveMatchBgColor else SearchMatchBgColor
                    val textColor = if (isActive) ActiveMatchTextColor else SearchMatchTextColor

                    addStyle(
                        SpanStyle(
                            background = bgColor,
                            color = textColor,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        ),
                        match.startIndex,
                        match.endIndex
                    )
                }
            }
        }

        return TransformedText(
            text = combined,
            offsetMapping = OffsetMapping.Identity
        )
    }
}
