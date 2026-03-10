package com.example.sentinelai

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sentinelai.navigation.AppNavHost
import com.example.sentinelai.navigation.Routes

@Composable
fun MainScreen(apps: List<AppInfo>) {
    MainScaffold(apps = apps)
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(apps: List<AppInfo>) {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = backStackEntry?.destination?.route

    val bottomItems = listOf(
        BottomNavItem(
            route = Routes.Home,
            label = "Home",
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
        ),
        BottomNavItem(
            route = Routes.Apps,
            label = "Apps",
            icon = { Icon(Icons.Filled.Apps, contentDescription = null) },
        ),
        BottomNavItem(
            route = Routes.Threats,
            label = "Threats",
            icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
        ),
        BottomNavItem(
            route = Routes.Reports,
            label = "Reports",
            icon = { Icon(Icons.Filled.Assessment, contentDescription = null) },
        ),
    )

    val isBottomBarVisible = currentRoute in setOf(
        Routes.Home,
        Routes.Apps,
        Routes.Threats,
        Routes.Reports,
    )

    val title = when (currentRoute) {
        Routes.Home -> "Overview"
        Routes.Apps -> "Installed apps"
        Routes.Threats -> "Threat center"
        Routes.Reports -> "Reports"
        Routes.Settings -> "Settings"
        Routes.AppDetailsRoute -> "App details"
        else -> "SentinelAI"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                actions = {
                    if (currentRoute != Routes.Settings) {
                        IconButton(onClick = { navController.navigate(Routes.Settings) }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            },
                            icon = item.icon,
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        AppNavHost(
            navController = navController,
            apps = apps,
            modifier = Modifier.padding(padding),
            startDestination = Routes.Home,
        )
    }
}