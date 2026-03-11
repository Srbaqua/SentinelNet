package com.example.sentinelai.data.scan

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampEpochMs: Long,
    val privacyScore: Int,
    val appsScanned: Int,
    val highRiskApps: Int,
    val suspiciousApps: Int,
)

