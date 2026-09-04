import os

filepath = 'app/src/main/java/com/example/ui/TreeScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Add new states
state_declarations = """    var showAddRemoteDialog by remember { mutableStateOf(false) }
    var showManageConnectionsDialog by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }"""

content = content.replace("    var showAddRemoteDialog by remember { mutableStateOf(false) }", state_declarations)

# Add the ManageConnectionsDialog
manage_dialog = """    if (showManageConnectionsDialog) {
        ManageConnectionsDialog(
            connections = remoteConnections,
            onDismiss = { showManageConnectionsDialog = false },
            onDelete = { conn ->
                viewModel.deleteRemoteConnection(conn)
            }
        )
    }

    if (showAddRemoteDialog) {"""

content = content.replace("    if (showAddRemoteDialog) {", manage_dialog)

# Update the Manage button click
manage_button = """Text("Manage", fontSize = 12.sp, color = AppleMint, modifier = Modifier.clickable { showManageConnectionsDialog = true })"""
content = content.replace("""Text("Manage", fontSize = 12.sp, color = AppleMint, modifier = Modifier.clickable { showAddRemoteDialog = true })""", manage_button)


# Update the FAB to open a menu
fab_code = """        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showFabMenu = true },
                    containerColor = AppleMint,
                    contentColor = Color.Black,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Menu")
                }
                
                DropdownMenu(
                    expanded = showFabMenu,
                    onDismissRequest = { showFabMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    DropdownMenuItem(
                        text = { Text("New Connection") },
                        onClick = {
                            showFabMenu = false
                            showAddRemoteDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("New Folder") },
                        onClick = {
                            showFabMenu = false
                            Toast.makeText(context, "New Folder feature coming soon", Toast.LENGTH_SHORT).show()
                        },
                        leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("New File") },
                        onClick = {
                            showFabMenu = false
                            Toast.makeText(context, "New File feature coming soon", Toast.LENGTH_SHORT).show()
                        },
                        leadingIcon = { Icon(Icons.Default.NoteAdd, contentDescription = null) }
                    )
                }
            }
        },"""

content = content.replace(
"""        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddRemoteDialog = true },
                containerColor = AppleMint,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Connection")
            }
        },""",
fab_code
)

# Add necessary imports
imports = """import androidx.compose.ui.theme.*
import com.example.ui.ManageConnectionsDialog
"""
content = content.replace("import com.example.ui.theme.*", imports)

with open(filepath, 'w') as f:
    f.write(content)
