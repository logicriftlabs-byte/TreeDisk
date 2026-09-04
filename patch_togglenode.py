import os

filepath = 'app/src/main/java/com/example/StorageViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace(
    'if (current.path == target.path) {',
    'if (current.path == target.path && current.isRemote == target.isRemote && current.connectionId == target.connectionId) {'
)

with open(filepath, 'w') as f:
    f.write(content)
