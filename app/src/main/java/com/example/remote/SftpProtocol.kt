package com.example.remote

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        
        val config = Properties()
        config["StrictHostKeyChecking"] = "no"
        s.setConfig(config)
        
        s.connect()
        session = s
        
        val c = s.openChannel("sftp") as ChannelSftp
        c.connect()
        channel = c
        c
    }

    override suspend fun testConnection() { connect() }
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
}
