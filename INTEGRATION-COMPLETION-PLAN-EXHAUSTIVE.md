# Project Blackbox — Exhaustive Integration Completion Plan
**Persistent directive. No skimming. All phases must be completed before APK push.**

---

## PHASE 0 — PREREQUISITES

### 0.1 Unified Data Layer Foundation
Create the single shared data layer that all modules must consume. No module may roll its own persistent storage after this phase.

**Files to create:**

1. `app/src/main/java/com/blackbox/data/BlackboxPreferences.kt`
   - DataStore-backed preferences facade
   - Must expose these exact keys ported from AnyClaw `data/PreferencesManager.smali`:
     - `apiKey`, `apiProvider`, `selectedModel`, `selectedModelId`
     - `discordEnabled`, `discordBotToken`, `discordGuildAllowlist`, `discordRequireMention`
     - `telegramEnabled`, `telegramBotToken`
     - `whatsappEnabled`
     - `openClawVersion`, `autoStartOpenClawOnBoot`, `openClawUpdatePromptSuppressedBundledVersion`
     - `codexappVersion`, `codexappBranch`, `autoStartCodexOnBoot`, `customWebViewUrl`
     - `openAiCompatibleBaseUrl`, `openAiCompatibleModelId`
     - `braveSearchApiKey`, `setupComplete`, `onboardingComplete`, `premiumActive`, `hasRated`
     - `gatewayWasRunning`, `lastAppOpenedAt`, `bundleUpdateFailure`, `checkLoginOnStart`
     - `autoStartSshd`, `batteryOptimizationPrompted`, `logSectionUnlocked`
     - `fakeUsUser`, `forceShowCalendar`, `forceShowRewardCalendarDebug`, `lastInterstitialAdShownDate`
     - `lastWebViewPath`, `appLanguageTag`
   - Expose as `Flow<Preferences>` and `StateFlow` for UI observation
   - All reads/writes from modules must go through this facade

2. `app/src/main/java/com/blackbox/data/BlackboxDatabase.kt` — extend
   - Add `ChannelMessageEntity`, `ChannelConversationEntity`, `AssistantSessionEntity`
   - Add DAOs: `ChannelMessageDao`, `ChannelConversationDao`, `AssistantSessionDao`
   - Migrate existing `ConversationEntity`/`MessageEntity` from Kai `db/` package

3. `app/src/main/java/com/blackbox/data/BlackboxRepository.kt`
   - Unified repository wrapping `BlackboxDatabase` + `BlackboxPreferences`
   - Methods: `observeMessages(channelId)`, `sendMessage(channelId, text, sender)`, `observeConversations()`, `createConversation(title, channel)`, `observeAssistantSessions()`, `logAssistantInvoke(sessionId, intent)`

4. `app/src/main/java/com/blackbox/data/SecretStore.kt` — already exists, keep
   - All API keys/tokens must move from plaintext prefs to `SecretStore` (EncryptedSharedPreferences)

### 0.2 Module Bus
**File:** `app/src/main/java/com/blackbox/module/ModuleBus.kt`
- In-memory event bus using `SharedFlow`/`StateFlow`
- Events:
  - `AssistantInvoke(sessionId, intent, source)`
  - `ChatMessageSent(channelId, message)`
  - `ServiceStateChanged(moduleId, serviceClass, state)`
  - `ModelDownloaded(modelId, path)`
  - `BootCompleted`
- Modules publish via `ModuleBus.publish(event)`, subscribe via `ModuleBus.subscribe<T>()`

### 0.3 Permission Coordinator
**File:** `app/src/main/java/com/blackbox/runtime/PermissionCoordinator.kt`
- Centralize all runtime permission requests
- Single source of truth for permission state
- Expose `StateFlow<Map<permission, PermissionStatus>>`
- Modules request permissions through coordinator, not directly

### 0.4 Build Dependencies Audit
**File:** `app/build.gradle.kts`
- Verify these exact dependencies present:
  - `androidx.core:core-ktx:1.15.0`
  - `androidx.lifecycle:lifecycle-runtime-ktx:2.8.0`
  - `androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0`
  - `androidx.navigation:navigation-compose:2.8.0`
  - `androidx.compose.ui:ui`
  - `androidx.compose.material3:material3`
  - `androidx.webkit:webkit`
  - `com.squareup.okhttp3:okhttp:4.12.0`
  - `org.jetbrains.kotlinx:kotlinx-coroutines-android`
  - `androidx.room:room-ktx`
  - `androidx.datastore:datastore-preferences`
  - `androidx.security:security-crypto`
  - `org.jetbrains.kotlinx:kotlinx-serialization-json`
  - `androidx.startup:startup-runtime`
  - `com.google.dagger:hilt-android` or manual DI
  - `androidx.hilt:hilt-navigation-compose` if Hilt used
  - `androidx.work:work-runtime-ktx` for boot receivers/workers

---

## PHASE 1 — ANYCLAW FULL PORT

Source of truth: `anyclaw-decompiled/smali_classes3/app/anyclaw/`

### 1.1 Data Layer Port
**Files to create:**

1. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/data/PreferencesManager.kt`
   - Port from `data/PreferencesManager.smali` and all `$` inner classes
   - Every key listed in 0.1 must be present
   - Delegate to `BlackboxPreferences` from app layer
   - Methods: `getApiKey(provider)`, `setApiKey(provider, key)`, `getSelectedModel()`, `setSelectedModel(modelId)`, `isOnboardingComplete()`, `setOnboardingComplete()`, `getChannelConfig()`, `setChannelConfig(config)`, etc.

2. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/data/ChannelConfig.kt` — already exists, extend
   - Add fields: `discordGuildAllowlist`, `discordRequireMention`, `telegramEnabled`, `whatsappEnabled`, `autoStartSshd`, `batteryOptimizationPrompted`, `customWebViewUrl`, `lastWebViewPath`, `appLanguageTag`, `forceShowCalendar`, `fakeUsUser`

3. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/data/GatewayStatus.kt`
   - Port from `data/GatewayStatus.smali`

4. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/data/BundleUpdateFailureRecord.kt`
   - Port from `data/BundleUpdateFailureRecord.smali`

5. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/data/BugReportBundle.kt`, `BugReportBundleBuilder.kt`, `BugReportEmailIntentBuilder.kt`, `BugReportEmailMetadata.kt`, `BugReportSessionErrorEntry.kt`, `BugReportZipArtifact.kt`, `BugReportZipWriter.kt`
   - Port from `data/BugReport*.smali`
   - Strip analytics/phone-home; keep local bundle generation for manual sharing

6. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/data/FeedbackArtifact.kt`, `FeedbackZipBuilder.kt`
   - Port from `data/FeedbackArtifact.smali`, `FeedbackZipBuilder.smali`

7. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/data/OpenRouterModel.kt`, `OpenRouterModelsKt.kt`
   - Port from `data/OpenRouterModel.smali`, `OpenRouterModelsKt.smali`

### 1.2 Auth / OAuth Port
**Files to create:**

1. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/auth/CodexAuthWriter.kt`
   - Port from `auth/CodexAuthWriter.smali`

2. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/auth/OpenRouterAuth.kt`
   - Port from `auth/OpenRouterAuth.smali` and `$exchangeCodeForKey$1`, `$exchangeCodeForKey$2`

3. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/auth/AuthorizationFlow.kt`
   - PKCE login, token exchange, refresh
   - Port from `ui/screen/settings/SettingsViewModel$AuthorizationFlow.smali`, `$exchangeAuthorizationCode$1.smali`, `$loginOpenAiCodexOAuth$1.smali`, `$OAuthTokenResult.smali`, `$refreshCodexAuthStatus$1.smali`

### 1.3 Proot / Runtime Port
**Files to create/extend:**

1. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/proot/ArchiveUtils.kt`
   - Port from `proot/ArchiveUtils.smali` and lambdas — tar/zip extraction for rootfs

2. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/proot/BundleUpdateAttemptResult.kt`
   - Port from `proot/BundleUpdateAttemptResult.smali`

3. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/proot/BundleUpdateFailureType.kt`, `BundleUpdateOutcome.kt`
   - Port from `proot/BundleUpdateFailureType.smali`, `BundleUpdateOutcome.smali`

4. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/proot/SetupException.kt`
   - Port from `proot/SetupException.smali`

5. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/proot/DeviceBridge.kt` — already exists, extend
   - Port `DeviceBridge$AudioHelper`, `CameraHelper`, `ClipboardHelper`, `LocationHelper`, `SensorHelper`, `SentinelResult`, `WriterOutputStream` from `proot/DeviceBridge$*.smali`
   - These are the actual device-bridge implementations used by proot runtime

6. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/proot/GatewayWsClient.kt`
   - Port from `proot/GatewayWsClient.smali` and all lambdas
   - Include `call`, `callViaGatewayCli`, `connect`, `logoutChannel`, `startWhatsAppLogin`, `waitWhatsAppLogin`

7. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/proot/ProcessManager.kt` — already exists, extend
   - Port from `proot/ProcessManager.smali` and all `$` classes:
     - `approvePairing`, `denyPairing`, `getSessionLogEntries`, `listPairingRequests`, `readProcessOutput`, `refreshPairingRequests`
     - `startCodexWebLocal`, `stopCodexWebLocal`, `updateCodexuiLatestInBackground`
     - `startHermesWebUi`, `stopHermesWebUi`
     - `startOpenCode`, `stopOpenCode`
     - `startSshdInBackground`, `stopSshd`
     - `scheduleCodexuiLatestAutoUpdate`
     - `startPairingObserver`

8. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/proot/ProcessManagerKt.kt`
   - Port from `proot/ProcessManagerKt.smali` and lambdas

9. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/proot/ProotManager.kt` — already exists, extend
   - Port from `proot/ProotManager.smali`, `$CommandResult`, `$Companion`, `detectChromiumExecutablePath`

10. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/proot/SetupManager.kt` — already exists, extend
    - Port from `SetupManager.smali` and all `$` classes:
      - `CachedRootfsDns`, `downloadRootfsFromGitHub`, `extractRootfsFromAssets`
      - `getBundleUpdateFailureState`, `getOpenClawUpdateInfo`
      - `installOpenClaw`, `reinstallOpenClawFromAssets`
      - `runBundleUpdateWithPolicy`, `runFullSetup`, `runFullSetupFromManualRootfs`
      - `runOpenClawManualSync`, `runRecoveryInstall`
      - `syncCodexAuthToClaude`, `tryInstallOpenClawIncremental`
      - `updateBundleIfNeeded`, `updateBundleIfNeededWithPolicy`, `verify`
    - All lambdas and synthetic lambdas must be ported

### 1.4 Receivers / Workers Port
**Files to create:**

1. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/receiver/BootReceiver.kt` — already exists, extend
   - Port from `receiver/BootReceiver.smali` and `$onReceive$1`

2. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/receiver/GatewayWatchdogReceiver.kt`
   - New file, port from `receiver/GatewayWatchdogReceiver.smali`, `$Companion`, `$onReceive$1`

3. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/receiver/GatewayWatchdogRecoveryWorker.kt`
   - New file, port from `receiver/GatewayWatchdogRecoveryWorker.smali`, `$Companion`, `$doWork$1`
   - Uses `androidx.work` for boot recovery

4. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/receiver/ReengagementNotificationWorker.kt`
   - New file, port from `receiver/ReengagementNotificationWorker.smali`, `$Companion`, `$doWork$1`

5. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/receiver/ReminderReceiver.kt`, `ReminderScheduler.kt`
   - New files, port from `receiver/ReminderReceiver.smali`, `ReminderScheduler.smali`

### 1.5 Service Port
**Files to create/extend:**

1. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/service/GatewayService.kt` — already exists, extend
   - Port from `service/GatewayService.smali` and all `$` classes:
     - `GatewayActionType` enum
     - `handleStart`, `handleStop`, `handleRestart`
     - `runAction`, `setDesiredRunningAndWatchdog`
     - `awaitGatewayStartupTerminalState`
     - `batteryStateReceiver`
     - `promptBatteryOptimizationExemptionOnce`
     - `stopServiceForeground`
     - `withTimedWakeLock`
     - `forceDesiredStopped`
   - Must declare `GatewayActionType` and all action handlers

### 1.6 Remote Config / Ads Strip
**Files to create (stripped shells):**

1. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/remoteconfig/AdCountryConfig.kt`
   - Minimal stub, no network calls

2. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/remoteconfig/AdRemoteConfigManager.kt`
   - Stub only, logs stripped

3. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/remoteconfig/UpdateAvailableConfig.kt`
   - Port config shape but no ad logic

**Stripped (do not port):**
- `ads/AdManager.smali` and all `$` classes — ads, rewarded ads, interstitials
- `billing/PremiumBillingManager.smali`, `PremiumSubscriptionPlan.smali` — paywalls
- `analytics/AnalyticsManager.smali` — analytics
- `data/PreferencesManager$claimRewardDay`, `$forceShowRewardCalendarDebug`, `$hasRated`, `$lastInterstitialAdShownDate`, `$premiumActive`, `$rewardLastClaimDate`, `$rewardStreak` — reward/premium fields stripped from PreferencesManager

### 1.7 UI Port
**Files to create:**

1. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/CodexWebViewActivity.kt`
   - Port from `ui/CodexWebViewActivity.smali` and all `$` classes
   - WebView clients, JS bridges, asset loader, file chooser, blob save, URL save, onCreateWindow
   - Bind to `CodexService` for start/stop/update

2. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/CodexWebViewClient.kt`
   - Extracted from `CodexWebViewActivity$setupWebViewClients$*.smali`

3. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/PasteAwareWebView.kt`
   - Port from `ui/PasteAwareWebView.smali`, `$injectImageFromUri$1`

4. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/HermesWebUiActivity.kt`
   - New file, port from `ProcessManager$startHermesWebUi$1` smali logic
   - Hermes Web UI start/stop/restart

5. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/screen/dashboard/DashboardViewModel.kt`
   - Port from `ui/screen/dashboard/DashboardViewModel.smali` and lambdas
   - State: `CodexWebLocalCard`, `HermesWebUiCard`, `OpenClawCard`, runtime status
   - Methods: `startHermesWebUiOnly`, `stopCodexWebLocalOnly`, `stopHermesWebUi`, `setCodexappBranch`, `setCodexappVersion`, `setSelectedModel`

6. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/screen/settings/SettingsViewModel.kt`
   - Port from `ui/screen/settings/SettingsViewModel.smali` and all lambdas
   - Methods: `fetchCodexappVersions`, `loginOpenAiCodexOAuth`, `logoutOpenAiCodex`, `refreshCodexAuthStatus`, `runCodexDirectPkceLogin`, `runOpenClawDoctorFix`, `runOpenClawUpdate`, `runRecoveryInstall`, `saveOpenAiCompatibleConfig`, `setApiProvider`, `setAutoStartCodexOnBoot`, `setAutoStartOpenClawOnBoot`, `setCodexappBranch`, `setCodexappVersion`, `setGptSubscription`, `setSelectedModel`, `shouldShowRestartPromptForProvider`
   - State: `AuthorizationFlow`, `OAuthTokenResult`, `OpenClawUpdateResult`

7. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/screen/terminal/TerminalViewModel.kt`
   - Port from `ui/screen/terminal/TerminalScreenKt.smali` and lambdas
   - Terminal input/output state, WebView client binding

8. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/screen/onboarding/OnboardingViewModel.kt`
   - Port from `ui/screen/onboarding/OnboardingViewModel.smali` and lambdas
   - Methods: `exchangeCodexToken`, `handleCodexAuthCode`, `startCodexCallbackServer`

9. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/theme/` — port Compose theme from `ui/theme/`
10. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/component/` — port reusable components from `ui/component/`
11. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/navigation/` — port nav graph from `ui/navigation/`
12. `modules/anyclaw/src/main/java/com/blackbox/module/anyclaw/ui/dynamicui/` — port dynamic UI from `ui/dynamicui/`

---

## PHASE 2 — KAI FULL PORT

Source of truth: `kai9000-decompiled/smali/com/inspiredandroid/kai/`

### 2.1 Data Layer Full Port
**Files to create:**

1. `modules/kai/src/main/java/com/blackbox/module/kai/data/AppSettings.kt`
   - Port from `data/AppSettings.smali`, `AppSettingsKt.smali`, `AppSettingsImportExportKt.smali`, `AppSettingsMigrationsKt.smali`, `AppSettingsServiceKt.smali`

2. `modules/kai/src/main/java/com/blackbox/module/kai/data/AssistantTurn.kt`
   - Port from `data/AssistantTurn.smali`

3. `modules/kai/src/main/java/com/blackbox/module/kai/data/Attachment.kt`
   - Port from `data/Attachment.smali`, `$Companion`, `$$serializer`

4. `modules/kai/src/main/java/com/blackbox/module/kai/data/BailoutReason.kt`
   - Port from `data/BailoutReason.smali`

5. `modules/kai/src/main/java/com/blackbox/module/kai/data/ChatPromptRuntimeContext.kt`, `ChatPromptUiMode.kt`, `ChatSystemPromptBuilder.kt`
   - Port from `data/ChatPromptRuntimeContext.smali`, `ChatPromptUiMode.smali`, `ChatSystemPromptBuilderKt.smali`

6. `modules/kai/src/main/java/com/blackbox/module/kai/data/Conversation.kt` — already exists, extend
   - Add `$Message` inner data class from `data/Conversation$Message.smali`
   - Add serialization if needed

7. `modules/kai/src/main/java/com/blackbox/module/kai/data/ConversationPersistence.kt`, `SqlConversationPersistence.kt`, `ConversationStorage.kt`
   - Port from `data/ConversationPersistence.smali`, `ConversationPersistenceKt.smali`, `ConversationPersistence_androidKt.smali`, `ConversationStorage.smali`, `ConversationStorageKt.smali`, `ConversationStorage_androidKt.smali`

8. `modules/kai/src/main/java/com/blackbox/module/kai/data/ConversationsData.kt`
   - Port from `data/ConversationsData.smali`, `$Companion`, `$$serializer`

9. `modules/kai/src/main/java/com/blackbox/module/kai/data/CuratedModelInfo.kt`
   - Port from `data/CuratedModelInfo.smali`

10. `modules/kai/src/main/java/com/blackbox/module/kai/data/DataRepository.kt`
    - Port from `data/DataRepository.smali`, `$DefaultImpls`

11. `modules/kai/src/main/java/com/blackbox/module/kai/data/EmailAccount.kt`, `EmailMessage.kt`, `EmailStore.kt`, `EmailSyncState.kt`
    - Port from `data/EmailAccount.smali`, `EmailMessage.smali`, `EmailStore.smali`, `EmailSyncState.smali` and inner classes

12. `modules/kai/src/main/java/com/blackbox/module/kai/data/FallbackStatus.kt`
    - Port from `data/FallbackStatus.smali`

13. `modules/kai/src/main/java/com/blackbox/module/kai/data/FileCategory.kt`, `FileClassification.kt`
    - Port from `data/FileCategory.smali`, `FileClassificationKt.smali`

14. `modules/kai/src/main/java/com/blackbox/module/kai/data/FreeMode.kt`, `FreeProviderSuggestions.kt`, `FreeProviderSuggestion.kt`, `FreeTierModels.kt`
    - Port from `data/FreeMode.smali`, `FreeProviderSuggestionsKt.smali`, `FreeProviderSuggestion.smali`, `FreeTierModels.smali`

15. `modules/kai/src/main/java/com/blackbox/module/kai/data/HeartbeatConfig.kt`, `HeartbeatLogEntry.kt`, `HeartbeatManager.kt`, `HeartbeatPendingEmail.kt`, `HeartbeatPendingNotification.kt`, `HeartbeatPendingSms.kt`, `HeartbeatPromotionCandidate.kt`, `HeartbeatPromptBuilder.kt`
    - Port all from `data/Heartbeat*.smali`

16. `modules/kai/src/main/java/com/blackbox/module/kai/data/ImportSection.kt`
    - Port from `data/ImportSection.smali`

17. `modules/kai/src/main/java/com/blackbox/module/kai/data/LoopChatResult.kt`
    - Port from `data/LoopChatResult.smali`

18. `modules/kai/src/main/java/com/blackbox/module/kai/data/MemoryCategory.kt`, `MemoryEntry.kt`, `MemoryStore.kt`
    - Port from `data/MemoryCategory.smali`, `MemoryEntry.smali`, `MemoryStore.smali`

