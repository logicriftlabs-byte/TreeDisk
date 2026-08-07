package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.StorageViewModel
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleGlassBorder

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Tree : Screen("tree", "Tree", Icons.Default.AccountTree)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val items = listOf(
    Screen.Dashboard,
    Screen.Tree,
    Screen.Settings
)

@Composable
fun MainApp(viewModel: StorageViewModel) {
    val navController = rememberNavController()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 720.dp

        if (isTablet) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Sleek Frosted Glass Navigation Rail for Tablets
                CupertinoNavigationRail(navController = navController)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Dashboard.route,
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = {
                            fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                            slideInHorizontally(
                                initialOffsetX = { it / 8 },
                                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                            )
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                            slideOutHorizontally(
                                targetOffsetX = { -it / 8 },
                                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                            )
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                            slideInHorizontally(
                                initialOffsetX = { -it / 8 },
                                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                            )
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                            slideOutHorizontally(
                                targetOffsetX = { it / 8 },
                                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                            )
                        }
                    ) {
                        composable(Screen.Dashboard.route) { DashboardScreen(viewModel) }
                        composable(Screen.Tree.route) { TreeScreen(viewModel) }
                        composable(Screen.Settings.route) { SettingsScreen(viewModel) }
                    }
                }
            }
        } else {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    CupertinoFloatingBottomBar(navController = navController)
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Dashboard.route,
                    modifier = Modifier.padding(innerPadding),
                    enterTransition = {
                        fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                        slideInHorizontally(
                            initialOffsetX = { it / 8 },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                        )
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                        slideOutHorizontally(
                            targetOffsetX = { -it / 8 },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                        )
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                        slideInHorizontally(
                            initialOffsetX = { -it / 8 },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                        )
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                        slideOutHorizontally(
                            targetOffsetX = { it / 8 },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                        )
                    }
                ) {
                    composable(Screen.Dashboard.route) { DashboardScreen(viewModel) }
                    composable(Screen.Tree.route) { TreeScreen(viewModel) }
                    composable(Screen.Settings.route) { SettingsScreen(viewModel) }
                }
            }
        }
    }
}

@Composable
fun CupertinoNavigationRail(navController: androidx.navigation.NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        modifier = Modifier
            .width(250.dp)
            .fillMaxHeight()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(0.dp)
            ),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // App Brand Identity Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 28.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppleBlue,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "TreeDisk",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            text = "Storage Analyzer",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Section 1: ANALYTICS & EXPLORER
                Text(
                    text = "ANALYTICS & EXPLORER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 1.1.sp,
                    modifier = Modifier.padding(start = 10.dp, bottom = 8.dp)
                )

                listOf(Screen.Dashboard, Screen.Tree).forEach { screen ->
                    AppleSidebarRow(
                        screen = screen,
                        isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onSelect = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 2: PREFERENCES
                Text(
                    text = "PREFERENCES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 1.1.sp,
                    modifier = Modifier.padding(start = 10.dp, bottom = 8.dp)
                )

                AppleSidebarRow(
                    screen = Screen.Settings,
                    isSelected = currentDestination?.hierarchy?.any { it.route == Screen.Settings.route } == true,
                    onSelect = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // Apple Sidebar Footer Widget: Mini Storage Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Internal Storage",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AppleBlue)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "System Ready • Fast Indexing",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AppleSidebarRow(
    screen: Screen,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val activeBgColor by animateColorAsState(
        targetValue = if (isSelected) AppleBlue else Color.Transparent,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "appleSidebarBg"
    )

    val activeTextColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "appleSidebarText"
    )

    val activeIconColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else AppleBlue,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "appleSidebarIcon"
    )

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = activeBgColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = screen.icon,
                contentDescription = screen.title,
                tint = activeIconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = screen.title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = activeTextColor
            )
        }
    }
}

@Composable
fun CupertinoFloatingBottomBar(navController: androidx.navigation.NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Frosted Glass Bar
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = Color.Black.copy(alpha = 0.3f),
                    ambientColor = Color.Black.copy(alpha = 0.2f)
                )
                .clip(RoundedCornerShape(32.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                ),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    val activeBgColor by animateColorAsState(
                        targetValue = if (isSelected) AppleBlue.copy(alpha = 0.15f) else Color.Transparent,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "activeBg"
                    )

                    val activeIconColor by animateColorAsState(
                        targetValue = if (isSelected) AppleBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "activeIcon"
                    )

                    val interactionSource = remember { MutableInteractionSource() }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(activeBgColor)
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = activeIconColor,
                                modifier = Modifier.size(20.dp)
                            )
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = expandHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) + fadeIn(),
                                exit = shrinkHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) + fadeOut()
                            ) {
                                Row {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = screen.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppleBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

