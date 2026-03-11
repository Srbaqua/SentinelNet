package com.example.sentinelai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

enum class ThreatIntelStatus {
    Clean,
    Suspicious,
    Malicious,
    NotFound,
    RateLimited,
    Error,
}

data class ThreatIntelResult(
    val sha256: String,
    val status: ThreatIntelStatus,
    val maliciousDetections: Int = 0,
    val suspiciousDetections: Int = 0,
)

class ThreatIntelScanner(private val context: Context) {

    suspend fun enrichAppsWithThreatIntel(
        apps: List<AppInfo>,
        apiKey: String,
    ): List<AppInfo> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext apps

        val pm = context.packageManager
        val enriched = mutableListOf<AppInfo>()

        for (app in apps) {
            val result = runCatching {
                val info = pm.getApplicationInfo(app.packageName, 0)
                val apkPath = info.sourceDir
                val hash = sha256ForFile(apkPath)
                lookupHash(apiKey = apiKey, sha256 = hash)
            }.getOrNull()

            if (result == null) {
                enriched += app.copy(
                    threatIntel = ThreatIntelResult(
                        sha256 = "",
                        status = ThreatIntelStatus.Error,
                    )
                )
            } else {
                enriched += app.copy(threatIntel = result)
            }

            // VirusTotal public API is heavily rate-limited.
            if (result?.status == ThreatIntelStatus.RateLimited) {
                enriched += apps.drop(enriched.size)
                break
            }
        }

        enriched
    }

    private fun sha256ForFile(path: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        BufferedInputStream(File(path).inputStream()).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString(separator = "") { "%02x".format(it) }
    }

    private fun lookupHash(apiKey: String, sha256: String): ThreatIntelResult {
        val connection = (URL("https://www.virustotal.com/api/v3/files/$sha256").openConnection() as HttpURLConnection)
        connection.requestMethod = "GET"
        connection.setRequestProperty("x-apikey", apiKey)
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        return try {
            val code = connection.responseCode
            when (code) {
                HttpURLConnection.HTTP_OK -> parseVirusTotalResponse(
                    sha256,
                    connection.inputStream.bufferedReader().use { it.readText() },
                )
                HttpURLConnection.HTTP_NOT_FOUND -> ThreatIntelResult(
                    sha256 = sha256,
                    status = ThreatIntelStatus.NotFound,
                )
                429 -> ThreatIntelResult(
                    sha256 = sha256,
                    status = ThreatIntelStatus.RateLimited,
                )
                else -> ThreatIntelResult(
                    sha256 = sha256,
                    status = ThreatIntelStatus.Error,
                )
            }
        } catch (_: Exception) {
            ThreatIntelResult(
                sha256 = sha256,
                status = ThreatIntelStatus.Error,
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun parseVirusTotalResponse(sha256: String, json: String): ThreatIntelResult {
        val root = JSONObject(json)
        val stats = root
            .optJSONObject("data")
            ?.optJSONObject("attributes")
            ?.optJSONObject("last_analysis_stats")

        val malicious = stats?.optInt("malicious") ?: 0
        val suspicious = stats?.optInt("suspicious") ?: 0

        val status = when {
            malicious > 0 -> ThreatIntelStatus.Malicious
            suspicious > 0 -> ThreatIntelStatus.Suspicious
            else -> ThreatIntelStatus.Clean
        }

        return ThreatIntelResult(
            sha256 = sha256,
            status = status,
            maliciousDetections = malicious,
            suspiciousDetections = suspicious,
        )
    }
}
