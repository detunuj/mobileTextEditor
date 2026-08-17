package com.example.mobiletexteditor.highlighting

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import java.util.regex.Pattern

/*
    applies syntax highlighting for Kotlin source code.
 */
object KotlinHighlighter {

    val KeywordColor = Color(0xFFCF5CFF)       // Vibrant Purple / Violet
    val TypeColor = Color(0xFF4FC3F7)          // Light Blue
    val StringColor = Color(0xFF81C784)        // Green
    val CommentColor = Color(0xFF888888)       // Muted Gray
    val AnnotationColor = Color(0xFFFFB74D)    // Amber / Gold

    val NumberColor = Color(0xFFFF8A65)        // Coral / Orange

    val KEYWORDS = setOf(
        "package", "import", "class", "interface", "object", "fun", "val", "var",
        "typealias", "constructor", "init", "enum", "data", "sealed", "annotation",
        "value", "inner", "public", "private", "protected", "internal", "override",
        "abstract", "final", "open", "inline", "infix", "operator", "tailrec",
        "suspend", "const", "lateinit", "vararg", "crossinline", "noinline",
        "companion", "if", "else", "when", "for", "while", "do", "try", "catch",
        "finally", "throw", "return", "break", "continue", "this", "super", "it",
        "true", "false", "null", "as", "is", "in", "by", "get", "set", "where"
    )

    val BUILTIN_TYPES = setOf(
        "Int", "Long", "Short", "Byte", "Float", "Double", "Boolean", "Char",
        "String", "Array", "List", "Map", "Set", "Unit", "Nothing", "Any",
        "CharSequence", "Number", "Comparable", "Throwable", "Exception"
    )

    private val KEYWORD_PATTERN = Pattern.compile("\\b(${KEYWORDS.joinToString("|")})\\b")
    private val TYPE_PATTERN = Pattern.compile("\\b(${BUILTIN_TYPES.joinToString("|")})\\b")
    private val ANNOTATION_PATTERN = Pattern.compile("@[a-zA-Z0-9_]+")
    private val NUMBER_PATTERN = Pattern.compile("\\b(0[xX][0-9a-fA-F]+|0[bB][01]+|[0-9]+(\\.[0-9]+)?([eE][+-]?[0-9]+)?[fFLl]?)\\b")
    private val STRING_PATTERN = Pattern.compile("\"(\\\\.|[^\"\\\\])*\"|'(\\\\.|[^'\\\\])*'|\"\"\"[\\s\\S]*?\"\"\"")
    private val COMMENT_LINE_PATTERN = Pattern.compile("//.*")
    private val COMMENT_BLOCK_PATTERN = Pattern.compile("/\\*[\\s\\S]*?\\*/")

    /*
         Highlights [text] with Kotlin syntax rules .
     */
    fun highlight(text: String, defaultColor: Color = Color.Unspecified): AnnotatedString {
        if (text.isEmpty()) return AnnotatedString("")

        return buildAnnotatedString {
            append(text)

            // 1. Numbers
            val numMatcher = NUMBER_PATTERN.matcher(text)
            while (numMatcher.find()) {
                addStyle(
                    SpanStyle(color = NumberColor),
                    numMatcher.start(),
                    numMatcher.end()
                )
            }

            // 2. Types
            val typeMatcher = TYPE_PATTERN.matcher(text)
            while (typeMatcher.find()) {
                addStyle(
                    SpanStyle(color = TypeColor, fontWeight = FontWeight.SemiBold),
                    typeMatcher.start(),
                    typeMatcher.end()
                )
            }

            // 3. Keywords
            val kwMatcher = KEYWORD_PATTERN.matcher(text)
            while (kwMatcher.find()) {
                addStyle(
                    SpanStyle(color = KeywordColor, fontWeight = FontWeight.Bold),
                    kwMatcher.start(),
                    kwMatcher.end()
                )
            }

            // 4. Annotations (@Composable, @Entity, etc.)
            val annoMatcher = ANNOTATION_PATTERN.matcher(text)
            while (annoMatcher.find()) {
                addStyle(
                    SpanStyle(color = AnnotationColor, fontWeight = FontWeight.Medium),
                    annoMatcher.start(),
                    annoMatcher.end()
                )
            }

            // 5. Strings (single-line and multi-line)
            val strMatcher = STRING_PATTERN.matcher(text)
            while (strMatcher.find()) {
                addStyle(
                    SpanStyle(color = StringColor),
                    strMatcher.start(),
                    strMatcher.end()
                )
            }

            // 6. Comments (Highest precedence: line comments & block comments)
            val blockCommentMatcher = COMMENT_BLOCK_PATTERN.matcher(text)
            while (blockCommentMatcher.find()) {
                addStyle(
                    SpanStyle(color = CommentColor, fontStyle = FontStyle.Italic),
                    blockCommentMatcher.start(),
                    blockCommentMatcher.end()
                )
            }

            val lineCommentMatcher = COMMENT_LINE_PATTERN.matcher(text)
            while (lineCommentMatcher.find()) {
                addStyle(
                    SpanStyle(color = CommentColor, fontStyle = FontStyle.Italic),
                    lineCommentMatcher.start(),
                    lineCommentMatcher.end()
                )
            }
        }
    }
}
