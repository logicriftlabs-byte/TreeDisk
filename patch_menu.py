import os

filepath = 'app/src/main/java/com/example/ui/WindowsContextMenu.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Add onOrganizeRequest parameter
content = content.replace(
'''    onExpandToggle: ((StorageNode.DirectoryNode) -> Unit)? = null,
    onDeleteRequest: (StorageNode) -> Unit
) {''',
'''    onExpandToggle: ((StorageNode.DirectoryNode) -> Unit)? = null,
    onOrganizeRequest: ((StorageNode.DirectoryNode) -> Unit)? = null,
    onDeleteRequest: (StorageNode) -> Unit
) {'''
)

# Add AI action button for DirectoryNode
ai_action = '''                        val isExpanded = node.isExpanded
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
                        
                        if (!node.isRemote) {
                            WindowsContextMenuItem(
                                icon = Icons.Default.AutoAwesome,
                                label = "Clean up with AI",
                                textColor = Color(0xFF8B5CF6),
                                hoverColor = winHoverBg,
                                onClick = {
                                    onDismiss()
                                    onOrganizeRequest?.invoke(node)
                                }
                            )
                        }'''

content = content.replace(
'''                        val isExpanded = node.isExpanded
                        WindowsContextMenuItem(
                            icon = if (isExpanded) Icons.Default.Folder else Icons.Default.FolderOpen,
                            label = if (isExpanded) "Collapse Folder" else "Expand Folder",
                            textColor = winTextColor,
                            hoverColor = winHoverBg,
                            onClick = {
                                onDismiss()
                                onExpandToggle?.invoke(node)
                            }
                        )''',
ai_action
)

content = content.replace('import androidx.compose.material.icons.filled.Delete', 'import androidx.compose.material.icons.filled.Delete\nimport androidx.compose.material.icons.filled.AutoAwesome')

with open(filepath, 'w') as f:
    f.write(content)
