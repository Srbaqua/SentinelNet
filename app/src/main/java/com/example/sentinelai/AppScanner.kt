package com.example.sentinelai

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

data class AppInfo(
    val appName: String,
    val packageName: String,
    val permissions: List<String>,
    val securityScore: Int,
    val threatIntel: ThreatIntelResult? = null,
)

class AppScanner(private val context: Context) {

    fun getInstalledApps(): List<AppInfo> {

        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)

        val appList = mutableListOf<AppInfo>()

        for (pkg in packages) {

            // Keep scan focused on user-installed packages only.
            if ((pkg.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                continue
            }

            val appName = pkg.applicationInfo.loadLabel(pm).toString()
            val packageName = pkg.packageName

            val permissions = grantedPermissionsFor(pm, pkg)

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

    private fun grantedPermissionsFor(pm: PackageManager, pkg: android.content.pm.PackageInfo): List<String> {
        val requested = pkg.requestedPermissions ?: return emptyList()
        return requested.filter { perm ->
            pm.checkPermission(perm, pkg.packageName) == PackageManager.PERMISSION_GRANTED
        }
    }
}