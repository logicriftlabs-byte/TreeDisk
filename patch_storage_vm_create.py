import os

filepath = 'app/src/main/java/com/example/StorageViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

new_funcs = """
    fun createFile(parent: StorageNode.DirectoryNode?, name: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val targetDir = if (parent != null && !parent.isRemote && parent.file != null) {
                    parent.file
                } else {
                    Environment.getExternalStorageDirectory()
                }
                
                val newFile = File(targetDir, name)
                if (newFile.exists()) {
                    withContext(Dispatchers.Main) { onResult(false, "File already exists") }
                    return@launch
                }
                
                val success = newFile.createNewFile()
                if (success) {
                    scanStorage()
                    withContext(Dispatchers.Main) { onResult(true, "File created successfully") }
                } else {
                    withContext(Dispatchers.Main) { onResult(false, "Failed to create file") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onResult(false, e.message ?: "Error creating file") }
            }
        }
    }
    
    fun createFolder(parent: StorageNode.DirectoryNode?, name: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val targetDir = if (parent != null && !parent.isRemote && parent.file != null) {
                    parent.file
                } else {
                    Environment.getExternalStorageDirectory()
                }
                
                val newFolder = File(targetDir, name)
                if (newFolder.exists()) {
                    withContext(Dispatchers.Main) { onResult(false, "Folder already exists") }
                    return@launch
                }
                
                val success = newFolder.mkdirs()
                if (success) {
                    scanStorage()
                    withContext(Dispatchers.Main) { onResult(true, "Folder created successfully") }
                } else {
                    withContext(Dispatchers.Main) { onResult(false, "Failed to create folder") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onResult(false, e.message ?: "Error creating folder") }
            }
        }
    }
"""

last_brace_idx = content.rfind('}')
if last_brace_idx != -1:
    content = content[:last_brace_idx] + new_funcs + content[last_brace_idx:]

with open(filepath, 'w') as f:
    f.write(content)
