import os

filepath = 'app/src/main/java/com/example/StorageViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

# I want to add an organize function
new_func = '''
    fun organizeDirectoryWithAI(directory: StorageNode.DirectoryNode, onResult: (Boolean, String) -> Unit) {
        if (directory.isRemote) {
            onResult(false, "AI Organization is currently only supported for local storage.")
            return
        }
        val dirFile = directory.file ?: return
        
        viewModelScope.launch {
            _isScanning.value = true
            try {
                // Get list of local files
                val filesToOrganize = directory.children.filterIsInstance<StorageNode.FileNode>()
                if (filesToOrganize.isEmpty()) {
                    withContext(Dispatchers.Main) { onResult(false, "No files to organize.") }
                    return@launch
                }
                
                val fileNames = filesToOrganize.map { it.name }
                val categorized = AIAssistant.categorizeFiles(fileNames)
                
                var movedCount = 0
                withContext(Dispatchers.IO) {
                    categorized.forEach { (folderName, fileList) ->
                        val targetDir = java.io.File(dirFile, folderName)
                        if (!targetDir.exists()) {
                            targetDir.mkdirs()
                        }
                        
                        fileList.forEach { fileName ->
                            val sourceFile = java.io.File(dirFile, fileName)
                            if (sourceFile.exists() && sourceFile.isFile) {
                                val targetFile = java.io.File(targetDir, fileName)
                                if (sourceFile.renameTo(targetFile)) {
                                    movedCount++
                                }
                            }
                        }
                    }
                }
                
                // Refresh the directory
                scanStorage()
                withContext(Dispatchers.Main) { onResult(true, "Organized $movedCount files into ${categorized.size} folders.") }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onResult(false, e.message ?: "Failed to organize files") }
            } finally {
                _isScanning.value = false
            }
        }
    }
'''

# Find the end of the class
last_brace_idx = content.rfind('}')
if last_brace_idx != -1:
    content = content[:last_brace_idx] + new_func + content[last_brace_idx:]

with open(filepath, 'w') as f:
    f.write(content)
