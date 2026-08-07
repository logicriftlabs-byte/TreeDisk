package com.example

import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class StorageViewModel : ViewModel() {
    private val analyzer = StorageAnalyzer()

    private val _rootNode = MutableStateFlow<StorageNode.DirectoryNode?>(null)
    val rootNode: StateFlow<StorageNode.DirectoryNode?> = _rootNode.asStateFlow()

    private val _topFiles = MutableStateFlow<List<StorageNode.FileNode>>(emptyList())
    val topFiles: StateFlow<List<StorageNode.FileNode>> = _topFiles.asStateFlow()

    private val _categoryStats = MutableStateFlow<List<CategoryStat>>(emptyList())
    val categoryStats: StateFlow<List<CategoryStat>> = _categoryStats.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
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
    }

    fun scanStorage() {
        viewModelScope.launch {
            _isScanning.value = true
            
            val externalStorage = Environment.getExternalStorageDirectory()
            updateStorageStats(externalStorage)
            
            val result = analyzer.analyzeDirectory(
                directory = externalStorage,
                includeHidden = _includeHiddenFiles.value,
                ignoreSystem = _ignoreSystemCache.value,
                minSizeMb = _minFileSizeMb.value
            )
            _rootNode.value = result.rootNode
            _topFiles.value = result.topFiles
            _categoryStats.value = result.categoryStats
            _lastScanTime.value = System.currentTimeMillis()
            
            _isScanning.value = false
        }
    }

    fun setIncludeHiddenFiles(value: Boolean) {
        _includeHiddenFiles.value = value
        scanStorage()
    }

    fun setIgnoreSystemCache(value: Boolean) {
        _ignoreSystemCache.value = value
        scanStorage()
    }

    fun setMinFileSizeMb(sizeMb: Int) {
        _minFileSizeMb.value = sizeMb
        scanStorage()
    }

    fun setStorageThreshold(threshold: Int) {
        _storageThreshold.value = threshold
    }

    fun setAutoScanOnLaunch(value: Boolean) {
        _autoScanOnLaunch.value = value
    }

    fun clearCache() {
        _rootNode.value = null
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
        if (current.file.absolutePath == target.file.absolutePath) {
            val willBeExpanded = !current.isExpanded
            val newChildren = if (willBeExpanded && current.children.isEmpty()) {
                analyzer.analyzeDirectory(
                    directory = current.file,
                    includeHidden = _includeHiddenFiles.value,
                    ignoreSystem = _ignoreSystemCache.value,
                    minSizeMb = _minFileSizeMb.value
                ).rootNode.children
            } else {
                current.children
            }
            return current.copy(isExpanded = willBeExpanded, children = newChildren)
        }
        
        val updatedChildren = current.children.map { 
            if (it is StorageNode.DirectoryNode) {
                if (target.file.absolutePath.startsWith(it.file.absolutePath)) {
                   return@map toggleNodeRecursive(it, target)
                }
            }
            it
        }
        return current.copy(children = updatedChildren)
    }
}
