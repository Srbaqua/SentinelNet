package com.example.sentinelai

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screens(
    val title: String,
    val icon: ImageVector
) {

    object Home : Screens("Home", Icons.Default.Home)

    object Apps : Screens("Apps", Icons.Default.Apps)

    object Threats : Screens("Threats", Icons.Default.Warning)

    object Reports : Screens("Reports", Icons.Default.Assessment)
}

@Composable
fun MainScreen(apps: List<AppInfo>) {

    var currentScreen by remember { mutableStateOf<Screens>(Screens.Home) }

    Scaffold(

        bottomBar = {

            NavigationBar {

                listOf(
                    Screens.Home,
                    Screens.Apps,
                    Screens.Threats,
                    Screens.Reports
                ).forEach { screen ->

                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) }
                    )
                }
            }
        }

    ) { padding ->

        when (currentScreen) {

            Screens.Home -> HomeScreen(apps, Modifier.padding(padding))

            Screens.Apps -> AppsScreen(apps, Modifier.padding(padding))

            Screens.Threats -> ThreatsScreen(apps, Modifier.padding(padding))

            Screens.Reports -> ReportsScreen(apps, Modifier.padding(padding))
        }
    }
}