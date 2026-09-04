import os

filepath = 'app/src/main/java/com/example/ui/TreeScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Fix CreateItemDialog invocation
old_dialog = """        CreateItemDialog(
            isFolder = isCreatingFolder,
            targetPath = selectedNodeForCreate?.path ?: "/",
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
        )"""

new_dialog = """        CreateItemDialog(
            isFolder = isCreatingFolder,
            initialPath = selectedNodeForCreate?.path ?: android.os.Environment.getExternalStorageDirectory().absolutePath,
            onDismiss = { 
                showCreateDialog = false
                selectedNodeForCreate = null
            },
            onCreate = { path, name ->
                if (isCreatingFolder) {
                    viewModel.createFolder(path, name) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    viewModel.createFile(path, name) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
                showCreateDialog = false
                selectedNodeForCreate = null
            }
        )"""

content = content.replace(old_dialog, new_dialog)

with open(filepath, 'w') as f:
    f.write(content)
