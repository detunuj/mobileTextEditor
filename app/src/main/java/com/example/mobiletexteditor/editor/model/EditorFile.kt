package com.example.mobiletexteditor.editor.model

import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets


enum class FileEncoding(val displayName: String, val charset: Charset) {
    UTF_8("UTF-8", StandardCharsets.UTF_8),
    UTF_16("UTF-16", StandardCharsets.UTF_16),
    US_ASCII("US-ASCII", StandardCharsets.US_ASCII),
    ISO_8859_1("ISO-8859-1", StandardCharsets.ISO_8859_1);

    companion object {
        fun fromDisplayName(name: String): FileEncoding {
            return entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: UTF_8
        }
    }
}

/*
  Represents the active file opened in the editor.
 */
data class EditorFile(
    val file: File? = null,
    val name: String = "Untitled.kt",
    val path: String = "",
    val encoding: FileEncoding = FileEncoding.UTF_8,
    val isReadOnly: Boolean = false,
    val isModified: Boolean = false,
    val lastModifiedTimestamp: Long = System.currentTimeMillis()
) {
    val isNewFile: Boolean get() = file == null || path.isBlank()
    val extension: String get() = name.substringAfterLast('.', "").lowercase()
    val isKotlin: Boolean get() = extension == "kt" || extension == "kts"
    val isMarkdown: Boolean get() = extension == "md" || extension == "markdown"
}
