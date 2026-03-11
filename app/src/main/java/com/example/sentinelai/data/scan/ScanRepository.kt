package com.example.sentinelai.data.scan

import android.content.Context
import kotlinx.coroutines.flow.Flow

class ScanRepository(private val dao: ScanDao) {
    val scans: Flow<List<ScanEntity>> = dao.observeAll()

    suspend fun insert(scan: ScanEntity): Long = dao.insert(scan)

    suspend fun getById(id: Long): ScanEntity? = dao.getById(id)

    companion object {
        fun from(context: Context): ScanRepository {
            val db = ScanDatabase.get(context)
            return ScanRepository(db.scanDao())
        }
    }
}

