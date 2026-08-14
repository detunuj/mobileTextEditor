package com.example.mobiletexteditor.highlighting

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.util.regex.Pattern

/**
 * High-performance Compose VisualTransformation that dynamically applies:
 * 1. Language syntax highlighting (Kotlin & Markdown)
 * 2. Visual in-editor Find highlights for all search matches (with active match highlighted in amber/orange).
 */
class SyntaxVisualTransformation(
    private val isKotlin: Boolean,
    private val isMarkdown: Boolean,
    private val searchQuery: String = "",
    private val activeMatchIndex: Int = -1,
    private val isCaseSensitive: Boolean = false,
    private val isMatchWholeWord: Boolean = false
) : VisualTransformation {

    private val allMatchesBgColor = Color(0x99FFE082)   // Soft warm yellow for all matches
    private val allMatchesTextColor = Color(0xFF1B1B1F)  // High contrast text
    private val activeMatchBgColor = Color(0xFFFF9800)   // Vibrant orange for active match
    private val activeMatchTextColor = Color.White       // White text on orange

    override fun filter(text: AnnotatedString): TransformedText {
        // 1. Apply base syntax highlighting
        val syntaxHighlighted: AnnotatedString = when {
            isKotlin -> KotlinHighlighter.highlight(text.text)
            isMarkdown -> MarkdownHighlighter.highlight(text.text)
            else -> text
        }

        // 2. Overlay Find & Replace search match highlights if a search term is present
        if (searchQuery.isEmpty() || text.text.isEmpty()) {
            return TransformedText(syntaxHighlighted, OffsetMapping.Identity)
        }

        val resultBuilder = buildAnnotatedString {
            append(syntaxHighlighted)

            try {
                val patternString = if (isMatchWholeWord) {
                    "\\b${Pattern.quote(searchQuery)}\\b"
                } else {
                    Pattern.quote(searchQuery)
                }

                val flags = if (isCaseSensitive) 0 else Pattern.CASE_INSENSITIVE
                val pattern = Pattern.compile(patternString, flags)
                val matcher = pattern.matcher(text.text)

                var matchIdx = 0
                while (matcher.find()) {
                    val start = matcher.start()
                    val end = matcher.end()

                    if (matchIdx == activeMatchIndex) {
                        // Currently selected match: distinct glowing orange
                        addStyle(
                            SpanStyle(
                                background = activeMatchBgColor,
                                color = activeMatchTextColor,
                                fontWeight = FontWeight.Bold
                            ),
                            start,
                            end
                        )
                    } else {
                        // Other matches: soft clear yellow highlight
                        addStyle(
                            SpanStyle(
                                background = allMatchesBgColor,
                                color = allMatchesTextColor
                            ),
                            start,
                            end
                        )
                    }
                    matchIdx++
                }
            } catch (e: Exception) {
                // Ignore any regex errors and keep syntax highlighting
            }
        }

        return TransformedText(resultBuilder, OffsetMapping.Identity)
    }
}
