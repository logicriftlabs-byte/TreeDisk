package com.example

import androidx.compose.ui.graphics.Color
import java.io.File

enum class FileCategory(
    val displayName: String,
    val color: Color
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
    val fileCount: Int
)

sealed class StorageNode {
    abstract val file: File
    abstract val size: Long
    abstract val isDirectory: Boolean
    val name: String get() = file.name

    data class FileNode(
        override val file: File,
        override val size: Long
    ) : StorageNode() {
        override val isDirectory: Boolean = false
        val category: FileCategory get() = getCategoryForFile(file)
    }

    data class DirectoryNode(
        override val file: File,
        override val size: Long,
        val childrenCount: Int,
        val children: List<StorageNode> = emptyList(),
        val isExpanded: Boolean = false
    ) : StorageNode() {
        override val isDirectory: Boolean = true
    }

    companion object {
        fun getCategoryForFile(file: File): FileCategory {
            val ext = file.extension.lowercase()
            return when (ext) {
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
