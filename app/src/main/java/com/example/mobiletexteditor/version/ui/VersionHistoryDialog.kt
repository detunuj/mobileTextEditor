/**
 * File: VersionHistoryDialog.kt
 * Purpose: Modal timeline dialog displaying saved version snapshots with version tags, change metrics,
 *          diff comparison triggers, and rollback restoration actions.
 * Group Member: Member 3 — Version Control & Database
 */
package com.example.mobiletexteditor.version.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mobiletexteditor.version.DiffManager
import com.example.mobiletexteditor.version.VersionManager
import com.example.mobiletexteditor.version.data.FileVersionEntity
import com.example.mobiletexteditor.version.model.DiffResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modal dialog displaying the version control history timeline for a file.
 */
@Composable
fun VersionHistoryDialog(
    filePath: String,
    fileName: String,
    currentContent: String,
    versionManager: VersionManager,
    onDismiss: () -> Unit,
    onRestoreContent: (restoredText: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val versions by versionManager.getVersionHistoryFlow(filePath).collectAsState(initial = emptyList())

    var showCreateDialog by remember { mutableStateOf(false) }
    var activeDiffResult by remember { mutableStateOf<Pair<String, DiffResult>?>(null) }
    var versionToRestore by remember { mutableStateOf<FileVersionEntity?>(null) }
    var versionToDelete by remember { mutableStateOf<FileVersionEntity?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Version History",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$fileName (${versions.size} snapshots)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Action Bar: Create Snapshot Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save New Version")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Timeline List
                if (versions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No version snapshots saved yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Click 'Save New Version' to save your first delta checkpoint.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(versions, key = { it.id }) { version ->
                            VersionItemCard(
                                version = version,
                                onInspectDiff = {
                                    coroutineScope.launch {
                                        val diff = versionManager.compareWithCurrent(
                                            filePath = filePath,
                                            currentContent = currentContent,
                                            historicalVersionNumber = version.versionNumber
                                        )
                                        diff.onSuccess { result ->
                                            activeDiffResult = Pair(version.versionName, result)
                                        }
                                    }
                                },
                                onRestoreClick = {
                                    versionToRestore = version
                                },
                                onDeleteClick = {
                                    versionToDelete = version
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Snapshot Dialog
    if (showCreateDialog) {
        val nextVersionNumber = (versions.maxOfOrNull { it.versionNumber } ?: 0) + 1
        CreateSnapshotDialog(
            fileName = fileName,
            nextVersionNumber = nextVersionNumber,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc ->
                showCreateDialog = false
                coroutineScope.launch {
                    versionManager.createSnapshot(
                        filePath = filePath,
                        fileName = fileName,
                        currentContent = currentContent,
                        versionName = name,
                        description = desc
                    )
                }
            }
        )
    }

    // Diff Viewer Dialog
    activeDiffResult?.let { (versionTitle, diff) ->
        DiffViewerDialog(
            title = "Diff: $versionTitle vs Current",
            subtitle = "Comparing snapshot with active editor buffer",
            diffResult = diff,
            onDismiss = { activeDiffResult = null },
            onRestore = {
                activeDiffResult = null
                versionToRestore = versions.firstOrNull { it.versionName == versionTitle }
            }
        )
    }

    // Rollback Confirmation Dialog
    versionToRestore?.let { version ->
        AlertDialog(
            onDismissRequest = { versionToRestore = null },
            icon = { Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Rollback to ${version.versionName}?") },
            text = {
                Text("This will replace the current editor text with the reconstructed contents of Version ${version.versionNumber}.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetNum = version.versionNumber
                        versionToRestore = null
                        coroutineScope.launch {
                            val restoredResult = versionManager.rollbackToVersion(filePath, targetNum)
                            restoredResult.onSuccess { text ->
                                onRestoreContent(text)
                                onDismiss()
                            }
                        }
                    }
                ) {
                    Text("Rollback")
                }
            },
            dismissButton = {
                TextButton(onClick = { versionToRestore = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Snapshot Dialog
    versionToDelete?.let { version ->
        AlertDialog(
            onDismissRequest = { versionToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Snapshot?") },
            text = {
                Text("Are you sure you want to delete ${version.versionName}? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val v = version
                        versionToDelete = null
                        coroutineScope.launch {
                            versionManager.deleteVersion(v)
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { versionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun VersionItemCard(
    version: FileVersionEntity,
    onInspectDiff: () -> Unit,
    onRestoreClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy · HH:mm:ss", Locale.getDefault()) }
    val formattedDate = remember(version.timestamp) { dateFormat.format(Date(version.timestamp)) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Version Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Version badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "v${version.versionNumber}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = version.versionName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (version.isBaseVersion) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Base Version", fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
            }

            if (version.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = version.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata row: Timestamp and Line statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                val statsText = if (version.isBaseVersion) {
                    "${version.totalLines} lines (${version.fileSizeBytes} B)"
                } else {
                    DiffManager.formatChangeSummary(version.linesAdded, version.linesDeleted)
                }

                Text(
                    text = statsText,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onInspectDiff,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Compare, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Diff")
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = onRestoreClick,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rollback")
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
