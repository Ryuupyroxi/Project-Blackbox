package com.blackbox.ui.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    onNavigateToChat: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToModules: () -> Unit = {},
    onNavigateToTerminal: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Blackbox Dashboard", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.fillMaxWidth())
        Card(modifier = Modifier.fillMaxWidth(), onClick = onNavigateToChat) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Unified Chat", style = MaterialTheme.typography.titleLarge)
                Text(text = "Kai, AnyClaw, ADT providers in one interface", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Card(modifier = Modifier.fillMaxWidth(), onClick = onNavigateToModules) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Modules", style = MaterialTheme.typography.titleLarge)
                Text(text = "Manage AnyClaw, Kai, ADT runtime services", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Card(modifier = Modifier.fillMaxWidth(), onClick = onNavigateToTerminal) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Terminal / Proot", style = MaterialTheme.typography.titleLarge)
                Text(text = "Proot shell, SSH, sandbox sessions", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Card(modifier = Modifier.fillMaxWidth(), onClick = onNavigateToSettings) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Settings", style = MaterialTheme.typography.titleLarge)
                Text(text = "Providers, models, bridges, permissions", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
