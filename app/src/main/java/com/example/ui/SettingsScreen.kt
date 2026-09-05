package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.StorageViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: StorageViewModel,
    onBack: () -> Unit = {},
) {
    val includeHidden by viewModel.includeHiddenFiles.collectAsState()
    val ignoreSystem by viewModel.ignoreSystemCache.collectAsState()
    val minSizeMb by viewModel.minFileSizeMb.collectAsState()
    val threshold by viewModel.storageThreshold.collectAsState()
    val autoScan by viewModel.autoScanOnLaunch.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val lastScanTime by viewModel.lastScanTime.collectAsState()

    var showClearDialog by remember { mutableStateOf(value = false) }
    var showAboutDialog by remember { mutableStateOf(value = false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    @Suppress("UnusedBoxWithConstraintsScope")
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = this.maxWidth >= 720.dp

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter,
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = if (isTablet) 900.dp else Dp.Unspecified)
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp)
                ) {
            // Header with Back Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Section 1: SCANNER & FILTERS
            item {
                CupertinoSectionHeader("SCANNER & FILTERS")

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        CupertinoSwitchRow(
                            icon = Icons.Default.Visibility,
                            iconBg = AppleBlue,
                            title = "Include Hidden Files",
                            subtitle = "Scan dotfiles (.git, .cache, .DS_Store)",
                            checked = includeHidden,
                            onCheckedChange = { viewModel.setIncludeHiddenFiles(it) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )

                        CupertinoSwitchRow(
                            icon = Icons.Default.FolderSpecial,
                            iconBg = ApplePurple,
                            title = "Ignore Android System Cache",
                            subtitle = "Skip restricted /Android/data folders",
                            checked = ignoreSystem,
                            onCheckedChange = { viewModel.setIgnoreSystemCache(it) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )

                        // Segmented Control Filter Row
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(AppleOrange.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = null,
                                        tint = AppleOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Minimum File Size Filter",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Index files exceeding size threshold",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Cupertino Segmented Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val options = listOf(
                                    0 to "All",
                                    1 to "> 1 MB",
                                    10 to "> 10 MB",
                                    50 to "> 50 MB"
                                )

                                options.forEach { (mb, label) ->
                                    val isSelected = minSizeMb == mb
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(
                                                if (isSelected) AppleBlue else Color.Transparent
                                            )
                                            .clickable { viewModel.setMinFileSizeMb(mb) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: ALERTS & AUTOMATION
            item {
                Spacer(modifier = Modifier.height(24.dp))
                CupertinoSectionHeader("ALERTS & AUTOMATION")

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        CupertinoSwitchRow(
                            icon = Icons.Default.SettingsSuggest,
                            iconBg = AppleTeal,
                            title = "Auto Scan on Launch",
                            subtitle = "Automatically index storage when app opens",
                            checked = autoScan,
                            onCheckedChange = { viewModel.setAutoScanOnLaunch(it) }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )

                        // Slider Row
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(AppleYellow.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = AppleYellow,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "High Storage Alert",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "$threshold% Full",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleYellow
                                )
                            }
                            Text(
                                text = "Highlight gauge when capacity exceeds threshold",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 44.dp, top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Slider(
                                value = threshold.toFloat(),
                                onValueChange = { viewModel.setStorageThreshold(it.toInt()) },
                                valueRange = 60f..95f,
                                steps = 6,
                                colors = SliderDefaults.colors(
                                    thumbColor = AppleYellow,
                                    activeTrackColor = AppleYellow,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            // Section 3: MAINTENANCE & ACTIONS
            item {
                Spacer(modifier = Modifier.height(24.dp))
                CupertinoSectionHeader("MAINTENANCE")

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Re-index
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (!isScanning) {
                                        viewModel.scanStorage()
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Storage scan triggered")
                                        }
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(AppleBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = AppleBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Re-index Storage",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (lastScanTime != null) {
                                            val formatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(
                                                Date(lastScanTime!!)
                                            )
                                            "Last scanned at $formatted"
                                        } else {
                                            "Not scanned yet"
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            SettingsScanActionButton(
                                isScanning = isScanning,
                                onScanClick = {
                                    viewModel.scanStorage()
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Storage re-indexed")
                                    }
                                }
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )

                        // Clear Cache
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showClearDialog = true }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(AppleRed.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = null,
                                        tint = AppleRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Clear Scan Cache",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Reset cached directory memory",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { showClearDialog = true },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppleRed),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppleRed)
                            ) {
                                Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section 4: DIAGNOSTICS & ABOUT
            item {
                Spacer(modifier = Modifier.height(24.dp))
                CupertinoSectionHeader("ABOUT & SYSTEM")

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showAboutDialog = true }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(AppleIndigo.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = AppleIndigo,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "About NucleusFS",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Version 4.0 • Jetpack Compose",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                icon = { Icon(Icons.Default.CleaningServices, contentDescription = null, tint = AppleRed) },
                title = { Text("Clear Scan Cache?") },
                text = { Text("This will reset current storage index memory. You can re-scan anytime.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearCache()
                            showClearDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Scan cache cleared")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleRed)
                    ) {
                        Text("Clear Cache")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                icon = { Icon(Icons.Default.Storage, contentDescription = null, tint = AppleBlue) },
                title = { Text("NucleusFS v4.0", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "NucleusFS is a high-performance Android storage analyzer, remote cloud file manager, and AI directory organizer built with Jetpack Compose.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "• SFTP, FTP & SMB Remote Cloud Storage Integration\n• Live Connection Status & Diagnostic Tracking\n• Interactive Directory Tree & Multi-Selection\n• Batch Copy, Move & Delete with Folder Tree Browsing\n• AI-Powered Storage Cleaning & Category Analytics\n• Adaptive Dark & Light Material 3 Theme Design",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { showAboutDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
}
