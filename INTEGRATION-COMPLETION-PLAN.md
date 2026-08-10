# Project Blackbox — Integration Completion Plan

**Status:** post-feature-audit  
**Scope:** finish AnyClaw/Kai/ADT ports, add Android Assistant layer, unify data layer, build unified UI  
**Constraint:** all code ported from decompiled smali or built as functional shells; no placeholder error screens

---

## 1. Single Data Layer

Currently each module uses its own storage mechanism. We will consolidate into one shared layer under `app/src/main/java/com/blackbox/data/`.

### 1.1 Unified Preferences / Settings
**File:** `app/src/main/java/com/blackbox/data/BlackboxPreferences.kt`  
- Port `PreferencesManager` logic from AnyClaw smali into a single `DataStore`-backed preferences facade.
- Fields to migrate from `PreferencesManager`:
  - API keys: `apiKey`, `apiProvider`, `selectedModel`, `selectedModelId`
  - Channel configs: `discordEnabled`, `discordBotToken`, `discordGuildAllowlist`, `discordRequireMention`
  - Channel configs: `telegramEnabled`, `telegramBotToken`
  - Channel configs: `whatsappEnabled`
  - OpenClaw: `openClawVersion`, `autoStartOpenClawOnBoot`, `openClawUpdatePromptSuppressedBundledVersion`
  - Codex: `codexappVersion`, `codexappBranch`, `autoStartCodexOnBoot`, `customWebViewUrl`
  - OpenAI-compatible: `openAiCompatibleBaseUrl`, `openAiCompatibleModelId`
  - Other: `braveSearchApiKey`, `setupComplete`, `onboardingComplete`, `premiumActive`, `hasRated`
  - Bridge/runtime: `gatewayWasRunning`, `lastAppOpenedAt`, `bundleUpdateFailure`, `checkLoginOnStart`
- Expose as Kotlin `Flow`/`StateFlow` so all modules observe the same source.

### 1.2 Unified Conversations / Messages
**File:** `app/src/main/java/com/blackbox/data/BlackboxRepository.kt`  
- Expand existing `BlackboxDatabase` to include:
  - `ChannelMessageEntity` — unified message table for Discord/Telegram/WhatsApp/Kai/ADT
  - `ChannelConversationEntity` — unified conversation/channel table
  - `AssistantSessionEntity` — assistant-layer session state
- DAOs: `ChannelMessageDao`, `ChannelConversationDao`, `AssistantSessionDao`
- Repository methods: `observeMessages(channelId)`, `sendMessage(channelId, text)`, `observeSessions()`, `createSession()`

### 1.3 Module-to-Data Contracts
Each module must use the shared data layer instead of rolling its own storage:
- **AnyClaw**: `PairingService`, `GatewayService`, `SetupManager` → write to `BlackboxPreferences`
- **Kai**: `FakeBlackboxRepository`, `RemoteDataRepository` → use `BlackboxDatabase`
- **ADT**: `AdtUnifiedRuntimeService`, `AgentForegroundService` → write sessions to `BlackboxDatabase`

---

## 2. Unified UI

Build a single Compose UI under `app/src/main/java/com/blackbox/ui/screen/` that all modules plug into via a shared navigation/router contract.

### 2.1 Navigation / Router
**File:** `app/src/main/java/com/blackbox/ui/BlackboxNavHost.kt`  
- Use `Accompanist` or `Navigation-Compose` with routes:
  - `dashboard` — unified home with cards for each runtime
  - `chat/{channelId}` — unified chat screen
  - `settings` — unified settings
  - `terminal` — terminal/proot shell
  - `onboarding` — first-run flow
- Each module registers its routes via `ModuleRegistry`.

### 2.2 Dashboard Screen
**File:** `app/src/main/java/com/blackbox/ui/screen/DashboardScreen.kt`  
- Cards: AnyClaw Gateway, Kai Chat, ADT Llama, ADT Whisper, ADT StableDiffusion, ZIM, Agent runtime
- Each card shows status from module `StateFlow`s and launches the appropriate screen/service.

### 2.3 Unified Chat Screen
**File:** `app/src/main/java/com/blackbox/ui/screen/chat/UnifiedChatScreen.kt`  
- Single chat UI backed by `ChannelConversationEntity` + `ChannelMessageEntity`
- Modules contribute providers through `KaiAiProvider`, `DiscordBridge`, etc.
- Message list, input, provider/model selector.

### 2.4 Settings Screen
**File:** `app/src/main/java/com/blackbox/ui/screen/settings/UnifiedSettingsScreen.kt`  
- API keys, provider config, model selection, channel enable/disable
- OpenClaw/Codex update/recovery buttons
- Permissions request flows
- Bug report dialog

### 2.5 Terminal / Proot Screen
**File:** `app/src/main/java/com/blackbox/ui/screen/terminal/UnifiedTerminalScreen.kt`  
- WebView-backed terminal from AnyClaw’s `TerminalScreen`
- Proot shell access, file browser, SSH launcher

