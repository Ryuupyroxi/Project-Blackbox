# REF-Kai9000-Structure.md
**Kai 9000 v3.0.0 — Complete Component Inventory**  
**Source:** `/home/Ryuu/Project-Blackbox-artifacts/kai9000.apk` → `/home/Ryuu/Project-Blackbox-worktree/kai9000-decompiled/`  
**Date:** 2026-08-09  
**Status:** Verified — valid APK, decompiled with apktool 2.7.0

---

## 1. Identity

| Field | Value |
|---|---|
| Package | `com.inspiredandroid.kai` |
| Version | 3.0.0 |
| Compile SDK | 37 (Android 17) |
| Min SDK | 26 |
| Main Activity | `com.inspiredandroid.kai.MainActivity` |
| Application class | `com.pairip.application.Application` |

**Note:** Uses Pairip license check framework.

---

## 2. Size Breakdown

| Component | Value |
|---|---|
| Total smali files | 19,151 |
| App-specific classes (`com/inspiredandroid/kai`) | 1,809 |
| Dex files | 3 (classes.dex, classes2.dex, classes3.dex) |

---

## 3. Permissions (13 total: 12 standard + 1 custom)

| Permission | Purpose |
|---|---|
| `INTERNET` | Network access |
| `ACCESS_LOCAL_NETWORK` | Local network |
| `READ_CALENDAR` | Calendar read |
| `WRITE_CALENDAR` | Calendar write |
| `POST_NOTIFICATIONS` | Notifications |
| `com.android.alarm.permission.SET_ALARM` | Alarm scheduling |
| `FOREGROUND_SERVICE` | Runtime service |
| `FOREGROUND_SERVICE_DATA_SYNC` | Data sync |
| `WRITE_EXTERNAL_STORAGE` | File write |
| `READ_PHONE_STATE` | Device capability |
| `READ_EXTERNAL_STORAGE` | File read |
| `com.inspiredandroid.kai.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | Internal receiver protection |
| `com.android.vending.CHECK_LICENSE` | **LICENSE CHECK — MUST STRIP** |

---

## 4. Activities

| Activity | Purpose |
|---|---|
| `com.inspiredandroid.kai.MainActivity` | Main entry point |
| `com.pairip.licensecheck.LicenseActivity` | License verification activity |

---

## 5. Services

| Service | Foreground Type | Purpose |
|---|---|---|
| `com.inspiredandroid.kai.DaemonService` | dataSync | Core foreground supervisor |
| `com.inspiredandroid.kai.inference.ModelDownloadService` | dataSync | Model download worker |
| `com.google.android.gms.metadata.ModuleDependencies` | — | Play Core metadata service (enabled=false) |

---

## 6. Providers

| Provider | Purpose |
|---|---|
| `com.inspiredandroid.kai.fileprovider` | File sharing (androidx.core.content.FileProvider) |
| `com.inspiredandroid.kai.FileKitFileProvider` | FileKit file picker |
| `com.inspiredandroid.kai.resources.AndroidContextProvider` | Compose resources |
| `com.inspiredandroid.kai.androidx-startup` | AndroidX startup |

---

## 7. Receivers

| Receiver | Purpose |
|---|---|
| `androidx.profileinstaller.ProfileInstallReceiver` | Profile install |

---

## 8. Key App Packages (1,809 classes)

### kai/
- `MainActivity` — App entry point
- `DaemonService` — Core foreground service
- `DaemonController` — Daemon state management
- `AndroidDaemonController` — Android-specific daemon control
- `AppKt` — Compose app UI
- `AppModuleKt` — DI module
- `BuildConfig` — Build config
- `BuildKonfigKt` — Build constants
- `Home` — Home screen
- `CommandHandle` — Command handling
- `ExtensionFunctionsKt` — Extensions
- `Version` — Version info

### kai/build/
- `BuildAgent` — Build agent abstraction
- `BuildAgents` — Agent registry
- `BuildEnvironmentState` — State: NotInstalled/Installing/Ready
- `BuildStep` — Build step enum
- `BuildSystemInfo` — System info
- `BuildTerminalSession` — Terminal session
- `KaiBuildState` — Build state management
- `TerminalInputView` — Terminal input
- `TerminalKeyboard_androidKt` — Keyboard handling

### kai/build/runtime/
- `BuildEnvironmentManager` — Proot environment lifecycle
- `BuildFileBrowser` — File browser operations
- `BuildPaths` — Path management
- `BuildProotExecutor` — Proot command execution
- `BuildProotHandle` — Proot process handle
- `BuildSession` — Session management
- `DebianRootfsInstaller` — Rootfs installation
- `DebianRootfsInstallerKt` — Installer extensions
- `ProotResult` — Command result model
- `TarExtractor` — Tar extraction
- `TarExtractorKt` — Tar extensions

### kai/build/terminal/
- `TerminalCell` — Terminal cell rendering
- `TerminalKeyEncoder` — Key encoding
- `TerminalKey` — Key definitions
- `TerminalModifiers` — Modifier keys
- `TerminalMouseEncoder` — Mouse encoding
- `TerminalMouseEncoding` — Mouse encoding modes
- `TerminalMouseState` — Mouse state
- `TerminalMouseTracking` — Mouse tracking
- `TerminalScreen` — Terminal screen buffer
- `TerminalScreen$Companion` — Terminal companion
- `TerminalSnapshot` — Screen snapshot
- `TerminalSnapshotKt` — Snapshot extensions
- `VtParser` — VT100 parser
- `VtParser$State` — Parser state enum

### kai/data/
- `AppSettings` — Unified settings
- `AppSettingsKt` — Settings extensions
- `AppSettingsMigrationsKt` — Settings migrations
- `AppSettingsServiceKt` — Settings service
- `AppSettingsImportExportKt` — Import/export
- `AssistantTurn` — AI turn model
- `Attachment` — File attachment
- `BailoutReason` — Tool loop bailout reasons
- `ChatPromptRuntimeContext` — Prompt context
- `ChatPromptUiMode` — UI mode
- `ChatSystemPromptBuilderKt` — System prompt builder
- `Conversation` — Conversation model
- `Conversation$Message` — Message model
- `ConversationIdContextKt` — Conversation ID context
- `ConversationIdElement` — Conversation ID element
- `ConversationPersistence` — Persistence interface
- `ConversationPersistence_androidKt` — Android persistence
- `ConversationPersistenceKt` — Persistence extensions
- `ConversationsData` — Conversations wrapper
- `ConversationStorage` — Storage interface
- `ConversationStorage_androidKt` — Android storage
- `ConversationStorageKt` — Storage extensions
- `CronExpression` — Cron parsing
- `CronExpressionKt` — Cron extensions
- `CuratedModelInfo` — Curated model info
- `DataRepository` — Repository interface
- `DataRepository$DefaultImpls` — Default implementations
- `EmailAccount` — Email account model
- `EmailAccountSummary` — Account summary
- `EmailMessage` — Email message model
- `EmailStore` — Email storage
- `EmailSyncState` — Sync state
- `FallbackStatus` — Fallback status
- `FileCategory` — File category enum
- `FileClassificationKt` — File classification
- `FreeMode` — Free tier mode
- `FreeProviderSuggestionsKt` — Free provider suggestions
- `FreeProviderSuggestion` — Suggestion model
- `FreeTierModels` — Free model list
- `HeartbeatConfig` — Heartbeat configuration
- `HeartbeatLogEntry` — Heartbeat log
- `HeartbeatManager` — Heartbeat execution
- `HeartbeatPendingEmail` — Pending email
- `HeartbeatPendingNotification` — Pending notification
- `HeartbeatPendingSms` — Pending SMS
- `HeartbeatPromotionCandidate` — Promotion candidate
- `HeartbeatPromptBuilderKt` — Prompt builder
- `ImportSection` — Import section
- `LoopChatResult` — Chat loop result
- `MemoryCategory` — Memory category
- `MemoryEntry` — Memory entry model
- `MemoryStore` — Long-term memory
- `ModelCapabilitiesKt` — Model capabilities
- `ModelCatalog` — Model catalog
- `ModelDefinition` — Model definition
- `ModelTransformationsKt` — Model transformations
- `NotificationRecord` — Notification record
- `NotificationStore` — Notification storage
- `NotificationSyncState` — Notification sync
- `PendingQueue` — Pending item queue
- `PendingTaskPartition` — Task partition
- `ReasoningRequestMode` — Reasoning mode
- `RemoteDataRepository` — **Core AI repository with 16+ dependencies**
- `ScheduledTask` — Scheduled task model
- `Service` — **26 AI providers enum**
- `ServiceEntry` — Service entry
- `ServiceInstance` — Service instance
- `SettingsConversationPersistence` — Settings persistence
- `SharedJsonKt` — JSON extensions
- `SmsDraft` — SMS draft model
- `SmsDraftStore` — Draft storage
- `SmsMessage` — SMS message model
- `SmsStore` — SMS storage
- `SmsSyncState` — SMS sync state
- `SqlConversationPersistence` — SQL persistence
- `SystemPromptVariant` — Prompt variant
- `TaskExecutionLogEntry` — Task log entry
- `TaskScheduler` — Background scheduler
- `TaskStatus` — Task status enum
- `TaskStore` — Task storage
- `TaskTrigger` — Task trigger enum
- `ThemeMode` — Theme mode
- `ToolExecutor` — Tool execution engine
- `ToolLoopStrategy` — Tool loop strategy
- `UiSubmission` — UI submission model

### kai/data/providers/
- `AnthropicMessagesKt` — Anthropic message formatting
- `OpenAIMessagesKt` — OpenAI message formatting

### kai/db/
- `KaiDatabase` — Room database
- `KaiDatabaseImpl` — Database implementation
- `ConversationEntity` — Conversation table
- `ConversationQueries` — Conversation queries
- `MessageEntity` — Message table

### kai/email/
- `EmailConnection` — Connection interface
- `EmailConnection_androidKt` — Android connection
- `EmailPoller` — IMAP polling
- `ImapClient` — IMAP client
- `JvmEmailConnection` — JVM connection
- `ServerAutoDetect` — Auto-detect servers
- `SmtpClient` — SMTP client

### kai/inference/
- `DevicePerformance` — Device performance class
- `DownloadedModel` — Downloaded model
- `DownloadError` — Download error
- `EngineState` — Engine state
- `ImportTarget` — Import target
- `InferenceMessage` — Inference message
- `InferencePlatform_androidKt` — Platform detection
- `InferenceTimeoutException` — Timeout exception
- `InsufficientMemoryException` — Memory exception
- `LiteRTInferenceEngine` — **Core LiteRT engine with LocalToolOpenApiAdapter**
- `LocalInferenceEngine` — Local inference interface
- `ModelCatalog` — Model catalog
- `ModelDownloadService` — Model download
- `ModelDownloader` — Download logic
- `ModelImportManager` — Model import

### kai/mcp/
- `McpClient` — MCP client
- `McpServerManager` — MCP server lifecycle
- `McpServerConfig` — Server config
- `McpTool` — Tool definition
- `McpToolMetadata` — Tool metadata
- `PopularMcpServersKt` — Built-in servers
- `PopularMcpServers` — Server list

### kai/network/
- `Requests` — HTTP client
- `dtos/anthropic/` — Anthropic DTOs
- `dtos/gemini/` — Gemini DTOs
- `dtos/openaicompatible/` — OpenAI-compatible DTOs
- `tools/` — Tool definitions

### kai/notifications/
- `NotificationListenerController` — Notification access
- `KaiNotificationListenerService` — Listener service

### kai/sandbox/
- `AndroidSandboxController` — Sandbox operations
- `SandboxController` — Controller interface
- `SandboxState` — State machine

### kai/shared/
- Shared utilities

### kai/skills/
- `SkillManager` — Skill management
- `Skill` — Skill model
- Skills marketplace integration

### kai/sms/
- `SmsPermissionController` — Permission handling
- `SmsSendPermissionController` — Send permission
- `SmsReader` — SMS reader
- `SmsSender` — SMS sender
- `SmsPoller` — SMS polling

### kai/splinterlands/
- `SplinterlandsBattleRunner` — Battle automation
- `SplinterlandsStore` — Battle state
- `SplinterlandsApi` — API client

### kai/tools/
- Tool definitions

### kai/ui/
- `AppKt` — Main app composable
- `Home` — Home screen

### kai/ui/build/
- `BuildAgent` UI
- `BuildEnvironmentManager` UI
- `BuildTerminalSession` UI
- `TerminalInputView` UI

### kai/ui/chat/
- `ChatScreenKt` — Main chat UI
- `History` — Message history
- `ConversationSummary` — Summary UI
- `ExecutingToolsState` — Tool execution UI
- `ToolCallInfo` — Tool call info
- `composables/` — Chat components

### kai/ui/components/
- Reusable UI components

### kai/ui/dynamicui/
- **Dynamic UI renderer** — 25+ node types:
  - `AccordionNode`, `AlertNode`, `AvatarNode`, `BadgeNode`
  - `BoxNode`, `ButtonNode`, `CardNode`, `CheckboxNode`
  - `ChipGroupNode`, `ChipItem`, `CodeNode`, `ColumnNode`
  - `CountdownNode`, `DividerNode`, `IconNode`, `ImageNode`
  - `ListNode`, `ProgressNode`, `QuoteNode`, `RadioGroupNode`
  - `RowNode`, `SelectNode`, `SliderNode`, `StatNode`
  - `SwitchNode`, `TabItem`, `TableNode`, `TabsNode`
  - `TextInputNode`, `TextNode`, `TextNodeStyle`
- `KaiUiParser` — JSON UI parser
- `KaiUiRendererKt` — Compose renderer
- `KaiUiTtsKt` — TTS support
- `CallbackAction`, `CopyToClipboardAction`, `OpenUrlAction`, `ToggleAction`, `UiAction`

### kai/ui/icons/
- `KaiIconsKt` — Icon definitions

### kai/ui/markdown/
- Markdown renderer with math support
- `MarkdownParserKt`, `MarkdownRendererKt`
- `math/` — Math rendering (matrix, fraction, radical, script, etc.)

### kai/ui/sandbox/
- `SandboxFileBrowserScreenKt` — File browser UI
- `SandboxPackagesScreenKt` — Packages UI
- `SandboxFileBrowserViewModel` — File browser VM
- `SandboxPackagesViewModel` — Packages VM
- `SandboxSessionViewModel` — Session VM
- `EditorState` — Editor state machine
- `FileBrowserUiState` — File browser state
- `PackageEntry` — Package entry
- `PackagesUiState` — Packages state
- `RenameState` — Rename state
- `SessionTab` — Session tab
- `SnackbarMessage` — Snackbar messages

### kai/ui/settings/
- `SettingsScreenKt` — Main settings UI
- `SettingsViewModel` — Settings VM with 50+ actions
- `AgentSettingsKt` — Agent settings
- `GeneralSettingsKt` — General settings
- `HeartbeatSectionKt` — Heartbeat settings
- `McpSectionKt` — MCP settings
- `ModelSelectionSheetKt` — Model selector
- `SandboxSettingsKt` — Sandbox settings
- `ServicesSettingsKt` — Services settings
- `SkillsSectionKt` — Skills settings
- `IntegrationsSettingsKt` — Integrations
- `ExportImportSectionKt` — Import/export
- `SplinterlandsComposablesKt` — Splinterlands settings
- `TerminalSheetKt` — Terminal sheet
- `ToolsSettingsKt` — Tools settings
- `PendingDeletion` — Deletion queue
- `SettingsActions` — Settings actions
- `SettingsModel` — Settings model
- `SettingsTab` — Tab enum
- `SettingsUiState` — UI state
- `ConnectionStatus` — Connection status
- `McpConnectionStatus` — MCP connection status
- `McpServerUiState` — MCP server UI state
- `SplinterlandsAccountUiState` — Splinterlands account UI
- `SplinterlandsAddStatus` — Add account status
- `SplinterlandsUiState` — Splinterlands UI
- `SplinterlandsViewModel` — Splinterlands VM
- `SandboxViewModel` — Sandbox VM
- `SandboxUiState` — Sandbox UI state
- `TerminalColors` — Terminal color scheme
- `AnsiParserKt` — ANSI parser
- `AnsiState` — ANSI state

---

## 9. Service Enum (30 Providers — Verified)

| Provider | Purpose |
|---|---|
| `OpenAI` | OpenAI API |
| `Anthropic` | Claude API |
| `Gemini` | Google Gemini |
| `OpenRouter` | OpenRouter aggregator |
| `OpenAICompatible` | Custom OpenAI-compatible |
| `OllamaCloud` | Ollama cloud |
| `LiteRT` | On-device LiteRT |
| `DeepSeek` | DeepSeek API |
| `Groq` | Groq API |
| `Mistral` | Mistral API |
| `Moonshot` | Moonshot API |
| `Nvidia` | NVIDIA API |
| `Together` | Together AI |
| `FireworksAI` | Fireworks AI |
| `Perplexity` | Perplexity API |
| `XAI` | xAI/Grok |
| `Venice` | Venice API |
| `Cerebras` | Cerebras API |
| `AtlasCloud` | Atlas Cloud |
| `DeepInfra` | DeepInfra API |
| `Minimax` | MiniMax API |
| `LongCat` | LongCat API |
| `AiHubMix` | AI Hub Mix |
| `AIHorde` | AI Horde |
| `PublicAI` | Public AI |
| `OpenCode` | OpenCode |
| `Zai` | Zai API |
| `ZaiCodingPlan` | Zai coding plan |
| `Free` | Free tier |
| `HuggingFace` | HuggingFace Inference API |

---

## 10. Core Data Models (Verified)

### Conversation Model
```kotlin
data class Conversation(
    val id: ConversationId,
    val title: String,
    val systemPrompt: String?,
    val provider: Service,
    val modelId: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class Conversation$Message(
    val id: String,
    val role: Role, // USER, ASSISTANT, SYSTEM, TOOL
    val content: String,
    val attachments: List<Attachment>,
    val timestamp: Long
)
```

### DataRepository Dependencies (Verified)
```kotlin
class RemoteDataRepository(
    val requests: Requests,
    val appSettings: AppSettings,
    val conversationStorage: ConversationStorage,
    val toolExecutor: ToolExecutor,
    val memoryStore: MemoryStore,
    val taskStore: TaskStore,
    val heartbeatManager: HeartbeatManager,
    val emailStore: EmailStore,
    val emailPoller: EmailPoller,
    val smsStore: SmsStore,
    val smsPoller: SmsPoller,
    val smsReader: SmsReader,
    val smsSender: SmsSender,
    val smsPermissionController: SmsPermissionController,
    val smsSendPermissionController: SmsSendPermissionController,
    val smsDraftStore: SmsDraftStore,
    val notificationStore: NotificationStore,
    val notificationListenerController: NotificationListenerController,
    val mcpServerManager: McpServerManager,
    val skillManager: SkillManager,
    val sandboxController: SandboxController,
    val localInferenceEngine: LocalInferenceEngine,
    val prettyJson: Json,
    val modelsByInstance: Map<ServiceInstance, List<ModelDefinition>>,
    val chatHistory: MutableStateFlow<List<AssistantTurn>>,
    val currentConversationId: MutableStateFlow<ConversationId?>,
    val fallbackStatus: MutableStateFlow<FallbackStatus>,
    val savedConversations: StateFlow<List<Conversation>>,
    val localToolDescriptionJsonCache: Map<String, String>,
    val createSkillId: String
)
```

---

## 11. Key Methods (Verified)

### ToolExecutor
- `executeTool(toolName, argsJson, context)` — Execute tool by name
- `getToolDisplayName(toolName)` — Get human-readable tool name
- Built-in tools: `shell`, `file_read`, `file_write`, `mcp_call`, etc.

### TaskScheduler
- `checkNewEmails` — Email polling task
- `runHeartbeat` — Heartbeat execution
- `handleTaskCompletion` — Task completion handler
- `handleTaskFailure` — Task failure handler
- `start` — Start scheduler loop

### RemoteDataRepository
- `ask` — Main chat request
- `askInternal` — Internal ask
- `askSilently` — Silent ask
- `askWithService` — Provider-specific ask
- `askWithTools` — Tool-enabled ask
- `askWithLocalEngine` — Local inference ask
- `executeToolCallsInParallel` — Parallel tool execution
- `connectMcpServer` — MCP server connection
- `browseSkillMarketplaces` — Skill discovery
- `installBrowsedSkill` — Install skill
- `installGitHubSkill` — Install from GitHub
- `validateConnection` — Provider validation
- `retryApiCall` — Retry logic
- `compactHistoryIfNeeded` — Context compaction

### McpServerManager
- Server lifecycle management
- Tool discovery
- Tool execution

### LiteRTInferenceEngine
- `chat` — Chat with local model
- `initialize` — Model initialization
- `release` — Resource cleanup
- `releaseInBackground` — Async cleanup
- `scheduleIdleRelease` — Idle cleanup
- `deleteModel` — Model deletion
- `importModel` — Model import
- `scanImportedModels` — Model scanning
- `LocalToolOpenApiAdapter` — Tool calling adapter

---

## 12. Dynamic UI System (Verified)

25+ node types for AI-generated UI:
- Text, Button, Image, Code, Card, List, Accordion, Tabs
- Slider, Checkbox, RadioGroup, TextInput, Select
- Progress, Countdown, Alert, Avatar, Badge, Icon
- Quote, Table, Column, Row, Box, Divider
- ChipGroup, ChipItem, Switch

---

## 13. Splinterlands Integration (Verified)

- `SplinterlandsBattleRunner` — Battle automation
- `SplinterlandsStore` — Battle state
- `SplinterlandsApi` — API client
- `SplinterlandsViewModel` — Settings VM
- Battle log, account management, team selection

---

## 14. License Check (CRITICAL — Must Strip)

| Component | Action |
|---|---|
| `com.pairip.licensecheck.LicenseActivity` | STRIP |
| `com.pairip.licensecheck.LicenseContentProvider` | STRIP |
| `com.pairip.application.Application` | STRIP |
| `com.android.vending.CHECK_LICENSE` permission | STRIP |
| Pairip license client classes (30 smali files) | STRIP |
| Play Core activities (`com.google.android.play.core.common.PlayCoreDialogWrapperActivity`) | STRIP |

**License check is active and must be removed for unified build.**

---

## 15. Strippable Components

| Component | Action |
|---|---|
| Pairip license framework | STRIP |
| `com.android.vending.CHECK_LICENSE` | STRIP |
| Play Core components | STRIP |
| Google Play Services metadata | STRIP |

---

## 16. Integration Points

### DaemonService
- Core foreground service
- Absorb into unified BlackboxRuntimeService

### ModelDownloadService
- Model download worker
- Merge with ADT's download service

### LiteRTInferenceEngine
- On-device LLM inference
- Merge with ADT's LiteRT + Ollama

### McpServerManager
- MCP server lifecycle
- Keep as-is, extend with ADT AiToolServer

### EmailPoller/ImapClient/SmtpClient
- Full email stack
- Keep as-is

### SmsReader/SmsSender/SmsPoller
- Full SMS stack
- Keep as-is

### NotificationListenerController
- Notification access
- Keep as-is

### TaskScheduler
- Background task execution
- Merge with ADT scheduled tasks

### HeartbeatManager
- Heartbeat with notifications/email/SMS
- Keep as-is

### BuildEnvironmentManager
- Proot environment
- Merge with AnyClaw's SetupManager

### Terminal emulation
- VtParser, TerminalScreen, TerminalCell
- Keep as-is

### Dynamic UI
- KaiUiParser + KaiUiRenderer
- Keep as universal response renderer

### Skills marketplace
- SkillManager, install/ browse/ uninstall
- Keep as-is

### Splinterlands
- Battle automation
- Keep as-is

---

## 17. Open Questions for Kai

1. Does `DaemonService` handle boot recovery, or is that via WorkManager only?
2. How does `AndroidSandboxController` interact with `BuildEnvironmentManager`?
3. Does `LiteRTInferenceEngine` support tool calling, or only chat completion?
