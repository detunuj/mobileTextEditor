/**
 * File: FileManager.kt
 * Purpose: Handles device storage file lifecycle operations including New, Open, Save, and Save As
 *          with multi-encoding support (UTF-8, UTF-16, US-ASCII, ISO-8859-1) and recent files tracking.
 * Group Member: Member 1 — Editor Engine & File Management
 */
package com.example.mobiletexteditor.editor

import android.content.Context
import com.example.mobiletexteditor.editor.model.EditorFile
import com.example.mobiletexteditor.editor.model.FileEncoding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset

/**
 * Handles all file system I/O, encoding/decoding, and recent file tracking.
 */
class FileManager(private val context: Context) {

    private val documentsDir: File = File(context.filesDir, "documents").apply {
        if (!exists()) mkdirs()
    }

    private val recentFilesList = mutableListOf<File>()

    /**
     * Creates a new blank file representation in memory.
     */
    fun createNewFile(name: String = "Untitled.kt"): EditorFile {
        return EditorFile(
            path = "",
            name = name,
            encoding = FileEncoding.UTF_8,
            isModified = false,
            isReadOnly = false
        )
    }

    /**
     * Reads and decodes a file from local storage using the specified character encoding.
     */
    suspend fun openFile(file: File, encoding: FileEncoding = FileEncoding.UTF_8): Result<Pair<EditorFile, String>> =
        withContext(Dispatchers.IO) {
            try {
                if (!file.exists()) {
                    return@withContext Result.failure(IllegalArgumentException("File does not exist: ${file.absolutePath}"))
                }

                val charset = Charset.forName(encoding.charsetName)
                val content = FileInputStream(file).use { stream ->
                    stream.bufferedReader(charset).readText()
                }

                recordRecentFile(file)

                val editorFile = EditorFile(
                    path = file.absolutePath,
                    name = file.name,
                    encoding = encoding,
                    isModified = false,
                    isReadOnly = !file.canWrite(),
                    lastModifiedTimestamp = file.lastModified()
                )

                Result.success(Pair(editorFile, content))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Saves the current text buffer to the active file path.
     */
    suspend fun saveFile(editorFile: EditorFile, content: String): Result<EditorFile> =
        withContext(Dispatchers.IO) {
            try {
                val targetFile = if (editorFile.path.isBlank()) {
                    File(documentsDir, editorFile.name)
                } else {
                    File(editorFile.path)
                }

                val charset = Charset.forName(editorFile.encoding.charsetName)
                FileOutputStream(targetFile).use { stream ->
                    stream.bufferedWriter(charset).use { writer ->
                        writer.write(content)
                        writer.flush()
                    }
                }

                recordRecentFile(targetFile)

                val updatedFile = editorFile.copy(
                    path = targetFile.absolutePath,
                    name = targetFile.name,
                    isModified = false,
                    lastModifiedTimestamp = targetFile.lastModified()
                )

                Result.success(updatedFile)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Saves the current text buffer to a new file name and encoding (Save As).
     */
    suspend fun saveFileAs(
        fileName: String,
        content: String,
        encoding: FileEncoding = FileEncoding.UTF_8
    ): Result<EditorFile> = withContext(Dispatchers.IO) {
        try {
            val safeFileName = if (fileName.isBlank()) "Untitled.kt" else fileName
            val targetFile = File(documentsDir, safeFileName)

            val charset = Charset.forName(encoding.charsetName)
            FileOutputStream(targetFile).use { stream ->
                stream.bufferedWriter(charset).use { writer ->
                    writer.write(content)
                    writer.flush()
                }
            }

            recordRecentFile(targetFile)

            val editorFile = EditorFile(
                path = targetFile.absolutePath,
                name = targetFile.name,
                encoding = encoding,
                isModified = false,
                isReadOnly = false,
                lastModifiedTimestamp = targetFile.lastModified()
            )

            Result.success(editorFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves all saved files in the internal documents folder.
     */
    suspend fun listSavedFiles(): List<File> = withContext(Dispatchers.IO) {
        documentsDir.listFiles()?.filter { it.isFile }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /**
     * Returns the list of recently accessed files.
     */
    fun getRecentFiles(): List<File> {
        return recentFilesList.filter { it.exists() }
    }

    private fun recordRecentFile(file: File) {
        recentFilesList.remove(file)
        recentFilesList.add(0, file)
        if (recentFilesList.size > 10) {
            recentFilesList.removeAt(recentFilesList.lastIndex)
        }
    }
}
