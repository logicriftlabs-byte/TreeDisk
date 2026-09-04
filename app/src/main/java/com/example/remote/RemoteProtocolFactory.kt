package com.example.remote

import com.example.data.ConnectionType
import com.example.data.RemoteConnection

object RemoteProtocolFactory {
    fun create(connection: RemoteConnection): RemoteProtocol {
        return when (connection.type) {
            ConnectionType.FTP -> FtpProtocol(connection.host, connection.port, connection.username, connection.password)
            ConnectionType.SFTP -> SftpProtocol(connection.host, connection.port, connection.username, connection.password)
            ConnectionType.SMB -> SmbProtocol(connection.host, connection.port, connection.username, connection.password)
        }
    }
}
