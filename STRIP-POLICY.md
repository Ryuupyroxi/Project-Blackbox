# Blackbox Stripping Policy
**Rule:** Only remove paywalls, license checks, ads, and analytics.  
**Never remove:** functional features, bridges, inference, media, tools, proot, email/SMS, ZIM, Tama, skills, dynamic UI, terminal, or build/runtime.

---

## AnyClaw v2.1.565

| Component | Classification | Action |
|---|---|---|
| `PremiumBillingManager` | **PAYWALL** | STRIP |
| `PremiumSubscriptionPlan` | **PAYWALL** | STRIP |
| `PremiumBuyDialogKt` | **PAYWALL UI** | STRIP |
| `AdManager` | Ads | STRIP |
| `AdRemoteConfigManager` | Ads config | STRIP |
| `AnalyticsManager` | Analytics | STRIP |
| Firebase providers | Analytics/crashlytics | STRIP |
| Google Mobile Ads init | Ads | STRIP |
| Play Core / install referrer | Ads/attribution | STRIP |
| `GatewayService` | Core runtime | KEEP |
| `ProcessManager` | Runtime orchestration | KEEP |
| `SetupManager` | Proot installer | KEEP |
| `ProotManager` | Proot lifecycle | KEEP |
| `CodexWebViewActivity` | Codex UI | KEEP |
| `BootReceiver` | Boot start | KEEP |
| `GatewayWatchdogReceiver` | Watchdog | KEEP |
| `ReminderReceiver` | Reminders | KEEP |
| `PreferencesManager` | Settings | KEEP |
| `CodexAuthWriter` | Auth | KEEP |
| Discord/Telegram/WhatsApp bridges | **Features** | KEEP |
| SSH daemon launcher | **Feature** | KEEP |
| Terminal/session/logs UI | **Feature** | KEEP |

---

## Kai 9000 v3.0.0

| Component | Classification | Action |
|---|---|---|
| `com.pairip.licensecheck.LicenseActivity` | **LICENSE CHECK / PAYWALL** | STRIP |
| `com.pairip.application.Application` | **LICENSE FRAMEWORK** | STRIP |
| `LicenseClient` | **LICENSE FRAMEWORK** | STRIP |
| `CHECK_LICENSE` permission | **LICENSE ENFORCEMENT** | STRIP |
| Play Core dialog activity | Ads/attribution | STRIP |
| `DaemonService` | Core runtime | KEEP |
| `ModelDownloadService` | Model mgmt | KEEP |
| `RemoteDataRepository` | AI chat + tools | KEEP |
| `ToolExecutor` | Tool execution | KEEP |
| `McpServerManager` | MCP tools | KEEP |
| `LiteRTInferenceEngine` | Local inference | KEEP |
| `TaskScheduler` | Background tasks | KEEP |
| Email/IMAP/SMTP stack | **Feature** | KEEP |
| SMS reader/sender/poller | **Feature** | KEEP |
| `NotificationListenerController` | **Feature** | KEEP |
| `BuildEnvironmentManager` | Proot build env | KEEP |
| `AndroidSandboxController` | Sandbox | KEEP |
| Dynamic UI renderer | **Feature** | KEEP |
| Skills marketplace | **Feature** | KEEP |
| Splinterlands integration | **Feature** | KEEP |
| Terminal/VT stack | **Feature** | KEEP |
| `HeartbeatManager` | Background automation | KEEP |

---

## ADT v0.948

| Component | Classification | Action |
|---|---|---|
| LlamaService | Core AI runtime | KEEP |
| LiteRtLmWorkerService | Core AI runtime | KEEP |
| AiRuntimeBootReceiver | Boot sequence | KEEP |
| KiwixService | ZIM offline wiki | KEEP |
| Whisper/STT | **Feature** | KEEP |
| Image/video generation | **Feature** | KEEP |
| TTS | **Feature** | KEEP |
| Tama system | **Feature** | KEEP |
| KnowledgeBase/RAG | **Feature** | KEEP |
| ML training services | **Feature** | KEEP |
| Proot binary deployment | **Feature** | KEEP |
| All 31 native .so libs | **Feature** | KEEP |
| Upscaler/SD models | **Feature** | KEEP |
| Billing/ads/license | None found | N/A |

---

## Global Rules

1. If unsure whether something is bloat or feature, ask before stripping.
2. If a component is part of a documented feature path, keep it.
3. License checks and paywalls are always stripped.
4. Ads and analytics are stripped unless user explicitly requests them later.
5. This policy takes precedence over automated stripping scripts.
