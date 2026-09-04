import os

filepath = 'app/src/main/java/com/example/ui/TreeScreen.kt'
with open(filepath, 'w') as f:
    f.write("""package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.StorageNode
import com.example.StorageViewModel
import com.example.Utils
import com.example.ui.theme.*

@Composable
fun TreeScreen(viewModel: StorageViewModel, onOpenDashboard: () -> Unit = {}, onOpenSettings: () -> Unit = {}) {
    val rootNode by viewModel.rootNode.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val remoteConnections by viewModel.remoteConnections.collectAsState()
    val flatList = rootNode?.let { flattenTree(it, 0) } ?: emptyList()

    var selectedNodeForMenu by remember { mutableStateOf<StorageNode?>(null) }
    var selectedNodeForDelete by remember { mutableStateOf<StorageNode?>(null) }
    var selectedNodeForInspector by remember { mutableStateOf<StorageNode?>(null) }
    var showAddRemoteDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    selectedNodeForMenu?.let { node ->
        WindowsContextMenuPopup(
            node = node,
            onDismiss = { selectedNodeForMenu = null },
            onExpandToggle = { folder ->
                viewModel.toggleNode(folder)
            },
            onOrganizeRequest = { folder ->
                viewModel.organizeDirectoryWithAI(folder) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            },
            onDeleteRequest = { nodeToDelete ->
                selectedNodeForDelete = nodeToDelete
            }
        )
    }

    if (showAddRemoteDialog) {
        AddRemoteConnectionDialog(
            onDismiss = { showAddRemoteDialog = false },
            onConnect = { connection ->
                viewModel.testAndAddRemoteConnection(connection) { success, msg ->
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

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
                // Top header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = AppleBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NucleusFS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = onOpenDashboard,
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue.copy(alpha=0.15f), contentColor = AppleBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("DASH", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                
                // Path Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Computer, contentDescription = null, tint = AppleMint, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = rootNode?.path ?: "/",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AppleMint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
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
                        Text(
                            text = "${flatList.count { !it.node.isDirectory }} Files / ${flatList.count { it.node.isDirectory }} Folders",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleMint
                        )
                        Text(
                            text = "Filtered: 0 hidden",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.4f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.2f))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Name (A-Z)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.4f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.2f))
                    ) {
                        Icon(imageVector = Icons.Default.FilterAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp).size(16.dp))
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddRemoteDialog = true },
                containerColor = AppleMint,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Connection")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CONNECTED NODES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                        Text("Manage", fontSize = 12.sp, color = AppleMint, modifier = Modifier.clickable { showAddRemoteDialog = true })
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            NodeCard("LOCAL", "Internal", "12GB/128GB", AppleMint)
                        }
                        items(remoteConnections) { conn ->
                            NodeCard("CLOUD", conn.name, "Active", AppleBlue)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("FILESYSTEM TREE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            
            if (rootNode == null) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(if (isScanning) "Scanning directory tree..." else "No storage data.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(flatList) { flatNode ->
                    AppleStorageNodeRow(
                        node = flatNode.node,
                        level = flatNode.level,
                        onToggle = { viewModel.toggleNode(it as StorageNode.DirectoryNode) },
                        onLongPress = { selectedNodeForMenu = it }
                    )
                }
            }
        }
    }
}
""")
