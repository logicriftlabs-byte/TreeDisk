package com.example.remote

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.util.*

class SftpProtocol(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
) : RemoteProtocol {

    private var session: Session? = null
    private var channel: ChannelSftp? = null

    private suspend fun connect(): ChannelSftp = withContext(Dispatchers.IO) {
        val currentChannel = channel
        if ((currentChannel != null) && currentChannel.isConnected) {
            return@withContext currentChannel
        }

        val jsch = JSch()
        val s = jsch.getSession(username, host, port)
        s.setPassword(password)
        s.timeout = 10000
        
        val config = Properties()
        config["StrictHostKeyChecking"] = "no"
        s.setConfig(config)
        
        try {
            s.connect(10000)
        } catch (e: Exception) {
            throw Exception("SFTP connection to $host:$port failed: ${e.localizedMessage}")
        }
        session = s
        
        val c = try {
            s.openChannel("sftp") as ChannelSftp
        } catch (e: Exception) {
            s.disconnect()
            throw Exception("Failed to open SFTP channel on $host:$port: ${e.localizedMessage}")
        }

        try {
            c.connect(10000)
        } catch (e: Exception) {
            s.disconnect()
            throw Exception("Failed to connect SFTP channel on $host:$port: ${e.localizedMessage}")
        }

        channel = c
        c
    }

    override suspend fun testConnection() {
        val c = connect()
        try {
            c.pwd()
        } catch (e: Exception) {
            throw Exception("Connected to $host:$port, but SFTP subsystem test failed: ${e.localizedMessage}")
        }
    }
    override suspend fun listFiles(path: String): List<RemoteFile> = withContext(Dispatchers.IO) {
        val c = connect()
        val vector = c.ls(path) as Vector<ChannelSftp.LsEntry>
        vector.filter { (it.filename != ".") && (it.filename != "..") }.map { entry ->
            RemoteFile(
                name = entry.filename,
                path = if (path.endsWith("/")) "$path${entry.filename}" else "$path/${entry.filename}",
                size = entry.attrs.size,
                isDirectory = entry.attrs.isDir,
                lastModified = entry.attrs.mTime.toLong() * 1000L,
            )
        }
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        channel?.disconnect()
        session?.disconnect()
        channel = null
        session = null
    }

    override suspend fun createFile(path: String, name: String): Boolean = withContext(Dispatchers.IO) {
        val c = connect()
        val targetPath = if (path.endsWith("/")) "$path$name" else "$path/$name"
        try {
            c.put(ByteArrayInputStream(ByteArray(0)), targetPath)
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun createFolder(path: String, name: String): Boolean = withContext(Dispatchers.IO) {
        val c = connect()
        val targetPath = if (path.endsWith("/")) "$path$name" else "$path/$name"
        try {
            c.mkdir(targetPath)
            true
        } catch (_: Exception) {
            false
        }
    }
}
