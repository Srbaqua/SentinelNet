package com.example.sentinelai

object RiskAnalyzer {

    private val highRiskPermissions = setOf(
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.SEND_SMS",
        "android.permission.READ_CONTACTS",
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_FINE_LOCATION"
    )

    private val mediumRiskPermissions = setOf(
        "android.permission.CAMERA",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE"
    )

    private val internetPermission =
        "android.permission.INTERNET"

    fun calculateRiskScore(permissions: List<String>): Int {

        val perms = permissions.toSet()

        var score = 100

        var permissionRisk = 0
        var interactionRisk = 0
        var densityRisk = 0

        perms.forEach { perm ->

            if (perm in highRiskPermissions)
                permissionRisk += 8

            if (perm in mediumRiskPermissions)
                permissionRisk += 4
        }

        if (perms.size > 15)
            densityRisk += 10
        else if (perms.size > 10)
            densityRisk += 6
        else if (perms.size > 6)
            densityRisk += 3

        fun has(p: String) = perms.contains(p)

        if (has("android.permission.CAMERA") &&
            has(internetPermission))
        {
            interactionRisk += 10
        }

        if (has("android.permission.ACCESS_FINE_LOCATION") &&
            has(internetPermission))
        {
            interactionRisk += 10
        }

        if (has("android.permission.READ_CONTACTS") &&
            has(internetPermission))
        {
            interactionRisk += 12
        }

        if (has("android.permission.RECORD_AUDIO") &&
            has(internetPermission))
        {
            interactionRisk += 10
        }

        if (has("android.permission.READ_SMS") &&
            has(internetPermission))
        {
            interactionRisk += 15
        }

        score -= permissionRisk
        score -= interactionRisk
        score -= densityRisk

        return score.coerceIn(0, 100)
    }

    fun detectRisks(permissions: List<String>): List<String> {

        val perms = permissions.toSet()

        val risks = mutableSetOf<String>()

        perms.forEach { perm ->

            if (perm in highRiskPermissions) {

                risks.add(
                    "${PermissionFormatter.format(perm)} can expose sensitive user data"
                )
            }

            if (perm in mediumRiskPermissions) {

                risks.add(
                    "Moderate risk permission: ${PermissionFormatter.format(perm)}"
                )
            }
        }

        fun has(p: String) = perms.contains(p)

        if (has("android.permission.CAMERA") &&
            has("android.permission.INTERNET"))
        {
            risks.add(
                "Camera + Internet may allow media upload to remote servers"
            )
        }

        if (has("android.permission.ACCESS_FINE_LOCATION") &&
            has("android.permission.INTERNET"))
        {
            risks.add(
                "Location + Internet may enable tracking"
            )
        }

        if (has("android.permission.READ_CONTACTS") &&
            has("android.permission.INTERNET"))
        {
            risks.add(
                "Contacts + Internet may allow contact harvesting"
            )
        }

        if (has("android.permission.READ_SMS") &&
            has("android.permission.INTERNET"))
        {
            risks.add(
                "SMS + Internet may expose verification codes"
            )
        }

        return risks.toList()
    }
}