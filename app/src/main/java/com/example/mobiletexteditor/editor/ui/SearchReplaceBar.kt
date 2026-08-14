/**
 * File: SearchReplaceBar.kt
 * Purpose: Docked search and replace control panel providing query input, match counter (e.g. 1/8),
 *          previous/next match navigation, case-sensitivity and whole-word toggles, and replace actions.
 * Group Member: Member 1 — Editor Engine & File Management
 */
package com.example.mobiletexteditor.editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiletexteditor.editor.EditorManager

/**
 * Docked Search & Replace control bar shown under the TopAppBar.
 */
@Composable
fun SearchReplaceBar(
    editorManager: EditorManager,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf(editorManager.searchResult.query) }
    var replaceQuery by remember { mutableStateOf("") }
    var isCaseSensitive by remember { mutableStateOf(editorManager.searchResult.isCaseSensitive) }
    var isMatchWholeWord by remember { mutableStateOf(editorManager.searchResult.isMatchWholeWord) }
    var isReplaceMode by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Row 1: Search Input + Navigation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        editorManager.performSearch(it, isCaseSensitive, isMatchWholeWord)
                    },
                    placeholder = { Text("Find text...", fontSize = 14.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Match count badge
                val matchCountText = if (editorManager.searchResult.hasMatches) {
                    "${editorManager.searchResult.currentMatchIndex + 1}/${editorManager.searchResult.totalMatches}"
                } else if (searchQuery.isNotEmpty()) {
                    "0/0"
                } else {
                    ""
                }

                if (matchCountText.isNotEmpty()) {
                    Text(
                        text = matchCountText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (editorManager.searchResult.hasMatches) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // Previous Match Button
                IconButton(
                    onClick = { editorManager.findPrevious() },
                    enabled = editorManager.searchResult.hasMatches
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous Match")
                }

                // Next Match Button
                IconButton(
                    onClick = { editorManager.findNext() },
                    enabled = editorManager.searchResult.hasMatches
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next Match")
                }

                // Close Button
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close Search")
                }
            }

            // Row 2: Filter Chips & Replace Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = isCaseSensitive,
                        onClick = {
                            isCaseSensitive = !isCaseSensitive
                            editorManager.performSearch(searchQuery, isCaseSensitive, isMatchWholeWord)
                        },
                        label = { Text("Match Case (Aa)", fontSize = 12.sp) }
                    )

                    FilterChip(
                        selected = isMatchWholeWord,
                        onClick = {
                            isMatchWholeWord = !isMatchWholeWord
                            editorManager.performSearch(searchQuery, isCaseSensitive, isMatchWholeWord)
                        },
                        label = { Text("Whole Word (\\b)", fontSize = 12.sp) }
                    )
                }

                FilterChip(
                    selected = isReplaceMode,
                    onClick = { isReplaceMode = !isReplaceMode },
                    label = { Text("Replace", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.FindReplace, contentDescription = null) }
                )
            }

            // Row 3: Replace Controls (if toggled)
            if (isReplaceMode) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replaceQuery,
                        onValueChange = { replaceQuery = it },
                        placeholder = { Text("Replace with...", fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = { editorManager.replaceCurrent(replaceQuery) },
                        enabled = editorManager.searchResult.hasMatches && !editorManager.isReadOnly,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Replace", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    OutlinedButton(
                        onClick = { editorManager.replaceAll(replaceQuery) },
                        enabled = editorManager.searchResult.hasMatches && !editorManager.isReadOnly,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("All", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
