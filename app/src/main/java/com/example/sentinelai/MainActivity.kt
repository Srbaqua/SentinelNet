package com.example.sentinelai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sentinelai.ui.theme.SentinelAITheme
import com.example.sentinelai.ui.theme.CalmDanger
import com.example.sentinelai.ui.theme.CalmSuccess
import com.example.sentinelai.ui.theme.CalmWarning
import com.example.sentinelai.settings.SettingsStore
import com.example.sentinelai.settings.ThemeMode
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scanner = AppScanner(this)
        val apps = scanner.getInstalledApps()

        setContent {
            val context = LocalContext.current
            val store = remember(context) { SettingsStore(context) }
            val themeMode by store.themeMode.collectAsState(initial = ThemeMode.System)
            val systemDark = isSystemInDarkTheme()

            val darkTheme = when (themeMode) {
                ThemeMode.System -> systemDark
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }

            SentinelAITheme(darkTheme = darkTheme) { MainScreen(apps) }
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
fun AppDetailScreen(
    app: AppInfo,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val risks = remember(app.permissions) { RiskAnalyzer.detectRisks(app.permissions) }
    val advice = remember(app.permissions) { MitigationAdvisor.getRecommendations(app.permissions) }

    val scoreTone = when {
        app.securityScore > 85 -> CalmSuccess
        app.securityScore > 70 -> CalmWarning
        else -> CalmDanger
    }
    val scoreLabel = when {
        app.securityScore > 85 -> "Safe"
        app.securityScore > 70 -> "Medium"
        else -> "High risk"
    }

    val groupedPermissions = remember(app.permissions) {
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
            .map { perm -> PermissionFormatter.format(perm) to perm }
            .groupBy { (_, raw) -> permissionCategory(raw) }
            .mapValues { (_, list) -> list.map { it.first }.distinct().sorted() }
            .toSortedMap(compareBy { it.order })
    }

    var showAllAdvice by rememberSaveable { mutableStateOf(false) }
    var showAllRisks by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ElevatedCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(app.appName, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("Security score", style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${app.securityScore} / 100",
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = scoreTone.copy(alpha = 0.14f),
                            ),
                        ) {
                            Text(
                                scoreLabel,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = scoreTone,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = (app.securityScore.coerceIn(0, 100) / 100f),
                        color = scoreTone,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        if (groupedPermissions.isNotEmpty()) {
            item {
                Text("Permissions", style = MaterialTheme.typography.titleMedium)
            }
            items(groupedPermissions.entries.toList(), key = { it.key.name }) { entry ->
                ElevatedCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(entry.key.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        entry.value.forEach { permLabel ->
                            ListItem(
                                headlineContent = { Text(permLabel) },
                                supportingContent = {
                                    val raw = entry.key.rawHintFor(permLabel)
                                    val explanation = SecurityAdvisor.explain(raw)
                                    Text(
                                        explanation,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                            )
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        }
                    }
                }
            }
        }

        item {
            ElevatedCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Permission interactions", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = onBack) { Text("Back") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    PermissionInteractionGraph(app.permissions.distinct())
                }
            }
        }

        item {
            ExpandableListCard(
                title = "Detected risks",
                subtitle = if (risks.isEmpty()) "No risks detected" else "${risks.size} findings",
                tone = MaterialTheme.colorScheme.error,
                items = risks.map { "⚠ $it" },
                expanded = showAllRisks,
                onToggleExpanded = { showAllRisks = !showAllRisks },
            )
        }

        item {
            ExpandableListCard(
                title = "Recommendations",
                subtitle = if (advice.isEmpty()) "No mitigation needed" else "${advice.size} suggestions",
                tone = MaterialTheme.colorScheme.secondary,
                items = advice,
                expanded = showAllAdvice,
                onToggleExpanded = { showAllAdvice = !showAllAdvice },
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

private enum class PermissionCategory(val title: String, val order: Int) {
    Location("Location", 1),
    Camera("Camera", 2),
    Microphone("Microphone", 3),
    Contacts("Contacts", 4),
    Sms("SMS", 5),
    Storage("Storage", 6),
    Other("Other", 99),
}

private fun permissionCategory(rawPermission: String): PermissionCategory = when {
    rawPermission.contains("LOCATION") -> PermissionCategory.Location
    rawPermission.contains("CAMERA") -> PermissionCategory.Camera
    rawPermission.contains("AUDIO") -> PermissionCategory.Microphone
    rawPermission.contains("CONTACT") -> PermissionCategory.Contacts
    rawPermission.contains("SMS") -> PermissionCategory.Sms
    rawPermission.contains("STORAGE") -> PermissionCategory.Storage
    else -> PermissionCategory.Other
}

// `SecurityAdvisor.explain` expects a raw permission string; this attempts a best-effort mapping.
private fun PermissionCategory.rawHintFor(prettyLabel: String): String = when (this) {
    PermissionCategory.Location -> "LOCATION"
    PermissionCategory.Camera -> "CAMERA"
    PermissionCategory.Microphone -> "AUDIO"
    PermissionCategory.Contacts -> "CONTACT"
    PermissionCategory.Sms -> "SMS"
    PermissionCategory.Storage -> "STORAGE"
    PermissionCategory.Other -> prettyLabel
}

@Composable
private fun ExpandableListCard(
    title: String,
    subtitle: String,
    tone: Color,
    items: List<String>,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
) {
    ElevatedCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (items.isNotEmpty()) {
                    TextButton(onClick = onToggleExpanded) {
                        Text(if (expanded) "Collapse" else "Expand", color = tone)
                    }
                }
            }

            if (items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                val visible = if (expanded) items else items.take(3)
                visible.forEach { line ->
                    Text(line, color = if (title.contains("risk", ignoreCase = true)) tone else MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))
                }
                if (!expanded && items.size > 3) {
                    Text(
                        "+ ${items.size - 3} more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}