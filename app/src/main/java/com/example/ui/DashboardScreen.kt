package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CategoryStat
import com.example.FileCategory
import com.example.StorageNode
import com.example.StorageViewModel
import com.example.Utils
import com.example.ui.theme.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun DashboardScreen(viewModel: StorageViewModel) {
    val context = LocalContext.current
    val totalSpace by viewModel.totalSpace.collectAsState()
    val usedSpace by viewModel.usedSpace.collectAsState()
    val freeSpace by viewModel.freeSpace.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val rootNode by viewModel.rootNode.collectAsState()
    val topFiles by viewModel.topFiles.collectAsState()
    val categoryStats by viewModel.categoryStats.collectAsState()

    val scannedTotalSize = categoryStats.sumOf { it.size }.coerceAtLeast(1L)

    var selectedNodeForMenu by remember { mutableStateOf<StorageNode?>(null) }
    var selectedNodeForDelete by remember { mutableStateOf<StorageNode?>(null) }

    // Windows Context Menu Overlay
    selectedNodeForMenu?.let { node ->
        WindowsContextMenuPopup(
            node = node,
            onDismiss = { selectedNodeForMenu = null },
            onExpandToggle = null,
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
                            text = "Dashboard",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Storage & Visual Analytics",
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

                if (isTablet) {
                    // Tablet Dual Pane Layout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Left Pane: Overview & Stats
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 40.dp)
                        ) {
                            item {
                                CupertinoSectionHeader("STORAGE OVERVIEW")

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
                                    Column(modifier = Modifier.padding(22.dp)) {
                                        val percentage = if (totalSpace > 0) (usedSpace * 100 / totalSpace).toInt() else 0

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            Column {
                                                Text(
                                                    text = "INTERNAL STORAGE",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    letterSpacing = 1.2.sp
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(verticalAlignment = Alignment.Bottom) {
                                                    Text(
                                                        text = Utils.formatSize(usedSpace).substringBefore(" "),
                                                        fontSize = 32.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        letterSpacing = (-0.5).sp
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "${Utils.formatSize(usedSpace).substringAfter(" ")} / ${Utils.formatSize(totalSpace)}",
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(bottom = 4.dp)
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(AppleBlue.copy(alpha = 0.15f))
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "$percentage% Used",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AppleBlue
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        val trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        val primaryColor = AppleBlue

                                        Canvas(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(16.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        ) {
                                            drawRoundRect(
                                                color = trackColor,
                                                size = size,
                                                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                                            )
                                            if (categoryStats.isNotEmpty()) {
                                                var currentX = 0f
                                                categoryStats.forEach { stat ->
                                                    val fraction = stat.size.toFloat() / scannedTotalSize.toFloat()
                                                    val segmentWidth = size.width * fraction
                                                    if (segmentWidth > 0f) {
                                                        drawRoundRect(
                                                            color = getAppleCategoryColor(stat.category),
                                                            topLeft = Offset(currentX, 0f),
                                                            size = Size(segmentWidth, size.height),
                                                            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                                                        )
                                                        currentX += segmentWidth
                                                    }
                                                }
                                            } else if (totalSpace > 0) {
                                                val usedWidth = size.width * (usedSpace.toFloat() / totalSpace.toFloat())
                                                drawRoundRect(
                                                    color = primaryColor,
                                                    size = Size(usedWidth, size.height),
                                                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(18.dp))

                                        if (categoryStats.isNotEmpty()) {
                                            LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                items(categoryStats) { stat ->
                                                    CupertinoLegendItem(
                                                        color = getAppleCategoryColor(stat.category),
                                                        label = stat.category.displayName,
                                                        sizeText = Utils.formatSize(stat.size)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                                CupertinoSectionHeader("SYSTEM STATS")

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    CupertinoWidgetCard(
                                        modifier = Modifier.weight(1f),
                                        icon = Icons.Default.SdCard,
                                        iconTint = AppleMint,
                                        label = "AVAILABLE",
                                        value = Utils.formatSize(freeSpace)
                                    )

                                    CupertinoWidgetCard(
                                        modifier = Modifier.weight(1f),
                                        icon = Icons.Default.Storage,
                                        iconTint = AppleBlue,
                                        label = "INDEXED FOLDERS",
                                        value = "${rootNode?.childrenCount ?: 0} Folders"
                                    )
                                }
                            }
                        }

                        // Right Pane: Categories & Top Files
                        LazyColumn(
                            modifier = Modifier.weight(1.2f),
                            contentPadding = PaddingValues(bottom = 40.dp)
                        ) {
                            item {
                                CupertinoSectionHeader(
                                    title = "CATEGORIES",
                                    rightText = if (categoryStats.isNotEmpty()) "${categoryStats.size} Items" else null
                                )
                            }

                            if (categoryStats.isEmpty()) {
                                item {
                                    CupertinoEmptyStateCard(
                                        text = if (isScanning) "Scanning storage categories..." else "Tap 'Scan' to generate category breakdown."
                                    )
                                }
                            } else {
                                items(categoryStats) { stat ->
                                    AppleCategoryRow(
                                        stat = stat,
                                        scannedTotalSize = scannedTotalSize
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                                CupertinoSectionHeader(
                                    title = "LARGEST FILES",
                                    rightText = if (topFiles.isNotEmpty()) "Top ${topFiles.size}" else null
                                )
                            }

                            if (topFiles.isEmpty()) {
                                item {
                                    CupertinoEmptyStateCard(
                                        text = if (isScanning) "Analyzing largest files..." else "No scanned files. Tap 'Scan' above."
                                    )
                                }
                            } else {
                                items(topFiles) { fileNode ->
                                    AppleTopFileRow(
                                        fileNode = fileNode,
                                        onClick = { openFile(context, fileNode.file) },
                                        onLongPress = { selectedNodeForMenu = fileNode }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Mobile Single-Pane Vertical Layout
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        item {
                            CupertinoSectionHeader("STORAGE OVERVIEW")

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
                                Column(modifier = Modifier.padding(22.dp)) {
                                    val percentage = if (totalSpace > 0) (usedSpace * 100 / totalSpace).toInt() else 0

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Column {
                                            Text(
                                                text = "INTERNAL STORAGE",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                letterSpacing = 1.2.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    text = Utils.formatSize(usedSpace).substringBefore(" "),
                                                    fontSize = 32.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    letterSpacing = (-0.5).sp
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${Utils.formatSize(usedSpace).substringAfter(" ")} / ${Utils.formatSize(totalSpace)}",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(bottom = 4.dp)
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(AppleBlue.copy(alpha = 0.15f))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "$percentage% Used",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppleBlue
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    val primaryColor = AppleBlue

                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(16.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        drawRoundRect(
                                            color = trackColor,
                                            size = size,
                                            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                                        )
                                        if (categoryStats.isNotEmpty()) {
                                            var currentX = 0f
                                            categoryStats.forEach { stat ->
                                                val fraction = stat.size.toFloat() / scannedTotalSize.toFloat()
                                                val segmentWidth = size.width * fraction
                                                if (segmentWidth > 0f) {
                                                    drawRoundRect(
                                                        color = getAppleCategoryColor(stat.category),
                                                        topLeft = Offset(currentX, 0f),
                                                        size = Size(segmentWidth, size.height),
                                                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                                                    )
                                                    currentX += segmentWidth
                                                }
                                            }
                                        } else if (totalSpace > 0) {
                                            val usedWidth = size.width * (usedSpace.toFloat() / totalSpace.toFloat())
                                            drawRoundRect(
                                                color = primaryColor,
                                                size = Size(usedWidth, size.height),
                                                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    if (categoryStats.isNotEmpty()) {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            items(categoryStats) { stat ->
                                                CupertinoLegendItem(
                                                    color = getAppleCategoryColor(stat.category),
                                                    label = stat.category.displayName,
                                                    sizeText = Utils.formatSize(stat.size)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            CupertinoSectionHeader("SYSTEM STATS")

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                CupertinoWidgetCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.SdCard,
                                    iconTint = AppleMint,
                                    label = "AVAILABLE",
                                    value = Utils.formatSize(freeSpace)
                                )

                                CupertinoWidgetCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Storage,
                                    iconTint = AppleBlue,
                                    label = "INDEXED FOLDERS",
                                    value = "${rootNode?.childrenCount ?: 0} Folders"
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            CupertinoSectionHeader(
                                title = "CATEGORIES",
                                rightText = if (categoryStats.isNotEmpty()) "${categoryStats.size} Items" else null
                            )
                        }

                        if (categoryStats.isEmpty()) {
                            item {
                                CupertinoEmptyStateCard(
                                    text = if (isScanning) "Scanning storage categories..." else "Tap 'Scan' to generate category breakdown."
                                )
                            }
                        } else {
                            items(categoryStats) { stat ->
                                AppleCategoryRow(
                                    stat = stat,
                                    scannedTotalSize = scannedTotalSize
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            CupertinoSectionHeader(
                                title = "LARGEST FILES",
                                rightText = if (topFiles.isNotEmpty()) "Top ${topFiles.size}" else null
                            )
                        }

                        if (topFiles.isEmpty()) {
                            item {
                                CupertinoEmptyStateCard(
                                    text = if (isScanning) "Analyzing largest files..." else "No scanned files. Tap 'Scan' above."
                                )
                            }
                        } else {
                            items(topFiles) { fileNode ->
                                AppleTopFileRow(
                                    fileNode = fileNode,
                                    onClick = { openFile(context, fileNode.file) },
                                    onLongPress = { selectedNodeForMenu = fileNode }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CupertinoLegendItem(color: Color, label: String, sizeText: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label • $sizeText",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CupertinoWidgetCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AppleCategoryRow(
    stat: CategoryStat,
    scannedTotalSize: Long
) {
    val category = stat.category
    val catColor = getAppleCategoryColor(category)
    val icon = getCategoryIcon(category)
    val percentage = if (scannedTotalSize > 0) (stat.size * 100f / scannedTotalSize.toFloat()) else 0f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(catColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = catColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = category.displayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${stat.fileCount} items",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(catColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = Utils.formatSize(stat.size),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = catColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Capsule progress bar
            LinearProgressIndicator(
                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape),
                color = catColor,
                trackColor = catColor.copy(alpha = 0.15f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppleTopFileRow(
    fileNode: StorageNode.FileNode,
    onClick: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    val category = fileNode.category
    val catColor = getAppleCategoryColor(category)
    val icon = getCategoryIcon(category)
    val parentPath = fileNode.file.parentFile?.name ?: "Storage"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(18.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(catColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = catColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fileNode.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Folder: $parentPath",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(catColor.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = Utils.formatSize(fileNode.size),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = catColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CupertinoEmptyStateCard(text: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun getAppleCategoryColor(category: FileCategory): Color {
    return when (category) {
        FileCategory.VIDEOS -> AppleRed
        FileCategory.IMAGES -> AppleOrange
        FileCategory.AUDIO -> AppleYellow
        FileCategory.DOCUMENTS -> AppleMint
        FileCategory.APPS -> AppleTeal
        FileCategory.ARCHIVES -> ApplePurple
        FileCategory.OTHER -> AppleBlue
    }
}

private fun getCategoryIcon(category: FileCategory): ImageVector {
    return when (category) {
        FileCategory.VIDEOS -> Icons.Default.Movie
        FileCategory.IMAGES -> Icons.Default.Image
        FileCategory.AUDIO -> Icons.Default.MusicNote
        FileCategory.DOCUMENTS -> Icons.AutoMirrored.Filled.Article
        FileCategory.APPS -> Icons.Default.Android
        FileCategory.ARCHIVES -> Icons.Default.FolderZip
        FileCategory.OTHER -> Icons.AutoMirrored.Filled.InsertDriveFile
    }
}
