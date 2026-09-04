import os

protocols = {
    'app/src/main/java/com/example/remote/RemoteProtocol.kt': [
        ('suspend fun disconnect()', 'suspend fun testConnection()\n    suspend fun disconnect()')
    ],
    'app/src/main/java/com/example/remote/FtpProtocol.kt': [
        ('override suspend fun listFiles', 'override suspend fun testConnection() { connect() }\n    override suspend fun listFiles')
    ],
    'app/src/main/java/com/example/remote/SftpProtocol.kt': [
        ('override suspend fun listFiles', 'override suspend fun testConnection() { connect() }\n    override suspend fun listFiles')
    ],
    'app/src/main/java/com/example/remote/SmbProtocol.kt': [
        ('override suspend fun listFiles', 'override suspend fun testConnection() { getSession() }\n    override suspend fun listFiles')
    ]
}

for file, replacements in protocols.items():
    with open(file, 'r') as f:
        content = f.read()
    for old, new in replacements:
        content = content.replace(old, new)
    with open(file, 'w') as f:
        f.write(content)
