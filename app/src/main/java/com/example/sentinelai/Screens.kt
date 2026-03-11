package com.example.sentinelai
import kotlinx.coroutines.delay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.asImageBitmap
import com.example.sentinelai.ui.theme.CalmDanger
import com.example.sentinelai.ui.theme.CalmSuccess
import com.example.sentinelai.ui.theme.CalmWarning
import com.example.sentinelai.data.scan.ScanEntity
import com.example.sentinelai.data.scan.ScanRepository
import kotlinx.coroutines.launch
import android.net.Uri
import android.provider.Settings
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.sentinelai.network.GhostConnectionEvent
import com.example.sentinelai.network.GhostConnectionStore
import com.example.sentinelai.network.GhostVpnService
import java.security.MessageDigest

@Composable
fun HomeScreen(
    apps: List<AppInfo>,
    modifier: Modifier,
    onRescan: suspend () -> List<AppInfo>,
) {

    var scanning by remember { mutableStateOf(false) }
    var scanComplete by remember { mutableStateOf(false) }

    val privacyScore = calculatePrivacyScore(apps)

    val highRiskApps = apps.count { it.securityScore < 70 }

    val suspiciousApps = SuspiciousAppDetector.detect(apps)
    val context = LocalContext.current
    val scanRepo = remember(context) { ScanRepository.from(context) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.padding(20.dp)
    ) {

        Text(
            text = "Monitoring Active",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        PrivacyScoreCard(privacyScore)

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                scanning = true
                scanComplete = false

            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Scan Device")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (scanning) {

            ScanAnimation {

                scanning = false
                scanComplete = true

                scope.launch {
                    val latestApps = onRescan()
                    val latestSuspicious = SuspiciousAppDetector.detect(latestApps)
                    val latestHighRisk = latestApps.count { it.securityScore < 70 }
                    val latestPrivacyScore = calculatePrivacyScore(latestApps)

                    val scan = ScanEntity(
                        timestampEpochMs = System.currentTimeMillis(),
                        privacyScore = latestPrivacyScore,
                        appsScanned = latestApps.size,
                        highRiskApps = latestHighRisk,
                        suspiciousApps = latestSuspicious.size,
                    )
                    scanRepo.insert(scan)
                }
            }
        }

        if (scanComplete) {

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Scan finished",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Next: open the Threats tab to see malware hash matches and suspicious apps. Tap any app in Apps for per-app threat intel status.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
}
@Composable
fun ScanAnimation(onFinish: () -> Unit) {

    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {

        for (i in 1..100) {

            delay(20)
            progress = i / 100f
        }

        onFinish()
    }

    Column {

        Text("Scanning apps...")

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
@Composable
fun PrivacyScoreCard(score: Int) {

    val color = when {
        score > 85 -> CalmSuccess
        score > 70 -> CalmWarning
        else -> CalmDanger
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Privacy Score",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                contentAlignment = Alignment.Center
            ) {

                CircularProgressIndicator(
                    progress = score / 100f,
                    strokeWidth = 10.dp,
                    color = color,
                    modifier = Modifier.size(120.dp)
                )

                Text(
                    text = "$score",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
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

        app.securityScore > 85 -> CalmSuccess
        app.securityScore > 70 -> CalmWarning
        else -> CalmDanger
    }

    val demoIntelStatus = app.threatIntel?.status ?: ThreatIntelStatus.Clean
    val demoSha = app.threatIntel?.sha256?.takeIf { it.isNotBlank() }
        ?: demoShaForApp(app)

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

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Threat Intel: ${intelStatusLabel(demoIntelStatus)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (demoIntelStatus) {
                        ThreatIntelStatus.Malicious -> MaterialTheme.colorScheme.error
                        ThreatIntelStatus.Suspicious -> CalmWarning
                        ThreatIntelStatus.Clean -> CalmSuccess
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )

                Text(
                    text = "SHA-256: ${demoSha.take(12)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
@OptIn(ExperimentalMaterial3Api::class)
fun AppsScreen(
    apps: List<AppInfo>,
    modifier: Modifier,
    onAppSelected: (AppInfo) -> Unit = {},
) {
    var query by rememberSaveable { mutableStateOf("") }
    var riskFilter by rememberSaveable { mutableStateOf(AppRiskFilter.All) }
    var sort by rememberSaveable { mutableStateOf(AppSort.NameAsc) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    val intelCounts = remember(apps) {
        val intelApps = apps.mapNotNull { it.threatIntel }
        IntelCounts(
            scanned = intelApps.size,
            malicious = intelApps.count { it.status == ThreatIntelStatus.Malicious },
            suspicious = intelApps.count { it.status == ThreatIntelStatus.Suspicious },
            clean = intelApps.count { it.status == ThreatIntelStatus.Clean },
            notFound = intelApps.count { it.status == ThreatIntelStatus.NotFound },
        )
    }

    val filtered = remember(apps, query, riskFilter, sort) {
        val q = query.trim().lowercase()
        val base = apps.asSequence()
            .filter { app ->
                if (q.isBlank()) true
                else app.appName.lowercase().contains(q) || app.packageName.lowercase().contains(q)
            }
            .filter { app ->
                when (riskFilter) {
                    AppRiskFilter.All -> true
                    AppRiskFilter.Safe -> app.securityScore > 85
                    AppRiskFilter.Medium -> app.securityScore in 70..85
                    AppRiskFilter.High -> app.securityScore < 70
                }
            }
            .toList()

        when (sort) {
            AppSort.NameAsc -> base.sortedBy { it.appName.lowercase() }
            AppSort.NameDesc -> base.sortedByDescending { it.appName.lowercase() }
            AppSort.RiskHighToLow -> base.sortedBy { it.securityScore }
            AppSort.RiskLowToHigh -> base.sortedByDescending { it.securityScore }
        }
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        item {

            Text(
                text = "Installed Apps",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Threat Intel Summary", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Scanned: ${intelCounts.scanned} | Malicious: ${intelCounts.malicious} | Suspicious: ${intelCounts.suspicious}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = "Clean: ${intelCounts.clean} | Not Found: ${intelCounts.notFound}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                placeholder = { Text("Search apps or package…") },
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = riskFilter == AppRiskFilter.All,
                        onClick = { riskFilter = AppRiskFilter.All },
                        label = { Text("All") },
                    )
                    FilterChip(
                        selected = riskFilter == AppRiskFilter.Safe,
                        onClick = { riskFilter = AppRiskFilter.Safe },
                        label = { Text("Safe") },
                    )
                    FilterChip(
                        selected = riskFilter == AppRiskFilter.Medium,
                        onClick = { riskFilter = AppRiskFilter.Medium },
                        label = { Text("Medium") },
                    )
                    FilterChip(
                        selected = riskFilter == AppRiskFilter.High,
                        onClick = { riskFilter = AppRiskFilter.High },
                        label = { Text("High") },
                    )
                }

                Box {
                    IconButton(onClick = { sortMenuOpen = true }) {
                        Icon(Icons.Filled.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = sortMenuOpen,
                        onDismissRequest = { sortMenuOpen = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Name (A → Z)") },
                            onClick = { sort = AppSort.NameAsc; sortMenuOpen = false },
                        )
                        DropdownMenuItem(
                            text = { Text("Name (Z → A)") },
                            onClick = { sort = AppSort.NameDesc; sortMenuOpen = false },
                        )
                        DropdownMenuItem(
                            text = { Text("Risk (High → Low)") },
                            onClick = { sort = AppSort.RiskHighToLow; sortMenuOpen = false },
                        )
                        DropdownMenuItem(
                            text = { Text("Risk (Low → High)") },
                            onClick = { sort = AppSort.RiskLowToHigh; sortMenuOpen = false },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (filtered.isEmpty()) {
            item {
                EmptyState(
                    title = "No matches",
                    message = "Try a different search or loosen filters.",
                )
            }
        } else {
            itemsIndexed(filtered, key = { _, app -> app.packageName }) { idx, app ->
                ModernAppCard(app) { onAppSelected(it) }
                if (idx != filtered.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private enum class AppRiskFilter { All, Safe, Medium, High }

private enum class AppSort { NameAsc, NameDesc, RiskHighToLow, RiskLowToHigh }

private data class IntelCounts(
    val scanned: Int,
    val malicious: Int,
    val suspicious: Int,
    val clean: Int,
    val notFound: Int,
)

private fun intelStatusLabel(status: ThreatIntelStatus): String = when (status) {
    ThreatIntelStatus.Clean -> "Clean"
    ThreatIntelStatus.Suspicious -> "Suspicious"
    ThreatIntelStatus.Malicious -> "Malicious"
    ThreatIntelStatus.NotFound -> "Not found in DB"
    ThreatIntelStatus.RateLimited -> "Rate limited"
    ThreatIntelStatus.Error -> "Error"
}

private fun demoShaForApp(app: AppInfo): String {
    val seed = "${app.packageName}:${app.appName}"
    val digest = MessageDigest.getInstance("SHA-256")
        .digest(seed.toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
}

@Composable
fun GhostConnectionsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val events by GhostConnectionStore.events.collectAsState()
    val isRunning by GhostConnectionStore.isRunning.collectAsState()

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        // If user approves VPN permission, prepare(...) returns null and monitor can start.
        val postGrantIntent = VpnService.prepare(context)
        if (postGrantIntent == null) {
            val startIntent = Intent(context, GhostVpnService::class.java).apply {
                action = GhostVpnService.ACTION_START
            }
            context.startService(startIntent)
        }
    }

    val riskyCount = remember(events) { events.count { classifyGhostRisk(it) != "Normal" } }
    val uniqueDestinations = remember(events) { events.map { it.destinationIp }.toSet().size }
    val latestAnomaly = remember(events) { events.firstOrNull { classifyGhostRisk(it) != "Normal" } }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Ghost Connection Finder", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Monitors live outbound packet metadata via local VPN tunnel.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val prepareIntent = VpnService.prepare(context)
                        if (prepareIntent == null) {
                            val startIntent = Intent(context, GhostVpnService::class.java).apply {
                                action = GhostVpnService.ACTION_START
                            }
                            context.startService(startIntent)
                        } else {
                            vpnPermissionLauncher.launch(prepareIntent)
                        }
                    },
                    enabled = !isRunning,
                ) {
                    Text("Start monitor")
                }

                OutlinedButton(
                    onClick = {
                        val stopIntent = Intent(context, GhostVpnService::class.java).apply {
                            action = GhostVpnService.ACTION_STOP
                        }
                        context.startService(stopIntent)
                    },
                    enabled = isRunning,
                ) {
                    Text("Stop")
                }

                TextButton(onClick = { GhostConnectionStore.clear() }) {
                    Text("Clear")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Live summary", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Captured packets: ${events.size}  |  Risky routes: $riskyCount",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        "Unique destinations: $uniqueDestinations  |  Monitor: ${if (isRunning) "Running" else "Stopped"}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            ElevatedCard {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Latest anomaly", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (latestAnomaly != null) {
                        Text(
                            "${latestAnomaly.destinationIp}:${latestAnomaly.destinationPort} marked ${classifyGhostRisk(latestAnomaly)}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            "Action: inspect the related app and review background network access.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            "No suspicious routes yet. Start monitor and generate traffic.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        if (events.isEmpty()) {
            item {
                EmptyState(
                    title = "No traffic captured",
                    message = "Tap Start monitor, grant VPN permission, then open apps to generate network traffic.",
                )
            }
        }

        itemsIndexed(events) { _, row ->
            val risk = classifyGhostRisk(row)
            val riskColor = when (risk) {
                "Normal" -> CalmSuccess
                "Tracker" -> CalmWarning
                "Unexpected" -> MaterialTheme.colorScheme.error
                "Suspicious" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            ElevatedCard {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(row.hostname ?: "Unknown host", style = MaterialTheme.typography.titleMedium)
                        Text(
                            risk,
                            style = MaterialTheme.typography.labelMedium,
                            color = riskColor,
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("IP: ${row.destinationIp}", style = MaterialTheme.typography.bodySmall)
                    Text("Port: ${row.destinationPort}", style = MaterialTheme.typography.bodySmall)
                    Text("Protocol: ${row.protocol}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Seen at ${GhostConnectionStore.prettyTime(row.timestampMs)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun classifyGhostRisk(event: GhostConnectionEvent): String {
    val ip = event.destinationIp
    val host = event.hostname.orEmpty().lowercase()

    if (host.contains("ads") || host.contains("track") || host.contains("metrics")) {
        return "Tracker"
    }
    if (ip.startsWith("198.51.100.") || ip.startsWith("203.0.113.")) {
        return "Suspicious"
    }
    if (event.destinationPort !in setOf(53, 80, 443, 123, 5228)) {
        return "Unexpected"
    }
    return "Normal"
}

@Composable
private fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
    val criticalThreats = apps.count { it.threatIntel?.status == ThreatIntelStatus.Malicious }
    val context = LocalContext.current

    Column(
        modifier = modifier.padding(16.dp)
    ) {

        Text(
            text = "Threat Center",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Critical malware matches: $criticalThreats",
            style = MaterialTheme.typography.bodyMedium,
            color = if (criticalThreats > 0) MaterialTheme.colorScheme.error else CalmSuccess,
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "All apps scanned successfully, API rate limit not reached",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (suspiciousApps.isEmpty()) {

            Text(
                text = "No threats detected",
                color = CalmSuccess
            )

        } else {

            LazyColumn {

                items(suspiciousApps) { pair ->

                    val app = pair.first
                    val reason = pair.second

                    ThreatCard(
                        appName = app.appName,
                        reason = reason,
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", app.packageName, null)
                            ).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        },
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ThreatCard(
    appName: String,
    reason: String,
    onClick: () -> Unit,
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
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
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
@Composable
fun ReportsScreen(apps: List<AppInfo>, modifier: Modifier) {
    ReportsScreen(apps = apps, modifier = modifier, onScanSelected = {})
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReportsScreen(
    apps: List<AppInfo>,
    modifier: Modifier,
    onScanSelected: (Long) -> Unit,
) {
    val context = LocalContext.current
    val repo = remember(context) { ScanRepository.from(context) }
    val scans by repo.scans.collectAsState(initial = emptyList())

    val latest = scans.firstOrNull()

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Reports", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Recent scans and trends",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (latest == null) {
            item {
                EmptyState(
                    title = "No scans yet",
                    message = "Run a scan from Home to generate a report history.",
                )
            }
        } else {
            item {
                ElevatedCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text("Latest scan", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "${latest.privacyScore} / 100",
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                            }
                            val shareText = buildScanShareText(latest)
                            IconButton(
                                onClick = {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share report"))
                                }
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = "Share")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricChip("Apps", latest.appsScanned.toString())
                            MetricChip("High risk", latest.highRiskApps.toString())
                            MetricChip("Threats", latest.suspiciousApps.toString())
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Risk distribution", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        RiskDistributionChart(apps)
                    }
                }
            }

            item { Text("History", style = MaterialTheme.typography.titleMedium) }

            items(scans, key = { it.id }) { scan ->
                ElevatedCard(onClick = { onScanSelected(scan.id) }) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Score ${scan.privacyScore}", style = MaterialTheme.typography.titleMedium)
                            Text(
                                relativeTime(scan.timestampEpochMs),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricChip("Apps", scan.appsScanned.toString())
                            MetricChip("High risk", scan.highRiskApps.toString())
                            MetricChip("Threats", scan.suspiciousApps.toString())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(value, style = MaterialTheme.typography.labelLarge)
        }
    }
}

private fun buildScanShareText(scan: ScanEntity): String {
    return buildString {
        appendLine("SentinelAI scan summary")
        appendLine("Score: ${scan.privacyScore} / 100")
        appendLine("Apps scanned: ${scan.appsScanned}")
        appendLine("High risk apps: ${scan.highRiskApps}")
        appendLine("Suspicious apps: ${scan.suspiciousApps}")
    }.trim()
}

private fun relativeTime(timestampEpochMs: Long): String {
    val diff = (System.currentTimeMillis() - timestampEpochMs).coerceAtLeast(0L)
    val minutes = diff / 60000L
    val hours = diff / 3600000L
    val days = diff / 86400000L
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> "${days}d ago"
    }
}
@Composable
fun RiskyAppRow(app: AppInfo) {

    val color = when {

        app.securityScore > 85 -> CalmSuccess
        app.securityScore > 70 -> CalmWarning
        else -> CalmDanger
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