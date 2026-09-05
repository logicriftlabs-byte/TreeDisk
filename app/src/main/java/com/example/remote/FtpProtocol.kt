package com.example.remote

import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

class FtpProtocol(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
) : RemoteProtocol {

    private var ftpClient: FTPClient? = null

    private suspend fun connect(): FTPClient = withContext(Dispatchers.IO) {
        val current = ftpClient
        if ((current != null) && current.isConnected) {
            return@withContext current
        }

        val client = FTPClient().apply {
            defaultTimeout = 10000
            connectTimeout = 10000
            setDataTimeout(10000)
        }

        try {
            client.connect(host, port)
        } catch (e: Exception) {
            throw Exception("Failed to connect to FTP host $host:$port - ${e.localizedMessage}")
        }

        val replyCode = client.replyCode
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            client.disconnect()
            throw Exception("FTP server $host:$port refused connection (Reply code: $replyCode)")
        }

        val loggedIn = client.login(username, password)
        if (!loggedIn) {
            client.disconnect()
            throw Exception("FTP authentication failed for user '$username'. Please check credentials.")
        }

        client.enterLocalPassiveMode()
        ftpClient = client
        client
    }

    override suspend fun testConnection() {
        val client = connect()
        if (!client.isConnected) {
            throw Exception("FTP connection test failed")
        }
    }
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

    override suspend fun createFile(path: String, name: String): Boolean = withContext(Dispatchers.IO) {
        val client = connect()
        val targetPath = if (path.endsWith("/")) "$path$name" else "$path/$name"
        client.storeFile(targetPath, ByteArrayInputStream(ByteArray(0)))
    }

    override suspend fun createFolder(path: String, name: String): Boolean = withContext(Dispatchers.IO) {
        val client = connect()
        val targetPath = if (path.endsWith("/")) "$path$name" else "$path/$name"
        client.makeDirectory(targetPath)
    }
}
