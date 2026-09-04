package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.ConnectionType
import com.example.data.RemoteConnection

@Composable
fun AddRemoteConnectionDialog(
    onDismiss: () -> Unit,
    onAdd: (RemoteConnection, (Boolean, String?) -> Unit) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("22") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(ConnectionType.SFTP) }

    var isTesting by remember { mutableStateOf(value = false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isTesting) onDismiss() },
        title = { Text("Add Remote Server") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ConnectionType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { 
                                type = t
                                port = when(t) {
                                    ConnectionType.FTP -> "21"
                                    ConnectionType.SFTP -> "22"
                                    ConnectionType.SMB -> "445"
                                }
                            },
                            label = { Text(t.name) },
                            enabled = !isTesting,
                        )
                    }
                }
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isTesting
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("Host / IP") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !isTesting
                    )
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Port") },
                        modifier = Modifier.width(80.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isTesting
                    )
                }
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isTesting
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isTesting
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val portInt = port.toIntOrNull() ?: 22
                    val finalUser = username.ifBlank { "anonymous" }
                    isTesting = true
                    errorMessage = null
                    
                    onAdd(
                        RemoteConnection(
                            name = name,
                            type = type,
                            host = host,
                            port = portInt,
                            username = finalUser,
                            password = password,
                        )
                    ) { success, error ->
                        isTesting = false
                        if (success) {
                            onDismiss()
                        } else {
                            errorMessage = error
                        }
                    }
                },
                enabled = name.isNotBlank() && host.isNotBlank() && !isTesting
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Testing...")
                } else {
                    Text("Add")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isTesting) {
                Text("Cancel")
            }
        }
    )
}
