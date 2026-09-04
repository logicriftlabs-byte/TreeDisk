import os

filepath = 'app/src/main/java/com/example/ui/TreeScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace(
"""        CreateItemDialog(
            isFolder = isCreatingFolder,
            onDismiss = { """,
"""        CreateItemDialog(
            isFolder = isCreatingFolder,
            targetPath = selectedNodeForCreate?.path ?: "/",
            onDismiss = { """
)

with open(filepath, 'w') as f:
    f.write(content)
