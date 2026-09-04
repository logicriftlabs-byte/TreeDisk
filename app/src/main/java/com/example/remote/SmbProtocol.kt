package com.example.remote

import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmbProtocol(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
    private val domain: String = "",
) : RemoteProtocol {

    private var client: SMBClient? = null
    private var session: Session? = null
    
    // For SMB, we usually need a share name. I'll assume the first part of the path is the share name if not specified.
    // Or I'll just list shares if path is root.
    
    private suspend fun getSession(): Session = withContext(Dispatchers.IO) {
        val currentSession = session
        if ((currentSession != null) && currentSession.connection.isConnected) {
            return@withContext currentSession
        }

        val c = SMBClient()
        val s = c.connect(host, port)
        val auth = AuthenticationContext(username, password.toCharArray(), domain)
        val sess = s.authenticate(auth)
        
        client = c
        session = sess
        sess
    }

    override suspend fun testConnection() { getSession() }
    override suspend fun listFiles(path: String): List<RemoteFile> = withContext(Dispatchers.IO) {
        val sess = getSession()
        
        // Basic implementation: expect path to be "shareName/folder/path"
        val parts = path.trimStart('/').split('/', limit = 2)
        val shareName = parts[0]
        val folderPath = if (parts.size > 1) parts[1] else ""
        
        if (shareName.isEmpty()) {
            // List shares? SMBJ doesn't make this trivial without specific APIs
            return@withContext emptyList()
        }

        val share = sess.connectShare(shareName) as DiskShare
        val list = share.list(folderPath)
        list.filter { (it.fileName != ".") && (it.fileName != "..") }.map { info ->
            RemoteFile(
                name = info.fileName,
                path = if (path.endsWith("/")) "$path${info.fileName}" else "$path/${info.fileName}",
                size = info.allocationSize, // Simplified
                isDirectory = info.fileAttributes.and(0x10L) != 0L, // 0x10 is Directory attribute
                lastModified = info.changeTime.toEpochMillis(),
            )
        }
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        session?.close()
        client?.close()
        session = null
        client = null
    }
}
