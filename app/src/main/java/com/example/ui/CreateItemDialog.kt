package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CreateItemDialog(
    isFolder: Boolean,
    initialPath: String,
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit, // path, name
) {
    var name by remember { mutableStateOf("") }
    var path by remember { mutableStateOf(initialPath) }
    var showFolderPicker by remember { mutableStateOf(value = false) }
    val title = if (isFolder) "Create New Folder" else "Create New File"
    
    if (showFolderPicker) {
        FolderPickerDialog(
            initialPath = path,
            onDismiss = { showFolderPicker = false },
        ) { selectedPath ->
            path = selectedPath
            showFolderPicker = false
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = path,
                        onValueChange = { path = it },
                        label = { Text("Location") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { showFolderPicker = true }) {
                        Text("Browse")
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && path.isNotBlank()) {
                        onCreate(path.trim(), name.trim())
                    }
                },
                enabled = name.isNotBlank() && path.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
