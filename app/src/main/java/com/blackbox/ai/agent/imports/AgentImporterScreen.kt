package com.blackbox.ai.agent.imports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentImporterScreen(navController: NavController) {
    var imported by remember { mutableStateOf(false) }
    val sampleAgents = listOf(
        Triple("Manager", "Pipeline manager, task dispatcher", "📋"),
        Triple("Coder", "Kotlin implementer, PR creator", "💻"),
        Triple("Debugger", "QA, bug isolation", "🐛"),
        Triple("Designer", "UI/UX architect, Material You", "🎨"),
        Triple("Accountant", "Revenue generation (post-launch)", "💰")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agent Importer") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!imported) {
                        IconButton(onClick = { imported = true }) {
                            Icon(Icons.Default.Add, "Import All")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Import agents from SID-OS context",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(sampleAgents) { (name, role, icon) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(icon, style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                role,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (imported) {
                            Spacer(modifier = Modifier.weight(1f))
                            AssistChip(
                                onClick = {},
                                label = { Text("Imported") }
                            )
                        }
                    }
                }
            }
        }
    }
}
