package com.example.remote

import org.apache.commons.net.ftp.FTPClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FtpProtocol(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
) : RemoteProtocol {

    private var ftpClient: FTPClient? = null

    private suspend fun connect(): FTPClient = withContext(Dispatchers.IO) {
        val client = ftpClient ?: FTPClient().apply {
            connect(host, port)
            login(username, password)
            enterLocalPassiveMode()
        }
        ftpClient = client
        client
    }

    override suspend fun testConnection() { connect() }
    override suspend fun listFiles(path: String): List<RemoteFile> = withContext(Dispatchers.IO) {
        val client = connect()
        client.changeWorkingDirectory(path)
        client.listFiles().map { ftpFile ->
            RemoteFile(
                name = ftpFile.name,
                path = if (path.endsWith("/")) "$path${ftpFile.name}" else "$path/${ftpFile.name}",
                size = ftpFile.size,
                isDirectory = ftpFile.isDirectory,
                lastModified = ftpFile.timestamp.timeInMillis,
            )
        }
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        ftpClient?.let {
            if (it.isConnected) {
                it.logout()
                it.disconnect()
            }
        }
        ftpClient = null
    }
}