19. `modules/kai/src/main/java/com/blackbox/module/kai/data/ModelCapabilities.kt`
    - Port from `data/ModelCapabilitiesKt.smali`

20. `modules/kai/src/main/java/com/blackbox/module/kai/data/ModelCatalog.kt`
    - Port from `data/ModelCatalog.smali`, `ModelTransformationsKt.smali`

21. `modules/kai/src/main/java/com/blackbox/module/kai/data/ModelDefinition.kt` — already exists in app, sync
    - Ensure Kai `ModelDefinition` matches or delegates to app-layer model

22. `modules/kai/src/main/java/com/blackbox/module/kai/data/NotificationRecord.kt`, `NotificationStore.kt`, `NotificationSyncState.kt`
    - Port from `data/NotificationRecord.smali`, `NotificationStore.smali`, `NotificationSyncState.smali`

23. `modules/kai/src/main/java/com/blackbox/module/kai/data/PendingQueue.kt`, `PendingTaskPartition.kt`
    - Port from `data/PendingQueue.smali`, `PendingTaskPartition.smali`

24. `modules/kai/src/main/java/com/blackbox/module/kai/data/ReasoningRequestMode.kt` — already exists in app, sync

25. `modules/kai/src/main/java/com/blackbox/module/kai/data/RemoteDataRepository.kt` — already exists, full rewrite
    - Must port from `data/RemoteDataRepository.smali` and all `$` classes:
      - `ask`, `askInternal`, `askSilently`, `askSilentlyWithInstance`
      - `askWithService`, `askWithLocalEngine`, `askWithTools`
      - `handleAnthropicChatWithTools`, `handleGeminiChatWithTools`, `handleOpenAICompatibleChatWithTools`
      - `fetchAnthropicModelsForInstance`, `fetchGeminiModelsForInstance`, `fetchOpenAICompatibleModelsForInstance`
      - `makeFinalCallWithoutTools`, `retryApiCall`
      - `executeToolCallsInParallel`, `runLocalToolWithUiFeedback`, `runToolLoop`
      - `connectMcpServer`, `browseSkillMarketplaces`, `installBrowsedSkill`, `installGitHubSkill`
      - `addAssistantMessage`, `compactHistoryIfNeeded`, `getActiveSystemPrompt`
      - `validateConnection`, `cancelScheduledTask`, `sendSmsDraft`, `updateMemoryContent`, `deleteMemory`
    - All provider handlers: Anthropic, Gemini, OpenAI, OpenAICompatible, OpenRouter, Ollama, Groq, Mistral, Together, etc.

26. `modules/kai/src/main/java/com/blackbox/module/kai/data/ScheduledTask.kt`
    - Port from `data/ScheduledTask.smali`, `$Companion`, `$$serializer`

27. `modules/kai/src/main/java/com/blackbox/module/kai/data/Service.kt` — already exists, extend
    - Add all 20+ provider configs from `data/Service$*.smali`: Anthropic, OpenAI, OpenRouter, Gemini, Groq, Mistral, Together, FireworksAI, Cerebras, DeepInfra, DeepSeek, Moonshot, Minimax, Nvidia, Venice, XAI, Perplexity, PublicAI, AtlasCloud, AiHubMix, LongCat, Zai, ZaiCodingPlan, OllamaCloud, HuggingFace, LiteRT, OpenCode, AIHorde, Free, OpenAICompatible
    - Add `ServiceInstance`, `ServiceEntry`

28. `modules/kai/src/main/java/com/blackbox/module/kai/data/SettingsConversationPersistence.kt`
    - Port from `data/SettingsConversationPersistence.smali`

29. `modules/kai/src/main/java/com/blackbox/module/kai/data/SmsDraft.kt`, `SmsDraftStore.kt`, `SmsMessage.kt`, `SmsStore.kt`, `SmsSyncState.kt`
    - Port from `data/SmsDraft.smali`, `SmsDraftStore.smali`, `SmsMessage.smali`, `SmsStore.smali`, `SmsSyncState.smali`

30. `modules/kai/src/main/java/com/blackbox/module/kai/data/SystemPromptVariant.kt`
    - Port from `data/SystemPromptVariant.smali`

31. `modules/kai/src/main/java/com/blackbox/module/kai/data/TaskExecutionLogEntry.kt`
    - Port from `data/TaskExecutionLogEntry.smali`, `$Companion`, `$$serializer`

32. `modules/kai/src/main/java/com/blackbox/module/kai/data/TaskScheduler.kt` — already exists as service, reconcile
    - Port from `data/TaskScheduler.smali`, `$Companion`, `$checkNewEmails$1`, `$handleTaskCompletion$1`, `$handleTaskFailure$1`, `$runHeartbeat$1`, `$start$1`

33. `modules/kai/src/main/java/com/blackbox/module/kai/data/TaskStore.kt`, `TaskTrigger.kt`, `TaskStatus.kt`
    - Port from `data/TaskStore.smali`, `TaskTrigger.smali`, `TaskStatus.smali`

34. `modules/kai/src/main/java/com/blackbox/module/kai/data/ToolExecutor.kt` — already exists in app, sync
    - Port from `data/ToolExecutor.smali`, `$executeTool$1`, `$executeTool$result$1`, `$getToolDisplayName$1`, `ToolExecutorKt.smali`

35. `modules/kai/src/main/java/com/blackbox/module/kai/data/ToolLoopStrategy.kt`
    - Port from `data/ToolLoopStrategy.smali`, `$DefaultImpls`

36. `modules/kai/src/main/java/com/blackbox/module/kai/data/UiSubmission.kt`
    - Port from `data/UiSubmission.smali`, `$Companion`, `$$serializer`

37. `modules/kai/src/main/java/com/blackbox/module/kai/data/SharedJson.kt`
    - Port from `data/SharedJsonKt.smali`

38. `modules/kai/src/main/java/com/blackbox/module/kai/data/a.smali` equivalent — inspect content, port if functional
39. `modules/kai/src/main/java/com/blackbox/module/kai/data/splinterlands/` — optional game module, port if required

### 2.2 DB Layer Port
**Files to create:**

1. `modules/kai/src/main/java/com/blackbox/module/kai/db/KaiDatabase.kt`
   - Port from `db/KaiDatabase.smali`, `$Companion`
   - Expose `ConversationQueries`

2. `modules/kai/src/main/java/com/blackbox/module/kai/db/ConversationQueries.kt`
   - Port from `db/ConversationQueries.smali`, `$selectAllConversations$2`, `$selectAllMessages$2`

3. `modules/kai/src/main/java/com/blackbox/module/kai/db/ConversationEntity.kt`
   - Port from `db/ConversationEntity.smali`

4. `modules/kai/src/main/java/com/blackbox/module/kai/db/MessageEntity.kt`
   - Port from `db/MessageEntity.smali`

### 2.3 Email Full Port
**Files to create:**

1. `modules/kai/src/main/java/com/blackbox/module/kai/email/EmailConnection.kt`
   - Port from `email/EmailConnection.smali`, `EmailConnection_androidKt.smali`

