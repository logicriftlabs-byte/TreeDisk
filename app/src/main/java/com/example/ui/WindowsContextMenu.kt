package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.StorageNode
import com.example.Utils
import java.io.File

fun openFile(context: Context, file: File?) {
    if (file == null) {
        Toast.makeText(context, "Remote file preview not supported yet", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        if (!file.exists()) {
            Toast.makeText(context, "File does not exist: ${file.name}", Toast.LENGTH_SHORT).show()
            return
        }
        val uri: Uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file,
            )
        } catch (_: Exception) {
            Uri.fromFile(file)
        }

        val ext = file.extension.lowercase()
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Cannot open ${file.name}: ${e.localizedMessage ?: "No handler application"}",
            Toast.LENGTH_LONG,
        ).show()
    }
}

@Composable
fun WindowsContextMenuPopup(
    node: StorageNode,
    onDismiss: () -> Unit,
    onSelectRequest: ((StorageNode) -> Unit)? = null,
    onExpandToggle: ((StorageNode.DirectoryNode) -> Unit)? = null,
    onOrganizeRequest: ((StorageNode.DirectoryNode) -> Unit)? = null,
    onCreateRequest: ((StorageNode.DirectoryNode, Boolean) -> Unit)? = null,
    onDeleteRequest: (StorageNode) -> Unit,
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val winBgColor = if (isDark) Color(0xFF202023) else Color(0xFFF9F9FB)
    val winBorderColor = if (isDark) Color(0xFF38383F) else Color(0xFFD1D5DB)
    val winTextColor = if (isDark) Color(0xFFF3F4F6) else Color(0xFF1F2937)
    val winSubTextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val winHoverBg = if (isDark) Color(0xFF2D2D35) else Color(0xFFECEEF2)
    val winDivider = if (isDark) Color(0xFF2E2E36) else Color(0xFFE5E7EB)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            // Windows Context Menu Card
            Surface(
                modifier = Modifier
                    .width(260.dp)
                    .clickable(enabled = false) {}
                    .shadow(12.dp, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, winBorderColor, RoundedCornerShape(10.dp)),
                color = winBgColor,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    // Windows Title Header displaying Item Name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (node.isDirectory) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                            contentDescription = null,
                            tint = if (node.isDirectory) Color(0xFF3B82F6) else Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = node.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = winTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = Utils.formatSize(node.size),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = winSubTextColor
                            )
                        }
                    }

                    HorizontalDivider(
                        color = winDivider,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    WindowsContextMenuItem(
                        icon = Icons.Default.CheckCircleOutline,
                        label = "Select",
                        textColor = winTextColor,
                        hoverColor = winHoverBg,
                        onClick = {
                            onDismiss()
                            onSelectRequest?.invoke(node)
                        }
                    )

                    // Actions depending on File vs Folder
                    if (node is StorageNode.DirectoryNode) {
                        val isExpanded = node.isExpanded
                        WindowsContextMenuItem(
                            icon = if (isExpanded) Icons.Default.Folder else Icons.Default.FolderOpen,
                            label = if (isExpanded) "Collapse Folder" else "Expand Folder",
                            textColor = winTextColor,
                            hoverColor = winHoverBg,
                            onClick = {
                                onDismiss()
                                onExpandToggle?.invoke(node)
                            }
                        )
                        
                        WindowsContextMenuItem(
                            icon = Icons.Default.CreateNewFolder,
                            label = "New Folder",
                            textColor = winTextColor,
                            hoverColor = winHoverBg,
                            onClick = {
                                onDismiss()
                                onCreateRequest?.invoke(node, true)
                            }
                        )
                        
                        WindowsContextMenuItem(
                            icon = Icons.AutoMirrored.Filled.NoteAdd,
                            label = "New File",
                            textColor = winTextColor,
                            hoverColor = winHoverBg,
                            onClick = {
                                onDismiss()
                                onCreateRequest?.invoke(node, false)
                            }
                        )
                        
                        if (!node.isRemote) {
                            WindowsContextMenuItem(
                                icon = Icons.Default.AutoAwesome,
                                label = "Clean up with AI",
                                textColor = Color(0xFF8B5CF6),
                                hoverColor = winHoverBg,
                                onClick = {
                                    onDismiss()
                                    onOrganizeRequest?.invoke(node)
                                }
                            )
                        }
                    } else if (node is StorageNode.FileNode) {
                        WindowsContextMenuItem(
                            icon = Icons.AutoMirrored.Filled.OpenInNew,
                            label = "Open File",
                            textColor = winTextColor,
                            hoverColor = winHoverBg,
                            onClick = {
                                onDismiss()
                                val file = node.file
                                openFile(context, file)
                            }
                        )
                    }

                    HorizontalDivider(
                        color = winDivider,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Delete Action (Windows Red Highlight)
                    WindowsContextMenuItem(
                        icon = Icons.Default.Delete,
                        label = if (node.isDirectory) "Delete Folder" else "Delete File",
                        textColor = Color(0xFFEF4444), // Windows Alert Red
                        hoverColor = Color(0xFFEF4444).copy(alpha = 0.12f),
                        onClick = {
                            onDismiss()
                            onDeleteRequest(node)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WindowsContextMenuItem(
    icon: ImageVector,
    label: String,
    textColor: Color,
    hoverColor: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(value = false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isPressed) hoverColor else Color.Transparent)
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun WindowsDeleteConfirmDialog(
    node: StorageNode,
    onDismiss: () -> Unit,
    onConfirmDelete: (StorageNode) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val dialogBg = if (isDark) Color(0xFF242428) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0xFF3B3B42) else Color(0xFFE5E7EB)
    val titleColor = if (isDark) Color(0xFFF9FAFB) else Color(0xFF111827)
    val subTextColor = if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563)

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .width(320.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        containerColor = dialogBg,
        icon = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = if (node.isDirectory) "Delete Folder?" else "Delete File?",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to permanently delete '${node.name}'?",
                    fontSize = 13.sp,
                    color = subTextColor,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                val parentPath = node.path.substringBeforeLast('/', "Storage").substringAfterLast('/', "Storage")
                Text(
                    text = "Location: $parentPath",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = subTextColor.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmDelete(node)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Delete", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
            ) {
                Text(
                    "Cancel",
                    fontSize = 13.sp,
                    color = if (isDark) Color(0xFFE5E7EB) else Color(0xFF374151)
                )
            }
        }
    )
}
