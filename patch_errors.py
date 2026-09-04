import os

filepath_dash = 'app/src/main/java/com/example/ui/DashboardScreen.kt'
with open(filepath_dash, 'r') as f:
    content = f.read()

content = content.replace('import androidx.compose.foundation.border', 'import androidx.compose.foundation.border\nimport androidx.compose.foundation.BorderStroke')
content = content.replace('PaddingValues(horizontal = 16.dp, bottom = 40.dp)', 'PaddingValues(start = 16.dp, end = 16.dp, bottom = 40.dp)')

with open(filepath_dash, 'w') as f:
    f.write(content)


filepath_tree = 'app/src/main/java/com/example/ui/TreeScreen.kt'
with open(filepath_tree, 'r') as f:
    content2 = f.read()

content2 = content2.replace(
'''        AddRemoteConnectionDialog(
            onDismiss = { showAddRemoteDialog = false },
            onConnect = { connection ->
                viewModel.testAndAddRemoteConnection(connection) { success, msg ->
                    if (success) {
                        showAddRemoteDialog = false
                    }
                    if (msg != null) {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        )''',
'''        AddRemoteConnectionDialog(
            onDismiss = { showAddRemoteDialog = false },
            onAdd = { connection, callback ->
                viewModel.testAndAddRemoteConnection(connection) { success, msg ->
                    callback(success, msg)
                    if (success) {
                        showAddRemoteDialog = false
                    }
                    if (msg != null) {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        )'''
)
with open(filepath_tree, 'w') as f:
    f.write(content2)


filepath_menu = 'app/src/main/java/com/example/ui/WindowsContextMenu.kt'
with open(filepath_menu, 'r') as f:
    content3 = f.read()

content3 = content3.replace('AutoAwesomeOutline', 'AutoAwesome')
content3 = content3.replace('DeleteOutline', 'Delete')

with open(filepath_menu, 'w') as f:
    f.write(content3)
