/**
 * File: DiffViewerDialog.kt
 * Purpose: Fullscreen modal dialog displaying color-coded line-by-line differences between file
 *          versions (green for additions, red for deletions) with line gutters, statistics, and direct rollback.
 * Group Member: Member 3 — Version Control & Database
 */
package com.example.mobiletexteditor.version.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mobiletexteditor.version.model.DiffLine
import com.example.mobiletexteditor.version.model.DiffResult
import com.example.mobiletexteditor.version.model.DiffType

private val AddedBgColor = Color(0x334CAF50)
private val AddedTextColor = Color(0xFF2E7D32)
private val DeletedBgColor = Color(0x33F44336)
private val DeletedTextColor = Color(0xFFC62828)
private val LineNumberColor = Color(0xFF9E9E9E)

/**
 * Fullscreen / modal dialog displaying line-by-line colored diffs between file versions.
 */
@Composable
fun DiffViewerDialog(
    title: String,
    subtitle: String,
    diffResult: DiffResult,
    onDismiss: () -> Unit,
    onRestore: (() -> Unit)? = null
) {
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
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                // Diff Statistics Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text("+${diffResult.linesAdded} added", color = AddedTextColor) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = AddedBgColor)
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("-${diffResult.linesDeleted} deleted", color = DeletedTextColor) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = DeletedBgColor)
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("${diffResult.linesUnchanged} unchanged") }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (onRestore != null) {
                        Button(
                            onClick = {
                                onRestore()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()

                // Diff Lines List
                if (diffResult.lines.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No differences found (Files are identical)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val horizontalScrollState = rememberScrollState()

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        items(diffResult.lines) { line ->
                            DiffLineRow(
                                line = line,
                                horizontalScrollState = horizontalScrollState
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiffLineRow(
    line: DiffLine,
    horizontalScrollState: androidx.compose.foundation.ScrollState
) {
    val (bgColor, textColor, prefix) = when (line.type) {
        DiffType.ADDED -> Triple(AddedBgColor, AddedTextColor, "+")
        DiffType.DELETED -> Triple(DeletedBgColor, DeletedTextColor, "-")
        DiffType.MODIFIED -> Triple(AddedBgColor, AddedTextColor, "~")
        DiffType.UNCHANGED -> Triple(Color.Transparent, MaterialTheme.colorScheme.onSurface, " ")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Line number gutters
        Text(
            text = line.oldLineNumber?.toString() ?: "",
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = LineNumberColor
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = line.newLineNumber?.toString() ?: "",
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = LineNumberColor
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Prefix (+ / - / space)
        Text(
            text = prefix,
            modifier = Modifier.width(16.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        // Text content
        Text(
            text = line.text,
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(horizontalScrollState),
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = textColor
        )
    }
}
