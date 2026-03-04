package com.example.sentinelai

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.utils.ColorTemplate

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

            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

            val data = PieData(dataSet)

            chart.data = data
            chart.description.isEnabled = false
            chart.setEntryLabelTextSize(12f)

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