2. `modules/kai/src/main/java/com/blackbox/module/kai/email/EmailPoller.kt` — already exists, extend
   - Port from `email/EmailPoller.smali`, `$Companion`, `$poll$1`

3. `modules/kai/src/main/java/com/blackbox/module/kai/email/ImapClient.kt` — already exists, extend
   - Port from `email/ImapClient.smali`, `$connect$1`, `$login$1`, `$search$2`, `$fetchHeaders$1`, `$fetchBody$1`, `$appendToMailbox$1`, `$createMailbox$1`, `$markAsRead$1`, `$selectInbox$1`, `$readUntilTaggedOrGreeting$1`, `$logout$1`, `ImapClientKt.smali`

4. `modules/kai/src/main/java/com/blackbox/module/kai/email/JvmEmailConnection.kt`
   - Port from `email/JvmEmailConnection.smali`, `$close$2`, `$readLine$2`, `$upgradeToTls$2`, `$writeLine$2`

5. `modules/kai/src/main/java/com/blackbox/module/kai/email/ServerAutoDetect.kt`
   - Port from `email/ServerAutoDetect.smali`, `$ServerConfig`

6. `modules/kai/src/main/java/com/blackbox/module/kai/email/SmtpClient.kt` — already exists, extend
   - Port from `email/SmtpClient.smali`, `$authenticate$1`, `$connect$1`, `$ehlo$1`, `$quit$1`, `$readResponse$1`, `$sendReply$1`, `$startTls$1`

### 2.4 Inference Full Port
**Files to create/extend:**

1. `modules/kai/src/main/java/com/blackbox/module/kai/inference/LiteRTInferenceEngine.kt` — already exists, full rewrite
   - Port from `inference/LiteRTInferenceEngine.smali` and all `$` classes:
     - `chat`, `deleteModel`, `importModel`, `initialize`, `initializeLocked`, `release`, `releaseInBackground`, `scheduleIdleRelease`, `scanImportedModels`, `startDownload`
     - `LocalToolOpenApiAdapter`
   - Must bind to actual LiteRT native via JNI/JNA or subprocess

2. `modules/kai/src/main/java/com/blackbox/module/kai/inference/LocalInferenceEngine.kt`, `LocalInferenceEngineProvider.kt`
   - Port from `inference/LocalInferenceEngine.smali`, `LocalInferenceEngineKt.smali`, `LocalInferenceEngineProvider_androidKt.smali`

3. `modules/kai/src/main/java/com/blackbox/module/kai/inference/LocalModel.kt`, `LocalModelCatalog.kt`, `LocalModelImport.kt`
   - Port from `inference/LocalModel.smali`, `LocalModelCatalogKt.smali`, `LocalModelImportKt.smali`

4. `modules/kai/src/main/java/com/blackbox/module/kai/inference/ModelDownloadService.kt`
   - Port from `inference/ModelDownloadService.smali`, `$Companion`

5. `modules/kai/src/main/java/com/blackbox/module/kai/inference/ModelImportResult.kt`, `ModelIntegrityException.kt`, `DownloadError.kt`, `InsufficientMemoryException.kt`, `InferenceTimeoutException.kt`, `NoModelDownloadedException.kt`, `ImportTarget.kt`, `DownloadedModel.kt`, `EngineState.kt`, `DevicePerformance.kt`, `InferenceMessage.kt`, `FileTooLargeException.kt`, `UnsupportedFileTypeException.kt`
   - Port all from `inference/*.smali`

### 2.5 MCP Full Port
**Files to create:**

1. `modules/kai/src/main/java/com/blackbox/module/kai/mcp/McpClient.kt`
   - Port from `mcp/McpClient.smali`, `$callTool$1`, `$initialize$1`, `$listTools$1`, `$sendNotification$1`, `$sendRequest$1`

2. `modules/kai/src/main/java/com/blackbox/module/kai/mcp/McpServerConfig.kt`, `McpServerManager.kt` — already exists, extend
   - Port from `mcp/McpServerConfig.smali`, `McpServerManager.smali`, `$connectAndDiscoverTools$1`, `$connectEnabledServers$2`, `McpServerManagerKt.smali`

3. `modules/kai/src/main/java/com/blackbox/module/kai/mcp/McpTool.kt`, `McpToolDefinition.kt`, `McpToolMetadata.kt`
   - Port from `mcp/McpTool.smali`, `McpToolDefinition.smali`, `McpToolMetadata.smali`, `$execute$1`, `$Companion`

4. `modules/kai/src/main/java/com/blackbox/module/kai/mcp/JsonRpcRequest.kt`, `JsonRpcResponse.kt`, `JsonRpcError.kt`, `McpCallToolResult.kt`, `McpContent.kt`, `McpToolsResult.kt`, `McpException.kt`
   - Port all from `mcp/*.smali`

5. `modules/kai/src/main/java/com/blackbox/module/kai/mcp/PopularMcpServers.kt`
   - Port from `mcp/PopularMcpServersKt.smali`, `PopularMcpServer.smali`

### 2.6 Network Full Port
**Files to create:**

1. `modules/kai/src/main/java/com/blackbox/module/kai/network/Requests.kt`
   - Port from `network/Requests.smali`, `RequestsKt.smali`, `$DebugKtorLogger`
   - Methods: `anthropicChat`, `geminiChat`, `openAICompatibleChat`, `getAnthropicModels`, `getGeminiModels`, `getOpenAICompatibleModels`, `validateOpenRouterApiKey`, `validatePerplexityApiKey`, `handleOpenAICompatibleError`

2. `modules/kai/src/main/java/com/blackbox/module/kai/network/ServiceCredentials.kt`
   - Port from `network/ServiceCredentials.smali`

3. `modules/kai/src/main/java/com/blackbox/module/kai/network/dtos/anthropic/` — all DTOs
   - Port all from `network/dtos/anthropic/*.smali`

4. `modules/kai/src/main/java/com/blackbox/module/kai/network/dtos/gemini/` — if exists
5. `modules/kai/src/main/java/com/blackbox/module/kai/network/dtos/openai/` — if exists

6. All exception classes from `network/`:
   - `AnthropicApiException`, `AnthropicGenericException`, `AnthropicInsufficientCreditsException`, `AnthropicInvalidApiKeyException`, `AnthropicOverloadedException`, `AnthropicRateLimitExceededException`
   - `GeminiApiException`, `GeminiGenericException`, `GeminiInvalidApiKeyException`, `GeminiRateLimitExceededException`
   - `OpenAICompatibleApiException`, `OpenAICompatibleBadRequestException`, `OpenAICompatibleConnectionException`, `OpenAICompatibleContentModerationException`, `OpenAICompatibleEmptyResponseException`, `OpenAICompatibleGenericException`, `OpenAICompatibleInvalidApiKeyException`, `OpenAICompatibleModelNotFoundException`, `OpenAICompatibleProviderErrorException`, `OpenAICompatibleQuotaExhaustedException`, `OpenAICompatibleRateLimitExceededException`, `OpenAICompatibleRequestTooLargeException`, `OpenAICompatibleServiceUnavailableException`, `OpenAICompatibleTimeoutException`
   - `AllServicesFailedException`, `ApiException`, `ContextWindowExceededException`, `FileTooLargeException`, `GenericNetworkException`, `UnsupportedFileTypeException`
   - `UiError`, `UiError$Resource`, `UiError$ResourceWithDetail`, `UiError$Text`

