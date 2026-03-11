package com.example.sentinelai.navigation

object Routes {
    const val Home = "home"
    const val Apps = "apps"
    const val Threats = "threats"
    const val Reports = "reports"
    const val Ghost = "ghost"
    const val Settings = "settings"

    const val AppDetails = "appDetails"
    const val AppDetailsPackageArg = "packageName"
    const val AppDetailsRoute = "$AppDetails/{$AppDetailsPackageArg}"

    fun appDetails(packageName: String): String = "$AppDetails/$packageName"

    const val ScanDetails = "scanDetails"
    const val ScanDetailsIdArg = "scanId"
    const val ScanDetailsRoute = "$ScanDetails/{$ScanDetailsIdArg}"

    fun scanDetails(scanId: Long): String = "$ScanDetails/$scanId"
}

