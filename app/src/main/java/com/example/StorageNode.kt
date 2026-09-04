package com.example

import androidx.compose.ui.graphics.Color
import java.io.File

enum class FileCategory(
    val displayName: String,
    val color: Color,
) {
    SYSTEM("Android System", Color(0xFF0284C7)), // Apple / Android System Blue
    VIDEOS("Videos", Color(0xFFF87171)),       // StorageRed
    IMAGES("Images", Color(0xFFFB923C)),       // StorageOrange
    AUDIO("Audio", Color(0xFFFACC15)),         // StorageYellow
    DOCUMENTS("Documents", Color(0xFF34D399)),  // StorageGreen
    APPS("Apps", Color(0xFF81D4FA)),           // LightBlue
    ARCHIVES("Archives", Color(0xFFD0BCFF)),   // PrimaryPurple
    OTHER("Other", Color(0xFF9CA3AF))          // Gray
}

data class CategoryStat(
    val category: FileCategory,
    val size: Long,
    val fileCount: Int,
)

sealed class StorageNode {
    abstract val name: String
    abstract val size: Long
    abstract val isDirectory: Boolean
    abstract val path: String

    data class FileNode(
        val file: File? = null, // Local file
        override val name: String,
        override val size: Long,
        override val path: String,
        val isRemote: Boolean = false,
    ) : StorageNode() {
        override val isDirectory: Boolean = false
        val category: FileCategory get() = getCategoryForFile(name)
    }

    data class DirectoryNode(
        val file: File? = null, // Local directory
        override val name: String,
        override val size: Long,
        override val path: String,
        val childrenCount: Int,
        val children: List<StorageNode> = emptyList(),
        val isExpanded: Boolean = false,
        val isRemote: Boolean = false,
        val connectionId: Long? = null, // For remote directories
    ) : StorageNode() {
        override val isDirectory: Boolean = true
    }

    companion object {
        fun getCategoryForFile(fileName: String): FileCategory {
            return when (fileName.substringAfterLast('.', "").lowercase()) {
                "mp4", "mkv", "avi", "mov", "3gp", "webm", "flv", "m4v" -> FileCategory.VIDEOS
                "jpg", "jpeg", "png", "gif", "webp", "heic", "bmp", "svg" -> FileCategory.IMAGES
                "mp3", "wav", "flac", "aac", "m4a", "ogg", "wma", "opus" -> FileCategory.AUDIO
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "json", "xml", "epub" -> FileCategory.DOCUMENTS
                "apk", "xapk", "apks", "aab" -> FileCategory.APPS
                "zip", "rar", "7z", "tar", "gz", "iso", "bz2" -> FileCategory.ARCHIVES
                else -> FileCategory.OTHER
            }
        }
    }
}
