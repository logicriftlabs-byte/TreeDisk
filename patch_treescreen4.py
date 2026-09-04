import os

filepath = 'app/src/main/java/com/example/ui/TreeScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Add states for creating items
state_declarations = """    var showManageConnectionsDialog by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var isCreatingFolder by remember { mutableStateOf(false) }
    var selectedNodeForCreate by remember { mutableStateOf<StorageNode.DirectoryNode?>(null) }"""

content = content.replace(
"""    var showManageConnectionsDialog by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }""", state_declarations)

# Add CreateItemDialog integration
dialogs_code = """    if (showCreateDialog) {
        CreateItemDialog(
            isFolder = isCreatingFolder,
            onDismiss = { 
                showCreateDialog = false
                selectedNodeForCreate = null
            },
            onCreate = { name ->
                val targetNode = selectedNodeForCreate
                if (isCreatingFolder) {
                    viewModel.createFolder(targetNode, name) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    viewModel.createFile(targetNode, name) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
                showCreateDialog = false
                selectedNodeForCreate = null
            }
        )
    }

    if (showAddRemoteDialog) {"""

content = content.replace("    if (showAddRemoteDialog) {", dialogs_code)

# Update dropdown menu actions
dropdown_code = """                    DropdownMenuItem(
                        text = { Text("New Folder") },
                        onClick = {
                            showFabMenu = false
                            isCreatingFolder = true
                            showCreateDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("New File") },
                        onClick = {
                            showFabMenu = false
                            isCreatingFolder = false
                            showCreateDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.NoteAdd, contentDescription = null) }
                    )"""

content = content.replace("""                    DropdownMenuItem(
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
                    )""", dropdown_code)

# Optionally: Allow user to create inside a specific folder by adding it to WindowsContextMenuPopup
# Actually, the user asked for the FAB menu implementation, and the Tree context menu doesn't need to be overly complicated right now.
# But let's check `WindowsContextMenuPopup` usages just to see if we want to add "New File/Folder" to it.
# For now, just having it globally via FAB is good enough.

imports = """import com.example.ui.ManageConnectionsDialog
import com.example.ui.CreateItemDialog"""
content = content.replace("import com.example.ui.ManageConnectionsDialog", imports)

with open(filepath, 'w') as f:
    f.write(content)
