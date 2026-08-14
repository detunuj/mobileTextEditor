package com.example.mobiletexteditor.editor.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.WrapText
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiletexteditor.editor.EditorLanguage
import com.example.mobiletexteditor.editor.EditorManager
import com.example.mobiletexteditor.editor.model.FileEncoding
import com.example.mobiletexteditor.highlighting.CrashRecoveryManager
import com.example.mobiletexteditor.highlighting.RecoveryDraft
import com.example.mobiletexteditor.highlighting.SyntaxVisualTransformation
import com.example.mobiletexteditor.highlighting.ui.CrashRecoveryDialog
import com.example.mobiletexteditor.highlighting.ui.MarkdownPreviewPanel
import com.example.mobiletexteditor.version.VersionManager
import com.example.mobiletexteditor.version.ui.CreateSnapshotDialog
import com.example.mobiletexteditor.version.ui.VersionHistoryDialog
import kotlinx.coroutines.launch

/**
 * Main application screen integrating:
 * - Member 1: Editor Engine, Undo/Redo, Search/Replace, Word-wrap, Read-only lock, File Sidebar
 * - Member 2: Kotlin/Markdown Syntax Highlighting, Live Markdown Preview, 10s Crash Auto-Backup
 * - Member 3: Incremental Delta Version Control, Room Persistence, Visual Diff Viewer, Rollback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainEditorScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val editorManager = remember { EditorManager(context) }
    val versionManager = remember { VersionManager(context) }
    val recoveryManager = remember { CrashRecoveryManager(context) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var showSearchReplaceBar by remember { mutableStateOf(false) }
    var showSaveAsDialog by remember { mutableStateOf(false) }
    var showMarkdownPreview by remember { mutableStateOf(false) }
    var showSnapshotDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var pendingRecoveryDraft by remember { mutableStateOf<RecoveryDraft?>(null) }

    // Start 10-second background auto-backup loop & check for crash draft on init
    LaunchedEffect(Unit) {
        recoveryManager.startAutoBackup(
            scope = coroutineScope,
            getActiveFile = { editorManager.activeFile },
            getActiveText = { editorManager.textContent }
        )

        // Check if unsaved crash recovery draft exists
        val draft = recoveryManager.checkForRecoveryDraft(
            filePath = editorManager.activeFile.path,
            originalFileLastModified = editorManager.activeFile.lastModifiedTimestamp
        )
        if (draft != null) {
            pendingRecoveryDraft = draft
        }
    }

    // Set initial sample text if blank
    LaunchedEffect(Unit) {
        if (editorManager.textContent.isBlank()) {
            val sampleKotlin = """
                // Welcome to Modern Mobile Text Editor!
                // Full Integration: Member 1 (Editor), Member 2 (Highlighting), Member 3 (Version Control)
                
                package com.example.mobiletexteditor
                
                data class ProjectMember(val id: Int, val name: String, val role: String)
                
                fun main() {
                    val members = listOf(
                        ProjectMember(1, "Member 1", "Editor Engine & File Management"),
                        ProjectMember(2, "Member 2", "Syntax Highlighting & Crash Recovery"),
                        ProjectMember(3, "Member 3", "Incremental Delta Version Control")
                    )
                    
                    for (member in members) {
                        println("Member ${'$'}{member.id}: ${'$'}{member.name} -> ${'$'}{member.role}")
                    }
                }
            """.trimIndent()
            editorManager.setContentDirectly(sampleKotlin)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            FileSidebarDrawer(
                fileManager = editorManager.fileManager,
                activeFilePath = editorManager.activeFile.path,
                onNewFile = {
                    editorManager.newFile()
                    Toast.makeText(context, "Created new file", Toast.LENGTH_SHORT).show()
                },
                onSaveFile = {
                    coroutineScope.launch {
                        val saveResult = editorManager.saveCurrentFile()
                        saveResult.onSuccess { saved ->
                            recoveryManager.clearBackup(saved.path)
                            Toast.makeText(context, "Saved ${saved.name}", Toast.LENGTH_SHORT).show()
                        }.onFailure { err ->
                            Toast.makeText(context, "Save failed: ${err.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onSaveAsFile = { showSaveAsDialog = true },
                onOpenFile = { file ->
                    coroutineScope.launch {
                        editorManager.openFile(file).onSuccess {
                            val draft = recoveryManager.checkForRecoveryDraft(file.absolutePath, file.lastModified())
                            if (draft != null) {
                                pendingRecoveryDraft = draft
                            }
                            Toast.makeText(context, "Opened ${file.name}", Toast.LENGTH_SHORT).show()
                        }.onFailure { err ->
                            Toast.makeText(context, "Open failed: ${err.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val fileIcon = if (editorManager.isKotlinLanguage) Icons.Default.Code else Icons.Default.Description
                                Icon(
                                    imageVector = fileIcon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Column {
                                    Text(
                                        text = editorManager.activeFile.name + if (editorManager.activeFile.isModified) " *" else "",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${editorManager.activeFile.encoding.displayName} · ${if (editorManager.isReadOnly) "Read-Only" else "Editable"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
                            }
                        },
                        actions = {
                            // Member 1: Undo / Redo
                            IconButton(
                                onClick = { editorManager.undo() },
                                enabled = editorManager.canUndo && !editorManager.isReadOnly
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = "Undo",
                                    tint = if (editorManager.canUndo && !editorManager.isReadOnly) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            }
                            IconButton(
                                onClick = { editorManager.redo() },
                                enabled = editorManager.canRedo && !editorManager.isReadOnly
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Redo,
                                    contentDescription = "Redo",
                                    tint = if (editorManager.canRedo && !editorManager.isReadOnly) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            }

                            // Member 1: Search & Replace
                            IconButton(onClick = { showSearchReplaceBar = !showSearchReplaceBar }) {
                                Icon(Icons.Default.Search, contentDescription = "Search & Replace")
                            }

                            // Member 2: Markdown Preview Panel (if markdown)
                            if (editorManager.isMarkdownLanguage) {
                                IconButton(onClick = { showMarkdownPreview = true }) {
                                    Icon(Icons.Default.Visibility, contentDescription = "Markdown Preview")
                                }
                            }

                            // Member 3: Snapshot
                            IconButton(onClick = { showSnapshotDialog = true }) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = "Create Snapshot")
                            }

                            // Member 3: Version History
                            IconButton(onClick = { showHistoryDialog = true }) {
                                Icon(Icons.Default.History, contentDescription = "Version History")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    )

                    // Secondary Quick Toolbar: Word wrap, Read-only, Font size controls
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Word wrap toggle
                                IconButton(onClick = { editorManager.isWordWrapEnabled = !editorManager.isWordWrapEnabled }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.WrapText,
                                        contentDescription = "Toggle Word Wrap",
                                        tint = if (editorManager.isWordWrapEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                }

                                // Read-only toggle
                                IconButton(onClick = { editorManager.isReadOnly = !editorManager.isReadOnly }) {
                                    Icon(
                                        imageVector = if (editorManager.isReadOnly) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = "Toggle Read-Only Mode",
                                        tint = if (editorManager.isReadOnly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Font size controls (A- and A+ buttons)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { editorManager.decreaseFontSize() },
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "A-",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = "${editorManager.fontSizeSp.toInt()} sp",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                Surface(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { editorManager.increaseFontSize() },
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "A+",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Search & Replace Docked Bar
                    if (showSearchReplaceBar) {
                        SearchReplaceBar(
                            editorManager = editorManager,
                            onClose = {
                                showSearchReplaceBar = false
                                editorManager.clearSearch()
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Editor Main Canvas: Synchronized Line Numbers + Syntax Highlighted BasicTextField
                val lineCount = remember(editorManager.textContent) {
                    editorManager.textContent.lines().size.coerceAtLeast(1)
                }

                val verticalScroll = rememberScrollState()
                val horizontalScroll = rememberScrollState()

                val currentFontSize = editorManager.fontSizeSp.sp
                val currentLineHeight = (editorManager.fontSizeSp * 1.5f).sp

                val editorTextStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = currentFontSize,
                    lineHeight = currentLineHeight,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    // Synchronized Line Numbers Gutter
                    Column(
                        modifier = Modifier
                            .width(46.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .verticalScroll(verticalScroll)
                            .padding(vertical = 12.dp, horizontal = 4.dp)
                    ) {
                        for (i in 1..lineCount) {
                            Text(
                                text = "$i",
                                fontFamily = FontFamily.Monospace,
                                fontSize = currentFontSize,
                                lineHeight = currentLineHeight,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Synchronized Real-Time Syntax Highlighted Buffer
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(verticalScroll)
                            .then(
                                if (!editorManager.isWordWrapEnabled) {
                                    Modifier.horizontalScroll(horizontalScroll)
                                } else {
                                    Modifier
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 12.dp)
                    ) {
                        BasicTextField(
                            value = editorManager.textContent,
                            onValueChange = { editorManager.updateContent(it) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = editorManager.isReadOnly,
                            textStyle = editorTextStyle,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            visualTransformation = SyntaxVisualTransformation(
                                isKotlin = editorManager.isKotlinLanguage,
                                isMarkdown = editorManager.isMarkdownLanguage,
                                searchResult = editorManager.searchResult
                            ),
                            decorationBox = { innerTextField ->
                                if (editorManager.textContent.isEmpty()) {
                                    Text(
                                        text = "Start typing code...",
                                        style = editorTextStyle.copy(color = MaterialTheme.colorScheme.outline)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                // Editor Bottom Status Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lines: ${editorManager.textContent.lines().size} | Chars: ${editorManager.textContent.length}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Language Mode Selector Chip (Click to switch between Kotlin, Markdown, Plain Text)
                        Box {
                            val activeLangName = when {
                                editorManager.isKotlinLanguage -> "Kotlin"
                                editorManager.isMarkdownLanguage -> "Markdown"
                                else -> "Plain Text"
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showLanguageMenu = true }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$activeLangName ▾",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            DropdownMenu(
                                expanded = showLanguageMenu,
                                onDismissRequest = { showLanguageMenu = false }
                            ) {
                                EditorLanguage.entries.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang.displayName) },
                                        onClick = {
                                            editorManager.languageOverride = lang
                                            showLanguageMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Member 1: Save As Dialog
    if (showSaveAsDialog) {
        SaveAsDialog(
            initialFileName = editorManager.activeFile.name,
            initialEncoding = editorManager.activeFile.encoding,
            onDismiss = { showSaveAsDialog = false },
            onConfirm = { name, encoding ->
                showSaveAsDialog = false
                coroutineScope.launch {
                    val result = editorManager.saveFileAs(name, encoding)
                    result.onSuccess { saved ->
                        recoveryManager.clearBackup(saved.path)
                        Toast.makeText(context, "Saved as ${saved.name} (${saved.encoding.displayName})", Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        Toast.makeText(context, "Save As failed: ${err.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // Member 2: Markdown Preview Panel
    if (showMarkdownPreview) {
        MarkdownPreviewPanel(
            markdownContent = editorManager.textContent,
            fileName = editorManager.activeFile.name,
            onDismiss = { showMarkdownPreview = false }
        )
    }

    // Member 2: Crash Recovery Dialog
    pendingRecoveryDraft?.let { draft ->
        CrashRecoveryDialog(
            draft = draft,
            onRestore = {
                editorManager.setContentDirectly(draft.recoveredContent, markModified = true)
                pendingRecoveryDraft = null
                Toast.makeText(context, "Recovered draft restored successfully!", Toast.LENGTH_SHORT).show()
            },
            onDiscard = {
                coroutineScope.launch {
                    recoveryManager.clearBackup(draft.filePath)
                }
                pendingRecoveryDraft = null
            }
        )
    }

    // Member 3: Save Snapshot Dialog
    if (showSnapshotDialog) {
        CreateSnapshotDialog(
            fileName = editorManager.activeFile.name,
            nextVersionNumber = 1,
            onDismiss = { showSnapshotDialog = false },
            onConfirm = { name, desc ->
                showSnapshotDialog = false
                coroutineScope.launch {
                    val snapshotResult = versionManager.createSnapshot(
                        filePath = editorManager.activeFile.path.ifBlank { editorManager.activeFile.name },
                        fileName = editorManager.activeFile.name,
                        currentContent = editorManager.textContent,
                        versionName = name,
                        description = desc
                    )
                    snapshotResult.onSuccess { v ->
                        Toast.makeText(
                            context,
                            "Saved Snapshot: ${v.versionName} (${if (v.isBaseVersion) "Base v1" else "Delta Patch"})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.onFailure { err ->
                        Toast.makeText(context, "Snapshot failed: ${err.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // Member 3: Version History Dialog
    if (showHistoryDialog) {
        VersionHistoryDialog(
            filePath = editorManager.activeFile.path.ifBlank { editorManager.activeFile.name },
            fileName = editorManager.activeFile.name,
            currentContent = editorManager.textContent,
            versionManager = versionManager,
            onDismiss = { showHistoryDialog = false },
            onRestoreContent = { restored ->
                editorManager.setContentDirectly(restored, markModified = true)
                Toast.makeText(context, "File rolled back successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
