# Project Blackbox: Functional Integration Patterns
**Status:** Patterns — Verified from Decompiled Code  
**Sources:** AnyClaw 2.1.565 + Kai 9000 3.0.0 + ADT 0.948  
**Location:** `/home/Ryuu/Project-Blackbox-worktree/`

---

## 1. Unified Runtime Service

Replace AnyClaw `GatewayService`, Kai `DaemonService`, and ADT `LlamaService` with one `BlackboxRuntimeService`.

### Wake Lock Strategy (from AnyClaw GatewayService)
- Active: 10 seconds
- Charging: infinite
- Synchronized guard to prevent leaks

### Watchdog Pattern (from AnyClaw GatewayWatchdogReceiver)
- Boot receiver starts service
- Watchdog receiver monitors service health
- Recovery worker restarts on failure

### Service Subsystem
```
BlackboxRuntimeService
├── AI Runtime: LiteRtLmWorkerService + LlamaClientService + AiToolServerService
├── Media: LlamaCallService + LiveTranslatorService + WhisperService
├── Image/Video: StableDiffusionService + OnnxImageGenerationService + VideoGenService
├── Audio/Speech: OnnxTtsGenerationService
├── ZIM/Wiki: KiwixService + ZimShareService
├── Tama: AgentForegroundService + AdventureForegroundService + TamaArtworkGenerationService
├── ML: QuadtrixTrainingService + KnowledgeBaseIndexingService + DeepResearchService
├── Tools: AiToolServerService + FileServerService + ModelShareService
└── Utility: DownloadService + SDEnvironmentService
```

---

## 2. Chat System (from Kai RemoteDataRepository)

### Core Repository
```kotlin
class BlackboxRepository {
    private val remoteDataRepository: RemoteDataRepository
    private val conversationStorage: ConversationStorage
    private val toolExecutor: ToolExecutor
    private val mcpServerManager: McpServerManager
    private val memoryStore: MemoryStore
    private val taskStore: TaskStore
    private val heartbeatManager: HeartbeatManager
    private val emailStore: EmailStore
    private val smsStore: SmsStore
    private val notificationStore: NotificationStore
    private val localInferenceEngine: LocalInferenceEngine
    private val sandboxController: SandboxController
}
```

### Chat Flow
1. User message → `RemoteDataRepository.ask()`
2. Provider selection → `Service` enum (26 providers)
3. Tool loop → `ToolExecutor.executeTool()` with bailout strategy
4. MCP tools → `McpServerManager.callTool()`
5. Local inference → `LiteRTInferenceEngine.chat()`
6. Dynamic UI → `KaiUiParser` + `KaiUiRenderer`

---

## 3. Tool Execution (from Kai ToolExecutor)

```kotlin
class BlackboxToolExecutor {
    suspend fun executeTool(toolName: String, argsJson: String, context: String): String {
        return when (toolName) {
            "mcp_call" -> mcpManager.callTool(...)
            "shell" -> prootSupervisor.executeShell(...)
            "file_read" -> prootSupervisor.readFile(...)
            "file_write" -> prootSupervisor.writeFile(...)
            "sms_send" -> messagingManager.sendSms(...)
            "email_send" -> messagingManager.sendEmail(...)
            "calendar_read" -> messagingManager.readCalendar(...)
            "discord_send" -> bridgeManager.sendMessage("discord", ...)
            "telegram_send" -> bridgeManager.sendMessage("telegram", ...)
            "whatsapp_send" -> bridgeManager.sendMessage("whatsapp", ...)
            "local_inference" -> localInferenceEngine.run(...)
            else -> error("Unknown tool: $toolName")
        }
    }
}
```

---

## 4. Proot Bridge Protocol (from AnyClaw DeviceBridge)

### File-based IPC
- Request: `bridgeDir/cmd-{uuid}.req`
- Response: `bridgeDir/cmd-{uuid}.resp`
- Bridge dir: `files/bridge/`

### Command Handler
```kotlin
class BridgeCommandHandler {
    fun handle(cmd: String, args: JSONArray): JSONObject {
        return when (cmd) {
            "start-activity", "start" -> handleAmStart(args)
            "broadcast" -> handleAmBroadcast(args)
            "intent" -> performIntentStart(args)
            "clipboardRead" -> handleClipboardRead()
            "clipboardWrite" -> handleClipboardWrite(args)
            "sensorRead" -> handleSensorRead(args)
            "locationSet" -> handleLocationSet(args)
            "cameraIntent" -> handleCameraIntent()
            "audioFocus" -> handleAudioFocus(args)
            "smsSend" -> handleSmsSend(args)
            "smsRead" -> handleSmsRead(args)
            "emailSend" -> handleEmailSend(args)
            "emailRead" -> handleEmailRead(args)
            "calendarRead" -> handleCalendarRead(args)
            "calendarWrite" -> handleCalendarWrite(args)
            "notificationRead" -> handleNotificationRead()
            "shellExec" -> handleShellExec(args)
            "fileRead" -> handleFileRead(args)
            "fileWrite" -> handleFileWrite(args)
            "fileList" -> handleFileList(args)
            "mcpCall" -> handleMcpCall(args)
            "modelDownload" -> handleModelDownload(args)
            "inferenceRun" -> handleInferenceRun(args)
            else -> error("unknown command: $cmd")
        }
    }
}
```

