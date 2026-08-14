/**
 * File: SaveAsDialog.kt
 * Purpose: Dialog allowing users to specify custom file names and select character encodings
 *          (UTF-8, UTF-16, US-ASCII, ISO-8859-1) before saving the file to storage.
 * Group Member: Member 1 — Editor Engine & File Management
 */
package com.example.mobiletexteditor.editor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobiletexteditor.editor.model.FileEncoding

/**
 * Save As dialog with file name input and character encoding dropdown selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveAsDialog(
    initialFileName: String,
    initialEncoding: FileEncoding,
    onDismiss: () -> Unit,
    onConfirm: (fileName: String, encoding: FileEncoding) -> Unit
) {
    var fileName by remember { mutableStateOf(initialFileName) }
    var selectedEncoding by remember { mutableStateOf(initialEncoding) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.SaveAs,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Save As",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name") },
                    placeholder = { Text("e.g. MyScript.kt, Notes.md") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Encoding Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedEncoding.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Character Encoding") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        FileEncoding.entries.forEach { encoding ->
                            DropdownMenuItem(
                                text = { Text(encoding.displayName) },
                                onClick = {
                                    selectedEncoding = encoding
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = fileName.trim().ifBlank { "Untitled.kt" }
                    onConfirm(finalName, selectedEncoding)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
