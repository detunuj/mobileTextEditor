package com.example.mobiletexteditor.highlighting

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import java.util.regex.Pattern

/**
 * In-editor syntax highlighter for Markdown documents.
 */
object MarkdownHighlighter {

    val HeaderColor = Color(0xFF64B5F6)      // Bright Blue
    val HeaderSubColor = Color(0xFF4DD0E1)   // Cyan
    val BoldColor = Color(0xFFFFB74D)        // Amber / Gold
    val ItalicColor = Color(0xFFCE93D8)      // Lavender
    val CodeColor = Color(0xFF81C784)        // Mint Green
    val CodeBlockBgColor = Color(0x2281C784) // Soft green tint
    val LinkColor = Color(0xFF4FC3F7)        // Light Blue
    val QuoteColor = Color(0xFFB0BEC5)       // Slate Gray
    val ListMarkerColor = Color(0xFFFF8A65)  // Coral

    private val H1_PATTERN = Pattern.compile("(?m)^#\\s+.*$")
    private val H2_PATTERN = Pattern.compile("(?m)^##\\s+.*$")
    private val H3_PATTERN = Pattern.compile("(?m)^###+\\s+.*$")
    private val BOLD_PATTERN = Pattern.compile("(\\*\\*|__)(.*?)\\1")
    private val ITALIC_PATTERN = Pattern.compile("(?<!\\*)(\\*)(?!\\*)(.*?)(?<!\\*)\\1|(?<!_)(_)(?!_)(.*?)(?<!_)\\3")
    private val INLINE_CODE_PATTERN = Pattern.compile("`([^`]+)`")
    private val CODE_BLOCK_PATTERN = Pattern.compile("(?s)```.*?```")
    private val LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)")
    private val QUOTE_PATTERN = Pattern.compile("(?m)^>\\s+.*$")
    private val LIST_PATTERN = Pattern.compile("(?m)^(\\s*[-*+]|\\s*\\d+\\.)\\s+")

    /**
     * Highlights in-editor Markdown syntax into an [AnnotatedString].
     */
    fun highlight(text: String): AnnotatedString {
        if (text.isEmpty()) return AnnotatedString("")

        return buildAnnotatedString {
            append(text)

            // 1. Headers (Colored + Bold)
            val h1Matcher = H1_PATTERN.matcher(text)
            while (h1Matcher.find()) {
                addStyle(
                    SpanStyle(color = HeaderColor, fontWeight = FontWeight.ExtraBold),
                    h1Matcher.start(),
                    h1Matcher.end()
                )
            }

            val h2Matcher = H2_PATTERN.matcher(text)
            while (h2Matcher.find()) {
                addStyle(
                    SpanStyle(color = HeaderColor, fontWeight = FontWeight.Bold),
                    h2Matcher.start(),
                    h2Matcher.end()
                )
            }

            val h3Matcher = H3_PATTERN.matcher(text)
            while (h3Matcher.find()) {
                addStyle(
                    SpanStyle(color = HeaderSubColor, fontWeight = FontWeight.SemiBold),
                    h3Matcher.start(),
                    h3Matcher.end()
                )
            }

            // 2. Bold text
            val boldMatcher = BOLD_PATTERN.matcher(text)
            while (boldMatcher.find()) {
                addStyle(
                    SpanStyle(color = BoldColor, fontWeight = FontWeight.Bold),
                    boldMatcher.start(),
                    boldMatcher.end()
                )
            }

            // 3. Italic text
            val italicMatcher = ITALIC_PATTERN.matcher(text)
            while (italicMatcher.find()) {
                addStyle(
                    SpanStyle(color = ItalicColor, fontStyle = FontStyle.Italic),
                    italicMatcher.start(),
                    italicMatcher.end()
                )
            }

            // 4. Links ([title](url))
            val linkMatcher = LINK_PATTERN.matcher(text)
            while (linkMatcher.find()) {
                addStyle(
                    SpanStyle(color = LinkColor, textDecoration = TextDecoration.Underline),
                    linkMatcher.start(),
                    linkMatcher.end()
                )
            }

            // 5. Blockquotes (> ...)
            val quoteMatcher = QUOTE_PATTERN.matcher(text)
            while (quoteMatcher.find()) {
                addStyle(
                    SpanStyle(color = QuoteColor, fontStyle = FontStyle.Italic),
                    quoteMatcher.start(),
                    quoteMatcher.end()
                )
            }

            // 6. List Markers (- , * , 1. )
            val listMatcher = LIST_PATTERN.matcher(text)
            while (listMatcher.find()) {
                addStyle(
                    SpanStyle(color = ListMarkerColor, fontWeight = FontWeight.Bold),
                    listMatcher.start(),
                    listMatcher.end()
                )
            }

            // 7. Inline Code (`code`)
            val inlineCodeMatcher = INLINE_CODE_PATTERN.matcher(text)
            while (inlineCodeMatcher.find()) {
                addStyle(
                    SpanStyle(
                        color = CodeColor,
                        fontFamily = FontFamily.Monospace,
                        background = CodeBlockBgColor
                    ),
                    inlineCodeMatcher.start(),
                    inlineCodeMatcher.end()
                )
            }

            // 8. Code Blocks (```...```)
            val codeBlockMatcher = CODE_BLOCK_PATTERN.matcher(text)
            while (codeBlockMatcher.find()) {
                addStyle(
                    SpanStyle(
                        color = CodeColor,
                        fontFamily = FontFamily.Monospace,
                        background = CodeBlockBgColor
                    ),
                    codeBlockMatcher.start(),
                    codeBlockMatcher.end()
                )
            }
        }
    }
}
