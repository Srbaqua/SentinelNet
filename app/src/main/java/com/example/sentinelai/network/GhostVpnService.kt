package com.example.sentinelai.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.example.sentinelai.R
import java.io.FileInputStream
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class GhostVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var readerThread: Thread? = null
    private val isRunning = AtomicBoolean(false)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopMonitoring()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopMonitoring()
        super.onDestroy()
    }

    private fun startMonitoring() {
        if (isRunning.get()) return

        val builder = Builder()
            .setSession("SentinelAI Ghost Monitor")
            .addAddress("10.10.0.2", 32)
            .addRoute("0.0.0.0", 0)

        vpnInterface = builder.establish() ?: return

        startInForeground()
        isRunning.set(true)
        GhostConnectionStore.markRunning(true)

        readerThread = Thread {
            readPackets(vpnInterface!!)
        }.apply {
            name = "GhostVpnReader"
            start()
        }
    }

    private fun stopMonitoring() {
        isRunning.set(false)
        GhostConnectionStore.markRunning(false)

        readerThread?.interrupt()
        readerThread = null

        try {
            vpnInterface?.close()
        } catch (_: Exception) {
        }
        vpnInterface = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun readPackets(pfd: ParcelFileDescriptor) {
        FileInputStream(pfd.fileDescriptor).use { input ->
            val buffer = ByteArray(32767)
            while (isRunning.get() && !Thread.currentThread().isInterrupted) {
                val length = try {
                    input.read(buffer)
                } catch (_: Exception) {
                    break
                }

                if (length <= 0) continue

                val event = parsePacket(buffer, length) ?: continue
                GhostConnectionStore.push(event)
            }
        }
    }

    private fun parsePacket(packet: ByteArray, length: Int): GhostConnectionEvent? {
        if (length < 20) return null

        val version = (packet[0].toInt() ushr 4) and 0x0F
        if (version != 4) return null

        val ihl = (packet[0].toInt() and 0x0F) * 4
        if (length < ihl + 4) return null

        val protocolByte = packet[9].toInt() and 0xFF
        val protocol = when (protocolByte) {
            6 -> "TCP"
            17 -> "UDP"
            else -> "IP"
        }

        val dstIpBytes = packet.copyOfRange(16, 20)
        val destinationIp = InetAddress.getByAddress(dstIpBytes).hostAddress ?: return null

        val destinationPort = when (protocolByte) {
            6, 17 -> {
                if (length < ihl + 4) 0 else {
                    val bb = ByteBuffer.wrap(packet, ihl + 2, 2)
                    bb.short.toInt() and 0xFFFF
                }
            }
            else -> 0
        }

        val hostname = try {
            val host = InetAddress.getByAddress(dstIpBytes).hostName
            if (host == destinationIp) null else host
        } catch (_: Exception) {
            null
        }

        return GhostConnectionEvent(
            destinationIp = destinationIp,
            destinationPort = destinationPort,
            protocol = protocol,
            hostname = hostname,
            timestampMs = System.currentTimeMillis(),
        )
    }

    private fun startInForeground() {
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Ghost Connections",
                    NotificationManager.IMPORTANCE_LOW,
                )
            )
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SentinelAI Ghost Monitor")
            .setContentText("Monitoring outbound connection metadata")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    companion object {
        const val ACTION_START = "com.example.sentinelai.GHOST_START"
        const val ACTION_STOP = "com.example.sentinelai.GHOST_STOP"

        private const val CHANNEL_ID = "ghost_monitor_channel"
        private const val NOTIFICATION_ID = 2026
    }
}
