package com.blackbox.ai.ui.agent

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.blackbox.ai.agent.runtime.AgentCatalog
import com.blackbox.ai.R
import com.blackbox.ai.agent.runtime.AgentHealthState
import com.blackbox.ai.agent.runtime.AgentRuntimeManager
import com.blackbox.ai.agent.runtime.RuntimeAgent
import com.blackbox.ai.engine.EngineKeysStore
import com.blackbox.ai.service.SSHService
import com.blackbox.ai.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentRuntimeScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    val connected by SSHService.isConnected.collectAsState()
    var status by remember { mutableStateOf("Connect to the local Termux/Ubuntu runtime to begin.") }
    var expandedLog by remember { mutableStateOf<String?>(null) }
    var showSetupGuide by remember { mutableStateOf(false) }
    val console by AgentRuntimeManager.console.collectAsState()
    val isBusy by AgentRuntimeManager.isBusy.collectAsState()

    val keys = remember { EngineKeysStore(context) }
    var runtimeMode by remember { mutableStateOf(keys.getRuntimeMode()) }
    var prootInstalled by remember { mutableStateOf(keys.isProotInstalled()) }

    val isProot = runtimeMode == EngineKeysStore.RUNTIME_MODE_PROOT
    val agentHealth by AgentRuntimeManager.agentHealth.collectAsState()

    fun copyToClipboard(text: String) {
        clipboard.setPrimaryClip(ClipData.newPlainText("SSH Setup", text))
    }

    suspend fun ensureConnection(): Boolean {
        return if (isProot) {
            if (prootInstalled) {
                status = "Linux proot runtime is ready."
                true
            } else {
                status = "Linux proot rootfs not installed yet. Tap Install below."
                false
            }
        } else {
            if (SSHService.isConnected.value) {
                true
            } else {
                AgentRuntimeManager.connect(context)
                    .fold(onSuccess = {
                        status = it
                        true
                    }, onFailure = {
                        status = "Auto-connect failed: ${it.message}"
                        false
                    })
            }
        }
    }

    // Periodic health refresh every 30 seconds
    LaunchedEffect(isProot, prootInstalled) {
        while (true) {
            delay(30_000L)
            if (isProot && prootInstalled) {
                AgentRuntimeManager.refreshAllAgentHealth(context)
            } else if (!isProot && SSHService.isConnected.value) {
                AgentRuntimeManager.refreshAllAgentHealth(context)
            }
        }
    }

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
            // ── Runtime mode switch + connection card ─────────────────────
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.runtime_mode_switch_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.runtime_mode_switch_desc),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !isProot,
                                onClick = {
                                    runtimeMode = EngineKeysStore.RUNTIME_MODE_TERMUX
                                    keys.setRuntimeMode(runtimeMode)
                                },
                                label = { Text(stringResource(R.string.runtime_mode_termux)) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = isProot,
                                onClick = {
                                    runtimeMode = EngineKeysStore.RUNTIME_MODE_PROOT
                                    keys.setRuntimeMode(runtimeMode)
                                },
                                label = { Text(stringResource(R.string.runtime_mode_proot)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isProot) stringResource(R.string.runtime_proot_note) else stringResource(R.string.runtime_termux_note),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isProot) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "OpenClaw, Hermes, Codex, OpenCode can run here.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Status indicator row
                        val statusColor = when {
                            isProot && prootInstalled -> Color(0xFF4CAF50)
                            !isProot && connected -> Color(0xFF4CAF50)
                            isProot && !prootInstalled -> Color(0xFFFFC107)
                            else -> Color(0xFFFF5722)
                        }
                        val statusText = when {
                            isProot && prootInstalled -> "Proot ready"
                            !isProot && connected -> "SSH connected"
                            isProot && !prootInstalled -> "Proot not installed"
                            else -> "Not connected"
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(statusText, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = statusColor)
                            if (status.isNotBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(status, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Action buttons — always visible, never squeezed
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isProot) {
                                if (!prootInstalled) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                status = "Installing Linux proot rootfs…"
                                                val result = runCatching {
                                                    val pm = com.blackbox.ai.data.proot.ProotManager(context)
                                                    var ok = false
                                                    runCatching { ok = pm.downloadRootfs { } }.getOrElse { }
                                                    if (ok) runCatching { ok = pm.extractRootfs { } }.getOrElse { }
                                                    ok
                                                }.getOrDefault(false)
                                                prootInstalled = result
                                                keys.setProotInstalled(result)
                                                status = if (result) "Proot rootfs installed and ready" else "Proot install failed — retry or check storage"
                                            }
                                        },
                                        enabled = !isBusy,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.runtime_proot_install))
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                AgentRuntimeManager.connect(context)
                                                    .onSuccess { status = it }
                                                    .onFailure { status = "Error: ${it.message}" }
                                            }
                                        },
                                        enabled = !isBusy,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reconnect")
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            AgentRuntimeManager.connect(context)
                                                .onSuccess { status = it }
                                                .onFailure { status = "Error: ${it.message}" }
                                        }
                                    },
                                    enabled = !isBusy,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        if (connected) Icons.Default.Refresh else Icons.Default.Link,
                                        null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (connected) "Reconnect" else "Connect")
                                }
                            }
                            OutlinedButton(
                                onClick = { showSetupGuide = !showSetupGuide },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Setup Guide")
                            }
                        }
                    }
                }
            }

            // ── Setup guide (collapsible) ─────────────────────────────────
            if (showSetupGuide) {
                item {
                    SshSetupGuideCard(
                        isProot = isProot,
                        onCopy = { copyToClipboard(it) }
                    )
                }
            }

            items(AgentCatalog.all, key = { it.id }) { agent ->
                val health = agentHealth[agent.id] ?: AgentHealthState.UNKNOWN
                RuntimeAgentCard(
                    agent = agent,
                    isBusy = isBusy,
                    healthState = health,
                    onInstall = {
                        scope.launch {
                            if (!ensureConnection()) {
                                return@launch
                            }
                            status = AgentRuntimeManager.install(context, agent)
                                .fold(onSuccess = { "Installed ${agent.name}" }, onFailure = { "Install failed: ${it.message}" })
                        }
                    },
                    onStart = {
                        scope.launch {
                            if (!ensureConnection()) {
                                return@launch
                            }
                            status = AgentRuntimeManager.start(context, agent)
                                .fold(onSuccess = { "Started ${agent.name}" }, onFailure = { "Start failed: ${it.message}" })
                        }
                    },
                    onStop = {
                        scope.launch {
                            if (!ensureConnection()) {
                                return@launch
                            }
                            status = AgentRuntimeManager.stop(context, agent)
                                .fold(onSuccess = { "Stopped ${agent.name}" }, onFailure = { "Stop failed: ${it.message}" })
                        }
                    },
                    onHealth = {
                        scope.launch {
                            if (!ensureConnection()) {
                                return@launch
                            }
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
    healthState: AgentHealthState,
    onInstall: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onHealth: () -> Unit,
    onLogs: () -> Unit,
    onOpenWeb: (() -> Unit)?
) {
    val statusColor = when {
        healthState.healthy -> Color(0xFF4CAF50)
        healthState.running -> Color(0xFFFFC107)
        healthState.installed -> Color(0xFF2196F3)
        healthState.lastCheck > 0L -> Color(0xFFFF5722)
        else -> Color.Gray
    }

    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(agent.emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(agent.name, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = statusColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = healthState.statusLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
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
                OutlinedButton(
                    onClick = onInstall,
                    enabled = !isBusy && !healthState.installed,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (healthState.installed) "Installed" else "Install")
                }
                Button(
                    onClick = onStart,
                    enabled = !isBusy && !healthState.running,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (healthState.running) "Running" else "Start")
                }
                OutlinedButton(
                    onClick = onStop,
                    enabled = !isBusy && healthState.running,
                    modifier = Modifier.weight(1f)
                ) {
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

@Composable
private fun SshSetupGuideCard(isProot: Boolean, onCopy: (String) -> Unit) {
    val sshPort = 8025
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                if (isProot) "Proot Setup Guide" else "SSH Server Setup Guide",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                if (isProot)
                    "Set up the embedded Linux proot environment. Install rootfs, then agents run inside it."
                else
                    "Set up a Termux/Ubuntu SSH server so Blackbox can connect and manage agents.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (isProot) {
                Text("Step 1: Tap \"Install Proot Rootfs\" above", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("This downloads and extracts the Ubuntu rootfs automatically.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Step 2: Tap \"Reconnect\" to initialize the runtime", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Step 3: Install agents from the cards below (Install → Start → Health)", fontSize = 13.sp)
            } else {
                val steps = listOf(
                    "Step 1: Open Termux (install from F-Droid or GitHub)" to null,
                    "Step 2: Install SSH server" to "pkg install openssh-server -y",
                    "Step 3: Configure SSH for password login" to "mkdir -p /run/sshd && chmod 755 /run/sshd\nsed -i '/^#\\?PermitRootLogin/d' /etc/ssh/sshd_config\nsed -i '/^#\\?PasswordAuthentication/d' /etc/ssh/sshd_config\nsed -i '/^#\\?Port /d' /etc/ssh/sshd_config\nprintf 'Port $sshPort\\nPermitRootLogin yes\\nPasswordAuthentication yes\\n' >> /etc/ssh/sshd_config",
                    "Step 4: Set a password" to "passwd",
                    "Step 5: Start the SSH server" to "sshd",
                    "Step 6: Come back here, enter your credentials, and tap Connect" to null
                )
                steps.forEach { (label, command) ->
                    Text(label, fontSize = 13.sp)
                    if (command != null) {
                        SetupCommandBlock(command = command, onCopy = onCopy)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("One-liner (paste into Termux):", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                SetupCommandBlock(
                    command = "pkg install openssh-server -y && mkdir -p /run/sshd && chmod 755 /run/sshd && sed -i '/^#\\?PermitRootLogin/d' /etc/ssh/sshd_config && sed -i '/^#\\?PasswordAuthentication/d' /etc/ssh/sshd_config && sed -i '/^#\\?Port /d' /etc/ssh/sshd_config && printf 'Port $sshPort\\nPermitRootLogin yes\\nPasswordAuthentication yes\\n' >> /etc/ssh/sshd_config && echo 'Now run: passwd'",
                    onCopy = onCopy
                )
                Text(
                    "Default port: $sshPort · Then run: passwd && sshd",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun SetupCommandBlock(command: String, onCopy: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                command,
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            IconButton(
                onClick = { onCopy(command) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
