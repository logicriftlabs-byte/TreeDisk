package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GeminiNanoDownloadDialog(
    onDismiss: () -> Unit,
    onDownloadComplete: () -> Unit,
) {
    var isDownloading by remember { mutableStateOf(value = false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!isDownloading) onDismiss() },
        confirmButton = {
            if (!isDownloading) {
                Button(
                    onClick = {
                        isDownloading = true
                        scope.launch {
                            // Simulate a model download
                            while (downloadProgress < 1f) {
                                delay((30..150).random().toLong())
                                downloadProgress += 0.02f
                            }
                            downloadProgress = 1f
                            delay(500)
                            onDownloadComplete()
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text("Download Model (~1GB)", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isDownloading) {
                TextButton(onClick = onDismiss) {
                    Text("Not Now", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enable On-Device AI", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                if (isDownloading) {
                    Text(
                        text = "Downloading Gemini Nano model via Google AI Edge (AICore)...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val animatedProgress by animateFloatAsState(
                        targetValue = downloadProgress,
                        animationSpec = tween(durationMillis = 200),
                        label = "progress"
                    )
                    
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.End)
                    )
                } else {
                    Text(
                        text = "NucleusFS can use Gemini Nano to organize files, summarize documents, and power smart search—all completely offline without sending your files to the cloud.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    NanoFeatureItem(
                        icon = Icons.Default.Security,
                        title = "100% Private",
                        desc = "Data never leaves your device"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NanoFeatureItem(
                        icon = Icons.Default.Speed,
                        title = "Zero Latency",
                        desc = "Instant AI processing locally"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NanoFeatureItem(
                        icon = Icons.Default.CloudDownload,
                        title = "Offline Support",
                        desc = "Works without internet access"
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun NanoFeatureItem(icon: ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
