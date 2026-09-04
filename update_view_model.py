import re

with open('app/src/main/java/com/example/StorageViewModel.kt', 'r') as f:
    content = f.read()

# Replace emptyList() // Handle remote later with actual logic
remote_handling_logic = """
                    if (current.isRemote && current.connectionId != null) {
                        val connection = remoteDao.getConnectionById(current.connectionId)
                        if (connection != null) {
                            try {
                                val protocol = RemoteProtocolFactory.create(connection)
                                val files = protocol.listFiles(current.path)
                                val nodes = files.map { rf ->
                                    if (rf.isDirectory) {
                                        StorageNode.DirectoryNode(
                                            name = rf.name,
                                            size = rf.size,
                                            path = rf.path,
                                            childrenCount = 0,
                                            isRemote = true,
                                            connectionId = connection.id
                                        )
                                    } else {
                                        StorageNode.FileNode(
                                            name = rf.name,
                                            size = rf.size,
                                            path = rf.path,
                                            isRemote = true
                                        )
                                    }
                                }
                                protocol.disconnect()
                                nodes.sortedByDescending { it.isDirectory }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                emptyList()
                            }
                        } else {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
"""

content = content.replace("emptyList() // Handle remote later", remote_handling_logic.strip())

scan_remote = """
    fun scanRemoteConnection(connection: RemoteConnection) {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val protocol = withContext(Dispatchers.IO) { RemoteProtocolFactory.create(connection) }
                val remoteFiles = withContext(Dispatchers.IO) { protocol.listFiles(connection.remotePath) }
                val childNodes = remoteFiles.map { rf ->
                    if (rf.isDirectory) {
                        StorageNode.DirectoryNode(
                            name = rf.name,
                            size = rf.size,
                            path = rf.path,
                            childrenCount = 0,
                            isRemote = true,
                            connectionId = connection.id
                        )
                    } else {
                        StorageNode.FileNode(
                            name = rf.name,
                            size = rf.size,
                            path = rf.path,
                            isRemote = true
                        )
                    }
                }.sortedByDescending { it.isDirectory }
                
                val totalSize = childNodes.sumOf { it.size }
                
                _rootNode.value = StorageNode.DirectoryNode(
                    name = connection.name,
                    size = totalSize,
                    path = connection.remotePath,
                    childrenCount = childNodes.size,
                    children = childNodes,
                    isExpanded = true,
                    isRemote = true,
                    connectionId = connection.id
                )
                
                _topFiles.value = emptyList()
                _categoryStats.value = emptyList()
                _appStats.value = emptyList()
                _categoryFiles.value = emptyMap()
                withContext(Dispatchers.IO) { protocol.disconnect() }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isScanning.value = false
            }
        }
    }
}
"""

content = content.replace("}\n}", "}\n" + scan_remote)

with open('app/src/main/java/com/example/StorageViewModel.kt', 'w') as f:
    f.write(content)

