package com.example.sentinelai

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.graphics.painter.rememberDrawablePainter
import androidx.compose.foundation.Image
import androidx.compose.runtime.remember
//import androidx.compose.ui.unit.dp
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.foundation.Image
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
@Composable
fun HomeScreen(apps: List<AppInfo>, modifier: Modifier) {

    val privacyScore = calculatePrivacyScore(apps)

    val highRiskApps =
        apps.count { it.securityScore < 70 }

    val suspiciousApps =
        SuspiciousAppDetector.detect(apps)

    Column(
        modifier = modifier
            .padding(20.dp)
    ) {

        Text(
            text = "Monitoring Active",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        PrivacyScoreCard(privacyScore)

        Spacer(modifier = Modifier.height(20.dp))

        QuickStats(apps.size, highRiskApps, suspiciousApps.size)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Risk Distribution",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        RiskDistributionChart(apps)
    }
}
@Composable
fun PrivacyScoreCard(score: Int) {

    val color = when {

        score > 85 -> Color(0xFF4CAF50)

        score > 70 -> Color(0xFFFFC107)

        else -> Color.Red
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "Privacy Score",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "$score / 100",
                style = MaterialTheme.typography.headlineLarge,
                color = color
            )
        }
    }
}
@Composable
fun QuickStats(
    totalApps: Int,
    highRisk: Int,
    suspicious: Int
) {

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {

        StatCard("Apps", totalApps)

        StatCard("High Risk", highRisk)

        StatCard("Threats", suspicious)
    }
}
@Composable
fun StatCard(
    title: String,
    value: Int
) {

    Card(
        modifier = Modifier
            .width(110.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernAppCard(
    app: AppInfo,
    onClick: (AppInfo) -> Unit
){

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
        ) {

            AppIcon(app.packageName)

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                PermissionPreview(app.permissions)
            }

            SecurityScoreBadge(app.securityScore, scoreColor)
        }
    }
}
@Composable
fun AppIcon(packageName: String) {

    val context = LocalContext.current
    val pm = context.packageManager

    val iconBitmap = remember(packageName) {

        try {

            val drawable = pm.getApplicationIcon(packageName)

            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            bitmap

        } catch (e: Exception) {
            null
        }
    }

    iconBitmap?.let {

        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
    }
}
@Composable
fun AppsScreen(apps: List<AppInfo>, modifier: Modifier) {

    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }

    if (selectedApp != null) {

        AppDetailScreen(selectedApp!!) {

            selectedApp = null
        }

        return
    }

    LazyColumn(
        modifier = modifier.padding(16.dp)
    ) {

        item {

            Text(
                text = "Installed Apps",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        items(apps) { app ->

            ModernAppCard(app) {

                selectedApp = it
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
@Composable
fun PermissionPreview(perms: List<String>) {

    val filtered = perms
        .distinct()
        .filter {

            it.contains("CAMERA") ||
                    it.contains("LOCATION") ||
                    it.contains("CONTACT") ||
                    it.contains("SMS") ||
                    it.contains("AUDIO") ||
                    it.contains("STORAGE")
        }
        .take(3)

    val text = filtered.joinToString(" • ") {

        PermissionFormatter.format(it)
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall
    )
}
@Composable
fun SecurityScoreBadge(score: Int, color: Color) {

    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.2f)
        )
    ) {

        Text(
            text = "$score",
            color = color,
            modifier = Modifier.padding(10.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }
}
@Composable
fun ThreatsScreen(apps: List<AppInfo>, modifier: Modifier) {

    val suspiciousApps = SuspiciousAppDetector.detect(apps)

    Column(
        modifier = modifier.padding(16.dp)
    ) {

        Text(
            text = "Threat Center",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (suspiciousApps.isEmpty()) {

            Text(
                text = "No threats detected",
                color = Color(0xFF4CAF50)
            )

        } else {

            LazyColumn {

                items(suspiciousApps) { pair ->

                    val app = pair.first
                    val reason = pair.second

                    ThreatCard(app.appName, reason)

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
@Composable
fun ThreatCard(
    appName: String,
    reason: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        )
    ) {

        Row(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "⚠",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {

                Text(
                    text = appName,
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
@Composable
fun ReportsScreen(apps: List<AppInfo>, modifier: Modifier) {

    val privacyScore = calculatePrivacyScore(apps)

    val highRiskApps = apps.count { it.securityScore < 70 }

    val riskyApps = apps
        .sortedBy { it.securityScore }
        .take(5)

    LazyColumn(
        modifier = modifier.padding(16.dp)
    ) {

        item {

            Text(
                text = "Security Report",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            PrivacyScoreCard(privacyScore)

            Spacer(modifier = Modifier.height(20.dp))

            QuickStats(
                apps.size,
                highRiskApps,
                riskyApps.size
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Risk Distribution",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(10.dp))

            RiskDistributionChart(apps)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Top Risky Apps",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        items(riskyApps) { app ->

            RiskyAppRow(app)

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
@Composable
fun RiskyAppRow(app: AppInfo) {

    val color = when {

        app.securityScore > 85 -> Color(0xFF4CAF50)

        app.securityScore > 70 -> Color(0xFFFFC107)

        else -> Color.Red
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = app.appName,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Score ${app.securityScore}",
                color = color
            )
        }
    }
}