package com.example.ui

import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun FolderPickerDialog(
    initialPath: String,
    onDismiss: () -> Unit,
    onFolderSelected: (String) -> Unit,
) {
    var currentDir by remember { mutableStateOf(File(initialPath)) }
    var currentPath by remember { mutableStateOf(initialPath) }
    
    LaunchedEffect(initialPath) {
        var startFile = File(initialPath)
        if (!startFile.exists() || !startFile.isDirectory) {
            startFile = Environment.getExternalStorageDirectory()
        }
        currentDir = startFile
        currentPath = startFile.absolutePath
    }
    
    var subDirs by remember { mutableStateOf(emptyList<File>()) }
    
    LaunchedEffect(currentDir) {
        val files = currentDir.listFiles()?.filter { it.isDirectory && !it.isHidden }?.sortedBy { it.name }
        subDirs = files ?: emptyList()
    }
    
    val extRoot = Environment.getExternalStorageDirectory().absolutePath
    val canGoUp = (currentDir.parentFile != null) && currentDir.absolutePath.startsWith(extRoot) && (currentDir.absolutePath != extRoot)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Select Location", fontWeight = FontWeight.Bold)
                Text(currentPath, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                if (canGoUp) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentDir.parentFile?.let {
                                        currentDir = it
                                        currentPath = it.absolutePath
                                    }
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go up", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("..")
                        }
                    }
                }
                
                items(subDirs) { dir ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentDir = dir
                                currentPath = dir.absolutePath
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = "Folder", tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(dir.name)
                    }
                }
                
                if (subDirs.isEmpty() && !canGoUp) {
                    item {
                        Text("No subdirectories", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onFolderSelected(currentPath) }) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
