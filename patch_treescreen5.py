import os

filepath = 'app/src/main/java/com/example/ui/TreeScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace(
'''            onOrganizeRequest = { folder ->
                viewModel.organizeDirectoryWithAI(folder) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            },
            onDeleteRequest = { nodeToDelete ->''',
'''            onOrganizeRequest = { folder ->
                viewModel.organizeDirectoryWithAI(folder) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            },
            onCreateRequest = { folder, isFolder ->
                selectedNodeForCreate = folder
                isCreatingFolder = isFolder
                showCreateDialog = true
            },
            onDeleteRequest = { nodeToDelete ->'''
)

with open(filepath, 'w') as f:
    f.write(content)

filepath_dash = 'app/src/main/java/com/example/ui/DashboardScreen.kt'
with open(filepath_dash, 'r') as f:
    content_dash = f.read()

content_dash = content_dash.replace(
'''            onOrganizeRequest = { folder ->
                viewModel.organizeDirectoryWithAI(folder) { success, msg ->
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                }
            },
            onDeleteRequest = { nodeToDelete ->''',
'''            onOrganizeRequest = { folder ->
                viewModel.organizeDirectoryWithAI(folder) { success, msg ->
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                }
            },
            onCreateRequest = null,
            onDeleteRequest = { nodeToDelete ->'''
)

with open(filepath_dash, 'w') as f:
    f.write(content_dash)

