package com.example.sentinelai.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.widget.Toast
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val store = remember(context) { SettingsStore(context) }
    val scope = rememberCoroutineScope()

    val themeMode by store.themeMode.collectAsState(initial = ThemeMode.System)
    val showScanTips by store.showScanTips.collectAsState(initial = true)
    val quickScanOnOpen by store.quickScanOnOpen.collectAsState(initial = false)
    val storedVirusTotalApiKey by store.virusTotalApiKey.collectAsState(initial = "")
    var virusTotalApiKeyInput by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(storedVirusTotalApiKey) {
        if (virusTotalApiKeyInput.isBlank()) {
            virusTotalApiKeyInput = storedVirusTotalApiKey
        }
    }

    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Appearance", style = MaterialTheme.typography.titleMedium)
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                ThemeModeOption(
                    label = "System",
                    description = "Follow device settings",
                    selected = themeMode == ThemeMode.System,
                    onSelect = { scope.launch { store.setThemeMode(ThemeMode.System) } },
                )
                ThemeModeOption(
                    label = "Light",
                    description = "Always use light theme",
                    selected = themeMode == ThemeMode.Light,
                    onSelect = { scope.launch { store.setThemeMode(ThemeMode.Light) } },
                )
                ThemeModeOption(
                    label = "Dark",
                    description = "Always use dark theme",
                    selected = themeMode == ThemeMode.Dark,
                    onSelect = { scope.launch { store.setThemeMode(ThemeMode.Dark) } },
                )
            }
        }

        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Scan preferences", style = MaterialTheme.typography.titleMedium)
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                SwitchRow(
                    title = "Show scan tips",
                    subtitle = "Surface helpful reminders during scans",
                    checked = showScanTips,
                    onCheckedChange = { enabled ->
                        scope.launch { store.setShowScanTips(enabled) }
                    },
                )
                SwitchRow(
                    title = "Quick scan on open",
                    subtitle = "Start scanning immediately when the app opens",
                    checked = quickScanOnOpen,
                    onCheckedChange = { enabled ->
                        scope.launch { store.setQuickScanOnOpen(enabled) }
                    },
                )
            }
        }

        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Threat intelligence", style = MaterialTheme.typography.titleMedium)
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                Text(
                    "Add your VirusTotal API key to enable hash-based malware checks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = virusTotalApiKeyInput,
                    onValueChange = { virusTotalApiKeyInput = it },
                    label = { Text("VirusTotal API key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    "Public API is rate-limited. Very large scans may pause early when the limit is hit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Button(
                    onClick = {
                        scope.launch {
                            if (virusTotalApiKeyInput.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Please enter a VirusTotal API key",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            } else {
                                store.setVirusTotalApiKey(virusTotalApiKeyInput)
                                Toast.makeText(
                                    context,
                                    "VirusTotal API key saved",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                    }
                ) {
                    Text("Save API Key")
                }

                if (storedVirusTotalApiKey.isNotBlank()) {
                    Text(
                        text = "Saved key: ${storedVirusTotalApiKey.take(6)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("Back") }
            Button(onClick = onBack) { Text("Done") }
        }
    }
}

@Composable
private fun ThemeModeOption(
    label: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        RadioButton(selected = selected, onClick = onSelect)
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

