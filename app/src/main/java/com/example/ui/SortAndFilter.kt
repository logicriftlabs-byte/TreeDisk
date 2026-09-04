package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.FileCategory
import com.example.StorageNode
import com.example.ui.theme.*

enum class StorageSortOption(val displayName: String, val shortName: String) {
    NAME_ASC("Name (A to Z)", "Name (A-Z)"),
    NAME_DESC("Name (Z to A)", "Name (Z-A)"),
    SIZE_DESC("Size (Largest first)", "Size (↓)"),
    SIZE_ASC("Size (Smallest first)", "Size (↑)"),
    TYPE("File Type / Extension", "Type")
}

enum class ItemTypeFilter(val label: String) {
    ALL("All Items"),
    FILES_ONLY("Files Only"),
    FOLDERS_ONLY("Folders Only")
}

enum class FileSizeFilter(val label: String, val minBytes: Long, val maxBytes: Long) {
    ANY("Any Size", 0L, Long.MAX_VALUE),
    SMALL("< 10 MB", 0L, 10L * 1024 * 1024),
    MEDIUM("10 - 100 MB", 10L * 1024 * 1024, 100L * 1024 * 1024),
    LARGE("100 MB - 1 GB", 100L * 1024 * 1024, 1024L * 1024 * 1024),
    HUGE("> 1 GB", 1024L * 1024 * 1024, Long.MAX_VALUE)
}

data class StorageFilterConfig(
    val category: FileCategory? = null,
    val itemType: ItemTypeFilter = ItemTypeFilter.ALL,
    val sizeFilter: FileSizeFilter = FileSizeFilter.ANY,
    val searchQuery: String = "",
) {
    val isActive: Boolean
        get() = (category != null) || (itemType != ItemTypeFilter.ALL) || (sizeFilter != FileSizeFilter.ANY) || searchQuery.isNotBlank()

    val activeFilterCount: Int
        get() {
            var count = 0
            if (category != null) count++
            if (itemType != ItemTypeFilter.ALL) count++
            if (sizeFilter != FileSizeFilter.ANY) count++
            if (searchQuery.isNotBlank()) count++
            return count
        }
}

/**
 * Recursively filters and sorts the tree of StorageNodes.
 */
fun filterAndSortTree(
    node: StorageNode,
    filter: StorageFilterConfig,
    sort: StorageSortOption,
    foldersFirst: Boolean = true,
): StorageNode? {
    when (node) {
        is StorageNode.FileNode -> {
            if (filter.itemType == ItemTypeFilter.FOLDERS_ONLY) return null
            if ((filter.category != null) && (node.category != filter.category)) return null
            if ((filter.sizeFilter != FileSizeFilter.ANY) && (node.size < filter.sizeFilter.minBytes || node.size > filter.sizeFilter.maxBytes)) return null
            if (filter.searchQuery.isNotBlank() && !node.name.contains(filter.searchQuery, ignoreCase = true)) return null
            return node
        }
        is StorageNode.DirectoryNode -> {
            if (filter.itemType == ItemTypeFilter.FILES_ONLY) return null

            val filteredChildren = node.children.mapNotNull { child ->
                filterAndSortTree(child, filter, sort, foldersFirst)
            }

            val selfMatchesSearch = filter.searchQuery.isBlank() || node.name.contains(filter.searchQuery, ignoreCase = true)
            val hasMatchingChildren = filteredChildren.isNotEmpty()

            val hasSpecificContentFilter = filter.category != null || filter.sizeFilter != FileSizeFilter.ANY
            if (hasSpecificContentFilter && !hasMatchingChildren) {
                return null
            }

            if (filter.searchQuery.isNotBlank() && !selfMatchesSearch && !hasMatchingChildren) {
                return null
            }

            val sortedChildren = sortNodes(filteredChildren, sort, foldersFirst)
            return node.copy(children = sortedChildren)
        }
    }
}

fun sortNodes(
    nodes: List<StorageNode>,
    sortOption: StorageSortOption,
    foldersFirst: Boolean = true,
): List<StorageNode> {
    val comparator = Comparator<StorageNode> { a, b ->
        if (foldersFirst && a.isDirectory != b.isDirectory) {
            if (a.isDirectory) -1 else 1
        } else {
            when (sortOption) {
                StorageSortOption.NAME_ASC -> a.name.compareTo(b.name, ignoreCase = true)
                StorageSortOption.NAME_DESC -> b.name.compareTo(a.name, ignoreCase = true)
                StorageSortOption.SIZE_DESC -> b.size.compareTo(a.size)
                StorageSortOption.SIZE_ASC -> a.size.compareTo(b.size)
                StorageSortOption.TYPE -> {
                    val extA = a.name.substringAfterLast('.', "").lowercase()
                    val extB = b.name.substringAfterLast('.', "").lowercase()
                    val extCmp = extA.compareTo(extB)
                    if (extCmp != 0) extCmp else a.name.compareTo(b.name, ignoreCase = true)
                }
            }
        }
    }
    return nodes.sortedWith(comparator)
}

