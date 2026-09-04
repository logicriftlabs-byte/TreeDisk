package com.example

import android.app.Application
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RemoteConnection
import com.example.remote.RemoteProtocolFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.File

class StorageViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStoreManager = com.example.data.RemoteConnectionDataStore(application)
    private val analyzer = StorageAnalyzer()

    val remoteConnections = dataStoreManager.connectionsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    
    private var _localRoot: StorageNode.DirectoryNode? = null
    private val _remoteRoots = mutableMapOf<Long, StorageNode.DirectoryNode>()
    
    private val _activeRoots = MutableStateFlow<List<StorageNode.DirectoryNode>>(emptyList())
    val activeRoots: StateFlow<List<StorageNode.DirectoryNode>> = _activeRoots.asStateFlow()
    
    private val _selectedRootId = MutableStateFlow<String?>("local")
    val selectedRootId: StateFlow<String?> = _selectedRootId.asStateFlow()
    
    fun selectRoot(id: String) {
        _selectedRootId.value = id
        rebuildVirtualRoot()

        val connId = id.toLongOrNull()
        if (connId != null) {
            val connection = remoteConnections.value.find { it.id == connId }
            if (connection != null) {
                val remoteRoot = _remoteRoots[connId]
                if ((remoteRoot == null) || remoteRoot.children.isEmpty()) {
                    scanRemoteConnection(connection)
                }
            }
        }
    }

    private fun rebuildVirtualRoot() {
        val roots = mutableListOf<StorageNode.DirectoryNode>()
        _localRoot?.let { 
            roots.add(it.copy(name = "Internal Storage", path = "local", isExpanded = true)) 
        }
        roots.addAll(_remoteRoots.values.map { it.copy(isExpanded = true) })
        
        _activeRoots.value = roots

        if (roots.isEmpty()) {
            _rootNode.value = null
        } else {
            val selected = roots.find { (it.path == _selectedRootId.value) || (it.connectionId?.toString() == _selectedRootId.value) } 
                ?: roots.firstOrNull()
            
            _selectedRootId.value = selected?.connectionId?.toString() ?: "local"
            _rootNode.value = selected
        }
    }

