package com.example.sentinelai

object RiskAnalyzer {

    private val highRiskPermissions = listOf(
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.SEND_SMS",
        "android.permission.READ_CONTACTS",
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_FINE_LOCATION"
    )

    private val mediumRiskPermissions = listOf(
        "android.permission.CAMERA",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE"
    )

    fun calculateRiskScore(permissions: List<String>): Int {

        var score = 100

        for (perm in permissions) {

            if (highRiskPermissions.contains(perm)) {
                score -= 10
            }

            if (mediumRiskPermissions.contains(perm)) {
                score -= 5
            }
        }

        if (score < 0) score = 0

        return score
    }

    fun detectRisks(permissions: List<String>): List<String> {

        val risks = mutableSetOf<String>()

        for (perm in permissions) {

            if (highRiskPermissions.contains(perm)) {
                risks.add("${PermissionFormatter.format(perm)} may expose sensitive data")
                risks.add("${PermissionFormatter.format(perm)} may pose privacy risks")
            }

            if (mediumRiskPermissions.contains(perm)) {
                risks.add("Medium Risk Permission: ${PermissionFormatter.format(perm)}")
            }
        }

        return risks.toList()
    }
}