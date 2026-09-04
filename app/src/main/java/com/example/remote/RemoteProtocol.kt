package com.example.remote

import com.example.FileCategory
import com.example.StorageNode
import java.io.File

data class RemoteFile(
    val name: String,
    val path: String,
    val size: Long,
    val isDirectory: Boolean,
    val lastModified: Long
)

interface RemoteProtocol {
    suspend fun listFiles(path: String): List<RemoteFile>
    suspend fun testConnection()
    suspend fun disconnect()
}
