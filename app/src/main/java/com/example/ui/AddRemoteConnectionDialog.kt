package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.ConnectionType
import com.example.data.RemoteConnection
import com.example.ui.theme.AppleMint

@Composable
fun AddRemoteConnectionDialog(
    connectionToEdit: RemoteConnection? = null,
    onDismiss: () -> Unit,
    onAdd: (RemoteConnection, (Boolean, String?) -> Unit) -> Unit,
    onTest: (RemoteConnection, (Boolean, String?) -> Unit) -> Unit,
) {
    var name by remember(connectionToEdit) { mutableStateOf(connectionToEdit?.name ?: "") }
    var host by remember(connectionToEdit) { mutableStateOf(connectionToEdit?.host ?: "") }
    var port by remember(connectionToEdit) {
        mutableStateOf(
            connectionToEdit?.port?.toString() ?: (if (connectionToEdit?.type == ConnectionType.FTP) "21" else if (connectionToEdit?.type == ConnectionType.SMB) "445" else "22")
        )
    }
    var username by remember(connectionToEdit) { mutableStateOf(connectionToEdit?.username ?: "") }
    var password by remember(connectionToEdit) { mutableStateOf(connectionToEdit?.password ?: "") }
    var type by remember(connectionToEdit) { mutableStateOf(connectionToEdit?.type ?: ConnectionType.SFTP) }

    var isTesting by remember { mutableStateOf(value = false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val isEditing = connectionToEdit != null
    val dialogTitle = if (isEditing) "Edit Remote Server" else "Add Remote Server"

    AlertDialog(
        onDismissRequest = { if (!isTesting) onDismiss() },
        title = { Text(dialogTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ConnectionType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { 
                                type = t
                                if (connectionToEdit == null) {
                                    port = when(t) {
                                        ConnectionType.FTP -> "21"
                                        ConnectionType.SFTP -> "22"
                                        ConnectionType.SMB -> "445"
                                    }
                                }
                                errorMessage = null
                                successMessage = null
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

                if (successMessage != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AppleMint,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = successMessage!!,
                            color = AppleMint,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        val portInt = port.toIntOrNull() ?: 22
                        val finalUser = username.ifBlank { "anonymous" }
                        isTesting = true
                        errorMessage = null
                        successMessage = null

                        val tempConn = RemoteConnection(
                            id = connectionToEdit?.id ?: 0L,
                            name = name.ifBlank { "Test" },
                            type = type,
                            host = host,
                            port = portInt,
                            username = finalUser,
                            password = password,
                            remotePath = connectionToEdit?.remotePath ?: "/"
                        )

                        onTest(tempConn) { success, msg ->
                            isTesting = false
                            if (success) {
                                successMessage = msg ?: "Connection valid and reachable!"
                            } else {
                                errorMessage = msg ?: "Connection check failed"
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = host.isNotBlank() && !isTesting
                ) {
                    Text("Test")
                }

                Button(
                    onClick = {
                        val portInt = port.toIntOrNull() ?: 22
                        val finalUser = username.ifBlank { "anonymous" }
                        isTesting = true
                        errorMessage = null
                        successMessage = null
                        
                        val connToSave = RemoteConnection(
                            id = connectionToEdit?.id ?: 0L,
                            name = name,
                            type = type,
                            host = host,
                            port = portInt,
                            username = finalUser,
                            password = password,
                            remotePath = connectionToEdit?.remotePath ?: "/"
                        )

                        onAdd(connToSave) { success, error ->
                            isTesting = false
                            if (success) {
                                onDismiss()
                            } else {
                                errorMessage = error
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = name.isNotBlank() && host.isNotBlank() && !isTesting
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Testing...")
                    } else {
                        Text("Save")
                    }
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
