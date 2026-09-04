import os

filepath = 'app/src/main/java/com/example/StorageViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Add the internal states
insert_idx = content.find('private val _rootNode = MutableStateFlow<StorageNode.DirectoryNode?>(null)')
internal_states = '''
    private var _localRoot: StorageNode.DirectoryNode? = null
    private val _remoteRoots = mutableMapOf<Long, StorageNode.DirectoryNode>()

    private fun rebuildVirtualRoot() {
        val children = mutableListOf<StorageNode.DirectoryNode>()
        _localRoot?.let { 
            // Wrapper for local
            children.add(it.copy(name = "Internal Storage")) 
        }
        children.addAll(_remoteRoots.values)

        if (children.isEmpty()) {
            _rootNode.value = null
        } else {
            val totalSize = children.sumOf { it.size }
            _rootNode.value = StorageNode.DirectoryNode(
                name = "My Device",
                size = totalSize,
                path = "virtual_root",
                childrenCount = children.size,
                children = children,
                isExpanded = true,
                isRemote = false
            )
        }
    }

'''
content = content[:insert_idx] + internal_states + content[insert_idx:]

# Update scanStorage to use _localRoot and rebuild
content = content.replace(
'''            _rootNode.value = root
            _topFiles.value = result.topFiles''',
'''            _localRoot = root
            rebuildVirtualRoot()
            _topFiles.value = result.topFiles'''
)

content = content.replace(
'''        _rootNode.value = null
        _topFiles.value = emptyList()''',
'''        _localRoot = null
        rebuildVirtualRoot()
        _topFiles.value = emptyList()'''
)

# Update scanRemoteConnection to add to _remoteRoots and rebuild
old_remote = '''                val totalSize = childNodes.sumOf { it.size }
                
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
                _categoryFiles.value = emptyMap()'''

new_remote = '''                val totalSize = childNodes.sumOf { it.size }
                
                _remoteRoots[connection.id] = StorageNode.DirectoryNode(
                    name = connection.name,
                    size = totalSize,
                    path = connection.remotePath,
                    childrenCount = childNodes.size,
                    children = childNodes,
                    isExpanded = false,
                    isRemote = true,
                    connectionId = connection.id
                )
                
                rebuildVirtualRoot()'''

content = content.replace(old_remote, new_remote)

with open(filepath, 'w') as f:
    f.write(content)
