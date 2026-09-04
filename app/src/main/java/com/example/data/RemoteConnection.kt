package com.example.data

enum class ConnectionType {
    FTP, SFTP, SMB
}

data class RemoteConnection(
    val id: Long = 0,
    val name: String,
    val type: ConnectionType,
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val remotePath: String = "/"
)
