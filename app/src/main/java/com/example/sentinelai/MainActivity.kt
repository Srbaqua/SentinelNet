package com.example.sentinelai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scanner = AppScanner(this)
        val apps = scanner.getInstalledApps()

        setContent {
            MainScreen(apps)
        }
    }
}

fun calculatePrivacyScore(apps: List<AppInfo>): Int {

    if (apps.isEmpty()) return 100

    val avg = apps.map { it.securityScore }.average()

    return avg.toInt()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentinelDashboard(apps: List<AppInfo>) {
    val suspiciousApps = SuspiciousAppDetector.detect(apps)

    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }

    if (selectedApp != null) {
        AppDetailScreen(selectedApp!!) {
            selectedApp = null
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SentinelAI Security Scanner") }
            )
        }
    ) { padding ->

        val privacyScore = calculatePrivacyScore(apps)

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            item {

                Text(
                    text = "Monitoring Active",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Privacy Score",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "$privacyScore / 100",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Total Apps Scanned: ${apps.size}")

                        val highRiskApps =
                            apps.count { it.securityScore < 70 }

                        Text("High Risk Apps: $highRiskApps")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Installed Apps",
                    style = MaterialTheme.typography.titleMedium
                )

//                Spacer(modifier = Modifier.height(10.dp))
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Risk Distribution",
                    style = MaterialTheme.typography.titleMedium
                )

                RiskDistributionChart(apps)

                Spacer(modifier = Modifier.height(20.dp))
            }
            if (suspiciousApps.isNotEmpty()) {

                item {

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "⚠ Suspicious Apps Detected",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Red
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(suspiciousApps.take(3)) { pair ->

                    val app = pair.first
                    val reason = pair.second

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = app.appName,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = reason,
                                color = Color.Red
                            )
                        }
                    }
                }
            }

            items(apps.take(25)) { app ->

                AppRow(app) {
                    selectedApp = it
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
        //        {

//
//            items(apps.take(25)) { app ->
//
//                AppRow(app) {
//                    selectedApp = it
//                }
//
//                Spacer(modifier = Modifier.height(10.dp))
//            }
//        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRow(app: AppInfo, onClick: (AppInfo) -> Unit) {

    val scoreColor = when {
        app.securityScore > 85 -> Color(0xFF4CAF50)
        app.securityScore > 70 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick(app) }
    ) {

        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Package: ${app.packageName}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "Score: ${app.securityScore}",
                color = scoreColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(app: AppInfo, onBack: () -> Unit) {

    val risks = RiskAnalyzer.detectRisks(app.permissions)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app.appName) }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
        ) {

            val scoreColor = when {
                app.securityScore > 85 -> Color(0xFF4CAF50)
                app.securityScore > 70 -> Color(0xFFFFC107)
                else -> Color.Red
            }

            Text(
                text = "Security Score: ${app.securityScore}/100",
                color = scoreColor,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Permissions",
                style = MaterialTheme.typography.titleMedium
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            app.permissions
                .distinct()
                .filter {
                    it.contains("CAMERA") ||
                            it.contains("LOCATION") ||
                            it.contains("CONTACT") ||
                            it.contains("SMS") ||
                            it.contains("AUDIO") ||
                            it.contains("STORAGE")
                }
                .distinct()
                .take(6)
                .forEach { perm ->

                    Text("• ${PermissionFormatter.format(perm)}")

                    Text(
                        text = SecurityAdvisor.explain(perm),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Detected Risks",
                style = MaterialTheme.typography.titleMedium
            )

            risks.forEach {

                Text(
                    text = "⚠ $it",
                    color = Color.Red
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Recommendations",
                style = MaterialTheme.typography.titleMedium
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            val advice = MitigationAdvisor.getRecommendations(app.permissions)

            if (advice.isEmpty()) {

                Text("No mitigation needed")

            } else {

                advice.forEach {

                    Text(" $it")
                }
            }
            Spacer(modifier = Modifier.height(30.dp))

            Button(onClick = onBack) {
                Text("Back")
            }
        }
    }
}