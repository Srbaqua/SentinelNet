package com.example.sentinelai

object SuspiciousAppDetector {

    fun detect(apps: List<AppInfo>): List<Pair<AppInfo, String>> {

        val suspiciousApps = mutableListOf<Pair<AppInfo, String>>()

        for (app in apps) {

            val perms = app.permissions
            val intel = app.threatIntel

            if (intel != null && intel.status == ThreatIntelStatus.Malicious) {
                suspiciousApps.add(
                    app to "Known malware signature detected (${intel.maliciousDetections} engines)"
                )
                continue
            }

            if (intel != null && intel.status == ThreatIntelStatus.Suspicious) {
                suspiciousApps.add(
                    app to "Suspicious hash reputation (${intel.suspiciousDetections} engines)"
                )
                continue
            }

            if (
                perms.any { it.contains("LOCATION") } &&
                perms.any { it.contains("CAMERA") } &&
                perms.any { it.contains("AUDIO") }
            ) {
                suspiciousApps.add(
                    app to "Uses Location + Camera + Microphone"
                )
            }

            else if (
                perms.any { it.contains("CONTACT") } &&
                perms.any { it.contains("SMS") }
            ) {
                suspiciousApps.add(
                    app to "Accesses Contacts and SMS"
                )
            }

            else if (app.securityScore < 60) {
                suspiciousApps.add(
                    app to "Very low security score"
                )
            }
        }

        return suspiciousApps
    }
}