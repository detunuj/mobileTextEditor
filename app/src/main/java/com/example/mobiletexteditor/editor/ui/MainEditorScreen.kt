package com.example.mobiletexteditor.editor.ui

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WrapText
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiletexteditor.editor.EditorManager
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
 * - Member 1: Editor Engine, Undo/Redo, Search/Replace, Word-wrap, Read-only lock, File Sidebar, Zooming
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
    var pendingRecoveryDraft by remember { mutableStateOf<RecoveryDraft?>(null) }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val textMeasurer = rememberTextMeasurer()

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
                                val fileIcon = if (editorManager.activeFile.isKotlin) Icons.Default.Code else Icons.Default.Description
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
                            // Member 1: Undo
                            IconButton(
                                onClick = { editorManager.undo() },
                                enabled = editorManager.canUndo && !editorManager.isReadOnly
                            ) {
                                Icon(Icons.Default.Undo, contentDescription = "Undo")
                            }

                            // Member 1: Redo (Corrected reactivity)
                            IconButton(
                                onClick = { editorManager.redo() },
                                enabled = editorManager.canRedo && !editorManager.isReadOnly
                            ) {
                                Icon(Icons.Default.Redo, contentDescription = "Redo")
                            }

                            // Member 1: Search & Replace
                            IconButton(onClick = { showSearchReplaceBar = !showSearchReplaceBar }) {
                                Icon(Icons.Default.Search, contentDescription = "Search & Replace")
                            }

                            // Member 2: Markdown Preview Panel (if markdown)
                            if (editorManager.activeFile.isMarkdown) {
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

                    // Secondary Quick Toolbar: Word wrap, Read-only, File Zooming
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
                                        imageVector = Icons.Default.WrapText,
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

                            // File Content Zooming (A- / A+)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { editorManager.zoomOut() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text(
                                        text = "A-",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Text(
                                    text = "${editorManager.fontSizeSp.toInt()} sp",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                IconButton(
                                    onClick = { editorManager.zoomIn() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text(
                                        text = "A+",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
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
                val verticalScroll = rememberScrollState()
                val horizontalScroll = rememberScrollState()
                val gutterColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)

                // Main Editor Area (Gutter + Text Field)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(verticalScroll)
                ) {
                    // Line Number Gutter (Accurately aligned with logical lines & word wrap)
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(vertical = 12.dp)
                            .drawBehind {
                                val layout = textLayoutResult ?: return@drawBehind
                                val text = editorManager.textContent
                                var currentLogicalLine = 1

                                for (visualLine in 0 until layout.lineCount) {
                                    val startOffset = layout.getLineStart(visualLine)
                                    val isLogicalStart = (visualLine == 0) || (startOffset > 0 && startOffset <= text.length && text[startOffset - 1] == '\n')

                                    if (isLogicalStart) {
                                        val lineStr = currentLogicalLine.toString()
                                        currentLogicalLine++

                                        val measured = textMeasurer.measure(
                                            text = lineStr,
                                            style = TextStyle(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = editorManager.fontSizeSp.sp,
                                                color = gutterColor,
                                                textAlign = TextAlign.End
                                            )
                                        )

                                        val topY = layout.getLineTop(visualLine)
                                        val x = size.width - measured.size.width - 6.dp.toPx()
                                        drawText(
                                            textLayoutResult = measured,
                                            topLeft = Offset(x, topY)
                                        )
                                    }
                                }
                            }
                    )

                    // Text Editor Canvas with dynamic VisualTransformation for syntax & search highlights
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 10.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
                            .then(
                                if (!editorManager.isWordWrapEnabled) {
                                    Modifier.horizontalScroll(horizontalScroll)
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        BasicTextField(
                            value = editorManager.textFieldValue,
                            onValueChange = { editorManager.updateContent(it) },
                            onTextLayout = { textLayoutResult = it },
                            readOnly = editorManager.isReadOnly,
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = editorManager.fontSizeSp.sp,
                                lineHeight = (editorManager.fontSizeSp * 1.45f).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            visualTransformation = SyntaxVisualTransformation(
                                isKotlin = editorManager.activeFile.isKotlin,
                                isMarkdown = editorManager.activeFile.isMarkdown,
                                searchQuery = editorManager.searchResult.query,
                                activeMatchIndex = editorManager.searchResult.currentMatchIndex,
                                isCaseSensitive = editorManager.searchResult.isCaseSensitive,
                                isMatchWholeWord = editorManager.searchResult.isMatchWholeWord
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
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
                        val logicalLines = editorManager.textContent.lines().size
                        val charCount = editorManager.textContent.length

                        Text(
                            text = "Lines: $logicalLines | Chars: $charCount | Zoom: ${editorManager.fontSizeSp.toInt()}sp",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = if (editorManager.activeFile.isKotlin) "Kotlin" else if (editorManager.activeFile.isMarkdown) "Markdown" else "Plain Text",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
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
