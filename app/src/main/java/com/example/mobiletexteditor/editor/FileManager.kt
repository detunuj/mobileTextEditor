package com.example.mobiletexteditor.editor

import android.content.Context
import android.content.SharedPreferences
import com.example.mobiletexteditor.editor.model.EditorFile
import com.example.mobiletexteditor.editor.model.FileEncoding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/*
  Open, New, Recent Files, Save, and Save As with encoding options.
 */
class FileManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("editor_file_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_RECENT_FILES = "recent_files_list"
        private const val MAX_RECENT_FILES = 10
    }

    /*
     Returns the default app-internal documents directory.
     */
    fun getDocumentsDirectory(): File {
        val dir = File(context.filesDir, "documents")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /*
     Initializes a new, unsaved file session.
     */
    fun createNewFile(name: String = "Untitled.kt"): EditorFile {
        return EditorFile(
            file = null,
            name = name,
            path = "",
            encoding = FileEncoding.UTF_8,
            isReadOnly = false,
            isModified = false,
            lastModifiedTimestamp = System.currentTimeMillis()
        )
    }

    /*
      Reads a file from disk.
     */
    suspend fun openFile(
        file: File,
        encoding: FileEncoding = FileEncoding.UTF_8
    ): Result<Pair<EditorFile, String>> = withContext(Dispatchers.IO) {
        try {
            if (!file.exists() || !file.canRead()) {
                return@withContext Result.failure(IllegalArgumentException("File does not exist or is not readable: ${file.path}"))
            }

            val text = FileInputStream(file).use { stream ->
                stream.bufferedReader(encoding.charset).readText()
            }

            val editorFile = EditorFile(
                file = file,
                name = file.name,
                path = file.absolutePath,
                encoding = encoding,
                isReadOnly = !file.canWrite(),
                isModified = false,
                lastModifiedTimestamp = file.lastModified()
            )

            addRecentFile(file.absolutePath)
            Result.success(Pair(editorFile, text))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
        Reads a file from disk given its absolute path.
     */
    suspend fun openFileFromPath(
        path: String,
        encoding: FileEncoding = FileEncoding.UTF_8
    ): Result<Pair<EditorFile, String>> {
        return openFile(File(path), encoding)
    }

    /*
     Saves changes directly to an existing file
     */
    suspend fun saveFile(
        editorFile: EditorFile,
        content: String
    ): Result<EditorFile> = withContext(Dispatchers.IO) {
        try {
            val targetFile = editorFile.file
                ?: File(getDocumentsDirectory(), editorFile.name)

            if (editorFile.isReadOnly) {
                return@withContext Result.failure(IllegalStateException("Cannot save a read-only file."))
            }

            FileOutputStream(targetFile).use { stream ->
                stream.bufferedWriter(editorFile.encoding.charset).use { writer ->
                    writer.write(content)
                    writer.flush()
                }
            }

            val updatedFile = editorFile.copy(
                file = targetFile,
                path = targetFile.absolutePath,
                name = targetFile.name,
                isModified = false,
                lastModifiedTimestamp = targetFile.lastModified()
            )

            addRecentFile(targetFile.absolutePath)
            Result.success(updatedFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /*
     Saves content as a new file with specified name and encoding.
     */
    suspend fun saveFileAs(
        targetDirectory: File = getDocumentsDirectory(),
        fileName: String,
        content: String,
        encoding: FileEncoding = FileEncoding.UTF_8
    ): Result<EditorFile> = withContext(Dispatchers.IO) {
        try {
            val destination = File(targetDirectory, fileName)
            FileOutputStream(destination).use { stream ->
                stream.bufferedWriter(encoding.charset).use { writer ->
                    writer.write(content)
                    writer.flush()
                }
            }

            val editorFile = EditorFile(
                file = destination,
                name = destination.name,
                path = destination.absolutePath,
                encoding = encoding,
                isReadOnly = false,
                isModified = false,
                lastModifiedTimestamp = destination.lastModified()
            )

            addRecentFile(destination.absolutePath)
            Result.success(editorFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /*
     Lists all saved files in the app documents folder.
     */
    fun listLocalFiles(): List<File> {
        val dir = getDocumentsDirectory()
        return dir.listFiles { file -> file.isFile }?.toList()?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    /*
     Adds a file path to the recent files history list.
     */
    fun addRecentFile(path: String) {
        if (path.isBlank()) return
        val current = getRecentFiles().toMutableList()
        current.remove(path)
        current.add(0, path)
        if (current.size > MAX_RECENT_FILES) {
            current.subList(MAX_RECENT_FILES, current.size).clear()
        }
        prefs.edit().putString(KEY_RECENT_FILES, current.joinToString(";")).apply()
    }

    /*
     Retrieves the list of recently opened file paths.
     */
    fun getRecentFiles(): List<String> {
        val raw = prefs.getString(KEY_RECENT_FILES, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(";").filter { it.isNotBlank() && File(it).exists() }
    }

    /*
     Clears the recent files history.
     */
    fun clearRecentFiles() {
        prefs.edit().remove(KEY_RECENT_FILES).apply()
    }

    /*
      Deletes a local file.
     */
    fun deleteLocalFile(file: File): Boolean {
        val deleted = file.delete()
        if (deleted) {
            val current = getRecentFiles().toMutableList()
            current.remove(file.absolutePath)
            prefs.edit().putString(KEY_RECENT_FILES, current.joinToString(";")).apply()
        }
        return deleted
    }
}