### 2.6 Onboarding Screen
**File:** `app/src/main/java/com/blackbox/ui/screen/onboarding/UnifiedOnboardingScreen.kt`  
- First-run: setup complete flag, API key entry, provider selection, permissions
- Port `OnboardingViewModel` logic from AnyClaw smali.

---

## 3. Android Assistant Layer Integration

No original APK exposed this, but Android supports it via `VoiceInteractionService` / `Assistant` APIs. We will build a new assistant surface that routes user intent to the active module/provider.

### 3.1 Assistant Service
**File:** `app/src/main/java/com/blackbox/assistant/BlackboxAssistantService.kt`  
- Extend `VoiceInteractionService` or implement `android.service.assist.AssistStructure` callbacks
- Manifest entry with `android:voiceInteractionService` and `BIND_VOICE_INTERACTION` permission
- Capture `AssistStructure` from any app, extract text/URL, route to active provider.

### 3.2 Intent Router
**File:** `app/src/main/java/com/blackbox/assistant/AssistantIntentRouter.kt`  
- Routes incoming assistant intents to:
  - Kai chat provider (Claude/GPT/OpenAI-compatible)
  - AnyClaw Codex/OpenClaw runtime
  - ADT LlamaService / LiteRtLmWorkerService
- Selection based on user’s default provider in `BlackboxPreferences`.

### 3.3 Assistant Session Tracking
**File:** `app/src/main/java/com/blackbox/data/AssistantSessionEntity.kt` + DAO  
- Logs assistant invocations to unified DB for later review in dashboard/logs.

---

## 4. AnyClaw Missing Ports

These exist in decompiled smali but are not yet ported.

### 4.1 PreferencesManager
**File:** `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/data/PreferencesManager.kt`  
- Port key/value store from `PreferencesManager.smali`
- Delegate reads/writes to `BlackboxPreferences` via module bridge.
- Migrate existing `ChannelConfig.kt` fields into this manager.

### 4.2 Auth / OAuth
**Files:**
- `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/auth/CodexAuthWriter.kt`
- `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/auth/OpenRouterAuth.kt`
- `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/auth/AuthorizationFlow.kt`  
- Port PKCE login, token exchange, refresh flows from smali `auth/` package.
- Store tokens in `SecretStore`.

### 4.3 Codex WebView Runtime
**Files:**
- `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/CodexWebViewActivity.kt`
- `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/CodexWebViewClient.kt`
- `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/PasteAwareWebView.kt`  
- Port WebView clients, JS bridges, asset loader, file chooser from smali.
- Bind to `CodexService` for start/stop/update.

### 4.4 Hermes Web UI Runtime
**Files:**
- `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/HermesWebUiActivity.kt`
- `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/runtime/HermesWebUiController.kt`  
- Port Hermes Web UI start/stop logic from `ProcessManager$startHermesWebUi` smali.

### 4.5 Dashboard / Settings / Terminal / Onboarding UI
**Files:**
- `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/screen/dashboard/DashboardViewModel.kt`
- `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/screen/settings/SettingsViewModel.kt`
- `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/screen/terminal/TerminalViewModel.kt`
- `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/screen/onboarding/OnboardingViewModel.kt`  
- Port ViewModel logic from smali into Compose-friendly state holders.
- The actual Compose UI screens will live in `app/src/main/.../ui/screen/` and call these ViewModels.

### 4.6 Update / Recovery / Doctor
**Files:**
- `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/runtime/OpenClawDoctor.kt`
- `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/runtime/BundleUpdatePolicy.kt`
- `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/runtime/RecoveryInstaller.kt`  
- Port update check, doctor fix, bundle update, recovery install from `SetupManager`/`ProcessManager` smali.

---

## 5. Kai Missing Ports

### 5.1 Anthropic Provider + DTOs
**Files:**
- `modules/kai/src/main/java/com/blackbox/module/kai/network/AnthropicApiException.kt`
- `modules/kai/src/main/java/com/blackbox/module/kai/network/AnthropicGenericException.kt`
- `modules/kai/src/main/java/com/blackbox/module/kai/network/AnthropicInsufficientCreditsException.kt`
- `modules/kai/src/main/java/com/blackbox/module/kai/network/AnthropicInvalidApiKeyException.kt`
- `modules/kai/src/main/java/com/blackbox/module/kai/network/AnthropicOverloadedException.kt`
- `modules/kai/src/main/java/com/blackbox/module/kai/network/AnthropicRateLimitExceededException.kt`
- `modules/kai/src/main/java/com/blackbox/module/kai/network/dtos/anthropic/AnthropicChatRequestDto.kt`
- `modules/kai/src/main/java/com/blackbox/module/kai/network/dtos/anthropic/AnthropicChatResponseDto.kt`
- `modules/kai/src/main/java/com/blackbox/module/kai/network/dtos/anthropic/AnthropicModelsResponseDto.kt`
- `modules/kai/src/main/java/com/blackbox/module/kai/network/Requests.kt`
- `modules/kai/src/main/java/com/blackbox/module/kai/data/Service.kt` (add Anthropic provider)
- `modules/kai/src/main/java/com/blackbox/module/kai/data/RemoteDataRepository.kt` (extend with Anthropic handler)
- `modules/kai/src/main/java/com/blackbox/module/kai/inference/AnthropicStrategy.kt`  
- Port full Anthropic chat/tools/streaming strategy from smali `data/providers/` and `network/`.

