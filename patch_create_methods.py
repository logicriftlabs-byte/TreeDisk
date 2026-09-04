import re

filepath = 'app/src/main/java/com/example/StorageViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Replace createFile
create_file_pattern = r'fun createFile\(parent: StorageNode\.DirectoryNode\?, name: String, onResult: \(Boolean, String\) -> Unit\) \{.*?\n    \}'
new_create_file = """fun createFile(targetDirPath: String, name: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val targetDir = java.io.File(targetDirPath)
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }
                
                val newFile = java.io.File(targetDir, name)
                if (newFile.exists()) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(false, "File already exists") }
                    return@launch
                }
                
                val success = newFile.createNewFile()
                if (success) {
                    val expanded = _rootNode.value?.let { getExpandedPaths(it) }
                    scanStorage(expanded)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(true, "File created successfully") }
                } else {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(false, "Failed to create file") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(false, e.message ?: "Error creating file") }
            }
        }
    }"""
content = re.sub(create_file_pattern, new_create_file, content, flags=re.DOTALL)

# Replace createFolder
create_folder_pattern = r'fun createFolder\(parent: StorageNode\.DirectoryNode\?, name: String, onResult: \(Boolean, String\) -> Unit\) \{.*?\n    \}'
new_create_folder = """fun createFolder(targetDirPath: String, name: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val targetDir = java.io.File(targetDirPath)
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }
                
                val newFolder = java.io.File(targetDir, name)
                if (newFolder.exists()) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(false, "Folder already exists") }
                    return@launch
                }
                
                val success = newFolder.mkdirs()
                if (success) {
                    val expanded = _rootNode.value?.let { getExpandedPaths(it) }
                    scanStorage(expanded)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(true, "Folder created successfully") }
                } else {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(false, "Failed to create folder") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(false, e.message ?: "Error creating folder") }
            }
        }
    }"""
content = re.sub(create_folder_pattern, new_create_folder, content, flags=re.DOTALL)

with open(filepath, 'w') as f:
    f.write(content)
