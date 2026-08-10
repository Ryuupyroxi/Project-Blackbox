# REF-AnyClaw-Structure.md
**AnyClaw v2.1.565 — Complete Component Inventory**  
**Source:** `/home/Ryuu/Project-Blackbox-artifacts/anyclaw.apk` → `/home/Ryuu/Project-Blackbox-worktree/anyclaw-decompiled/`  
**Date:** 2026-08-09  
**Status:** Verified — valid APK, decompiled with apktool 2.7.0

---

## 1. Identity

| Field | Value |
|---|---|
| Package | `gptos.intelligence.assistant` |
| Version | 2.1.565 |
| Compile SDK | 35 (Android 15) |
| Min SDK | 26 |
| Main Activity | `app.anyclaw.MainActivity` |
| Second Activity | `app.anyclaw.ui.CodexWebViewActivity` |
| App Theme | `Theme.AnyClaw` |

---

## 2. Size Breakdown

| Component | Value |
|---|---|
| Total smali files | 47,036 |
| App-specific classes (`app/anyclaw`) | 1,400 |
| Dex files | 5 (classes.dex through classes5.dex, plus `assets/rish/rish_shizuku.dex`) |

---

## 3. Permissions (27 explicit + 1 custom)

| Permission | Purpose |
|---|---|
| `INTERNET` | Network access |
| `ACCESS_NETWORK_STATE` | Network check |
| `com.google.android.gms.permission.AD_ID` | Ad ID |
| `FOREGROUND_SERVICE` | Runtime service |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Special-use foreground |
| `WAKE_LOCK` | Keep device awake |
| `RECEIVE_BOOT_COMPLETED` | Auto-start on boot |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Prevent Doze |
| `POST_NOTIFICATIONS` | Notifications |
| `CAMERA` | Camera access |
| `RECORD_AUDIO` | Voice recording |
| `MODIFY_AUDIO_SETTINGS` | Audio routing |
| `READ_CALENDAR` | Calendar read |
| `WRITE_CALENDAR` | Calendar write |
| `VIBRATE` | Haptic feedback |
| `ACCESS_WIFI_STATE` | WiFi state |
| `ACCESS_FINE_LOCATION` | GPS |
| `ACCESS_COARSE_LOCATION` | GPS |
| `com.android.vending.BILLING` | In-app billing |
| `com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE` | Play Core |
| `ACCESS_ADSERVICES_ATTRIBUTION` | Ads |
| `ACCESS_ADSERVICES_AD_ID` | Ads |
| `ACCESS_ADSERVICES_TOPICS` | Ads |
| `gptos.intelligence.assistant.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | Internal receiver protection |

**Note:** Contains billing and ads permissions — these should be stripped for unified build.

---

## 4. Activities

| Activity | Purpose |
|---|---|
| `app.anyclaw.MainActivity` | Main entry point |
| `app.anyclaw.ui.CodexWebViewActivity` | Codex AI WebView UI |

---

## 5. Services

| Service | Purpose |
|---|---|
| `app.anyclaw.service.GatewayService` | Core foreground supervisor |

---

## 6. Receivers

| Receiver | Purpose |
|---|---|
| `app.anyclaw.receiver.BootReceiver` | Boot auto-start |
| `app.anyclaw.receiver.GatewayWatchdogReceiver` | Service watchdog |
| `app.anyclaw.receiver.ReminderReceiver` | Reminder notifications |
| `app.anyclaw.receiver.ReengagementNotificationWorker` | Re-engagement notifications |

---

## 7. Providers

| Provider | Purpose |
|---|---|
| `androidx.core.content.FileProvider` | File sharing |
| `gptos.intelligence.assistant.firebaseinitprovider` | Firebase init |
| `gptos.intelligence.assistant.mobileadsinitprovider` | Ads init |
| `androidx.startup.InitializationProvider` | AndroidX startup |
| `com.google.android.datatransport.runtime.backends.TransportBackendDiscovery` | DataTransport |
| `com.google.android.datatransport.runtime.scheduling.jobscheduling.JobInfoSchedulerService` | Job scheduling |
| `com.google.android.gms.measurement.AppMeasurementReceiver` | Analytics |
| `com.google.android.gms.measurement.AppMeasurementService` | Analytics |
| `com.google.android.gms.measurement.AppMeasurementJobService` | Analytics |
| `androidx.room.MultiInstanceInvalidationService` | Room DB |
| `androidx.profileinstaller.ProfileInstallReceiver` | Profile install |
| `androidx.work.impl.background.systemalarm.SystemAlarmService` | WorkManager |
| `androidx.work.impl.background.systemjob.SystemJobService` | WorkManager |
| `androidx.work.impl.utils.ForceStopRunnable$BroadcastReceiver` | WorkManager |
| `androidx.work.impl.diagnostics.DiagnosticsReceiver` | WorkManager |

---

## 8. Key App Packages (1,400 classes)

### ads/
- `AdManager` — Interstitial + rewarded ads
- `AdRemoteConfigManager` — Remote config for ads
- `AdCountryConfig` — Country-based ad config

### analytics/
- `AnalyticsManager` — Usage analytics

### auth/
- `CodexAuthWriter` — Writes auth.json for Codex CLI and OpenClaw
- `OpenRouterAuth` — OAuth PKCE flow for OpenRouter

### billing/
- `PremiumBillingManager` — In-app billing
- `PremiumSubscriptionPlan` — Subscription plans
- `PremiumBuyDialogKt` — Premium purchase UI

### data/
- `BugReportBundleBuilder` — Bug report packaging
- `BugReportZipWriter` — ZIP creation for bug reports
- `PreferencesManager` — 45+ preference keys with inline setters
- `PreferencesManagerKt` — Preference extensions

### proot/
- `ProotManager` — Proot lifecycle and process management
- `ProcessManager` — Process orchestration (start/stop Codex, OpenClaw, SSH, HermesWebUi)
- `SetupManager` — Rootfs install, OpenClaw incremental sync, recovery install, bundle updates
- `AssetInstaller` — Asset installation
- `TarInstaller` — Tar-based rootfs installation
- `RuntimeRecoverableFailureDetector` — Failure detection and recovery
- `ArchiveUtils` — Tar/zip extraction utilities

### receiver/
- `BootReceiver` — Boot completed handler
- `GatewayWatchdogReceiver` — Service watchdog
- `GatewayWatchdogRecoveryWorker` — Recovery worker
- `ReminderReceiver` — Reminder handler
- `ReminderScheduler` — Reminder scheduling
- `ReengagementNotificationWorker` — Re-engagement notifications

### remoteconfig/
- Remote config management

### service/
- `GatewayService` — Core foreground service
- `GatewayService$GatewayActionType` — Enum: startCodex, startOpenClaw, stopCodex, stopOpenClaw, restart
- `GatewayService$batteryStateReceiver$1` — Battery state monitoring

### ui/
- `DashboardScreenKt` — Main dashboard UI
- `LogsScreenKt` — Log viewer
- `OnboardingScreenKt` — First-run onboarding
- `SettingsScreenKt` — Settings UI with 60+ composables
- `SetupScreenKt` — Setup wizard
- `TerminalScreenKt` — Terminal UI
- `CodexWebViewActivity` — Codex WebView
- `WhatsAppQrDialogKt` — WhatsApp QR pairing dialog
- `WhatsAppQrState` — QR state machine (Idle, Loading, QrReady, Connected, Error)
- `SessionLogsDialogKt` — Session log viewer
- `UpdateAvailableDialogKt` — Update notification
- `PremiumBuyDialogKt` — Premium purchase dialog
- `ModelSelectionDialogKt` — Model selector
- `ApiKeyInputDialog` — API key input
- `BotTokenInputDialog` — Bot token input
- `BraveSearchKeyDialog` — Brave search key input
- `BugReportDialog` — Bug report UI
- `ChannelDisconnectProgressDialog` — Channel disconnect UI
- `CodexappVersionCard` — Version info card
- `GptSubscriptionDialog` — Subscription dialog
- `MultilineTextInputDialog` — Text input dialog
- `ProviderSelectionDialog` — Provider selector
- `RecoveryInstallProgressDialog` — Recovery install UI
- `SettingsViewModel` — Settings state management
- `SetupViewModel` — Setup state management
- `Screen` — Navigation screen enum

### ui/component/
- Reusable UI components

### ui/navigation/
- Navigation graph

### ui/theme/
- Theme definitions

### ui/util/
- UI utilities

---

## 9. ProcessManager Methods (Verified)

| Method | Purpose |
|---|---|
| `startCodexWebLocal` | Start Codex Web UI locally |
| `stopCodexWebLocal` | Stop Codex Web UI |
| `startHermesWebUi` | Start Hermes Web UI |
| `stopHermesWebUi` | Stop Hermes Web UI |
| `startOpenCode` | Start OpenCode |
| `stopOpenCode` | Stop OpenCode |
| `startSshdInBackground` | Start SSH daemon |
| `stopSshd` | Stop SSH daemon |
| `approvePairing` | Approve device pairing |
| `denyPairing` | Deny device pairing |
| `listPairingRequests` | List pending pairings |
| `refreshPairingRequests` | Refresh pairing list |
| `readProcessOutput` | Read process stdout/stderr |
| `scheduleCodexuiLatestAutoUpdate` | Auto-update Codex UI |
| `updateCodexuiLatestInBackground` | Background update |
| `getSessionLogEntries` | Get session history |

---

## 10. SetupManager Methods (Verified)

| Method | Purpose |
|---|---|
| `runFullSetup` | Complete first-time setup |
| `runFullSetupFromManualRootfs` | Setup from manual rootfs |
| `runRecoveryInstall` | Recovery installation |
| `installOpenClaw` | Install OpenClaw |
| `runOpenClawManualSync` | Manual OpenClaw sync |
| `tryInstallOpenClawIncremental` | Incremental OpenClaw update |
| `updateBundleIfNeeded` | Bundle update check |
| `updateBundleIfNeededWithPolicy` | Policy-based bundle update |
| `runBundleUpdateWithPolicy` | Execute bundle update |
| `getBundleUpdateFailureState` | Check failure state |
| `getOpenClawUpdateInfo` | Check for updates |
| `verify` | Verify installation |
| `downloadRootfsFromGitHub` | Download rootfs from GitHub |
| `extractRootfsFromAssets` | Extract rootfs from APK assets |
| `reinstallOpenClawFromAssets` | Reinstall from assets |
| `syncCodexAuthToClaude` | Sync auth to Claude |
| `RootfsSource` — enum for rootfs sources |

---

## 11. GatewayService Action Types (Verified)

| Action | Purpose |
|---|---|
| `startCodex` | Start Codex Web UI |
| `startOpenClaw` | Start OpenClaw proot |
| `stopCodex` | Stop Codex Web UI |
| `stopOpenClaw` | Stop OpenClaw proot |
| `restart` | Restart runtime |

---

## 12. PreferencesManager Keys (Verified Subset)

| Key | Purpose |
|---|---|
| `api_key` | AI provider API key |
| `api_provider` | Selected AI provider |
| `selected_model` | Selected model name |
| `selected_model_id` | Selected model ID |
| `openai_compatible_base_url` | Custom API base URL |
| `openai_compatible_model_id` | Custom model ID |
| `discord_enabled` | Discord bridge toggle |
| `discord_bot_token` | Discord bot token |
| `discord_guild_allowlist` | Allowed Discord guilds |
| `discord_require_mention` | Require @mention |
| `telegram_enabled` | Telegram bridge toggle |
| `telegram_bot_token` | Telegram bot token |
| `whatsapp_enabled` | WhatsApp bridge toggle |
| `auto_start_codex_on_boot` | Auto-start Codex |
| `auto_start_openclaw_on_boot` | Auto-start OpenClaw |
| `auto_start_sshd` | Auto-start SSH |
| `setup_complete` | Setup wizard done |
| `onboarding_complete` | Onboarding done |
| `custom_web_view_url` | Custom Codex URL |
| `brave_search_api_key` | Brave Search API key |
| `app_language_tag` | App language |

---

## 13. Bundled Assets

| Asset | Purpose |
|---|---|
| `assets/rish/rish` | Shizuku shell wrapper binary |
| `assets/rish/rish_shizuku.dex` | Shizuku DEX loader |

---

## 14. Strippable Components

| Component | Action |
|---|---|
| `ads/` (AdManager, AdRemoteConfigManager) | STRIP |
| `analytics/` (AnalyticsManager) | STRIP |
| `billing/` (PremiumBillingManager, PremiumSubscriptionPlan) | STRIP |
| Google Mobile Ads (manifest entries) | STRIP |
| Firebase (manifest entries) | STRIP |
| Google Analytics (manifest entries) | STRIP |
| Play Core (manifest entries) | STRIP |
| AdServices permissions | STRIP |

---

## 15. Integration Points

### Proot Layer
- `ProotManager` — process lifecycle
- `ProcessManager` — process orchestration
- `SetupManager` — rootfs install/update
- `AssetInstaller` — asset deployment
- `TarInstaller` — tar-based install
- `RuntimeRecoverableFailureDetector` — failure recovery

### Codex Integration
- `CodexWebViewActivity` — WebView host
- `CodexAuthWriter` — OAuth token management
- `ProcessManager.startCodexWebLocal` — local Codex server
- `ProcessManager.startHermesWebUi` — Hermes Web UI

### OpenClaw Integration
- `ProcessManager.startOpenCode` — OpenClaw launcher
- `SetupManager.installOpenClaw` — OpenClaw installer
- `SetupManager.tryInstallOpenClawIncremental` — incremental updates
- `CodexAuthWriter` — auth-profiles.json writer

### Bridges
- `PreferencesManager.discord_*` — Discord bot config
- `PreferencesManager.telegram_*` — Telegram bot config
- `PreferencesManager.whatsapp_enabled` — WhatsApp toggle
- `WhatsAppQrState` — QR pairing state machine
- `SettingsViewModel.startWhatsAppQr` — QR launch flow

### SSH
- `ProcessManager.startSshdInBackground` — SSH daemon
- `ProcessManager.stopSshd` — SSH stop
- `SettingsViewModel.refreshSshdEndpoint` — endpoint refresh
