import os

filepath = 'app/src/main/java/com/example/StorageViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace(
    'private val db = AppDatabase.getDatabase(application)\n    private val remoteDao = db.remoteConnectionDao()',
    'private val dataStoreManager = com.example.data.RemoteConnectionDataStore(application)'
)

content = content.replace(
    'val remoteConnections = remoteDao.getAllConnections().stateIn(',
    'val remoteConnections = dataStoreManager.connectionsFlow.stateIn('
)

content = content.replace(
    'val connection = remoteDao.getConnectionById(node.connectionId)',
    'val connection = remoteConnections.value.find { it.id == node.connectionId }'
)

content = content.replace(
    'val connection = remoteDao.getConnectionById(current.connectionId)',
    'val connection = remoteConnections.value.find { it.id == current.connectionId }'
)

content = content.replace(
    'remoteDao.insertConnection(connection)',
    'dataStoreManager.saveConnection(connection)'
)

content = content.replace(
    'remoteDao.deleteConnection(connection)',
    'dataStoreManager.deleteConnection(connection)'
)

with open(filepath, 'w') as f:
    f.write(content)