### 2.7 Notifications Port
**Files to create/extend:**

1. `modules/kai/src/main/java/com/blackbox/module/kai/notifications/KaiNotificationListenerService.kt`
   - Port from `notifications/KaiNotificationListenerService.smali`, `$Companion`, `$onListenerConnected$1`, `$onListenerDisconnected$1`, `$onNotificationPosted$1`

2. `modules/kai/src/main/java/com/blackbox/module/kai/notifications/NotificationReader.kt`
   - Port from `notifications/NotificationReader.smali`, `NotificationReader_androidKt.smali`, `$search$$inlined$sortedByDescending$1`

### 2.8 Sandbox / Proot Port
**Files to create:**

1. `modules/kai/src/main/java/com/blackbox/module/kai/sandbox/LinuxSandboxManager.kt`
   - Port from `sandbox/LinuxSandboxManager.smali`, `$installOptionalPackagesInternal$1`, `$installPackages$1`, `$installRequiredPackagesInternal$1`, `$reset$1`, `$scheduleTranscriptSave$1$1`, `$setup$1`, `$setupInternal$1`, `LinuxSandboxManagerKt.smali`

2. `modules/kai/src/main/java/com/blackbox/module/kai/sandbox/PersistentSandboxShell.kt`
   - Port from `sandbox/PersistentSandboxShell.smali`, `$CommandSink`, `$ensureShell$1`, `$Result`, `$run$1`, `$run$2`, `$cancelForeground$1`, `PersistentSandboxShellKt.smali`

3. `modules/kai/src/main/java/com/blackbox/module/kai/sandbox/ProotExecutor.kt`, `ProotHandle.kt`
   - Port from `sandbox/ProotExecutor.smali`, `ProotExecutorKt.smali`, `ProotHandle.smali`

4. `modules/kai/src/main/java/com/blackbox/module/kai/sandbox/RootfsDownloader.kt`
   - Port from `sandbox/RootfsDownloader.smali`, `$download$1`, `$downloadFrom$2`, `RootfsDownloaderKt.smali`

5. `modules/kai/src/main/java/com/blackbox/module/kai/sandbox/SandboxFiles.kt`, `SandboxModule.kt`, `SandboxState.kt`
   - Port from `sandbox/SandboxFilesKt.smali`, `SandboxModuleKt.smali`, `SandboxState.smali` and variants

6. `modules/kai/src/main/java/com/blackbox/module/kai/sandbox/SessionShell.kt`, `SshConfigManager.kt`
   - Port from `sandbox/SessionShell.smali`, `SessionShellKt.smali`, `$run$1`, `SshConfigManager.smali`, `$Companion`

7. `modules/kai/src/main/java/com/blackbox/module/kai/sandbox/SandboxFileEntry.kt`, `SandboxRequiredPackages.kt`, `SandboxSessions.kt`, `SandboxStatus.kt`
   - Port from `sandbox/SandboxFileEntry.smali`, `SandboxRequiredPackages.smali`, `SandboxSessions.smali`, `SandboxStatus.smali`

### 2.9 Skills / Marketplaces Port
**Files to create:**

1. `modules/kai/src/main/java/com/blackbox/module/kai/skills/SkillManager.kt`
   - Port from `skills/SkillManager.smali`, `$Companion`, `$browseMarketplaces$1`, `$install$1`, `$installFromGitHub$1`, `$installFromRegistryEntry$1`, `$load$1`, `$loadBuiltInSkills$1`, `$uninstall$1`, `SkillManagerKt.smali`

2. `modules/kai/src/main/java/com/blackbox/module/kai/skills/SkillRegistry.kt`
   - Port from `skills/SkillRegistry.smali`, `$browseMarketplace$1`, `$browseMarketplaces$1`, `$fetchRawFile$1`, `$fetchRepoTree$1`, `$fetchSkillFiles$1`, `$fetchSkillFiles$2`, `$Quad`, `$Companion`

3. `modules/kai/src/main/java/com/blackbox/module/kai/skills/SkillMarketplaces.kt`
   - Port from `skills/SkillMarketplacesKt.smali`, `SkillMarketplace.smali`

4. `modules/kai/src/main/java/com/blackbox/module/kai/skills/SkillManifest.kt`, `DownloadedSkill.kt`, `RegistrySkillEntry.kt`, `SkillFrontmatterParser.kt`, `SkillSource.kt`
   - Port all from `skills/*.smali`

### 2.10 Tools Port
**Files to create:**

1. `modules/kai/src/main/java/com/blackbox/module/kai/tools/CommonTools.kt`
   - Port from `tools/CommonTools.smali`, `$ipLocationTool$1`, `$localTimeTool$1`, `$memoryForgetTool$1`, `$memoryLearnTool$1`, `$memoryReinforceTool$1`, `$memoryStoreTool$1`, `$openUrlTool$1`

2. `modules/kai/src/main/java/com/blackbox/module/kai/tools/EmailTools.kt`
   - Port from `tools/EmailTools.smali`, `$checkEmailTool$1`, `$composeEmailTool$1`, `$readEmailTool$1`, `$replyEmailTool$1`, `$saveCopyToSentFolder$1`, `$searchEmailTool$1`, `$setupEmailTool$1`, `$withImapSession$1`, `$withSmtpSession$1`

3. `modules/kai/src/main/java/com/blackbox/module/kai/tools/FetchUrlTool.kt`
   - Port from `tools/FetchUrlTool.smali`, `$execute$1`, `FetchUrlToolKt.smali`

4. `modules/kai/src/main/java/com/blackbox/module/kai/tools/HeartbeatTools.kt`
   - Port from `tools/HeartbeatTools.smali`, `$promoteLearningTool$1`

5. `modules/kai/src/main/java/com/blackbox/module/kai/tools/NotificationTools.kt`, `NotificationHelper.kt`
   - Port from `tools/NotificationTools.smali`, `NotificationHelper.smali`, `$createNotificationChannel$channelDescription$1`, `$createNotificationChannel$channelName$1`, `$sendNotification$1`

6. `modules/kai/src/main/java/com/blackbox/module/kai/tools/OpenFileTool.kt`
   - Port from `tools/OpenFileTool.smali`, `OpenFileToolKt.smali`

7. `modules/kai/src/main/java/com/blackbox/module/kai/tools/ProcessManagerTool.kt`, `ProcessManager.kt`
   - Port from `tools/ProcessManagerTool.smali`, `tools/ProcessManager.smali`, `$Session`

