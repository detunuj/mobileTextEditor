package com.example.mobiletexteditor.editor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiletexteditor.editor.EditorManager

/**
 * Search and Search-and-Replace bar that docks above the editor.
 */
@Composable
fun SearchReplaceBar(
    editorManager: EditorManager,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf(editorManager.searchResult.query) }
    var replaceQuery by remember { mutableStateOf("") }
    var showReplaceRow by remember { mutableStateOf(false) }
    var isCaseSensitive by remember { mutableStateOf(false) }
    var isMatchWholeWord by remember { mutableStateOf(false) }

    val searchResult = editorManager.searchResult

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Find Row
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
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Find in file...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchResult.query.isNotEmpty()) {
                            val matchText = if (searchResult.hasMatches) {
                                "${searchResult.currentMatchNumber}/${searchResult.totalMatches}"
                            } else {
                                "0/0"
                            }
                            Text(
                                text = matchText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (searchResult.hasMatches) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { editorManager.findNext() })
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = { editorManager.findPrevious() },
                    enabled = searchResult.hasMatches
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous Match")
                }

                IconButton(
                    onClick = { editorManager.findNext() },
                    enabled = searchResult.hasMatches
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next Match")
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close Search")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Options Row: Case sensitive & Whole word chips + Toggle Replace
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isCaseSensitive,
                    onClick = {
                        isCaseSensitive = !isCaseSensitive
                        editorManager.performSearch(searchQuery, isCaseSensitive, isMatchWholeWord)
                    },
                    label = { Text("Aa (Case)", fontSize = 11.sp) }
                )

                FilterChip(
                    selected = isMatchWholeWord,
                    onClick = {
                        isMatchWholeWord = !isMatchWholeWord
                        editorManager.performSearch(searchQuery, isCaseSensitive, isMatchWholeWord)
                    },
                    label = { Text("\\b (Word)", fontSize = 11.sp) }
                )

                FilterChip(
                    selected = showReplaceRow,
                    onClick = { showReplaceRow = !showReplaceRow },
                    label = { Text("Replace", fontSize = 11.sp) }
                )
            }

            // Replace Row (Conditional)
            if (showReplaceRow) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replaceQuery,
                        onValueChange = { replaceQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Replace with...") },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { editorManager.replaceCurrent(replaceQuery) },
                        enabled = searchResult.hasMatches && !editorManager.isReadOnly
                    ) {
                        Text("Replace")
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = { editorManager.replaceAll(replaceQuery) },
                        enabled = searchResult.hasMatches && !editorManager.isReadOnly
                    ) {
                        Text("All")
                    }
                }
            }
        }
    }
}
