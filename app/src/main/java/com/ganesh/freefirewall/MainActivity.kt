package com.ganesh.freefirewall

import android.Manifest
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    private val vm by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF176B4D))) { FirewallScreen() } }
    }

    @Composable private fun FirewallScreen() {
        val apps by vm.apps.collectAsStateWithLifecycle()
        val blocked by vm.blocked.collectAsStateWithLifecycle()
        var query by remember { mutableStateOf("") }
        var active by remember { mutableStateOf(false) }
        val vpnPermission = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) { startFirewall(); active = true }
        }
        val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
        val shown = apps.filter { it.label.contains(query, true) || it.packageName.contains(query, true) }

        Scaffold(topBar = { @OptIn(ExperimentalMaterial3Api::class) TopAppBar(title = { Text("Free Firewall") }) }) { pad ->
            Column(Modifier.padding(pad).padding(16.dp).fillMaxSize()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(if (active) "Firewall active" else "Firewall stopped", fontWeight = FontWeight.Bold)
                            Text("${blocked.size} app(s) blocked")
                        }
                        Switch(checked = active, onCheckedChange = { enabled ->
                            if (enabled) {
                                if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                val prepare = VpnService.prepare(this@MainActivity)
                                if (prepare != null) vpnPermission.launch(prepare) else { startFirewall(); active = true }
                            } else { stopFirewall(); active = false }
                        })
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(query, { query = it }, label = { Text("Search installed apps") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text("Turn Block on for an app, then restart the firewall to apply changed rules.", style = MaterialTheme.typography.bodySmall)
                LazyColumn(Modifier.fillMaxSize()) {
                    items(shown, key = { it.packageName }) { app ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text(app.label, fontWeight = FontWeight.Medium); Text(app.packageName, style = MaterialTheme.typography.bodySmall) }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Switch(checked = app.packageName in blocked, onCheckedChange = { vm.setBlocked(app.packageName, it) })
                                Text(if (app.packageName in blocked) "Blocked" else "Allowed", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    private fun startFirewall() { ContextCompat.startForegroundService(this, Intent(this, FirewallVpnService::class.java).setAction(FirewallVpnService.ACTION_START)) }
    private fun stopFirewall() { startService(Intent(this, FirewallVpnService::class.java).setAction(FirewallVpnService.ACTION_STOP)) }
}
