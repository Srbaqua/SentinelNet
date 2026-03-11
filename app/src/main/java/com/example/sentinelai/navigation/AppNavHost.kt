package com.example.sentinelai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.sentinelai.AppInfo
import com.example.sentinelai.AppDetailScreen
import com.example.sentinelai.AppsScreen
import com.example.sentinelai.GhostConnectionsScreen
import com.example.sentinelai.HomeScreen
import com.example.sentinelai.ReportsScreen
import com.example.sentinelai.ThreatsScreen
import com.example.sentinelai.reports.ScanDetailsScreen
import com.example.sentinelai.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    apps: List<AppInfo>,
    onRescan: suspend () -> List<AppInfo>,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.Home,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Routes.Home) {
            HomeScreen(apps = apps, modifier = modifier, onRescan = onRescan)
        }
        composable(Routes.Apps) {
            AppsScreen(
                apps = apps,
                modifier = modifier,
                onAppSelected = { navController.navigate(Routes.appDetails(it.packageName)) },
            )
        }
        composable(Routes.Threats) {
            ThreatsScreen(apps = apps, modifier = modifier)
        }
        composable(Routes.Reports) {
            ReportsScreen(
                apps = apps,
                modifier = modifier,
                onScanSelected = { navController.navigate(Routes.scanDetails(it)) },
            )
        }
        composable(Routes.Ghost) {
            GhostConnectionsScreen(modifier = modifier)
        }
        composable(Routes.Settings) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.ScanDetailsRoute,
            arguments = listOf(
                navArgument(Routes.ScanDetailsIdArg) { type = NavType.LongType }
            ),
        ) { entry ->
            val scanId = entry.arguments?.getLong(Routes.ScanDetailsIdArg) ?: return@composable
            ScanDetailsScreen(
                scanId = scanId,
                onBack = { navController.popBackStack() },
                modifier = modifier,
            )
        }
        composable(
            route = Routes.AppDetailsRoute,
            arguments = listOf(
                navArgument(Routes.AppDetailsPackageArg) { type = NavType.StringType }
            ),
        ) { entry ->
            val packageName = entry.arguments?.getString(Routes.AppDetailsPackageArg)
            val app = apps.firstOrNull { it.packageName == packageName }
            if (app != null) {
                AppDetailScreen(app = app, onBack = { navController.popBackStack() })
            } else {
                // If the app can't be found (rare), just navigate back.
                navController.popBackStack()
            }
        }
    }
}

