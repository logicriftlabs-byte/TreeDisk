import os

filepath = 'app/src/main/java/com/example/StorageViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace(
'''    fun deleteRemoteConnection(connection: RemoteConnection) {
        viewModelScope.launch {
            dataStoreManager.deleteConnection(connection)
        }
    }''',
'''    fun deleteRemoteConnection(connection: RemoteConnection) {
        viewModelScope.launch {
            dataStoreManager.deleteConnection(connection)
            _remoteRoots.remove(connection.id)
            rebuildVirtualRoot()
        }
    }'''
)

with open(filepath, 'w') as f:
    f.write(content)
