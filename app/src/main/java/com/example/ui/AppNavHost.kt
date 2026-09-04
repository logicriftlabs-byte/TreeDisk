package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.example.StorageViewModel
import androidx.activity.compose.BackHandler

import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun MainApp(viewModel: StorageViewModel) {
    var showDashboard by remember { mutableStateOf(value = false) }
    var showSettings by remember { mutableStateOf(value = false) }

    // Ask to download Gemini Nano model on first app load
    var showNanoDownload by rememberSaveable { mutableStateOf(value = true) }
    var nanoModelReady by rememberSaveable { mutableStateOf(value = false) }

    BackHandler(enabled = showDashboard || showSettings) {
        if (showSettings) {
            showSettings = false
        } else if (showDashboard) {
            showDashboard = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TreeScreen(
            viewModel = viewModel,
            onOpenDashboard = { showDashboard = true },
            onOpenSettings = { showSettings = true },
        )
        
        if (showNanoDownload) {
            GeminiNanoDownloadDialog(
                onDismiss = { showNanoDownload = false },
                onDownloadComplete = { 
                    nanoModelReady = true 
                    // Tell ViewModel or Assistant that model is ready
                },
            )
        }
        
        AnimatedVisibility(
            visible = showDashboard,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
            ) + fadeOut(),
            modifier = Modifier.zIndex(10f)
        ) {
            DashboardScreen(
                viewModel = viewModel,
                onClose = { showDashboard = false },
            )
        }
        
        AnimatedVisibility(
            visible = showSettings,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier.zIndex(20f)
        ) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { showSettings = false },
            )
        }
    }
}
