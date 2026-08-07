package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    if (rootNode == null) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            if (isScanning) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Scanning complete storage tree...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Text("No storage data. Go to Dashboard to scan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    val flatList = flattenTree(rootNode!!, 0)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        if (isScanning) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), color = MaterialTheme.colorScheme.primary)
        }
        
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DIRECTORY TREE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(flatList) { item ->
                        StorageNodeRow(
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
fun StorageNodeRow(node: StorageNode, level: Int, onToggle: (StorageNode) -> Unit) {
    val paddingStart = 8.dp + (level * 20).dp
    
    val sizeColor = when {
        node.size > 5L * 1024 * 1024 * 1024 -> StorageRed
        node.size > 1L * 1024 * 1024 * 1024 -> StorageOrange
        node.size > 100L * 1024 * 1024 -> StorageYellow
        else -> StorageGreen
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle(node) }
            .padding(start = paddingStart, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = when (node) {
            is StorageNode.DirectoryNode -> if (node.isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder
            is StorageNode.FileNode -> Icons.Default.Description
        }
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = sizeColor,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = node.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        
        Box(
            modifier = Modifier
                .background(sizeColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = Utils.formatSize(node.size),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = sizeColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
