package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.StorageNode
import com.example.StorageViewModel
import com.example.Utils
import com.example.ui.theme.*
import com.example.ui.openFile
import com.example.ui.WindowsDeleteConfirmDialog
import com.example.ui.StorageSortOption
import com.example.ui.ItemTypeFilter
import com.example.ui.FileSizeFilter
import com.example.ui.StorageFilterConfig
import com.example.ui.filterAndSortTree
import com.example.ui.countNodes
import com.example.ui.SortDropdownMenu
import com.example.ui.FilterDialog

@Composable
fun TreeScreen(viewModel: StorageViewModel, onOpenDashboard: () -> Unit = {}, onOpenSettings: () -> Unit = {}) {
    val rootNode by viewModel.rootNode.collectAsState()
    val activeRoots by viewModel.activeRoots.collectAsState()
    val selectedRootId by viewModel.selectedRootId.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val remoteConnections by viewModel.remoteConnections.collectAsState()

    var sortOption by remember { mutableStateOf(StorageSortOption.NAME_ASC) }
    var foldersFirst by remember { mutableStateOf(true) }
    var showSortMenu by remember { mutableStateOf(false) }

    var filterConfig by remember { mutableStateOf(StorageFilterConfig()) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }

    val processedRootNode = remember(rootNode, filterConfig, sortOption, foldersFirst) {
        rootNode?.let { filterAndSortTree(it, filterConfig, sortOption, foldersFirst) }
    }

    val flatList = remember(processedRootNode, filterConfig.searchQuery) {
        val autoExpand = filterConfig.searchQuery.isNotBlank()
        val list = mutableListOf<FlatNode>()
        if (processedRootNode is StorageNode.DirectoryNode) {
            for (child in processedRootNode.children) {
                list.addAll(flattenTree(child, 0, autoExpand = autoExpand))
            }
        }
        list
    }

    val totalUnfilteredCount = remember(rootNode) { countNodes(rootNode) }
    val totalFilteredCount = remember(processedRootNode) { countNodes(processedRootNode) }
    val hiddenCount = (totalUnfilteredCount - totalFilteredCount).coerceAtLeast(0)

    val listState = rememberLazyListState()

    var selectedNodeForMenu by remember { mutableStateOf<StorageNode?>(null) }
    var selectedNodeForDelete by remember { mutableStateOf<StorageNode?>(null) }
    var showAddRemoteDialog by remember { mutableStateOf(false) }
    var showManageConnectionsDialog by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var isCreatingFolder by remember { mutableStateOf(false) }
    var selectedNodeForCreate by remember { mutableStateOf<StorageNode.DirectoryNode?>(null) }
    var lastExpandedNode by remember { mutableStateOf<StorageNode.DirectoryNode?>(null) }

    val context = LocalContext.current

    selectedNodeForMenu?.let { node ->
        WindowsContextMenuPopup(
            node = node,
            onDismiss = { selectedNodeForMenu = null },
            onExpandToggle = { folder ->
                viewModel.toggleNode(folder)
            },
            onOrganizeRequest = { folder ->
                viewModel.organizeDirectoryWithAI(folder) { _, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            },
            onCreateRequest = { folder, isFolder ->
                selectedNodeForCreate = folder
                isCreatingFolder = isFolder
                showCreateDialog = true
            },
            onDeleteRequest = { nodeToDelete ->
                selectedNodeForDelete = nodeToDelete
            }
        )
    }

    if (showManageConnectionsDialog) {
        ManageConnectionsDialog(
            connections = remoteConnections,
            onDismiss = { showManageConnectionsDialog = false },
            onDelete = { conn ->
                viewModel.deleteRemoteConnection(conn)
            }
        )
    }

    if (showCreateDialog) {
        CreateItemDialog(
            isFolder = isCreatingFolder,
            initialPath = selectedNodeForCreate?.path ?: android.os.Environment.getExternalStorageDirectory().absolutePath,
            onDismiss = { 
                showCreateDialog = false
                selectedNodeForCreate = null
            },
            onCreate = { path, name ->
                if (isCreatingFolder) {
                    viewModel.createFolder(path, name) { _, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    viewModel.createFile(path, name) { _, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
                showCreateDialog = false
                selectedNodeForCreate = null
            }
        )
    }

    if (showAddRemoteDialog) {
        AddRemoteConnectionDialog(
            onDismiss = { showAddRemoteDialog = false },
            onAdd = { connection, callback ->
                viewModel.testAndAddRemoteConnection(connection) { success, msg ->
                    callback(success, msg)
                    if (success) {
                        showAddRemoteDialog = false
                    }
                    if (msg != null) {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    if (showFilterDialog) {
        FilterDialog(
            config = filterConfig,
            onDismiss = { showFilterDialog = false },
            onApply = { newConfig -> filterConfig = newConfig },
            onReset = { filterConfig = StorageFilterConfig() }
        )
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
                // Top header
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).statusBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = AppleBlue, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NucleusFS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onOpenDashboard() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Overview", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(24.dp))
                    }
                    
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { isSearchActive = !isSearchActive }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (isSearchActive || filterConfig.searchQuery.isNotBlank()) AppleBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Sticky / Pinned Connected Nodes Section
                if (activeRoots.isNotEmpty()) {
                    val isCollapsed by remember {
                        derivedStateOf {
                            (listState.firstVisibleItemIndex > 0) || (listState.firstVisibleItemScrollOffset > 20)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = if (isCollapsed) 4.dp else 8.dp)
                            .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "CONNECTED NODES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "Manage",
                                fontSize = 12.sp,
                                color = AppleMint,
                                modifier = Modifier.clickable { showManageConnectionsDialog = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(if (isCollapsed) 6.dp else 10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(
                                count = activeRoots.size,
                                key = { index -> activeRoots[index].connectionId?.toString() ?: "local" }
                            ) { index ->
                                val root = activeRoots[index]
                                val rootId = root.connectionId?.toString() ?: "local"
                                val isSelected = rootId == selectedRootId
                                NodeCard(
                                    type = if (root.isRemote) "CLOUD" else "LOCAL",
                                    name = root.name,
                                    usage = if (root.isRemote) "Active" else "Internal",
                                    color = if (root.isRemote) AppleBlue else AppleMint,
                                    isSelected = isSelected,
                                    isCollapsed = isCollapsed,
                                    onClick = { viewModel.selectRoot(rootId) }
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.2f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.1f))
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = "${flatList.count { !it.node.isDirectory }} Files / ${flatList.count { it.node.isDirectory }} Folders",
                            transitionSpec = { fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) togetherWith fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) },
                            label = "countsAnim"
                        ) { countText ->
                            Text(
                                text = countText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleMint
                            )
                        }
                        AnimatedContent(
                            targetState = if (filterConfig.isActive) "Filtered: $hiddenCount hidden" else "Filtered: 0 hidden",
                            transitionSpec = { fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) togetherWith fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) },
                            label = "filteredAnim"
                        ) { filteredText ->
                            Text(
                                text = filteredText,
                                fontSize = 12.sp,
                                color = if ((filterConfig.isActive) && (hiddenCount > 0)) AppleMint else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = if (filterConfig.isActive) Modifier.clickable { showFilterDialog = true } else Modifier
                            )
                        }
                    }

                    // Sort Button & Dropdown
                    Box {
                        Surface(
                            onClick = { showSortMenu = true },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, AppleMint.copy(alpha=0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort",
                                    tint = AppleMint,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                AnimatedContent(
                                    targetState = sortOption.shortName,
                                    transitionSpec = {
                                        (slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 2 } + fadeIn())
                                            .togetherWith(slideOutVertically(spring(stiffness = Spring.StiffnessMediumLow)) { -it / 2 } + fadeOut())
                                    },
                                    label = "sortLabelAnim"
                                ) { sortName ->
                                    Text(
                                        text = sortName,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        SortDropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            currentSort = sortOption,
                            onSortSelected = { sortOption = it },
                            foldersFirst = foldersFirst,
                            onToggleFoldersFirst = { foldersFirst = it }
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Filter Button
                    Surface(
                        onClick = { showFilterDialog = true },
                        color = if (filterConfig.isActive) AppleMint.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.4f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (filterConfig.isActive) AppleMint.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline.copy(alpha=0.2f)
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.TopEnd,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = "Filter",
                                tint = if (filterConfig.isActive) AppleMint else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            if (filterConfig.isActive) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AppleMint)
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showFabMenu = true },
                    containerColor = AppleMint,
                    contentColor = Color.Black,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Menu")
                }
                
                DropdownMenu(
                    expanded = showFabMenu,
                    onDismissRequest = { showFabMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    DropdownMenuItem(
                        text = { Text("New Connection") },
                        onClick = {
                            showFabMenu = false
                            showAddRemoteDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("New Folder") },
                        onClick = {
                            showFabMenu = false
                            isCreatingFolder = true
                            selectedNodeForCreate = lastExpandedNode // Pre-select the last expanded folder
                            showCreateDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("New File") },
                        onClick = {
                            showFabMenu = false
                            isCreatingFolder = false
                            selectedNodeForCreate = lastExpandedNode // Pre-select the last expanded folder
                            showCreateDialog = true
                        },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null) }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (rootNode == null) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(if (isScanning) "Scanning directory tree..." else "No storage data.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else if (flatList.isEmpty()) {
                item(key = "empty_filtered_state") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(
                                fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                fadeOutSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching files or folders found",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your filters or search terms",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                filterConfig = StorageFilterConfig()
                                isSearchActive = false
                            },
                            border = BorderStroke(1.dp, AppleMint.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = AppleMint, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Filters", color = AppleMint, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(
                    items = flatList,
                    key = { it.node.path }
                ) { flatNode ->
                    AppleStorageNodeRow(
                        node = flatNode.node,
                        level = flatNode.level,
                        modifier = Modifier.animateItem(
                            fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            fadeOutSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ),
                        onToggle = { nodeToToggle -> 
                            if (nodeToToggle is StorageNode.DirectoryNode) {
                                if (!nodeToToggle.isExpanded) {
                                    lastExpandedNode = nodeToToggle
                                } else if (lastExpandedNode == nodeToToggle) {
                                    lastExpandedNode = null
                                }
                                viewModel.toggleNode(nodeToToggle) 
                            }
                        },
                        onLongPress = { selectedNodeForMenu = it }
                    )
                }
            }
        }
    }
}

@Composable
fun NodeCard(
    type: String,
    name: String,
    usage: String,
    color: Color,
    isSelected: Boolean = false,
    isCollapsed: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val cardBg = if (isSelected) color.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
    val cardBorder = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    if (isCollapsed) {
        // Lean compact pill button when scrolled
        Surface(
            color = cardBg,
            shape = CircleShape,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = name,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    } else {
        // Full standard card
        Surface(
            color = cardBg,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier
                .width(135.dp)
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(type, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                Text(usage, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { 0.4f },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }
        }
    }
}

data class FlatNode(val node: StorageNode, val level: Int)

fun flattenTree(node: StorageNode, level: Int, autoExpand: Boolean = false): List<FlatNode> {
    val list = mutableListOf<FlatNode>()
    list.add(FlatNode(node, level))
    if ((node is StorageNode.DirectoryNode) && (node.isExpanded || (autoExpand && node.children.isNotEmpty()))) {
        for (child in node.children) {
            list.addAll(flattenTree(child, level + 1, autoExpand = autoExpand))
        }
    }
    return list
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppleStorageNodeRow(
    node: StorageNode,
    level: Int,
    onToggle: (StorageNode) -> Unit,
    onLongPress: (StorageNode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val paddingStart = 16.dp + (level * 18).dp

    val isDirectory = node is StorageNode.DirectoryNode
    val isExpanded = isDirectory && node.isExpanded

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "chevronRotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = {
                    if (node is StorageNode.FileNode) {
                        openFile(context, node.file)
                    } else {
                        onToggle(node)
                    }
                },
                onLongClick = { onLongPress(node) }
            )
            .padding(start = paddingStart, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isDirectory) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppleBlue,
                modifier = Modifier.size(16.dp).rotate(chevronRotation)
            )
            Spacer(modifier = Modifier.width(6.dp))
        } else {
            Spacer(modifier = Modifier.width(22.dp))
        }

        val icon = when (node) {
            is StorageNode.DirectoryNode -> if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder
            is StorageNode.FileNode -> Icons.AutoMirrored.Filled.InsertDriveFile
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDirectory) AppleYellow else AppleMint,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = node.name,
            fontSize = 13.sp,
            fontWeight = if (isDirectory) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = Utils.formatSize(node.size),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
