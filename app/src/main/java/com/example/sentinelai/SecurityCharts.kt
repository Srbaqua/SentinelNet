package com.example.sentinelai

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieData
import android.graphics.Color as AColor

@Composable
fun RiskDistributionChart(apps: List<AppInfo>) {

    val highRisk = apps.count { it.securityScore < 70 }
    val mediumRisk = apps.count { it.securityScore in 70..85 }
    val safeApps = apps.count { it.securityScore > 85 }

    AndroidView(
        factory = { context ->

            val chart = PieChart(context)

            val entries = listOf(
                PieEntry(highRisk.toFloat(), "High Risk"),
                PieEntry(mediumRisk.toFloat(), "Medium"),
                PieEntry(safeApps.toFloat(), "Safe")
            )

            val dataSet = PieDataSet(entries, "Risk Distribution")

            // Calm palette: danger / warning / safe
            dataSet.colors = listOf(
                AColor.parseColor("#B5484A"), // danger (muted red)
                AColor.parseColor("#B57A1B"), // warning (muted amber)
                AColor.parseColor("#2F8F6B")  // safe (muted green)
            )

            val data = PieData(dataSet)
            data.setValueTextSize(12f)
            data.setValueTextColor(AColor.WHITE)

            chart.data = data
            chart.description.isEnabled = false
            chart.setEntryLabelTextSize(12f)
            chart.setEntryLabelColor(AColor.WHITE)

            chart.legend.isEnabled = true
            chart.legend.textColor = AColor.WHITE
            chart.legend.textSize = 12f

            chart.layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    600
                )

            chart.invalidate()

            chart
        }
    )
}