import os

filepath = 'app/src/main/java/com/example/ui/DashboardScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

new_menu = '''        WindowsContextMenuPopup(
            node = node,
            onDismiss = { selectedNodeForMenu = null },
            onExpandToggle = null,
            onOrganizeRequest = { folder ->
                viewModel.organizeDirectoryWithAI(folder) { success, msg ->
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                }
            },
            onDeleteRequest = { nodeToDelete ->
                selectedNodeForDelete = nodeToDelete
            }
        )'''

content = content.replace(
'''        WindowsContextMenuPopup(
            node = node,
            onDismiss = { selectedNodeForMenu = null },
            onExpandToggle = null,
            onDeleteRequest = { nodeToDelete ->
                selectedNodeForDelete = nodeToDelete
            }
        )''',
new_menu
)

with open(filepath, 'w') as f:
    f.write(content)
