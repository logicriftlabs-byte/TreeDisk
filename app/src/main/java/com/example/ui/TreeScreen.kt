package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun TreeScreen(viewModel: StorageViewModel) {
    val rootNode by viewModel.rootNode.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val flatList = rootNode?.let { flattenTree(it, 0) } ?: emptyList()

    var selectedNodeForMenu by remember { mutableStateOf<StorageNode?>(null) }
    var selectedNodeForDelete by remember { mutableStateOf<StorageNode?>(null) }
    var selectedNodeForInspector by remember { mutableStateOf<StorageNode?>(null) }

    // Windows Context Menu Overlay
    selectedNodeForMenu?.let { node ->
        WindowsContextMenuPopup(
            node = node,
            onDismiss = { selectedNodeForMenu = null },
            onExpandToggle = { folder ->
                viewModel.toggleNode(folder)
            },
            onDeleteRequest = { nodeToDelete ->
                selectedNodeForDelete = nodeToDelete
            }
        )
    }

    // Windows Delete Confirmation Dialog
    selectedNodeForDelete?.let { node ->
        WindowsDeleteConfirmDialog(
            node = node,
            onDismiss = { selectedNodeForDelete = null },
            onConfirmDelete = { nodeToDelete ->
                viewModel.deleteNode(nodeToDelete)
            }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 720.dp

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tree",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Hierarchical File & Directory Breakdown",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Cupertino Glass Action Pill Button
                    ScanPillButton(
                        isScanning = isScanning,
                        onScanClick = { viewModel.scanStorage() }
                    )
                }

                if (isScanning) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(CircleShape),
                        color = AppleBlue,
                        trackColor = AppleBlue.copy(alpha = 0.15f)
                    )
                }

                if (isTablet) {
                    // Tablet 2-Pane Master-Detail Layout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Left Master Pane: Directory Hierarchy Tree
                        Column(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight()
                        ) {
                            CupertinoSectionHeader(
                                title = "DIRECTORY HIERARCHY",
                                rightText = if (rootNode != null) "${flatList.size} Items" else null
                            )

                            if (rootNode == null) {
                                CupertinoEmptyStateCard(
                                    text = if (isScanning) "Scanning directory tree..." else "No storage data. Scan from Dashboard."
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(26.dp))
                                        .border(
                                            width = 1.dp,
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(26.dp)
                                        )
                                        .background(MaterialTheme.colorScheme.surface),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(flatList) { item ->
                                        AppleStorageNodeRow(
                                            node = item.node,
                                            level = item.level,
                                            onToggle = { node ->
                                                if (node is StorageNode.DirectoryNode) {
                                                    viewModel.toggleNode(node)
                                                }
                                                selectedNodeForInspector = node
                                            },
                                            onLongPress = { selectedNodeForMenu = it }
                                        )
                                    }
                                }
                            }
                        }

                        // Right Detail Inspector Pane
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            CupertinoSectionHeader(title = "FILE & FOLDER INSPECTOR")

                            NodeInspectorPanel(
                                node = selectedNodeForInspector ?: rootNode,
                                viewModel = viewModel,
                                onDeleteRequest = { selectedNodeForDelete = it }
                            )
                        }
                    }
                } else {
                    // Mobile Single Column Tree Layout
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        item {
                            CupertinoSectionHeader(
                                title = "DIRECTORY HIERARCHY",
                                rightText = if (rootNode != null) "${flatList.size} Items" else null
                            )
                        }

                        if (rootNode == null) {
                            item {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(26.dp))
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(26.dp)
                                        ),
                                    shape = RoundedCornerShape(26.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isScanning) "Scanning directory tree..." else "No storage data. Scan from Dashboard.",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            item {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(26.dp))
                                        .border(
                                            width = 1.dp,
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(26.dp)
                                        ),
                                    shape = RoundedCornerShape(26.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        flatList.forEach { item ->
                                            AppleStorageNodeRow(
                                                node = item.node,
                                                level = item.level,
                                                onToggle = {
                                                    if (it is StorageNode.DirectoryNode) {
                                                        viewModel.toggleNode(it)
                                                    }
                                                },
                                                onLongPress = { selectedNodeForMenu = it }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NodeInspectorPanel(
    node: StorageNode?,
    viewModel: StorageViewModel,
    onDeleteRequest: (StorageNode) -> Unit
) {
    val context = LocalContext.current

    if (node == null) {
        CupertinoEmptyStateCard("Select a file or directory from the tree to inspect details.")
        return
    }

    val isDirectory = node is StorageNode.DirectoryNode
    val category = if (node is StorageNode.FileNode) node.category else null
    val color = when {
        node.size > 5L * 1024 * 1024 * 1024 -> AppleRed
        node.size > 1L * 1024 * 1024 * 1024 -> AppleOrange
        node.size > 100L * 1024 * 1024 -> AppleYellow
        else -> AppleBlue
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            ),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header Badge & Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val icon = if (isDirectory) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = node.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isDirectory) "Directory / Folder" else (category?.displayName ?: "File"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(20.dp))

            // File Size & Attributes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "TOTAL SIZE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Utils.formatSize(node.size),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = color
                    )
                }

                if (node is StorageNode.DirectoryNode) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "CHILD ITEMS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${node.children.size} Items",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Full Path Card
            Text(
                text = "FULL FILE PATH",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = node.file.absolutePath,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (node is StorageNode.FileNode) {
                    Button(
                        onClick = { openFile(context, node.file) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open File", fontWeight = FontWeight.Bold)
                    }
                } else if (node is StorageNode.DirectoryNode) {
                    Button(
                        onClick = { viewModel.toggleNode(node) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (node.isExpanded) "Collapse Folder" else "Expand Folder", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = { onDeleteRequest(node) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppleRed),
                    border = BorderStroke(1.dp, AppleRed.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            }

            // Preview of children inside folder if directory
            if (node is StorageNode.DirectoryNode && node.children.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "FOLDER CONTENTS PREVIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    node.children.take(6).forEach { child ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (child is StorageNode.DirectoryNode) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = child.name,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = Utils.formatSize(child.size),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

data class FlatNode(val node: StorageNode, val level: Int)

fun flattenTree(node: StorageNode, level: Int): List<FlatNode> {
    val list = mutableListOf<FlatNode>()
    list.add(FlatNode(node, level))

    if (node is StorageNode.DirectoryNode && node.isExpanded) {
        for (child in node.children) {
            list.addAll(flattenTree(child, level + 1))
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
    onLongPress: (StorageNode) -> Unit
) {
    val paddingStart = 16.dp + (level * 18).dp

    val sizeColor = when {
        node.size > 5L * 1024 * 1024 * 1024 -> AppleRed
        node.size > 1L * 1024 * 1024 * 1024 -> AppleOrange
        node.size > 100L * 1024 * 1024 -> AppleYellow
        else -> AppleMint
    }

    val isDirectory = node is StorageNode.DirectoryNode
    val isExpanded = isDirectory && (node as StorageNode.DirectoryNode).isExpanded

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = { onToggle(node) },
                onLongClick = { onLongPress(node) }
            )
            .padding(start = paddingStart, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Expand Chevron for Directories
        if (isDirectory) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(16.dp)
                    .rotate(if (isExpanded) 90f else 0f)
            )
            Spacer(modifier = Modifier.width(6.dp))
        } else {
            Spacer(modifier = Modifier.width(22.dp))
        }

        // Squircle Icon Badge
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(sizeColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (node) {
                is StorageNode.DirectoryNode -> if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder
                is StorageNode.FileNode -> Icons.AutoMirrored.Filled.InsertDriveFile
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = sizeColor,
                modifier = Modifier.size(15.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = node.name,
            fontSize = 14.sp,
            fontWeight = if (isDirectory) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(sizeColor.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = Utils.formatSize(node.size),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = sizeColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
