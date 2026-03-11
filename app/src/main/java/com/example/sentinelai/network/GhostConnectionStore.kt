package com.example.sentinelai.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GhostConnectionEvent(
    val destinationIp: String,
    val destinationPort: Int,
    val protocol: String,
    val hostname: String?,
    val timestampMs: Long,
)

object GhostConnectionStore {
    private const val MAX_EVENTS = 120

    private val _events = MutableStateFlow<List<GhostConnectionEvent>>(emptyList())
    val events: StateFlow<List<GhostConnectionEvent>> = _events.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun markRunning(running: Boolean) {
        _isRunning.value = running
    }

    fun push(event: GhostConnectionEvent) {
        val next = buildList {
            add(event)
            addAll(_events.value)
        }.take(MAX_EVENTS)
        _events.value = next
    }

    fun clear() {
        _events.value = emptyList()
    }

    fun prettyTime(timestampMs: Long): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestampMs))
    }
}
