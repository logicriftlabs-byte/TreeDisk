import os

filepath = 'app/src/main/java/com/example/ui/TreeScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('WindowsContextMenu.openFile(context, node.file)', 'openFile(context, node.file)')
if 'import com.example.ui.openFile' not in content:
    content = content.replace('import com.example.ui.theme.*', 'import com.example.ui.theme.*\nimport com.example.ui.openFile\nimport com.example.ui.WindowsDeleteConfirmDialog')

with open(filepath, 'w') as f:
    f.write(content)
