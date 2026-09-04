import os

filepath = 'app/src/main/java/com/example/ui/TreeScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Add state for snackbar if it's missing, but we can just use a simple state or toast.
# Since we are in TreeScreen, let's just use Toast for simplicity.
toast_import = 'import android.widget.Toast\nimport androidx.compose.ui.platform.LocalContext\n'
if 'import android.widget.Toast' not in content:
    content = content.replace('import androidx.compose.ui.Modifier', toast_import + 'import androidx.compose.ui.Modifier')

new_menu = '''        val context = LocalContext.current
        WindowsContextMenuPopup(
            node = node,
            onDismiss = { selectedNodeForMenu = null },
            onExpandToggle = { folder ->
                viewModel.toggleNode(folder)
            },
            onOrganizeRequest = { folder ->
                viewModel.organizeDirectoryWithAI(folder) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
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
            onExpandToggle = { folder ->
                viewModel.toggleNode(folder)
            },
            onDeleteRequest = { nodeToDelete ->
                selectedNodeForDelete = nodeToDelete
            }
        )''',
new_menu
)

with open(filepath, 'w') as f:
    f.write(content)
