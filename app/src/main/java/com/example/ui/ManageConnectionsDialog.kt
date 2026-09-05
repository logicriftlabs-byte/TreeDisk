package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RemoteConnection
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleMint
import com.example.ui.theme.AppleRed

@Composable
fun ManageConnectionsDialog(
    connections: List<RemoteConnection>,
    onDismiss: () -> Unit,
    onEdit: (RemoteConnection) -> Unit,
    onDelete: (RemoteConnection) -> Unit,
    onTest: (RemoteConnection, (Boolean, String?) -> Unit) -> Unit,
) {
    val testingMap = remember { mutableStateMapOf<Long, Boolean>() }
    val statusMap = remember { mutableStateMapOf<Long, Pair<Boolean, String?>>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Connections", fontWeight = FontWeight.Bold) },
        text = {
            if (connections.isEmpty()) {
                Text("No saved connections.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(connections, key = { it.id }) { conn ->
                        val isTesting = testingMap[conn.id] == true
                        val status = statusMap[conn.id]

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(conn.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(
                                            "${conn.type.name} • ${conn.host}:${conn.port}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        TextButton(
                                            onClick = {
                                                testingMap[conn.id] = true
                                                statusMap.remove(conn.id)
                                                onTest(conn) { success, msg ->
                                                    testingMap[conn.id] = false
                                                    statusMap[conn.id] = Pair(success, msg)
                                                }
                                            },
                                            enabled = !isTesting,
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            if (isTesting) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(14.dp),
                                                    strokeWidth = 2.dp,
                                                    color = AppleBlue
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Testing...", fontSize = 12.sp)
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Test Connection",
                                                    tint = AppleBlue,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Test", fontSize = 12.sp, color = AppleBlue)
                                            }
                                        }

                                        IconButton(
                                            onClick = { onEdit(conn) },
                                            enabled = !isTesting
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Connection", tint = AppleBlue)
                                        }

                                        IconButton(
                                            onClick = { onDelete(conn) },
                                            enabled = !isTesting
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Connection", tint = AppleRed)
                                        }
                                    }
                                }

                                if (status != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (status.first) Icons.Default.CheckCircle else Icons.Default.Error,
                                            contentDescription = null,
                                            tint = if (status.first) AppleMint else AppleRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = status.second ?: if (status.first) "Valid connection" else "Connection failed",
                                            fontSize = 11.sp,
                                            color = if (status.first) AppleMint else AppleRed
                                        )
                                    }
                                }
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
