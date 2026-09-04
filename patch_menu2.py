import os

filepath = 'app/src/main/java/com/example/ui/WindowsContextMenu.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Add parameter
content = content.replace(
'''    onExpandToggle: ((StorageNode.DirectoryNode) -> Unit)? = null,
    onOrganizeRequest: ((StorageNode.DirectoryNode) -> Unit)? = null,
    onDeleteRequest: (StorageNode) -> Unit
) {''',
'''    onExpandToggle: ((StorageNode.DirectoryNode) -> Unit)? = null,
    onOrganizeRequest: ((StorageNode.DirectoryNode) -> Unit)? = null,
    onCreateRequest: ((StorageNode.DirectoryNode, Boolean) -> Unit)? = null,
    onDeleteRequest: (StorageNode) -> Unit
) {'''
)

new_menu_items = """                        val isExpanded = node.isExpanded
                        WindowsContextMenuItem(
                            icon = if (isExpanded) Icons.Default.Folder else Icons.Default.FolderOpen,
                            label = if (isExpanded) "Collapse Folder" else "Expand Folder",
                            textColor = winTextColor,
                            hoverColor = winHoverBg,
                            onClick = {
                                onDismiss()
                                onExpandToggle?.invoke(node)
                            }
                        )
                        
                        WindowsContextMenuItem(
                            icon = Icons.Default.CreateNewFolder,
                            label = "New Folder",
                            textColor = winTextColor,
                            hoverColor = winHoverBg,
                            onClick = {
                                onDismiss()
                                onCreateRequest?.invoke(node, true)
                            }
                        )
                        
                        WindowsContextMenuItem(
                            icon = Icons.Default.NoteAdd,
                            label = "New File",
                            textColor = winTextColor,
                            hoverColor = winHoverBg,
                            onClick = {
                                onDismiss()
                                onCreateRequest?.invoke(node, false)
                            }
                        )"""

content = content.replace(
"""                        val isExpanded = node.isExpanded
                        WindowsContextMenuItem(
                            icon = if (isExpanded) Icons.Default.Folder else Icons.Default.FolderOpen,
                            label = if (isExpanded) "Collapse Folder" else "Expand Folder",
                            textColor = winTextColor,
                            hoverColor = winHoverBg,
                            onClick = {
                                onDismiss()
                                onExpandToggle?.invoke(node)
                            }
                        )""", new_menu_items)


import_code = """import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.NoteAdd"""
if "import androidx.compose.material.icons.filled.CreateNewFolder" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.AutoAwesome", "import androidx.compose.material.icons.filled.AutoAwesome\nimport androidx.compose.material.icons.filled.CreateNewFolder\nimport androidx.compose.material.icons.filled.NoteAdd")
    
# Also fix deprecated open in new
content = content.replace("Icons.Default.OpenInNew", "Icons.AutoMirrored.Filled.OpenInNew")
if "import androidx.compose.material.icons.automirrored.filled.OpenInNew" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.AutoAwesome", "import androidx.compose.material.icons.automirrored.filled.OpenInNew\nimport androidx.compose.material.icons.filled.AutoAwesome")

with open(filepath, 'w') as f:
    f.write(content)
