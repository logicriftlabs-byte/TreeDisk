package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleMint
import kotlinx.coroutines.delay

enum class ScanButtonState {
    Idle, Scanning, Done
}

@Composable
fun ScanPillButton(
    isScanning: Boolean,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var wasScanning by remember { mutableStateOf(isScanning) }
    var showDoneState by remember { mutableStateOf(false) }

    LaunchedEffect(isScanning) {
        if (wasScanning && !isScanning) {
            showDoneState = true
            delay(2500)
            showDoneState = false
        }
        wasScanning = isScanning
    }

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isScanning -> AppleBlue.copy(alpha = 0.15f)
            showDoneState -> AppleMint.copy(alpha = 0.18f)
            else -> AppleBlue.copy(alpha = 0.12f)
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pillBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isScanning -> AppleBlue.copy(alpha = 0.4f)
            showDoneState -> AppleMint.copy(alpha = 0.5f)
            else -> AppleBlue.copy(alpha = 0.25f)
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pillBorder"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isScanning -> AppleBlue
            showDoneState -> AppleMint
            else -> AppleBlue
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pillContent"
    )

    Surface(
        shape = CircleShape,
        color = backgroundColor,
        modifier = modifier
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(enabled = !isScanning) {
                showDoneState = false
                onScanClick()
            }
    ) {
        AnimatedContent(
            targetState = when {
                isScanning -> ScanButtonState.Scanning
                showDoneState -> ScanButtonState.Done
                else -> ScanButtonState.Idle
            },
            transitionSpec = {
                (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(initialScale = 0.82f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)))
                    .togetherWith(fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.82f))
            },
            label = "ScanPillContent"
        ) { state ->
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (state) {
                    ScanButtonState.Scanning -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = contentColor,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scanning...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor
                        )
                    }
                    ScanButtonState.Done -> {
                        var playTickAnim by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            playTickAnim = true
                        }
                        val iconScale by animateFloatAsState(
                            targetValue = if (playTickAnim) 1.15f else 0.3f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "tickScale"
                        )
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Scan Done",
                            modifier = Modifier
                                .size(15.dp)
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                },
                            tint = contentColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Done!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                    ScanButtonState.Idle -> {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan Storage",
                            modifier = Modifier.size(15.dp),
                            tint = contentColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScanActionButton(
    isScanning: Boolean,
    onScanClick: () -> Unit
) {
    var wasScanning by remember { mutableStateOf(isScanning) }
    var showDoneState by remember { mutableStateOf(false) }

    LaunchedEffect(isScanning) {
        if (wasScanning && !isScanning) {
            showDoneState = true
            delay(2500)
            showDoneState = false
        }
        wasScanning = isScanning
    }

    val containerColor by animateColorAsState(
        targetValue = when {
            isScanning -> AppleBlue.copy(alpha = 0.15f)
            showDoneState -> AppleMint
            else -> AppleBlue
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "btnBg"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isScanning -> AppleBlue
            showDoneState -> androidx.compose.ui.graphics.Color.White
            else -> androidx.compose.ui.graphics.Color.White
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "btnFg"
    )

    if (isScanning) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = AppleBlue,
            strokeWidth = 2.dp
        )
    } else {
        Button(
            onClick = {
                showDoneState = false
                onScanClick()
            },
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor
            )
        ) {
            AnimatedContent(
                targetState = if (showDoneState) ScanButtonState.Done else ScanButtonState.Idle,
                transitionSpec = {
                    (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)))
                        .togetherWith(fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f))
                },
                label = "SettingsScanButtonContent"
            ) { state ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state == ScanButtonState.Done) {
                        var playTickAnim by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            playTickAnim = true
                        }
                        val iconScale by animateFloatAsState(
                            targetValue = if (playTickAnim) 1.2f else 0.4f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "settingsTickScale"
                        )
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Scan Done",
                            modifier = Modifier
                                .size(14.dp)
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                },
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Done!", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Re-scan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