fun countNodes(node: StorageNode?): Int {
    if (node == null) return 0
    var count = 1
    if (node is StorageNode.DirectoryNode) {
        for (child in node.children) {
            count += countNodes(child)
        }
    }
    return count
}

@Composable
fun SortDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    currentSort: StorageSortOption,
    onSortSelected: (StorageSortOption) -> Unit,
    foldersFirst: Boolean,
    onToggleFoldersFirst: (Boolean) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .width(220.dp)
    ) {
        Text(
            text = "SORT BY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        StorageSortOption.entries.forEach { option ->
            val isSelected = option == currentSort
            DropdownMenuItem(
                text = {
                    Text(
                        text = option.displayName,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) AppleMint else MaterialTheme.colorScheme.onSurface
                    )
                },
                onClick = {
                    onSortSelected(option)
                    onDismissRequest()
                },
                leadingIcon = {
                    val icon = when (option) {
                        StorageSortOption.NAME_ASC -> Icons.Default.SortByAlpha
                        StorageSortOption.NAME_DESC -> Icons.Default.SortByAlpha
                        StorageSortOption.SIZE_DESC -> Icons.Default.ArrowDownward
                        StorageSortOption.SIZE_ASC -> Icons.Default.ArrowUpward
                        StorageSortOption.TYPE -> Icons.Default.Category
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) AppleMint else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = AppleMint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        DropdownMenuItem(
            text = {
                Text(
                    text = "Folders on top",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            onClick = {
                onToggleFoldersFirst(!foldersFirst)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = AppleYellow,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                Checkbox(
                    checked = foldersFirst,
                    onCheckedChange = { onToggleFoldersFirst(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AppleMint,
                        checkmarkColor = Color.Black
                    )
                )
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterDialog(
    config: StorageFilterConfig,
    onDismiss: () -> Unit,
    onApply: (StorageFilterConfig) -> Unit,
    onReset: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(config.category) }
    var selectedItemType by remember { mutableStateOf(config.itemType) }
    var selectedSizeFilter by remember { mutableStateOf(config.sizeFilter) }

    val hasChanges = selectedCategory != config.category ||
            selectedItemType != config.itemType ||
            selectedSizeFilter != config.sizeFilter

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = null,
                        tint = AppleMint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Filter Files",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (selectedCategory != null || selectedItemType != ItemTypeFilter.ALL || selectedSizeFilter != FileSizeFilter.ANY) {
                    TextButton(
                        onClick = {
                            selectedCategory = null
                            selectedItemType = ItemTypeFilter.ALL
                            selectedSizeFilter = FileSizeFilter.ANY
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Reset All", fontSize = 12.sp, color = AppleRed)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Section
                Column {
                    Text(
                        text = "CATEGORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("All", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppleMint.copy(alpha = 0.2f),
                                selectedLabelColor = AppleMint
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategory == null,
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                selectedBorderColor = AppleMint
                            )
                        )

                        FileCategory.entries.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = if (isSelected) null else cat },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(cat.color)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(cat.displayName, fontSize = 12.sp)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = cat.color.copy(alpha = 0.2f),
                                    selectedLabelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    selectedBorderColor = cat.color
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Item Type Section
                Column {
                    Text(
                        text = "ITEM TYPE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ItemTypeFilter.entries.forEach { type ->
                            val isSelected = selectedItemType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedItemType = type },
                                label = { Text(type.label, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppleBlue.copy(alpha = 0.2f),
                                    selectedLabelColor = AppleBlue
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    selectedBorderColor = AppleBlue
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Size Section
                Column {
                    Text(
                        text = "FILE SIZE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FileSizeFilter.entries.forEach { sizeOpt ->
                            val isSelected = selectedSizeFilter == sizeOpt
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSizeFilter = sizeOpt },
                                label = { Text(sizeOpt.label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppleYellow.copy(alpha = 0.2f),
                                    selectedLabelColor = AppleYellow
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    selectedBorderColor = AppleYellow
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(
                        config.copy(
                            category = selectedCategory,
                            itemType = selectedItemType,
                            sizeFilter = selectedSizeFilter
                        )
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppleMint,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Apply", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onReset()
                    onDismiss()
                }
            ) {
                Text("Clear All")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}
