package com.ganesh.freefirewall

import android.app.*
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import java.io.FileInputStream
import java.util.concurrent.atomic.AtomicBoolean

class FirewallVpnService : VpnService() {
    private var vpn: ParcelFileDescriptor? = null
    private var worker: Thread? = null
    private val running = AtomicBoolean(false)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopFirewall()
            else -> startFirewall()
        }
        return START_STICKY
    }

    private fun startFirewall() {
        if (running.get()) return
        createChannel()
        startForeground(NOTIFICATION_ID, NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(com.ganesh.freefirewall.R.drawable.ic_shield)
            .setContentTitle("Free Firewall is active")
            .setContentText("Blocked application traffic is being discarded locally")
            .setOngoing(true).build())

        val blocked = RulesRepository(this).blockedPackages()
        if (blocked.isEmpty()) { stopSelf(); return }
        val builder = Builder().setSession("Free Firewall").setMtu(1500)
            .addAddress("10.88.0.1", 32).addRoute("0.0.0.0", 0)
            .addAddress("fd00:88::1", 128).addRoute("::", 0)
        blocked.forEach { pkg -> runCatching { builder.addAllowedApplication(pkg) } }
        vpn = builder.establish() ?: run { stopSelf(); return }
        running.set(true)
        worker = Thread {
            val buffer = ByteArray(32767)
            FileInputStream(vpn!!.fileDescriptor).use { input ->
                while (running.get()) {
                    val read = runCatching { input.read(buffer) }.getOrDefault(-1)
                    if (read < 0) break
                    // Deliberately discard packets. Only blocked apps are routed into this local VPN.
                }
            }
        }.apply { name = "FirewallPacketDropper"; start() }
    }

    private fun stopFirewall() {
        running.set(false)
        runCatching { vpn?.close() }
        vpn = null
        worker?.interrupt(); worker = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() { stopFirewall(); super.onDestroy() }
    override fun onRevoke() { stopFirewall(); super.onRevoke() }

    private fun createChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Firewall status", NotificationManager.IMPORTANCE_LOW))
    }

    companion object {
        const val ACTION_START = "com.ganesh.freefirewall.START"
        const val ACTION_STOP = "com.ganesh.freefirewall.STOP"
        private const val CHANNEL_ID = "firewall_status"
        private const val NOTIFICATION_ID = 417
    }
}
