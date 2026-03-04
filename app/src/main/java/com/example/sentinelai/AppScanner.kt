package com.example.sentinelai

import android.content.Context
import android.content.pm.PackageManager

data class AppInfo(
    val appName: String,
    val packageName: String,
    val permissions: List<String>,
    val securityScore: Int
)

class AppScanner(private val context: Context) {

    fun getInstalledApps(): List<AppInfo> {

        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)

        val appList = mutableListOf<AppInfo>()

        for (pkg in packages) {

            val appName = pkg.applicationInfo.loadLabel(pm).toString()
            val packageName = pkg.packageName

            val permissions = pkg.requestedPermissions?.toList() ?: emptyList()

            val score = RiskAnalyzer.calculateRiskScore(permissions)

            appList.add(
                AppInfo(
                    appName,
                    packageName,
                    permissions,
                    score
                )
            )
        }

        return appList
    }
}