### 5.2 Skills / Marketplaces
**Files:**
- `modules/kai/src/main/java/com/blackbox/module/kai/skills/SkillRegistry.kt`
- `modules/kai/src/main/java/com/blackbox/module/kai/skills/SkillMarketplaces.kt`
- `modules/kai/src/main/java/com/blackbox/module/kai/skills/SkillDescriptor.kt`
- `modules/kai/src/main/java/com/blackbox/module/kai/skills/SkillQuad.kt`  
- Port skill fetch/install/browse from smali `skills/`.

### 5.3 Model Catalog / Transforms
**Files:**
- `modules/kai/src/main/java/com/blackbox/module/kai/data/ModelCatalog.kt`
- `modules/kai/src/main/java/com/blackbox/module/kai/data/ModelTransformations.kt`
- `modules/kai/src/main/java/com/blackbox/module/kai/inference/LocalModelCatalog.kt`  
- Port catalog logic and transformations from smali.

### 5.4 Build Agents
**File:** `modules/kai/src/main/java/com/blackbox/module/kai/build/BuildAgents.kt`  
- Port agent metadata from smali.

### 5.5 Chat UI State + Full Chat UI
**File:** `modules/kai/src/main/java/com/blackbox/module/kai/ui/chat/ChatUiState.kt`  
- Port chat state machine from smali.
- Unified chat screen in `app/ui/screen/chat/` consumes this.

### 5.6 RemoteDataRepository Full Port
**File:** `modules/kai/src/main/java/com/blackbox/module/kai/data/RemoteDataRepository.kt`  
- Extend beyond stub to include all provider handlers from smali.

---

## 6. ADT Gaps

ADT ports are largely complete. Remaining items:

### 6.1 AdtServiceCatalog Completeness
- Verify all 33 services from original APK are represented.
- Add missing media/adventure services if found in smali.

### 6.2 Native Library Binding
**File:** `modules/adt/src/main/java/com/blackbox/module/adt/runtime/AdtNativeLoader.kt`  
- Current version extracts libs from APK; add fallback to `lib/` directory if bundled in app.
- Wire into `AdtUnifiedRuntimeService.onLoad()`.

### 6.3 Tama Widget
- If Tama classes exist in smali, port as `TamaWidgetService.kt` and `TamaPetController.kt`.

---

## 7. Cross-Cutting Concerns

### 7.1 Module Bus
**File:** `app/src/main/java/com/blackbox/module/ModuleBus.kt`  
- In-memory event bus for cross-module signaling.
- Events: `AssistantInvoke`, `ChatMessageSent`, `ServiceStateChanged`, `ModelDownloaded`.
- Modules publish/subscribe via `ModuleRegistry`.

### 7.2 Permission Gate Centralization
**File:** `app/src/main/java/com/blackbox/runtime/PermissionCoordinator.kt`  
- Centralize permission requests for all modules.
- Single runtime permission flow in UI.

### 7.3 Build Dependencies
**File:** `app/build.gradle.kts`  
- Ensure these are present:
  - `androidx.core:core-ktx`
  - `androidx.lifecycle:lifecycle-runtime-ktx`
  - `androidx.lifecycle:lifecycle-viewmodel-compose`
  - `androidx.navigation:navigation-compose`
  - `androidx.compose.ui:ui`
  - `androidx.compose.material3:material3`
  - `androidx.webkit:webkit`
  - `com.squareup.okhttp3:okhttp`
  - `org.jetbrains.kotlinx:kotlinx-coroutines-android`
  - `com.google.dagger:hilt` or manual DI
  - `androidx.room:room-ktx`
  - `androidx.datastore:datastore-preferences`
  - `androidx.security:security-crypto`

---

## 8. Execution Order

1. **Unified data layer** — `BlackboxPreferences`, expanded `BlackboxDatabase`, shared repositories
2. **Module bus + permission coordinator**
3. **AnyClaw PreferencesManager + auth** — required before UI
4. **AnyClaw WebView/terminal UI shells** — Codex, Hermes, Terminal
5. **AnyClaw dashboard/settings/onboarding ViewModels** — wire to unified UI screens
6. **Kai Anthropic provider + DTOs** — highest-value missing feature
7. **Kai skills/marketplaces + model catalog**
8. **Unified UI screens** — dashboard, chat, settings, terminal, onboarding
9. **Android Assistant layer** — new service + router + session tracking
10. **ADT native loader fallback + Tama check**
11. **CI verification pass** — compile, lint, unit tests
12. **Final mutation audit**

---

## 9. Verification Gates

- Each phase ends with a file-existence grep + compile check on GitHub runner
- No phase marked complete without verifiable files on disk
- Android Assistant layer tested via `adb shell am start` with `ASSIST` intent after CI green

---

## 10. Out of Scope / Stripped

- Ads, analytics, Firebase, Play Core — per `STRIP-POLICY.md`
- Paywalls, license checks, reward calendar, premium dialogs
- Remote config / ad manager classes
