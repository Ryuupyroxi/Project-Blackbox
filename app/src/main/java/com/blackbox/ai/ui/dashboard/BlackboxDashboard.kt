package com.blackbox.ai.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blackbox.ai.core.ProcessManager
import com.blackbox.ai.core.AppSettings
import com.blackbox.ai.toolkit.DeviceToolkit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlackboxDashboard(
    onNavigateToOnline: () -> Unit,
    onNavigateToAgent: () -> Unit,
    onNavigateToToolkit: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val processes by ProcessManager.processes.collectAsState()
    val tools by DeviceToolkit(null).tools.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blackbox") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Welcome to Blackbox",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your ultimate AI workstation",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item {
                SectionHeader(title = "AI Engines")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DashboardCard(
                        title = "Local AI",
                        subtitle = "${processes.size} engines",
                        icon = Icons.Default.Memory,
                        modifier = Modifier.weight(1f),
                        onClick = {}
                    )
                    DashboardCard(
                        title = "Online Hub",
                        subtitle = "AI services",
                        icon = Icons.Default.Cloud,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToOnline
                    )
                }
            }
            
            item {
                SectionHeader(title = "Tools & Agents")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DashboardCard(
                        title = "Screen Agent",
                        subtitle = "AI automation",
                        icon = Icons.Default.PhoneAndroid,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAgent
                    )
                    DashboardCard(
                        title = "Device Toolkit",
                        subtitle = "${tools.flatMap { it.tools }.size} tools",
                        icon = Icons.Default.Build,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToToolkit
                    )
                }
            }
            
            item {
                SectionHeader(title = "System Status")
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Running Processes", style = MaterialTheme.typography.bodyMedium)
                            Text("${processes.size}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tracker Filter", style = MaterialTheme.typography.bodyMedium)
                            Text("Active", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun DashboardCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
