package com.example.sentinelai

import android.content.Context
import android.content.pm.PackageInfo
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

            val permissions = grantedPermissionsFor(pkg)

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

    private fun grantedPermissionsFor(pkg: PackageInfo): List<String> {
        val requested = pkg.requestedPermissions ?: return emptyList()
        val flags = pkg.requestedPermissionsFlags ?: return emptyList()

        val granted = mutableListOf<String>()
        for (i in requested.indices) {
            val perm = requested[i]
            val flag = flags.getOrNull(i) ?: 0
            if ((flag and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                granted.add(perm)
            }
        }
        return granted
    }
}