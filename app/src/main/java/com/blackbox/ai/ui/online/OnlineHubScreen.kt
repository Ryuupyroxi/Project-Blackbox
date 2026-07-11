package com.blackbox.ai.ui.online

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blackbox.ai.core.WebViewSessionStore
import com.blackbox.ai.service.online.AiServiceManager
import com.blackbox.ai.service.online.AiServiceDefinition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineHubScreen(
    onNavigateToSettings: () -> Unit
) {
    val tabs by WebViewSessionStore.tabs.collectAsState()
    val services by AiServiceManager.availableServices.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blackbox Online") },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Service")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (tabs.isNotEmpty()) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "New Tab")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (tabs.isNotEmpty()) {
                TabRow(selectedTabIndex = selectedTab.coerceIn(0, tabs.lastIndex)) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    tab.title.ifEmpty { "Tab ${index + 1}" },
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }
            }
            if (tabs.isEmpty()) {
                ServiceListContent(services, onLaunch = { service ->
                    WebViewSessionStore.getOrCreate(service.id, service.url, service.name)
                })
            }
        }
    }

    if (showAddDialog) {
        AddServiceDialog(
            services = services,
            onDismiss = { showAddDialog = false },
            onAdd = { service ->
                WebViewSessionStore.getOrCreate(service.id, service.url, service.name)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ServiceListContent(
    services: List<AiServiceDefinition>,
    onLaunch: (AiServiceDefinition) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "AI Services",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Launch your favorite AI assistants",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(services) { service ->
            ServiceCard(service, onClick = { onLaunch(service) })
        }
    }
}

@Composable
fun ServiceCard(service: AiServiceDefinition, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(service.name, style = MaterialTheme.typography.titleMedium)
                Text(service.url, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AddServiceDialog(
    services: List<AiServiceDefinition>,
    onDismiss: () -> Unit,
    onAdd: (AiServiceDefinition) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add AI Service") },
        text = {
            LazyColumn {
                items(services) { service ->
                    TextButton(onClick = { onAdd(service) }) {
                        Text(service.name)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
