package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.StorageNode
import com.example.StorageViewModel
import com.example.Utils
import com.example.ui.theme.*

@Composable
fun TreeScreen(viewModel: StorageViewModel) {
    val rootNode by viewModel.rootNode.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (rootNode == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isScanning) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AppleBlue, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Scanning directory tree...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "No storage data. Scan from Dashboard.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val flatList = flattenTree(rootNode!!, 0)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
            ) {
                // Apple Header matching Settings & Dashboard tab style
                Column(modifier = Modifier.padding(top = 20.dp, bottom = 20.dp)) {
                    Text(
                        text = "Tree",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Hierarchical File & Directory Breakdown",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

                // Section Label Placement
                CupertinoSectionHeader(
                    title = "DIRECTORY HIERARCHY",
                    rightText = "${flatList.size} Items"
                )

                // Apple Glass Inset Tree Container
                Surface(
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
                        ),
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(flatList) { item ->
                            AppleStorageNodeRow(
                                node = item.node,
                                level = item.level,
                                onToggle = {
                                    if (it is StorageNode.DirectoryNode) {
                                        viewModel.toggleNode(it)
                                    }
                                }
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

@Composable
fun AppleStorageNodeRow(node: StorageNode, level: Int, onToggle: (StorageNode) -> Unit) {
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
            .clickable { onToggle(node) }
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
