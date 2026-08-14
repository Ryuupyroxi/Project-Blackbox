package com.blackbox.ai.ui.agent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.blackbox.ai.R
import com.blackbox.ai.BlackboxApplication
import com.blackbox.ai.agent.runtime.AgentRuntimeManager
import com.blackbox.ai.agent.runtime.EmbeddedRuntimeManager
import com.blackbox.ai.agent.voice.BlackboxVoice
import com.blackbox.ai.agent.workspace.AgentWorkspace
import com.blackbox.ai.agent.workspace.FeatureAccessStore
import com.blackbox.ai.agent.workspace.WorkspaceChannel
import com.blackbox.ai.agent.workspace.WorkspaceStore
import com.blackbox.ai.engine.ChatChannel
import com.blackbox.ai.engine.ChatChannelClient
import com.blackbox.ai.engine.EngineKeysStore
import com.blackbox.ai.engine.enabledChannels
import com.blackbox.ai.service.AssistantDaemonService
import com.blackbox.ai.ui.navigation.Screen
import kotlinx.coroutines.launch

/**
 * Agents tab landing. One place to:
 *  - manage agent workspaces (add/switch, per-workspace channel)
 *  - configure unified engine channels (local server or any API key)
 *  - run coding agents in the local Termux/Ubuntu runtime
 *  - control the Kai-style assistant daemon and its feature access
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentHubScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keys = remember { EngineKeysStore(context) }
    val workspaceStore = remember { WorkspaceStore(context) }
    val featureAccess = remember { FeatureAccessStore(context) }

    var workspaces by remember { mutableStateOf(workspaceStore.list()) }
    var activeWorkspaceId by remember { mutableStateOf(workspaceStore.active().id) }

    var showAddWorkspace by remember { mutableStateOf(false) }
    var newWorkspaceName by remember { mutableStateOf("") }
    var newWorkspaceChannel by remember { mutableStateOf(WorkspaceChannel.LOCAL) }

    // Channel config state
    var openAiKey by remember { mutableStateOf(keys.getOpenAiKey()) }
    var openAiModel by remember { mutableStateOf(keys.getOpenAiModel()) }
    var openAiEnabled by remember { mutableStateOf(keys.isOpenAiEnabled()) }
    var openRouterKey by remember { mutableStateOf(keys.getOpenRouterKey()) }
    var openRouterModel by remember { mutableStateOf(keys.getOpenRouterModel()) }
    var openRouterEnabled by remember { mutableStateOf(keys.isOpenRouterEnabled()) }
    var anthropicKey by remember { mutableStateOf(keys.getAnthropicKey()) }
    var anthropicModel by remember { mutableStateOf(keys.getAnthropicModel()) }
    var anthropicEnabled by remember { mutableStateOf(keys.isAnthropicEnabled()) }
    var localUrl by remember { mutableStateOf(keys.getLocalBaseUrl()) }
    var localModel by remember { mutableStateOf(keys.getLocalModel()) }
    var localEnabled by remember { mutableStateOf(keys.isLocalEnabled()) }

    // Termux channel state
    var termuxHost by remember { mutableStateOf(keys.getTermuxHost()) }
    var termuxPort by remember { mutableStateOf(keys.getTermuxPort().toString()) }
    var termuxUser by remember { mutableStateOf(keys.getTermuxUser()) }
    var termuxPassword by remember { mutableStateOf(keys.getTermuxPassword()) }
    var termuxStatus by remember { mutableStateOf("Not connected") }

    // Embedded LOCAL runtime state (zero-Termux channel)
    var localRuntimeStatus by remember { mutableStateOf("Not installed") }
    var localRuntimeOutput by remember { mutableStateOf("") }
    var localRuntimeBusy by remember { mutableStateOf(false) }
    val localConsole by EmbeddedRuntimeManager.console.collectAsState()
    var loginApiKey by remember { mutableStateOf(keys.getOpenAiKey()) }
    // True only while the embedded LOCAL codex server is actually running, so
    // Quick Chat routes through it only when it is up (and falls back otherwise).
    var localRuntimeReady by remember { mutableStateOf(false) }

    // Active workspace decides execution routing (LOCAL/SSH/KAI).
    val activeWorkspace = remember(activeWorkspaceId) {
        workspaces.firstOrNull { it.id == activeWorkspaceId } ?: workspaces.first()
    }
    suspend fun refreshLocalRuntime() {
        val st = EmbeddedRuntimeManager.status(context)
        localRuntimeStatus = when {
            st.ready -> "Installed"
            st.bootstrap -> "Installed (partial)"
            else -> "Not installed"
        }
        localRuntimeReady = st.serverRunning
    }
    fun engineChannelsForWorkspace(): List<ChatChannel> {
        var base = enabledChannels(keys)
        if (activeWorkspace.channel == WorkspaceChannel.LOCAL && localRuntimeReady) {
            // Prefer the embedded LOCAL codex server (OpenAI-compatible) when it is up.
            val local = ChatChannel.LocalOpenAi("http://127.0.0.1:18923", keys.getLocalModel())
            base = if (base.isEmpty()) listOf(local) else listOf(local) + base.filterNot { it is ChatChannel.LocalOpenAi }
        }
        return base
    }

    // Quick chat state
    var chatPrompt by remember { mutableStateOf("") }
    var chatResponse by remember { mutableStateOf("") }
    var chatLoading by remember { mutableStateOf(false) }
    var chatError by remember { mutableStateOf<String?>(null) }

    // Voice round-trip state (Kai-style TTS/STT)
    val voice = remember { (context.applicationContext as BlackboxApplication).getVoice() }
    var voiceReady by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    var speakReplies by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        voice.init { ready -> voiceReady = ready }
        onDispose { voice.shutdown() }
    }
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            listening = true
            voice.startListening(
                onResult = { text ->
                    chatPrompt = chatPrompt.trim().let { if (it.isBlank()) text else "$it $text" }
                    listening = false
                },
                onError = { err ->
                    chatError = err
                    listening = false
                }
            )
        } else {
            chatError = "Microphone permission needed for voice input"
        }
    }
    fun startListening() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            listening = true
            voice.startListening(
                onResult = { text ->
                    chatPrompt = chatPrompt.trim().let { if (it.isBlank()) text else "$it $text" }
                    listening = false
                },
                onError = { err ->
                    chatError = err
                    listening = false
                }
            )
        } else {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Assistant daemon state
    var daemonEnabled by remember { mutableStateOf(AssistantDaemonService.isRunning(context)) }
    val grants = featureAccess.granted()
    var grantedFeatures by remember { mutableStateOf(grants) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agents") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.Dashboard.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                    } }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "All agent features, one place. Every feature can run via the local server, the Termux/SSH runtime, or any API key you add below.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Prompts the user to add an API key or a local llama server when none
            // is configured, so the Kai-style assistant has a backend to talk to.
            if (enabledChannels(keys).isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Connect an AI backend to get started",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Add an API key below, or point Blackbox at a local llama.cpp / Ollama server to use the assistant fully offline.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    scope.launch { listState.animateScrollToItem(6) }
                                }) { Text("Add API key") }
                                OutlinedButton(onClick = {
                                    scope.launch { listState.animateScrollToItem(6) }
                                }) { Text("Link local server") }
                            }
                        }
                    }
                }
            }

            // ── Runtime mode ──────────────────────────────────────────────
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
                                selected = keys.getRuntimeMode() == EngineKeysStore.RUNTIME_MODE_TERMUX,
                                onClick = {
                                    keys.setRuntimeMode(EngineKeysStore.RUNTIME_MODE_TERMUX)
                                },
                                label = { Text(stringResource(R.string.runtime_mode_termux)) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = keys.getRuntimeMode() == EngineKeysStore.RUNTIME_MODE_PROOT,
                                onClick = {
                                    keys.setRuntimeMode(EngineKeysStore.RUNTIME_MODE_PROOT)
                                },
                                label = { Text(stringResource(R.string.runtime_mode_proot)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ── Workspaces ────────────────────────────────────────────────
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Workspaces", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            TextButton(onClick = {
                                newWorkspaceName = ""
                                newWorkspaceChannel = WorkspaceChannel.LOCAL
                                showAddWorkspace = true
                            }) { Text("+ Add") }
                        }
                        workspaces.forEach { ws ->
                            WorkspaceRow(
                                workspace = ws,
                                isActive = ws.id == activeWorkspaceId,
                                onSwitch = {
                                    workspaceStore.switchTo(ws)
                                    activeWorkspaceId = ws.id
                                    com.blackbox.ai.service.AgentService.setCurrentProjectFolder(ws.folder)
                                    scope.launch {
                                        AgentRuntimeManager.ensureWorkspaceFolder(context, ws.folder)
                                    }
                                },
                                onDelete = {
                                    workspaceStore.delete(ws.id)
                                    workspaces = workspaceStore.list()
                                    if (ws.id == activeWorkspaceId) {
                                        activeWorkspaceId = workspaceStore.active().id
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Channel: how this workspace executes (Local / SSH / Kai).",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Runtime agents ──────────────────────────────────────────
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Coding Runtimes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Run Hermes, Codex CLI and OpenClaw inside the local Termux/Ubuntu environment (Blackbox-style SSH channel).",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { navController.navigate(Screen.AgentRuntime.route) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Runtime Agents")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { navController.navigate(Screen.Agent.route) }, modifier = Modifier.weight(1f)) {
                                Text("Coding Agent Chat")
                            }
                            OutlinedButton(onClick = { navController.navigate(Screen.Termux.route) }, modifier = Modifier.weight(1f)) {
                                Text("Termux Tools")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { navController.navigate(Screen.AgentWorkspace.route) }, modifier = Modifier.weight(1f)) {
                                Text("Workspace Files")
                            }
                            OutlinedButton(onClick = { navController.navigate(Screen.AgentImporter.route) }, modifier = Modifier.weight(1f)) {
                                Text("Import Agents")
                            }
                        }
                    }
                }
            }

            // ── Local Termux channel ────────────────────────────────────
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Local Termux / SSH Channel", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Optional external SSH channel to a separate Termux/Ubuntu proot outside this app. " +
                            "For agent coding in the app's own runtime, use the Embedded LOCAL Runtime above.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = termuxHost,
                            onValueChange = { termuxHost = it },
                            label = { Text("Host") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = termuxPort,
                                onValueChange = { termuxPort = it.filter(Char::isDigit) },
                                label = { Text("Port") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = termuxUser,
                                onValueChange = { termuxUser = it },
                                label = { Text("User") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = termuxPassword,
                            onValueChange = { termuxPassword = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(termuxStatus, fontSize = 12.sp, color = Color.Gray)
                            Button(onClick = {
                                keys.setTermux(termuxHost, termuxPort.toIntOrNull() ?: 8025, termuxUser, termuxPassword)
                                scope.launch {
                                    termuxStatus = AgentRuntimeManager.connect(context)
                                        .fold(onSuccess = { it }, onFailure = { "Error: ${it.message}" })
                                }
                            }) { Text("Connect") }
                        }
                    }
                }
            }

            // ── Embedded LOCAL runtime (zero-Termux channel) ───────────
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Embedded LOCAL Runtime", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Zero-setup coding runtime: Termux bootstrap, Node.js, and Codex CLI extracted into the app sandbox. No Termux or SSH server needed.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LaunchedEffect(Unit) {
                            refreshLocalRuntime()
                        }
                        Text("Status: $localRuntimeStatus", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        localRuntimeBusy = true
                                        localRuntimeOutput = ""
                                        val result = EmbeddedRuntimeManager.install(context)
                                        localRuntimeOutput = result.getOrElse { it.message ?: "Install failed" }
                                        if (result.isSuccess) refreshLocalRuntime() else localRuntimeStatus = "Install failed"
                                        localRuntimeBusy = false
                                    }
                                },
                                enabled = !localRuntimeBusy,
                                modifier = Modifier.weight(1f)
                            ) { Text("Install") }
                            Button(
                                onClick = {
                                    scope.launch {
                                        localRuntimeBusy = true
                                        localRuntimeOutput = ""
                                        localRuntimeOutput = EmbeddedRuntimeManager.startServer(context)
                                            .getOrElse { it.message ?: "Start failed" }
                                        localRuntimeStatus = if (localRuntimeOutput.startsWith("Server running")) "Running" else localRuntimeOutput
                                        localRuntimeReady = localRuntimeOutput.startsWith("Server running")
                                        localRuntimeBusy = false
                                    }
                                },
                                enabled = !localRuntimeBusy,
                                modifier = Modifier.weight(1f)
                            ) { Text("Start") }
                            Button(
                                onClick = {
                                    scope.launch {
                                        localRuntimeBusy = true
                                        localRuntimeOutput = EmbeddedRuntimeManager.stop(context)
                                            .getOrElse { it.message ?: "Stop failed" }
                                        localRuntimeStatus = "Stopped"
                                        localRuntimeReady = false
                                        localRuntimeBusy = false
                                    }
                                },
                                enabled = !localRuntimeBusy,
                                modifier = Modifier.weight(1f)
                            ) { Text("Stop") }
                            Button(
                                onClick = {
                                    scope.launch {
                                        localRuntimeBusy = true
                                        localRuntimeOutput = ""
                                        localRuntimeOutput = EmbeddedRuntimeManager.healthCheck(context)
                                            .getOrElse { it.message ?: "Health failed" }
                                        localRuntimeStatus = localRuntimeOutput
                                        localRuntimeReady = localRuntimeOutput.startsWith("Health check passed")
                                        localRuntimeBusy = false
                                    }
                                },
                                enabled = !localRuntimeBusy,
                                modifier = Modifier.weight(1f)
                            ) { Text("Health") }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // OpenClaw gateway / Control UI + login (LOCAL channel full UI)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        localRuntimeBusy = true
                                        localRuntimeOutput = ""
                                        localRuntimeOutput = EmbeddedRuntimeManager.startOpenClaw(context)
                                            .getOrElse { it.message ?: "OpenClaw start failed" }
                                        localRuntimeStatus = localRuntimeOutput
                                        localRuntimeBusy = false
                                    }
                                },
                                enabled = !localRuntimeBusy,
                                modifier = Modifier.weight(1f)
                            ) { Text("OpenClaw") }
                            Button(
                                onClick = {
                                    navController.navigate(
                                        Screen.TermuxWebView.createRoute(
                                            EmbeddedRuntimeManager.controlUiUrl,
                                            "OpenClaw Control UI",
                                            "openclaw-control"
                                        )
                                    )
                                },
                                enabled = !localRuntimeBusy,
                                modifier = Modifier.weight(1f)
                            ) { Text("Web UI") }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = loginApiKey,
                                onValueChange = { loginApiKey = it },
                                label = { Text("Codex login key") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    scope.launch {
                                        localRuntimeBusy = true
                                        localRuntimeOutput = ""
                                        localRuntimeOutput = EmbeddedRuntimeManager.login(context, loginApiKey)
                                            .getOrElse { it.message ?: "Login failed" }
                                        localRuntimeStatus = localRuntimeOutput
                                        localRuntimeBusy = false
                                    }
                                },
                                enabled = !localRuntimeBusy && loginApiKey.isNotBlank()
                            ) { Text("Login") }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Live progress stream
                        if (localConsole.isNotBlank() || localRuntimeOutput.isNotBlank()) {
                            Text(
                                (if (localConsole.isNotBlank()) localConsole else localRuntimeOutput).takeLast(3200),
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }
            }

            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("API Key Channels", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Add any API key and every agent feature can run through it.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ChannelFields(
                            title = "OpenAI",
                            keyValue = openAiKey,
                            onKeyChange = { openAiKey = it },
                            modelValue = openAiModel,
                            onModelChange = { openAiModel = it },
                            enabled = openAiEnabled,
                            onEnabledChange = { openAiEnabled = it },
                            onSave = {
                                keys.setOpenAi(openAiKey, openAiModel, openAiEnabled)
                                chatError = null
                            }
                        )
                        ChannelFields(
                            title = "OpenRouter",
                            keyValue = openRouterKey,
                            onKeyChange = { openRouterKey = it },
                            modelValue = openRouterModel,
                            onModelChange = { openRouterModel = it },
                            enabled = openRouterEnabled,
                            onEnabledChange = { openRouterEnabled = it },
                            onSave = {
                                keys.setOpenRouter(openRouterKey, openRouterModel, openRouterEnabled)
                                chatError = null
                            }
                        )
                        ChannelFields(
                            title = "Anthropic",
                            keyValue = anthropicKey,
                            onKeyChange = { anthropicKey = it },
                            modelValue = anthropicModel,
                            onModelChange = { anthropicModel = it },
                            enabled = anthropicEnabled,
                            onEnabledChange = { anthropicEnabled = it },
                            onSave = {
                                keys.setAnthropic(anthropicKey, anthropicModel, anthropicEnabled)
                                chatError = null
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(checked = localEnabled, onCheckedChange = { localEnabled = it })
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Local OpenAI-compatible server", fontWeight = FontWeight.Bold)
                                Text(
                                    "llama.cpp / Ollama on this device",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = localUrl,
                                onValueChange = { localUrl = it },
                                label = { Text("Base URL") },
                                modifier = Modifier.weight(2f)
                            )
                            OutlinedTextField(
                                value = localModel,
                                onValueChange = { localModel = it },
                                label = { Text("Model") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            keys.setLocal(localUrl, localModel, localEnabled)
                            chatError = null
                        }) { Text("Save Local Channel") }
                    }
                }
            }

            // ── Quick agent chat ────────────────────────────────────────
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Quick Agent Chat", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Runs through the first enabled channel (local → OpenRouter → OpenAI → Anthropic).",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = chatPrompt,
                            onValueChange = { chatPrompt = it },
                            label = { Text("Ask the agent anything") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (listening) {
                                            voice.stopListening()
                                            listening = false
                                        } else {
                                            startListening()
                                        }
                                    },
                                    enabled = voiceReady && !chatLoading
                                ) {
                                    Icon(
                                        if (listening) Icons.Default.Stop else Icons.Default.Mic,
                                        contentDescription = if (listening) "Stop listening" else "Voice input",
                                        tint = if (listening) MaterialTheme.colorScheme.error else LocalContentColor.current
                                    )
                                }
                                Text(
                                    if (listening) "Listening…" else "Voice input",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(checked = speakReplies, onCheckedChange = { speakReplies = it })
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Speak replies", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                            chatError?.let {
                                Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Button(
                                onClick = {
                                    val channels = engineChannelsForWorkspace()
                                    if (channels.isEmpty()) {
                                        chatError = "No channel enabled. Save an API key or enable the local server."
                                        return@Button
                                    }
                                    chatError = null
                                    chatLoading = true
                                    chatResponse = ""
                                    scope.launch {
                                        var lastError: String? = null
                                        var done = false
                                        for (channel in channels) {
                                            if (done) break
                                            val result = ChatChannelClient.chat(
                                                channel,
                                                listOf("user" to chatPrompt)
                                            )
                                            result.fold(
                                                onSuccess = {
                                                    chatResponse = "[via ${channel.label}]\n\n$it"
                                                    if (speakReplies && voiceReady) voice.speak(it)
                                                    done = true
                                                },
                                                onFailure = { e ->
                                                    lastError = "${channel.label}: ${e.message}"
                                                }
                                            )
                                        }
                                        if (!done) {
                                            chatResponse = ""
                                            chatError = lastError ?: "All channels failed"
                                        }
                                        chatLoading = false
                                    }
                                },
                                enabled = chatPrompt.isNotBlank() && !chatLoading
                            ) {
                                if (chatLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                } else {
                                    Icon(Icons.Default.Send, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Send")
                                }
                            }
                            }
                        }
                        if (chatResponse.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    chatResponse,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                        .verticalScroll(rememberScrollState())
                                        .heightIn(max = 320.dp),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── Assistant daemon (Kai-style) ────────────────────────────
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Assistant (Kai-style)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Keeps a background daemon alive. To open the agent chat with the system assist gesture (long-press home), set Blackbox as your default assistant in Android settings.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = daemonEnabled,
                                onCheckedChange = { on ->
                                    daemonEnabled = on
                                    if (on) AssistantDaemonService.start(context) else AssistantDaemonService.stop(context)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (daemonEnabled) "Daemon running" else "Daemon off", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Assistant feature access (requires your authorization)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        FeatureAccessStore.allFeatures().forEach { (feature, label) ->
                            val granted = feature in grantedFeatures
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = granted,
                                    onCheckedChange = { on ->
                                        if (on) featureAccess.grant(feature) else featureAccess.revoke(feature)
                                        grantedFeatures = featureAccess.granted()
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showAddWorkspace) {
        AlertDialog(
            onDismissRequest = { showAddWorkspace = false },
            title = { Text("Add Workspace") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newWorkspaceName,
                        onValueChange = { newWorkspaceName = it },
                        label = { Text("Workspace name") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Execution channel", fontWeight = FontWeight.Bold)
                    WorkspaceChannel.values().forEach { channel ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = newWorkspaceChannel == channel,
                                onClick = { newWorkspaceChannel = channel }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(channel.label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val ws = workspaceStore.add(newWorkspaceName, newWorkspaceName, newWorkspaceChannel)
                    workspaces = workspaceStore.list()
                    activeWorkspaceId = ws.id
                    workspaceStore.switchTo(ws)
                    com.blackbox.ai.service.AgentService.setCurrentProjectFolder(ws.folder)
                    showAddWorkspace = false
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showAddWorkspace = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun WorkspaceRow(
    workspace: AgentWorkspace,
    isActive: Boolean,
    onSwitch: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isActive) Icons.Default.CheckCircle else Icons.Default.Folder,
                null,
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(workspace.name, fontWeight = FontWeight.Bold)
                Text(
                    "/workspace/${workspace.folder} · ${workspace.channel.label}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isActive) {
                TextButton(onClick = onSwitch) { Text("Switch") }
            } else {
                Text("Active", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete")
            }
        }
    }
}

@Composable
private fun ChannelFields(
    title: String,
    keyValue: String,
    onKeyChange: (String) -> Unit,
    modelValue: String,
    onModelChange: (String) -> Unit,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onSave) { Text("Save") }
        }
        OutlinedTextField(
            value = keyValue,
            onValueChange = onKeyChange,
            label = { Text("API key") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = modelValue,
            onValueChange = onModelChange,
            label = { Text("Model") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
