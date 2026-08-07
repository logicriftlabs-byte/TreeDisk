package com.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class StorageAnalyzer {

    data class ScanResult(
        val rootNode: StorageNode.DirectoryNode,
        val topFiles: List<StorageNode.FileNode>,
        val categoryStats: List<CategoryStat>
    )

    suspend fun analyzeDirectory(
        directory: File,
        includeHidden: Boolean = false,
        ignoreSystem: Boolean = true,
        minSizeMb: Int = 0
    ): ScanResult = withContext(Dispatchers.IO) {
        val topFiles = mutableListOf<StorageNode.FileNode>()
        val categorySizes = mutableMapOf<FileCategory, Long>()
        val categoryCounts = mutableMapOf<FileCategory, Int>()

        FileCategory.values().forEach {
            categorySizes[it] = 0L
            categoryCounts[it] = 0
        }

        val minSizeBytes = minSizeMb.toLong() * 1024L * 1024L

        fun shouldProcessFile(file: File): Boolean {
            if (!includeHidden && file.name.startsWith(".")) return false
            if (ignoreSystem && (file.path.contains("/Android/data") || file.path.contains("/Android/obb"))) return false
            if (minSizeBytes > 0 && file.length() < minSizeBytes) return false
            return true
        }

        fun processFile(file: File) {
            val length = file.length()
            if (length <= 0) return

            val cat = StorageNode.getCategoryForFile(file)
            categorySizes[cat] = (categorySizes[cat] ?: 0L) + length
            categoryCounts[cat] = (categoryCounts[cat] ?: 0) + 1

            val fileNode = StorageNode.FileNode(file, length)
            topFiles.add(fileNode)
            if (topFiles.size > 100) {
                topFiles.sortByDescending { it.size }
                while (topFiles.size > 20) {
                    topFiles.removeAt(topFiles.size - 1)
                }
            }
        }

        fun scanDir(dir: File): Long {
            var dirSize = 0L
            if (!includeHidden && dir.name.startsWith(".")) return 0L
            if (ignoreSystem && (dir.path.contains("/Android/data") || dir.path.contains("/Android/obb"))) return 0L

            val files = dir.listFiles() ?: return 0L
            for (f in files) {
                if (f.isDirectory) {
                    dirSize += scanDir(f)
                } else {
                    if (shouldProcessFile(f)) {
                        val len = f.length()
                        dirSize += len
                        processFile(f)
                    }
                }
            }
            return dirSize
        }

        val children = directory.listFiles()?.filter { f ->
            if (!includeHidden && f.name.startsWith(".")) false
            else if (ignoreSystem && (f.path.contains("/Android/data") || f.path.contains("/Android/obb"))) false
            else true
        } ?: emptyList()

        var totalSize = 0L
        val childNodes = mutableListOf<StorageNode>()

        for (child in children) {
            if (child.isDirectory) {
                val size = scanDir(child)
                totalSize += size
                childNodes.add(StorageNode.DirectoryNode(child, size, child.list()?.size ?: 0))
            } else {
                if (shouldProcessFile(child)) {
                    val len = child.length()
                    totalSize += len
                    processFile(child)
                    childNodes.add(StorageNode.FileNode(child, len))
                }
            }
        }

        childNodes.sortByDescending { it.size }
        topFiles.sortByDescending { it.size }
        val finalTop10 = topFiles.take(10)

        val stats = FileCategory.values().map { cat ->
            CategoryStat(
                category = cat,
                size = categorySizes[cat] ?: 0L,
                fileCount = categoryCounts[cat] ?: 0
            )
        }.filter { it.fileCount > 0 || it.size > 0 }

        val rootNode = StorageNode.DirectoryNode(directory, totalSize, children.size, childNodes, isExpanded = true)

        ScanResult(
            rootNode = rootNode,
            topFiles = finalTop10,
            categoryStats = stats
        )
    }
}