private val _rootNode = MutableStateFlow<StorageNode.DirectoryNode?>(null)
    val rootNode: StateFlow<StorageNode.DirectoryNode?> = _rootNode.asStateFlow()

    private val _topFiles = MutableStateFlow<List<StorageNode.FileNode>>(emptyList())
    val topFiles: StateFlow<List<StorageNode.FileNode>> = _topFiles.asStateFlow()

    private val _categoryStats = MutableStateFlow<List<CategoryStat>>(emptyList())
    val categoryStats: StateFlow<List<CategoryStat>> = _categoryStats.asStateFlow()

    private val _categoryFiles = MutableStateFlow<Map<FileCategory, List<StorageNode.FileNode>>>(emptyMap())
    val categoryFiles: StateFlow<Map<FileCategory, List<StorageNode.FileNode>>> = _categoryFiles.asStateFlow()

    private val _appStats = MutableStateFlow<List<StorageAnalyzer.AppStat>>(emptyList())
    val appStats: StateFlow<List<StorageAnalyzer.AppStat>> = _appStats.asStateFlow()

    private val _isScanning = MutableStateFlow(value = false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _totalSpace = MutableStateFlow(0L)
    val totalSpace: StateFlow<Long> = _totalSpace.asStateFlow()

    private val _freeSpace = MutableStateFlow(0L)
    val freeSpace: StateFlow<Long> = _freeSpace.asStateFlow()
    
    private val _usedSpace = MutableStateFlow(0L)
    val usedSpace: StateFlow<Long> = _usedSpace.asStateFlow()

    // Settings State
    private val _includeHiddenFiles = MutableStateFlow(false)
    val includeHiddenFiles: StateFlow<Boolean> = _includeHiddenFiles.asStateFlow()

    private val _ignoreSystemCache = MutableStateFlow(true)
    val ignoreSystemCache: StateFlow<Boolean> = _ignoreSystemCache.asStateFlow()

    private val _minFileSizeMb = MutableStateFlow(0)
    val minFileSizeMb: StateFlow<Int> = _minFileSizeMb.asStateFlow()

    private val _storageThreshold = MutableStateFlow(85)
    val storageThreshold: StateFlow<Int> = _storageThreshold.asStateFlow()

    private val _autoScanOnLaunch = MutableStateFlow(true)
    val autoScanOnLaunch: StateFlow<Boolean> = _autoScanOnLaunch.asStateFlow()

    private val _lastScanTime = MutableStateFlow<Long?>(null)
    val lastScanTime: StateFlow<Long?> = _lastScanTime.asStateFlow()

    init {
        scanStorage()
        viewModelScope.launch {
            remoteConnections.collect { connections ->
                val currentIds = connections.map { it.id }.toSet()
                _remoteRoots.keys.retainAll(currentIds)

                connections.forEach { conn ->
                    if (!_remoteRoots.containsKey(conn.id)) {
                        _remoteRoots[conn.id] = StorageNode.DirectoryNode(
                            name = conn.name,
                            size = 0L,
                            path = conn.remotePath.ifBlank { "/" },
                            childrenCount = 0,
                            children = emptyList(),
                            isExpanded = true,
                            isRemote = true,
                            connectionId = conn.id,
                        )
                    } else {
                        val existing = _remoteRoots[conn.id]
                        if (existing != null && (existing.name != conn.name || existing.path != conn.remotePath)) {
                            _remoteRoots[conn.id] = existing.copy(
                                name = conn.name,
                                path = conn.remotePath.ifBlank { "/" },
                            )
                        }
                    }
                }
                rebuildVirtualRoot()
            }
        }
    }

    fun scanStorage(preservedPaths: Set<String>? = null) {
        viewModelScope.launch {
            _isScanning.value = true

            val currentSelectedId = _selectedRootId.value
            val remoteConnId = currentSelectedId?.toLongOrNull()
            val currentRemoteConn = if (remoteConnId != null) remoteConnections.value.find { it.id == remoteConnId } else null

            if (currentRemoteConn != null) {
                scanRemoteConnection(currentRemoteConn)
                _isScanning.value = false
                return@launch
            }
            
            val externalStorage = Environment.getExternalStorageDirectory()
            updateStorageStats(externalStorage)
            
            val result = analyzer.analyzeDirectory(
                directory = externalStorage,
                includeHidden = _includeHiddenFiles.value,
                ignoreSystem = _ignoreSystemCache.value,
                minSizeMb = _minFileSizeMb.value
            )
            
            var root = result.rootNode
            if (preservedPaths != null) {
                root = restoreExpansionState(root, preservedPaths)
            }
            
            _localRoot = root
            rebuildVirtualRoot()
            _topFiles.value = result.topFiles
            _categoryFiles.value = result.categoryFiles
            _appStats.value = result.appStats

            val scannedUserSize = result.categoryStats.filter { it.category != FileCategory.SYSTEM }.sumOf { it.size }
            val systemSize = (_usedSpace.value - scannedUserSize).coerceAtLeast(0L)

            val updatedStats = mutableListOf<CategoryStat>()
            if (systemSize > 0L) {
                updatedStats.add(
                    CategoryStat(
                        category = FileCategory.SYSTEM,
                        size = systemSize,
                        fileCount = 1
                    )
                )
            }
            updatedStats.addAll(result.categoryStats.filter { it.category != FileCategory.SYSTEM })

            _categoryStats.value = updatedStats
            _lastScanTime.value = System.currentTimeMillis()
            
            _isScanning.value = false
        }
    }

    private fun getExpandedPaths(node: StorageNode): Set<String> {
        val paths = mutableSetOf<String>()
        if ((node is StorageNode.DirectoryNode) && node.isExpanded) {
            paths.add(node.path)
            node.children.forEach { paths.addAll(getExpandedPaths(it)) }
        }
        return paths
    }

    private suspend fun restoreExpansionState(node: StorageNode.DirectoryNode, expandedPaths: Set<String>): StorageNode.DirectoryNode {
        if (expandedPaths.contains(node.path)) {
            val children = if (node.children.isEmpty()) {
                val file = node.file
                if (file != null) {
                    analyzer.analyzeDirectory(
                        directory = file,
                        includeHidden = _includeHiddenFiles.value,
                        ignoreSystem = _ignoreSystemCache.value,
                        minSizeMb = _minFileSizeMb.value
                    ).rootNode.children
                } else {
                    if (node.isRemote && node.connectionId != null) {
                        val connection = remoteConnections.value.find { it.id == node.connectionId }
                        if (connection != null) {
                            try {
                                val protocol = RemoteProtocolFactory.create(connection)
                                val files = protocol.listFiles(node.path)
                                val nodes = files.map { rf ->
                                    if (rf.isDirectory) {
                                        StorageNode.DirectoryNode(
                                            name = rf.name,
                                            size = rf.size,
                                            path = rf.path,
                                            childrenCount = 0,
                                            isRemote = true,
                                            connectionId = connection.id
                                        )
                                    } else {
                                        StorageNode.FileNode(
                                            name = rf.name,
                                            size = rf.size,
                                            path = rf.path,
                                            isRemote = true
                                        )
                                    }
                                }
                                protocol.disconnect()
                                nodes.sortedByDescending { it.isDirectory }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                emptyList()
                            }
                        } else {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                }
            } else {
                node.children
            }
            
            val updatedChildren = children.map { child ->
                if (child is StorageNode.DirectoryNode) {
                    restoreExpansionState(child, expandedPaths)
                } else {
                    child
                }
            }
            return node.copy(isExpanded = true, children = updatedChildren)
        }
        return node
    }

    fun setIncludeHiddenFiles(value: Boolean) {
        _includeHiddenFiles.value = value
        scanStorage(getExpandedPaths(_rootNode.value ?: return))
    }

    fun setIgnoreSystemCache(value: Boolean) {
        _ignoreSystemCache.value = value
        scanStorage(getExpandedPaths(_rootNode.value ?: return))
    }

    fun setMinFileSizeMb(sizeMb: Int) {
        _minFileSizeMb.value = sizeMb
        scanStorage(getExpandedPaths(_rootNode.value ?: return))
    }

    fun setStorageThreshold(threshold: Int) {
        _storageThreshold.value = threshold
    }

    fun setAutoScanOnLaunch(value: Boolean) {
        _autoScanOnLaunch.value = value
    }

    fun clearCache() {
        _localRoot = null
        rebuildVirtualRoot()
        _topFiles.value = emptyList()
        _categoryStats.value = emptyList()
        _lastScanTime.value = null
    }

    private fun updateStorageStats(file: File) {
        try {
            val stat = StatFs(file.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong
            
            _totalSpace.value = totalBlocks * blockSize
            _freeSpace.value = availableBlocks * blockSize
            _usedSpace.value = _totalSpace.value - _freeSpace.value
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleNode(node: StorageNode.DirectoryNode) {
        viewModelScope.launch {
            val currentRoot = _rootNode.value ?: return@launch
            _isScanning.value = true
            
            val newRoot = toggleNodeRecursive(currentRoot, node)
            _rootNode.value = newRoot
            
            _isScanning.value = false
        }
    }
    
    private suspend fun toggleNodeRecursive(current: StorageNode.DirectoryNode, target: StorageNode.DirectoryNode): StorageNode.DirectoryNode {
        if (current.path == target.path && current.isRemote == target.isRemote && current.connectionId == target.connectionId) {
            val willBeExpanded = !current.isExpanded
            val newChildren = if (willBeExpanded && current.children.isEmpty()) {
                val file = current.file
                if (file != null) {
                    analyzer.analyzeDirectory(
                        directory = file,
                        includeHidden = _includeHiddenFiles.value,
                        ignoreSystem = _ignoreSystemCache.value,
                        minSizeMb = _minFileSizeMb.value
                    ).rootNode.children
                } else {
                    if (current.isRemote && current.connectionId != null) {
                        val connection = remoteConnections.value.find { it.id == current.connectionId }
                        if (connection != null) {
                            try {
                                val protocol = RemoteProtocolFactory.create(connection)
                                val files = protocol.listFiles(current.path)
                                val nodes = files.map { rf ->
                                    if (rf.isDirectory) {
                                        StorageNode.DirectoryNode(
                                            name = rf.name,
                                            size = rf.size,
                                            path = rf.path,
                                            childrenCount = 0,
                                            isRemote = true,
                                            connectionId = connection.id
                                        )
                                    } else {
                                        StorageNode.FileNode(
                                            name = rf.name,
                                            size = rf.size,
                                            path = rf.path,
                                            isRemote = true
                                        )
                                    }
                                }
                                protocol.disconnect()
                                nodes.sortedByDescending { it.isDirectory }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                emptyList()
                            }
                        } else {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                }
            } else {
                current.children
            }
            return current.copy(isExpanded = willBeExpanded, children = newChildren)
        }
        
        val updatedChildren = current.children.map { 
            if (it is StorageNode.DirectoryNode) {
                if (target.path.startsWith(it.path)) {
                   return@map toggleNodeRecursive(it, target)
                }
            }
            it
        }
        return current.copy(children = updatedChildren)
    }

    fun deleteNode(node: StorageNode, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isScanning.value = true
            val success = withContext(Dispatchers.IO) {
                try {
                    val file = (node as? StorageNode.FileNode)?.file ?: (node as? StorageNode.DirectoryNode)?.file
                    if (file != null && file.exists()) {
                        file.deleteRecursively()
                    } else true
                } catch (_: Exception) {
                    false
                }
            }
            if (success) {
                onResult(true, "Successfully deleted '${node.name}'")
                val preservedPaths = _rootNode.value?.let { getExpandedPaths(it) }
                scanStorage(preservedPaths)
            } else {
                _isScanning.value = false
                onResult(false, "Failed to delete '${node.name}'")
            }
        }
    }

    fun testAndAddRemoteConnection(connection: RemoteConnection, onResult: (Boolean, String?) -> Unit) {
        if (connection.name.isBlank()) {
            onResult(false, "Connection name is required")
            return
        }
        if (connection.host.isBlank()) {
            onResult(false, "Host/IP is required")
            return
        }

        viewModelScope.launch {
            try {
                val protocol = withContext(Dispatchers.IO) { RemoteProtocolFactory.create(connection) }
                withContext(Dispatchers.IO) { 
                    protocol.testConnection()
                    protocol.disconnect() 
                }
                
                val savedConn = dataStoreManager.saveConnection(connection)
                selectRoot(savedConn.id.toString())
                onResult(true, null)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false, e.message ?: "Failed to connect to the server")
            }
        }
    }

    fun deleteRemoteConnection(connection: RemoteConnection) {
        viewModelScope.launch {
            dataStoreManager.deleteConnection(connection)
            _remoteRoots.remove(connection.id)
            if (_selectedRootId.value == connection.id.toString()) {
                _selectedRootId.value = "local"
            }
            rebuildVirtualRoot()
        }
    }

    fun scanRemoteConnection(connection: RemoteConnection) {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val targetPath = connection.remotePath.ifBlank { "/" }
                val protocol = withContext(Dispatchers.IO) { RemoteProtocolFactory.create(connection) }
                val remoteFiles = withContext(Dispatchers.IO) { protocol.listFiles(targetPath) }
                val childNodes = remoteFiles.map { rf ->
                    if (rf.isDirectory) {
                        StorageNode.DirectoryNode(
                            name = rf.name,
                            size = rf.size,
                            path = rf.path,
                            childrenCount = 0,
                            isRemote = true,
                            connectionId = connection.id,
                        )
                    } else {
                        StorageNode.FileNode(
                            name = rf.name,
                            size = rf.size,
                            path = rf.path,
                            isRemote = true,
                        )
                    }
                }.sortedByDescending { it.isDirectory }
                
                val totalSize = childNodes.sumOf { it.size }
                
                _remoteRoots[connection.id] = StorageNode.DirectoryNode(
                    name = connection.name,
                    size = totalSize,
                    path = targetPath,
                    childrenCount = childNodes.size,
                    children = childNodes,
                    isExpanded = true,
                    isRemote = true,
                    connectionId = connection.id,
                )
                
                rebuildVirtualRoot()
                withContext(Dispatchers.IO) { protocol.disconnect() }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun organizeDirectoryWithAI(directory: StorageNode.DirectoryNode, onResult: (Boolean, String) -> Unit) {
        if (directory.isRemote) {
            onResult(false, "AI Organization is currently only supported for local storage.")
            return
        }
        val dirFile = directory.file ?: return
        
        viewModelScope.launch {
            _isScanning.value = true
            try {
                // Get list of local files
                val filesToOrganize = directory.children.filterIsInstance<StorageNode.FileNode>()
                if (filesToOrganize.isEmpty()) {
                    withContext(Dispatchers.Main) { onResult(false, "No files to organize.") }
                    return@launch
                }
                
                val fileNames = filesToOrganize.map { it.name }
                val categorized = AIAssistant.categorizeFiles(fileNames)
                
                var movedCount = 0
                withContext(Dispatchers.IO) {
                    categorized.forEach { (folderName, fileList) ->
                        val targetDir = File(dirFile, folderName)
                        if (!targetDir.exists()) {
                            targetDir.mkdirs()
                        }
                        
                        fileList.forEach { fileName ->
                            val sourceFile = File(dirFile, fileName)
                            if (sourceFile.exists() && sourceFile.isFile) {
                                val targetFile = File(targetDir, fileName)
                                if (sourceFile.renameTo(targetFile)) {
                                    movedCount++
                                }
                            }
                        }
                    }
                }
                
                // Refresh the directory
                scanStorage()
                withContext(Dispatchers.Main) { onResult(true, "Organized $movedCount files into ${categorized.size} folders.") }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onResult(false, e.message ?: "Failed to organize files") }
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun createFile(targetDirPath: String, name: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val targetDir = File(targetDirPath)
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }
                
                val newFile = File(targetDir, name)
                if (newFile.exists()) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(false, "File already exists") }
                    return@launch
                }
                
                val success = newFile.createNewFile()
                if (success) {
                    val expanded = _rootNode.value?.let { getExpandedPaths(it) }
                    scanStorage(expanded)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(true, "File created successfully") }
                } else {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(false, "Failed to create file") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(false, e.message ?: "Error creating file") }
            }
        }
    }
    
    fun createFolder(targetDirPath: String, name: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val targetDir = java.io.File(targetDirPath)
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }
                
                val newFolder = java.io.File(targetDir, name)
                if (newFolder.exists()) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(false, "Folder already exists") }
                    return@launch
                }
                
                val success = newFolder.mkdirs()
                if (success) {
                    val expanded = _rootNode.value?.let { getExpandedPaths(it) }
                    scanStorage(expanded)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(true, "Folder created successfully") }
                } else {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(false, "Failed to create folder") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(false, e.message ?: "Error creating folder") }
            }
        }
    }
}

