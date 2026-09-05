package com.example.remote

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

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

        val config = SmbConfig.builder()
            .withTimeout(10000, TimeUnit.MILLISECONDS)
            .withSoTimeout(10000, TimeUnit.MILLISECONDS)
            .build()
        val c = SMBClient(config)
        
        val conn = try {
            c.connect(host, port)
        } catch (e: Exception) {
            throw Exception("SMB connection to $host:$port failed: ${e.localizedMessage}")
        }

        val auth = AuthenticationContext(username, password.toCharArray(), domain)
        val sess = try {
            conn.authenticate(auth)
        } catch (e: Exception) {
            conn.close()
            throw Exception("SMB authentication failed for user '$username': ${e.localizedMessage}")
        }

        client = c
        session = sess
        sess
    }

    override suspend fun testConnection() {
        val sess = getSession()
        if (!sess.connection.isConnected) {
            throw Exception("SMB connection check failed")
        }
    }
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

    override suspend fun createFile(path: String, name: String): Boolean = withContext(Dispatchers.IO) {
        val sess = getSession()
        val parts = path.trimStart('/').split('/', limit = 2)
        val shareName = parts[0]
        if (shareName.isEmpty()) return@withContext false
        
        val folderPath = if (parts.size > 1) parts[1] else ""
        val targetPath = if (folderPath.isEmpty()) name else "$folderPath\\$name"
        
        try {
            val share = sess.connectShare(shareName) as DiskShare
            val f = share.openFile(
                targetPath, 
                setOf(AccessMask.GENERIC_WRITE),
                setOf(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_CREATE,
                null
            )
            f.close()
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun createFolder(path: String, name: String): Boolean = withContext(Dispatchers.IO) {
        val sess = getSession()
        val parts = path.trimStart('/').split('/', limit = 2)
        val shareName = parts[0]
        if (shareName.isEmpty()) return@withContext false
        
        val folderPath = if (parts.size > 1) parts[1] else ""
        val targetPath = if (folderPath.isEmpty()) name else "$folderPath\\$name"
        
        try {
            val share = sess.connectShare(shareName) as DiskShare
            share.mkdir(targetPath)
            true
        } catch (_: Exception) {
            false
        }
    }
}
