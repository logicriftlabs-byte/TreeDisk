package com.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class StorageAnalyzer {

    data class AppStat(
        val packageName: String,
        val displayName: String,
        val size: Long,
        val fileCount: Int,
        val color: androidx.compose.ui.graphics.Color,
        val files: List<StorageNode.FileNode> = emptyList(),
    )

    data class ScanResult(
        val rootNode: StorageNode.DirectoryNode,
        val topFiles: List<StorageNode.FileNode>,
        val categoryStats: List<CategoryStat>,
        val categoryFiles: Map<FileCategory, List<StorageNode.FileNode>> = emptyMap(),
        val appStats: List<AppStat> = emptyList(),
    )

    suspend fun analyzeDirectory(
        directory: File,
        includeHidden: Boolean = false,
        ignoreSystem: Boolean = true,
        minSizeMb: Int = 0,
    ): ScanResult = withContext(Dispatchers.IO) {
        val topFiles = mutableListOf<StorageNode.FileNode>()
        val categorySizes = mutableMapOf<FileCategory, Long>()
        val categoryCounts = mutableMapOf<FileCategory, Int>()
        val categoryFiles = mutableMapOf<FileCategory, MutableList<StorageNode.FileNode>>()
        val appSizes = mutableMapOf<String, Long>()
        val appCounts = mutableMapOf<String, Int>()
        val appFiles = mutableMapOf<String, MutableList<StorageNode.FileNode>>()

        FileCategory.entries.forEach {
            categorySizes[it] = 0L
            categoryCounts[it] = 0
            categoryFiles[it] = mutableListOf()
        }

        val minSizeBytes = minSizeMb.toLong() * 1024L * 1024L

        fun shouldProcessFile(file: File): Boolean {
            if (!includeHidden && file.name.startsWith(".")) return false
            if (ignoreSystem && (file.path.contains("/Android/data/com.android.") || file.path.contains("/Android/obb/com.android."))) return false
            return (minSizeBytes <= 0) || (file.length() >= minSizeBytes)
        }

        fun processFile(file: File) {
            val length = file.length()
            if (length <= 0) return

            val cat = StorageNode.getCategoryForFile(file.name)
            categorySizes[cat] = (categorySizes[cat] ?: 0L) + length
            categoryCounts[cat] = (categoryCounts[cat] ?: 0) + 1

            val fileNode = StorageNode.FileNode(file = file, name = file.name, size = length, path = file.absolutePath)
            categoryFiles[cat]?.add(fileNode)

            val pkg = getAppPackageForFile(file)
            if (pkg != null) {
                appSizes[pkg] = (appSizes[pkg] ?: 0L) + length
                appCounts[pkg] = (appCounts[pkg] ?: 0) + 1
                if (!appFiles.containsKey(pkg)) {
                    appFiles[pkg] = mutableListOf()
                }
                appFiles[pkg]?.add(fileNode)
            }

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
            if (ignoreSystem && (dir.path.contains("/Android/data/com.android.") || dir.path.contains("/Android/obb/com.android."))) return 0L

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
            val isHidden = !includeHidden && f.name.startsWith(".")
            val isSystem = ignoreSystem && (f.path.contains("/Android/data/com.android.") || f.path.contains("/Android/obb/com.android."))
            !isHidden && !isSystem
        } ?: emptyList()

        var totalSize = 0L
        val childNodes = mutableListOf<StorageNode>()

        for (child in children) {
            if (child.isDirectory) {
                val size = scanDir(child)
                totalSize += size
                childNodes.add(StorageNode.DirectoryNode(file = child, name = child.name, size = size, path = child.absolutePath, childrenCount = child.list()?.size ?: 0))
            } else {
                if (shouldProcessFile(child)) {
                    val len = child.length()
                    totalSize += len
                    processFile(child)
                    childNodes.add(StorageNode.FileNode(file = child, name = child.name, size = len, path = child.absolutePath))
                }
            }
        }

        childNodes.sortByDescending { it.size }
        topFiles.sortByDescending { it.size }
        val finalTop10 = topFiles.take(10)

        // Sort files in each category by size descending
        val sortedCategoryFiles = categoryFiles.mapValues { entry ->
            entry.value.sortedByDescending { it.size }
        }

        val stats = FileCategory.entries.map { cat ->
            CategoryStat(
                category = cat,
                size = categorySizes[cat] ?: 0L,
                fileCount = categoryCounts[cat] ?: 0,
            )
        }.filter { (it.fileCount > 0) || (it.size > 0) }

        val rootNode = StorageNode.DirectoryNode(
            file = directory,
            name = directory.name.ifEmpty { "Root" },
            size = totalSize,
            path = directory.absolutePath,
            childrenCount = children.size,
            children = childNodes,
            isExpanded = true
        )

        val appStatsList = appSizes.map { (pkg, size) ->
            AppStat(
                packageName = pkg,
                displayName = getAppDisplayName(pkg),
                size = size,
                fileCount = appCounts[pkg] ?: 0,
                color = getAppColor(pkg),
                files = (appFiles[pkg] ?: emptyList()).sortedByDescending { it.size }
            )
        }.sortedByDescending { it.size }

        ScanResult(
            rootNode = rootNode,
            topFiles = finalTop10,
            categoryStats = stats,
            categoryFiles = sortedCategoryFiles,
            appStats = appStatsList
        )
    }

    private fun getAppPackageForFile(file: File): String? {
        val path = file.absolutePath
        
        // Detect via Android/data or Android/media or Android/obb
        val androidPathRegex = Regex(".*/Android/(data|media|obb)/([^/]+).*")
        val match = androidPathRegex.find(path)
        return match?.groupValues?.get(2) ?: when {
            path.contains("/WhatsApp/", ignoreCase = true) -> "com.whatsapp"
            path.contains("/Telegram/", ignoreCase = true) -> "org.telegram.messenger"
            path.contains("/Instagram/", ignoreCase = true) -> "com.instagram.android"
            path.contains("/Facebook/", ignoreCase = true) -> "com.facebook.katana"
            path.contains("/DCIM/Facebook/", ignoreCase = true) -> "com.facebook.katana"
            path.contains("/Snapchat/", ignoreCase = true) -> "com.snapchat.android"
            path.contains("/Download/Messenger/", ignoreCase = true) -> "com.facebook.orca"
            path.contains("/Spotify/", ignoreCase = true) -> "com.spotify.music"
            else -> null
        }
    }

    private fun getAppDisplayName(packageName: String): String {
        return when (packageName) {
            "com.whatsapp" -> "WhatsApp"
            "org.telegram.messenger" -> "Telegram"
            "com.instagram.android" -> "Instagram"
            "com.facebook.katana" -> "Facebook"
            "com.facebook.orca" -> "Messenger"
            "com.snapchat.android" -> "Snapchat"
            "com.spotify.music" -> "Spotify"
            "com.google.android.youtube" -> "YouTube"
            "com.google.android.apps.photos" -> "Photos"
            "com.android.chrome" -> "Chrome"
            "com.google.android.gm" -> "Gmail"
            "com.microsoft.office.outlook" -> "Outlook"
            "com.netflix.mediaclient" -> "Netflix"
            "com.disney.disneyplus" -> "Disney+"
            "com.amazon.mShop.android.shopping" -> "Amazon"
            "com.twitter.android" -> "Twitter / X"
            "com.reddit.frontpage" -> "Reddit"
            "com.valvesoftware.android.steam.community" -> "Steam"
            "com.discord" -> "Discord"
            "com.tiktok.android" -> "TikTok"
            else -> {
                packageName.split(".").lastOrNull()?.replaceFirstChar { it.uppercase() } ?: packageName
            }
        }
    }

    private fun getAppColor(packageName: String): androidx.compose.ui.graphics.Color {
        return when (packageName) {
            "com.whatsapp" -> androidx.compose.ui.graphics.Color(0xFF25D366)
            "org.telegram.messenger" -> androidx.compose.ui.graphics.Color(0xFF24A1DE)
            "com.instagram.android" -> androidx.compose.ui.graphics.Color(0xFFE4405F)
            "com.facebook.katana", "com.facebook.orca" -> androidx.compose.ui.graphics.Color(0xFF1877F2)
            "com.snapchat.android" -> androidx.compose.ui.graphics.Color(0xFFFFFC00)
            "com.spotify.music" -> androidx.compose.ui.graphics.Color(0xFF1DB954)
            "com.google.android.youtube" -> androidx.compose.ui.graphics.Color(0xFFFF0000)
            "com.netflix.mediaclient" -> androidx.compose.ui.graphics.Color(0xFFE50914)
            "com.tiktok.android" -> androidx.compose.ui.graphics.Color(0xFF000000)
            "com.discord" -> androidx.compose.ui.graphics.Color(0xFF5865F2)
            else -> {
                val hash = packageName.hashCode()
                val hue = (hash % 360).let { if (it < 0) it + 360 else it }.toFloat()
                androidx.compose.ui.graphics.Color.hsl(hue, 0.6f, 0.5f)
            }
        }
    }
}
