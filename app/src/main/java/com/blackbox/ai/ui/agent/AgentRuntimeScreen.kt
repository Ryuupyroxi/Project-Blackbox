package com.blackbox.ai.ui.agent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.blackbox.ai.agent.runtime.AgentCatalog
import com.blackbox.ai.agent.runtime.AgentRuntimeManager
import com.blackbox.ai.agent.runtime.RuntimeAgent
import com.blackbox.ai.service.SSHService
import com.blackbox.ai.ui.navigation.Screen
import kotlinx.coroutines.launch

/**
 * Local runtime agent manager (termux-agents-hub style): install, start, stop,
 * health-check and view logs for Hermes, Codex CLI and OpenClaw, all running
 * inside the local Termux/Ubuntu SSH channel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentRuntimeScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val connected by SSHService.isConnected.collectAsState()
    var status by remember { mutableStateOf("Connect to the local Termux/Ubuntu runtime to begin.") }
    var expandedLog by remember { mutableStateOf<String?>(null) }
    val console by AgentRuntimeManager.console.collectAsState()
    val isBusy by AgentRuntimeManager.isBusy.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Runtime Agents") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Local Termux / Ubuntu runtime", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Agents run in the same environment ADT uses for its local Termux tools (SSH into the Termux-hosted Ubuntu proot).",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(status, fontSize = 12.sp, color = if (connected) Color(0xFF4CAF50) else Color.Gray)
                            Button(
                                onClick = {
                                    scope.launch {
                                        AgentRuntimeManager.connect(context)
                                            .onSuccess { status = it }
                                            .onFailure { status = "Error: ${it.message}" }
                                    }
                                },
                                enabled = !isBusy
                            ) {
                                Text(if (connected) "Reconnect" else "Connect")
                            }
                        }
                    }
                }
            }

            items(AgentCatalog.all, key = { it.id }) { agent ->
                RuntimeAgentCard(
                    agent = agent,
                    isBusy = isBusy,
                    onInstall = {
                        scope.launch {
                            status = AgentRuntimeManager.install(context, agent)
                                .fold(onSuccess = { "Installed ${agent.name}" }, onFailure = { "Install failed: ${it.message}" })
                        }
                    },
                    onStart = {
                        scope.launch {
                            status = AgentRuntimeManager.start(context, agent)
                                .fold(onSuccess = { "Started ${agent.name}" }, onFailure = { "Start failed: ${it.message}" })
                        }
                    },
                    onStop = {
                        scope.launch {
                            status = AgentRuntimeManager.stop(context, agent)
                                .fold(onSuccess = { "Stopped ${agent.name}" }, onFailure = { "Stop failed: ${it.message}" })
                        }
                    },
                    onHealth = {
                        scope.launch {
                            status = AgentRuntimeManager.health(context, agent)
                                .fold(onSuccess = { "Health ${agent.name}: ${it.trim()}" }, onFailure = { "Health: ${it.message}" })
                        }
                    },
                    onLogs = {
                        scope.launch {
                            expandedLog = AgentRuntimeManager.logTail(context, agent)
                                .fold(onSuccess = { it }, onFailure = { "No log: ${it.message}" })
                        }
                    },
                    onOpenWeb = agent.webUrl?.let { url ->
                        { navController.navigate(Screen.TermuxWebView.createRoute(url, agent.name, agent.id)) }
                    }
                )
            }

            if (console.isNotBlank()) {
                item {
                    Card {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Console", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                console,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }
            }

            expandedLog?.let { log ->
                item {
                    Card {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Agent log", fontWeight = FontWeight.Bold)
                                TextButton(onClick = { expandedLog = null }) { Text("Close") }
                            }
                            Text(
                                log,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 320.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun RuntimeAgentCard(
    agent: RuntimeAgent,
    isBusy: Boolean,
    onInstall: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onHealth: () -> Unit,
    onLogs: () -> Unit,
    onOpenWeb: (() -> Unit)?
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(agent.emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(agent.name, fontWeight = FontWeight.Bold)
                    Text(
                        agent.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (agent.port > 0) {
                    Text(":${agent.port}", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onInstall, enabled = !isBusy, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Install")
                }
                Button(onClick = onStart, enabled = !isBusy, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start")
                }
                OutlinedButton(onClick = onStop, enabled = !isBusy, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Stop, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stop")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onHealth, enabled = !isBusy, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.MonitorHeart, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Health")
                }
                OutlinedButton(onClick = onLogs, enabled = !isBusy, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.List, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Logs")
                }
                if (onOpenWeb != null) {
                    OutlinedButton(onClick = onOpenWeb, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Language, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Web UI")
                    }
                }
            }
        }
    }
}
