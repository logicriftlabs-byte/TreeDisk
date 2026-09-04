import os

filepath_db = 'app/src/main/java/com/example/data/AppDatabase.kt'
with open(filepath_db, 'r') as f:
    content = f.read()
content = content.replace('abstract fun remoteConnectionDao(): RemoteConnectionDao\n', '')
content = content.replace('entities = [RemoteConnection::class]', 'entities = []') # Wait, Room needs at least 1 entity. 

filepath_rc = 'app/src/main/java/com/example/data/RemoteConnection.kt'
with open(filepath_rc, 'r') as f:
    content2 = f.read()
content2 = content2.replace('@Entity(tableName = "remote_connections")\n', '')
content2 = content2.replace('import androidx.room.Entity\n', '')
content2 = content2.replace('@PrimaryKey(autoGenerate = true) ', '')
content2 = content2.replace('import androidx.room.PrimaryKey\n', '')
with open(filepath_rc, 'w') as f:
    f.write(content2)
