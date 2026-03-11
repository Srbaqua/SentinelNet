package com.example.sentinelai.reports

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sentinelai.data.scan.ScanEntity
import com.example.sentinelai.data.scan.ScanRepository

@Composable
fun ScanDetailsScreen(
    scanId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val repo = remember(context) { ScanRepository.from(context) }
    var scan by remember { mutableStateOf<ScanEntity?>(null) }

    LaunchedEffect(scanId) {
        scan = repo.getById(scanId)
    }

    val data = scan
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Scan details", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) { Text("Back") }
        }

        if (data == null) {
            Text(
                "Loading…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }

        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Score", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${data.privacyScore} / 100", style = MaterialTheme.typography.headlineSmall)
                    }
                    IconButton(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, buildShareText(data))
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share report"))
                        }
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Apps scanned: ${data.appsScanned}", style = MaterialTheme.typography.bodyMedium)
                Text("High risk apps: ${data.highRiskApps}", style = MaterialTheme.typography.bodyMedium)
                Text("Suspicious apps: ${data.suspiciousApps}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun buildShareText(scan: ScanEntity): String =
    buildString {
        appendLine("SentinelAI scan summary")
        appendLine("Score: ${scan.privacyScore} / 100")
        appendLine("Apps scanned: ${scan.appsScanned}")
        appendLine("High risk apps: ${scan.highRiskApps}")
        appendLine("Suspicious apps: ${scan.suspiciousApps}")
    }.trim()

