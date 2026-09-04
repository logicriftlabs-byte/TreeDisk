import os

filepath = 'app/src/main/java/com/example/ui/TreeScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Add a state for the last expanded folder
state_declarations = """    var selectedNodeForCreate by remember { mutableStateOf<StorageNode.DirectoryNode?>(null) }
    var lastExpandedNode by remember { mutableStateOf<StorageNode.DirectoryNode?>(null) }"""
content = content.replace("    var selectedNodeForCreate by remember { mutableStateOf<StorageNode.DirectoryNode?>(null) }", state_declarations)

# Update the FAB menu to use lastExpandedNode if available
dropdown_code = """                    DropdownMenuItem(
                        text = { Text("New Folder") },
                        onClick = {
                            showFabMenu = false
                            isCreatingFolder = true
                            selectedNodeForCreate = lastExpandedNode // Pre-select the last expanded folder
                            showCreateDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("New File") },
                        onClick = {
                            showFabMenu = false
                            isCreatingFolder = false
                            selectedNodeForCreate = lastExpandedNode // Pre-select the last expanded folder
                            showCreateDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.NoteAdd, contentDescription = null) }
                    )"""

content = content.replace("""                    DropdownMenuItem(
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
                    )""", dropdown_code)

# Update the onToggle to track the last expanded folder
ontoggle_code = """                    AppleStorageNodeRow(
                        node = flatNode.node,
                        level = flatNode.level,
                        onToggle = { directoryNode -> 
                            if (!directoryNode.isExpanded) {
                                // If we are expanding it, set it as the last expanded node
                                lastExpandedNode = directoryNode
                            } else if (lastExpandedNode == directoryNode) {
                                // If we are collapsing the currently selected one, clear it
                                lastExpandedNode = null
                            }
                            viewModel.toggleNode(directoryNode) 
                        },
                        onLongPress = { selectedNodeForMenu = it }
                    )"""

content = content.replace("""                    AppleStorageNodeRow(
                        node = flatNode.node,
                        level = flatNode.level,
                        onToggle = { viewModel.toggleNode(it as StorageNode.DirectoryNode) },
                        onLongPress = { selectedNodeForMenu = it }
                    )""", ontoggle_code)

with open(filepath, 'w') as f:
    f.write(content)