8. `modules/kai/src/main/java/com/blackbox/module/kai/tools/SchedulingTools.kt`
   - Port from `tools/SchedulingTools.smali`, `$cancelTaskTool$1`, `$listTasksTool$1`, `$scheduleTaskTool$1`

9. `modules/kai/src/main/java/com/blackbox/module/kai/tools/ShellCommandTool.kt`
   - Port from `tools/ShellCommandTool.smali`, `$execute$1`, `ShellCommandToolKt.smali`

10. `modules/kai/src/main/java/com/blackbox/module/kai/tools/SmsTools.kt`
    - Port from `tools/SmsTools.smali`, `$checkSmsTool$1`, `$readSmsTool$1`, `$replySmsTool$1`, `$searchSmsTool$1`, `$sendSmsTool$1`

11. `modules/kai/src/main/java/com/blackbox/module/kai/tools/SshConfigureHostTool.kt`
    - Port from `tools/SshConfigureHostTool.smali`, `SshConfigureHostToolKt.smali`

12. `modules/kai/src/main/java/com/blackbox/module/kai/tools/WebSearchTool.kt`
    - Port from `tools/WebSearchTool.smali`, `$execute$1`, `WebSearchToolKt.smali`

13. Permission controllers:
    - `CalendarPermissionController.kt`, `CalendarRepository.kt`, `CalendarResult.kt`
    - `LocalNetworkPermissionController.kt`, `LocalNetworkPermissionController_androidKt.kt`
    - `NotificationPermissionController.kt`, `NotificationPermissionController_androidKt.kt`, `NotificationResult.kt`
    - `SmsPermissionController.kt`, `SmsPermissionController_androidKt.kt`, `SmsSendPermissionController.kt`, `SmsSendPermissionController_androidKt.kt`

14. `tools/HtmlUtils.kt`, `IpConnectionInfo.kt`, `IpLocationResponse.kt`, `IpTimezoneInfo.kt`, `OpenFileToolKt.smali` equivalent

### 2.11 Build / Terminal / UI Port
**Files to create:**

1. `modules/kai/src/main/java/com/blackbox/module/kai/build/BuildAgents.kt`
   - Port from `build/BuildAgents.smali`, `BuildAgent.smali`

2. `modules/kai/src/main/java/com/blackbox/module/kai/build/BuildEnvironmentManager.kt` — already exists, extend
   - Port from `build/BuildEnvironmentManager.smali`, `BuildEnvironmentState.smali`, `BuildStep.smali`, `BuildSystemInfo.smali`, `BuildTerminalSession.smali`, `build/runtime/*`

3. `modules/kai/src/main/java/com/blackbox/module/kai/build/TerminalInputView.kt`
   - Port from `build/TerminalInputView.smali`, `$Companion`, `$TerminalInputConnection`, `TerminalKeyboard_androidKt.smali`

4. `modules/kai/src/main/java/com/blackbox/module/kai/build/KaiBuildController.kt`, `AndroidKaiBuildController.kt`, `DaemonController.kt`, `DaemonService.kt`
   - Port from `build/KaiBuildController.smali`, `AndroidKaiBuildController.smali`, `DaemonController.smali`, `DaemonService.smali`, `DaemonController_androidKt.smali`

5. `modules/kai/src/main/java/com/blackbox/module/kai/ui/chat/ChatUiState.kt`
   - Port from `ui/chat/ChatUiState.smali`, `ChatUiStateKt.smali`

6. `modules/kai/src/main/java/com/blackbox/module/kai/ui/settings/Settings.kt`
   - Port from `ui/settings/Settings.smali`

7. `modules/kai/src/main/java/com/blackbox/module/kai/ui/Theme.kt`
   - Port from `ui/ThemeKt.smali`, `ui/theme/`

8. `modules/kai/src/main/java/com/blackbox/module/kai/ui/components/`
   - Port from `ui/components/`

9. `modules/kai/src/main/java/com/blackbox/module/kai/ui/sandbox/`, `ui/markdown/`, `ui/icons/`, `ui/dynamicui/`
   - Port all from respective smali packages

10. `modules/kai/src/main/java/com/blackbox/module/kai/Platform.kt`, `Platform_androidKt.kt`
    - Port from `Platform.smali`, `Platform_androidKt.smali`, `Platform$Mobile$Android.smali`

11. `modules/kai/src/main/java/com/blackbox/module/kai/ExtensionFunctions.kt`
    - Port from `ExtensionFunctionsKt.smali`

12. `modules/kai/src/main/java/com/blackbox/module/kai/ReviewHelper.kt`
    - Port from `ReviewHelperKt.smali`

13. `modules/kai/src/main/java/com/blackbox/module/kai/Version.kt`
    - Port from `Version.smali`

---

## PHASE 3 — ADT COMPLETION

Source of truth: `adt-decompiled/smali_classes2/com/example/llamadroid/`

### 3.1 ADT Deep Port
**Files to create/extend:**

1. `modules/adt/src/main/java/com/blackbox/module/adt/runtime/LlamaService.kt` — already exists, extend
   - Port from `com/example/llamadroid/service/LlamaService.smali` if found
   - Wire to `AdtNativeLoader` for actual LiteRT binding

2. `modules/adt/src/main/java/com/blackbox/module/adt/runtime/LiteRtLmWorkerService.kt` — already exists, extend
   - Add JNI/JNA binding to `liblitert.so` via `AdtNativeLoader`

3. `modules/adt/src/main/java/com/blackbox/module/adt/runtime/WhisperService.kt` — already exists, extend
   - Wire to `libwhisper.so` + `libggml.so` via `AdtNativeLoader`

4. `modules/adt/src/main/java/com/blackbox/module/adt/runtime/AdtNativeLoader.kt` — already exists, extend
   - Add bundled-APK lib fallback
   - Verify all 62 `.so` files are extractable
   - Add integrity checks (SHA-256 per `ModuleVerifier`)

5. `modules/adt/src/main/java/com/blackbox/module/adt/runtime/ModelDownloadService.kt` — already exists, extend
   - Port full download logic with progress, pause/resume, checksum verification from smali if found

6. `modules/adt/src/main/java/com/blackbox/module/adt/tama/TamaWidgetService.kt`, `TamaPetController.kt`
   - Port from `example/llamadroid/tama/` if smali exists

7. `modules/adt/src/main/java/com/blackbox/module/adt/ui/` — ADT UI shell
   - Port minimal activity/screens from `example/llamadroid/ui/` if required

8. `modules/adt/src/main/java/com/blackbox/module/adt/util/` — utility classes
   - Port from `example/llamadroid/util/`

9. `modules/adt/src/main/java/com/blackbox/module/adt/widget/` — widget classes
   - Port from `example/llamadroid/widget/`

---

## PHASE 4 — UNIFIED UI

All unified UI lives under `app/src/main/java/com/blackbox/ui/screen/`.

