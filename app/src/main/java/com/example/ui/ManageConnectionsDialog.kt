package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.RemoteConnection
import com.example.ui.theme.AppleRed

@Composable
fun ManageConnectionsDialog(
    connections: List<RemoteConnection>,
    onDismiss: () -> Unit,
    onDelete: (RemoteConnection) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Connections", fontWeight = FontWeight.Bold) },
        text = {
            if (connections.isEmpty()) {
                Text("No saved connections.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(connections) { conn ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(conn.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("${conn.type.name} - ${conn.host}:${conn.port}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { onDelete(conn) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AppleRed)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