---

## 5. Settings Migration

### From AnyClaw SharedPreferences
- `api_key` → `auth.api_key`
- `api_provider` → `ai.provider`
- `selected_model` → `ai.selected_model`
- `discord_enabled` → `bridge.discord.enabled`
- `discord_bot_token` → `bridge.discord.token` (encrypted)
- `telegram_enabled` → `bridge.telegram.enabled`
- `telegram_bot_token` → `bridge.telegram.token` (encrypted)
- `whatsapp_enabled` → `bridge.whatsapp.enabled`
- `auto_start_codex_on_boot` → `runtime.auto_start_codex`
- `auto_start_openclaw_on_boot` → `runtime.auto_start_openclaw`
- `auto_start_sshd` → `runtime.auto_start_sshd`
- `setup_complete` → `app.setup_complete`
- `onboarding_complete` → `app.onboarding_complete`
- `custom_web_view_url` → `ai.custom_web_view_url`
- `brave_search_api_key` → `ai.brave_search_api_key`
- `app_language_tag` → `app.language`

### From Kai AppSettings
- Already in structured format, merge into unified `BlackboxSettings`
- Migrations handled by `AppSettingsMigrationsKt`

---

## 6. Boot Sequence

```
1. BootReceiver.onReceive(BOOT_COMPLETED)
   └── startForegroundService(BlackboxRuntimeService)

2. BlackboxRuntimeService.onCreate()
   ├── Create foreground notification
   ├── Initialize ProotSupervisor
   ├── Initialize BridgeServer
   ├── Initialize TaskScheduler
   ├── Initialize McpServerManager
   └── Initialize NotificationBridge

3. ProotSupervisor.start()
   ├── Check rootfs state
   ├── Start proot process
   ├── Setup bridge channel
   ├── Port forward localhost:18923
   └── Start default sessions

4. TaskScheduler.start()
   ├── Email poller
   ├── SMS poller
   ├── Heartbeat manager
   └── Cron checker (every 60s)

5. McpServerManager.start()
   ├── Connect built-in servers
   ├── Discover tools
   └── Cache tool definitions

6. Boot complete → service running, proot healthy, all managers active
```

---

## 7. Error Handling

### Proot Crash Recovery
- Exponential backoff: 1s, 2s, 4s, 8s, max 60s
- Rootfs reinstall on repeated failure
- RuntimeRecoverableFailureDetector pattern

### MCP Server Failure
- Disable after 5 consecutive failures
- Auto-retry on next app launch
- Fallback to built-in tools

### Email/SMS Polling
- Backoff on repeated failures
- Exponential delay up to 15 minutes
- Reset on success

---

## 8. Performance Budget

| Resource | Budget | Notes |
|---|---|---|
| Memory | 800–1800 MB | 3.6 GB device |
| Proot rootfs | 200 MB max | Consider OBB expansion |
| Native libs | 300 MB | Load on demand per service |
| Base APK | 150 MB | With all assets |
| Model storage | External | Download on demand |
| Battery | Optimize | Wake locks only when needed |

---

## 9. Security

- Proot isolation: no root access outside proot
- Bridge protocol: file-based IPC in app-private storage
- MCP allowlist: only approved servers
- Encrypted secrets: API keys, bot tokens
- No telemetry: strip analytics
- No license checks: strip Pairip
- No ads: strip AdManager

---

## 10. Integration Checklist

### Phase 1: Runtime
- [ ] Create BlackboxRuntimeService
- [ ] Merge proot layers
- [ ] Strip Kai license/ads/billing
- [ ] Strip AnyClaw ads/analytics/billing
- [ ] Create unified manifest
- [ ] Verify boot sequence

### Phase 2: Data Layer
- [ ] Merge Room schemas
- [ ] Migrate AnyClaw SharedPreferences
- [ ] Migrate Kai AppSettings
- [ ] Create unified chat DB
- [ ] Merge conversation models

### Phase 3: AI + MCP
- [ ] Merge provider enums
- [ ] Port McpServerManager
- [ ] Add Ollama/LiteRT providers
- [ ] Merge model catalogs
- [ ] Port ToolExecutor

### Phase 4: Chat + Tools
- [ ] Port RemoteDataRepository
- [ ] Port dynamic UI renderer
- [ ] Merge conversation flow
- [ ] Add memory injection
- [ ] Implement tool loop

### Phase 5: Background
- [ ] Merge TaskSchedulers
- [ ] Port email/SMS/notifications
- [ ] Merge heartbeat
- [ ] Port scheduled tasks

### Phase 6: Bridges + Extras
- [ ] Port messaging bridges
- [ ] Port build environment
- [ ] Port skills marketplace
- [ ] Port Splinterlands
- [ ] Port Tama system
- [ ] Port Kiwix/ZIM

### Phase 7: Testing
- [ ] Unit tests
- [ ] Integration tests
- [ ] Device testing
- [ ] APK optimization
- [ ] Update BUILD_PLAN.md