### 4.1 Navigation
**File:** `app/src/main/java/com/blackbox/ui/BlackboxNavHost.kt`
- Routes: `dashboard`, `chat/{channelId}`, `settings`, `terminal`, `onboarding`, `assistant`
- Each module registers routes via `ModuleRegistry`
- Use `Navigation-Compose` with `rememberNavController`

### 4.2 Dashboard Screen
**File:** `app/src/main/java/com/blackbox/ui/screen/DashboardScreen.kt`
- Cards for: AnyClaw Gateway, Kai Chat, ADT Llama, ADT Whisper, ADT StableDiffusion, ZIM, Agent runtime
- Each card binds to module `StateFlow` for status
- Tap card navigates to appropriate screen or starts service

### 4.3 Unified Chat Screen
**File:** `app/src/main/java/com/blackbox/ui/screen/chat/UnifiedChatScreen.kt`
- Single chat UI backed by `ChannelConversationEntity` + `ChannelMessageEntity`
- Modules contribute providers: `KaiAiProvider`, `DiscordBridge`, `TelegramBridge`, `WhatsAppBridge`
- Message list, input, provider/model selector
- Markdown rendering via `ui/markdown/`

### 4.4 Unified Settings Screen
**File:** `app/src/main/java/com/blackbox/ui/screen/settings/UnifiedSettingsScreen.kt`
- API keys, provider config, model selection
- Channel enable/disable toggles
- OpenClaw/Codex update/recovery buttons
- Permissions request flows
- Bug report dialog

### 4.5 Unified Terminal Screen
**File:** `app/src/main/java/com/blackbox/ui/screen/terminal/UnifiedTerminalScreen.kt`
- WebView-backed terminal from AnyClaw `TerminalScreen`
- Proot shell access, file browser, SSH launcher

### 4.6 Unified Onboarding Screen
**File:** `app/src/main/java/com/blackbox/ui/screen/onboarding/UnifiedOnboardingScreen.kt`
- First-run flow: setup complete flag, API key entry, provider selection, permissions
- Port logic from AnyClaw `OnboardingViewModel`

---

## PHASE 5 — ANDROID ASSISTANT LAYER

### 5.1 Assistant Service
**File:** `app/src/main/java/com/blackbox/assistant/BlackboxAssistantService.kt`
- Extend `VoiceInteractionService` or implement assist callbacks
- Manifest entry:
  ```xml
  <service
      android:name=".assistant.BlackboxAssistantService"
      android:permission="android.permission.BIND_VOICE_INTERACTION"
      android:exported="true">
      <intent-filter>
          <action android:name="android.service.voice.VoiceInteractionService" />
      </intent-filter>
      <meta-data
          android:name="android.voice_interaction"
          android:resource="@xml/voice_interaction" />
  </service>
  ```
- Capture `AssistStructure` from any app
- Extract text/URL/intent from `AssistStructure`
- Route to active provider via `AssistantIntentRouter`

### 5.2 Assistant Intent Router
**File:** `app/src/main/java/com/blackbox/assistant/AssistantIntentRouter.kt`
- Routes to:
  - Kai chat provider (Claude/GPT/OpenAI-compatible)
  - AnyClaw Codex/OpenClaw runtime
  - ADT LlamaService / LiteRtLmWorkerService
- Selection based on `BlackboxPreferences` default provider
- Falls back to last-used provider per session

### 5.3 Assistant Session Tracking
**Files:**
- `app/src/main/java/com/blackbox/data/AssistantSessionEntity.kt`
- `app/src/main/java/com/blackbox/data/AssistantSessionDao.kt`
- Log every assistant invocation: timestamp, source app, extracted text, routed provider, response

### 5.4 Voice Interaction XML
**File:** `app/src/main/res/xml/voice_interaction.xml`
- Minimal voice interaction metadata for `VoiceInteractionService`

---

## PHASE 6 — CROSS-CUTTING WIRING

### 6.1 Module Bus Integration
- `BlackboxApp.onCreate()` initializes `ModuleBus`
- Each module’s `onLoad()` registers event handlers
- `AssistantInvoke` event triggers `AssistantIntentRouter`
- `ServiceStateChanged` updates dashboard cards

### 6.2 Permission Coordinator Wiring
- `BlackboxApp.onCreate()` initializes `PermissionCoordinator`
- AnyClaw, Kai, ADT all request permissions through coordinator
- Unified permission UI in `UnifiedSettingsScreen`

### 6.3 Data Layer Wiring
- All module storage delegates to `BlackboxPreferences` / `BlackboxDatabase`
- `SecretStore` holds all API keys/tokens
- Module `onLoad()` receives shared `Context` + `ClassLoader` + data layer refs

### 6.4 AndroidManifest Updates
- Add `BlackboxAssistantService` declaration
- Add `BIND_VOICE_INTERACTION` permission
- Add `VoiceInteractionService` metadata
- Ensure all module services declared with correct `process` attributes
- Ensure `LiteRtLmWorkerService` runs in `:litert` process

---

## PHASE 7 — CI + VERIFICATION

### 7.1 Build Verification
- GitHub Actions workflow runs `./gradlew assembleDebug --no-daemon --stacktrace`
- Add `./gradlew lintDebug` if Android SDK lint available on runner
- Add unit tests for `BlackboxPreferences`, `BlackboxDatabase`, `ModuleBus`, `PermissionCoordinator`

### 7.2 Mutation Audit
- Final grep for `:modules:core`, `com.blackbox.core`, old class names
- Verify all manifest-declared classes exist on disk
- Verify all module registrations use correct class names
- Verify no plaintext API keys in source

### 7.3 Functional Verification
- Each module’s services must start without crash
- `ModuleBus` events must flow between modules
- Assistant layer must respond to `ASSIST` intent via `adb shell am start`
- Unified UI must render dashboard, chat, settings, terminal, onboarding

---

## STRIPPED / OUT OF SCOPE (per STRIP-POLICY.md)

- AnyClaw: `ads/`, `billing/`, `analytics/`, reward calendar, premium dialogs, interstitials, rewarded ads
- AnyClaw: `remoteconfig/` ad configs — keep minimal config stubs only
- AnyClaw: `data/PreferencesManager$*reward*`, `*premium*`, `*interstitial*`, `*ad*` fields
- Kai: `splinterlands/` game module — optional, out of scope unless requested
- ADT: Firebase, Play Core, analytics — already stripped from decompiled

---

## EXECUTION ORDER SUMMARY

1. Phase 0: Unified data layer + ModuleBus + PermissionCoordinator + build deps
2. Phase 1: AnyClaw full port (data → auth → proot → receivers → services → UI)
3. Phase 2: Kai full port (data → db → email → inference → mcp → network → notifications → sandbox → skills → tools → UI)
4. Phase 3: ADT completion (native loader fallback, tama, ui/util/widget)
5. Phase 4: Unified UI screens
6. Phase 5: Android Assistant layer
7. Phase 6: Cross-cutting wiring + manifest updates
8. Phase 7: CI verification + mutation audit + functional test

No phase is complete until every file in that phase exists on disk and compiles on the GitHub runner.
