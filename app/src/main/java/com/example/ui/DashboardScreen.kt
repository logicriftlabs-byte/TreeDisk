package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.StorageViewModel
import com.example.ui.theme.*

@Composable
fun DashboardScreen(viewModel: StorageViewModel, onClose: () -> Unit) {
    val totalSpace by viewModel.totalSpace.collectAsState()
    val usedSpace by viewModel.usedSpace.collectAsState()
    val freeSpace by viewModel.freeSpace.collectAsState()
    val categoryStats by viewModel.categoryStats.collectAsState()
    val remoteConnections by viewModel.remoteConnections.collectAsState()
    
    val usedGb = usedSpace / (1024L * 1024L * 1024L)
    val freeGb = freeSpace / (1024L * 1024L * 1024L)
    val percent = if (totalSpace > 0) ((usedSpace.toFloat() / totalSpace.toFloat()) * 100).toInt() else 0
    
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Overview", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("Storage health, usage, and activity", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                color = AppleBlue.copy(alpha=0.15f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AppleBlue.copy(alpha=0.5f))
            ) {
                Text("LIVE", color = AppleBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
        
        // Summary Cards
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardSummaryCard("TOTAL STORAGE", "$usedGb GB", "$percent% used · $freeGb GB free", Modifier.weight(1f))
            DashboardSummaryCard("ACTIVE NODES", "${1 + remoteConnections.size} online", "1 local · ${remoteConnections.size} cloud", Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.15f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Storage usage", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Text("$percent%", fontWeight = FontWeight.Bold, color = AppleMint)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Canvas Pie Chart
                            Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val totalStatSize = categoryStats.sumOf { it.size }.toFloat()
                                    var startAngle = -90f
                                    if (totalStatSize == 0f) {
                                        drawArc(
                                            color = Color.DarkGray,
                                            startAngle = 0f,
                                            sweepAngle = 360f,
                                            useCenter = true
                                        )
                                    } else {
                                        categoryStats.forEach { stat ->
                                            val sweepAngle = (stat.size.toFloat() / totalStatSize) * 360f
                                            if (sweepAngle > 0) {
                                                drawArc(
                                                    color = stat.category.color,
                                                    startAngle = startAngle,
                                                    sweepAngle = sweepAngle,
                                                    useCenter = true,
                                                    style = Fill
                                                )
                                                startAngle += sweepAngle
                                            }
                                        }
                                    }
                                }
                                // Center hole for donut effect
                                Box(modifier = Modifier.size(50.dp).clip(androidx.compose.foundation.shape.CircleShape).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.15f)), contentAlignment = Alignment.Center) {
                                    Text("$percent%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(24.dp))
                            
                            // Legend
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                categoryStats.sortedByDescending { it.size }.take(4).forEach { stat ->
                                    val statGb = "%.2f".format(stat.size / (1024f * 1024f * 1024f))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(stat.category.color))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(stat.category.displayName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("$statGb GB", fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                                
                                if (categoryStats.isEmpty()) {
                                    Text("No files analyzed yet", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Handle space
        Spacer(modifier = Modifier.height(80.dp))
    }
    
    // Bottom 10% interactive area to pull up and close
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.12f)
            .align(Alignment.BottomCenter)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -15) { // Pull up
                        onClose()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Close",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    }
}

@Composable
fun DashboardSummaryCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.2f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        }
    }
}
