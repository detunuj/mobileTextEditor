/**
 * File: CrashRecoveryDialog.kt
 * Purpose: Alert dialog displayed on launch when an unsaved crash recovery draft is detected,
 *          providing draft preview and options to restore or discard unsaved work.
 * Group Member: Member 2 — Syntax Highlighting & Recovery
 */
package com.example.mobiletexteditor.highlighting.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiletexteditor.highlighting.RecoveryDraft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dialog alerting the user to restore unsaved work after an app crash or interruption.
 */
@Composable
fun CrashRecoveryDialog(
    draft: RecoveryDraft,
    onRestore: () -> Unit,
    onDiscard: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy · HH:mm:ss", Locale.getDefault()) }
    val formattedDate = remember(draft.timestamp) { dateFormat.format(Date(draft.timestamp)) }

    AlertDialog(
        onDismissRequest = onDiscard,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "Unsaved Work Detected",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "A background recovery draft for \"${draft.fileName}\" was found from $formattedDate.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Draft Preview:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    val previewLines = draft.recoveredContent.lines().take(5).joinToString("\n")
                    Text(
                        text = previewLines + if (draft.recoveredContent.lines().size > 5) "\n..." else "",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onRestore) {
                Text("Restore Draft")
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Text("Discard")
            }
        }
    )
}
