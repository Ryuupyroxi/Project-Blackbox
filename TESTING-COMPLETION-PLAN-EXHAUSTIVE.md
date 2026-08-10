# Exhaustive Testing & Verification Plan
**Project:** Blackbox Unified Android App  
**Modules:** AnyClaw (50 files), Kai (56), ADT (19), Core (18), App (27)  
**Total:** 170 Kotlin files  
**Constraint:** No skimming. Every present feature must be tested or explicitly marked deferred with a blocker reason. No placeholder "works on my machine" claims.

---

## Guiding Rules
1. Every test must assert observable behavior, not just "compiles".
2. If a test cannot run because of a missing dependency or environment, the blocker must be recorded in `TEST-GAPS.md`.
3. Tests must be runnable on GitHub Actions before claiming success.
4. No removing or disabling tests that fail; fix the underlying code.
5. After any code change, rerun the affected test tier.

---

## Test Tiers
| Tier | Scope | Runner | Blocking for merge |
|---|---|---|---|
| T0 | Static analysis: lint, detekt, ktlint, dependency convergence | GitHub Actions | Yes |
| T1 | Unit tests: pure Kotlin, no Android runtime | JVM test via Gradle | Yes |
| T2 | Instrumented tests: Android runtime, services, receivers | GitHub Actions (emulator) | Yes |
| T3 | UI tests: Compose, navigation, assistant layer | GitHub Actions (emulator) | Yes |
| T4 | Integration tests: module lifecycle, cross-module bus | GitHub Actions (emulator) | Yes |
| T5 | Manual verification: native libs, ADT runtime, bridges | Device + manual script | Yes (recorded) |
| T6 | Security/privacy audit: SecretStore, network, reflection | Manual + script | Yes |
| T7 | Performance: startup time, memory, service restarts | Manual + script | Yes |

---

## Module-by-Module Test Matrix

### Core (`:modules:core`)
| Feature | T0 | T1 | T2 | T3 | T4 | T5 | T6 | Notes |
|---|---|---|---|---|---|---|---|---|
| `BlackboxModule` interface contract | — | ✅ | — | — | — | — | — | onLoad/onUnload idempotency |
| `ModuleRegistry` register/resolve/unregister | — | ✅ | — | — | ✅ | — | — | thread-safety under concurrency |
| `ModuleBus` publish/subscribe | — | ✅ | — | — | ✅ | — | — | event delivery, ordering, leak |
| `ModuleManager` listInstalled | — | ✅ | — | — | — | — | — | zip parsing stub later |
| `BlackboxPreferences` DataStore read/write | — | — | ✅ | — | — | — | ✅ | key migration, encryption |
| `BlackboxDatabase` Room entities/DAOs | — | ✅ | ✅ | — | — | — | ✅ | migration v1→v2, injection |
| `SecretStore` encrypt/decrypt roundtrip | — | ✅ | — | — | — | — | ✅ | key rotation, wipe |
| `BlackboxRepository` Flow wrappers | — | ✅ | — | — | — | — | — | once DAOs are live |

### AnyClaw (`:modules:anyclaw`)
| Feature | T0 | T1 | T2 | T3 | T4 | T5 | T6 | Notes |
|---|---|---|---|---|---|---|---|---|
| `AnyClawModuleImpl` load/unload lifecycle | — | ✅ | ✅ | — | ✅ | — | — | starts GatewayService |
| `PreferencesManager` key mapping | — | ✅ | — | — | — | — | — | every AnyClaw pref key |
| `OpenRouterAuth` OAuth flow | — | ✅ | — | — | — | — | ✅ | CustomTabs redirect |
| `ArchiveUtils` ZIP extraction | — | ✅ | — | — | — | — | ✅ | path traversal, signature |
| `ProcessManager` start/stop/kill | — | ✅ | — | — | — | ✅ | — | proot native binary presence |
| `ProotManager` env setup | — | ✅ | — | — | — | ✅ | — | rootfs mount, permissions |
| `SetupManager` first-run flow | — | ✅ | — | — | — | ✅ | — | download, extract, verify |
| `GatewayService` foreground service | — | — | ✅ | — | — | ✅ | — | notification channel, restart |
| `GatewayWatchdogReceiver` + Worker | — | — | ✅ | — | — | — | — | WorkManager constraints |
| `BootReceiver` auto-start | — | — | ✅ | — | — | — | — | BOOT_COMPLETED delivery |
| `CodexService` / `OpenClawService` | — | — | ✅ | — | — | — | — | binder IPC if present |
| `PairingService` pairing flow | — | — | ✅ | — | — | — | ✅ | token exchange, expiry |
| `DeviceBridge` AudioHelper/CameraHelper | — | — | ✅ | — | — | — | — | permission gating |
| `BlackboxBootController` | — | — | ✅ | — | — | — | — | manifest-registered receiver |
| `AdRemoteConfigManager` fetch/apply | — | — | — | — | — | — | ✅ | network policy |
| Compose UI: Dashboard/Settings/Terminal/Onboarding | — | — | — | ✅ | — | — | — | navigation, state, inputs |
| Bridge commands (Discord/Telegram/WhatsApp) | — | — | — | — | — | ✅ | ✅ | end-to-end message flow |
| `ServiceBridge` binder interface | — | — | ✅ | — | ✅ | — | — | cross-process calls |

### Kai (`:modules:kai`)
| Feature | T0 | T1 | T2 | T3 | T4 | T5 | T6 | Notes |
|---|---|---|---|---|---|---|---|---|
| `KaiModuleImpl` lifecycle | — | ✅ | ✅ | — | ✅ | — | — | registers services |
| `KaiDatabase` Room entities/DAOs | — | ✅ | ✅ | — | — | — | ✅ | conversation/message CRUD |
| `EmailConnection` interface contract | — | ✅ | — | — | — | — | — | IMAP/SMTP auth, folder ops |
| `ServerAutoDetect` MX record lookup | — | ✅ | — | — | — | — | — | DNS, timeouts, fallback |
| `JvmEmailConnection` connect/send | — | — | ✅ | — | — | — | — | real IMAP/SMTP server or mock |
| `EmailPoller` IMAP IDLE/poll | — | — | ✅ | — | — | — | — | backoff, error recovery |
| `ImapClient` fetch/parse MIME | — | ✅ | — | — | — | — | — | large attachments, encoding |
| `SmtpClient` send with auth | — | ✅ | — | — | — | — | — | TLS, auth, receipts |
| `SmsReader` / `SmsSender` / `SmsPoller` | — | — | ✅ | — | — | — | — | requires SEND_SMS permission |
| `KaiNotificationListenerService` | — | — | ✅ | — | — | — | — | NotificationListener permission |
| `NotificationReader` extract text/icons | — | ✅ | — | — | — | — | — | API level variances |
| `LocalInferenceEngine` load model | — | — | — | — | — | ✅ | — | LiteRT TFLite model loading |
| `LiteRTInferenceEngine` inference | — | — | — | — | — | ✅ | — | GPU delegate, quantized model |
| `McpServerManager` start/stop/list | — | ✅ | — | — | ✅ | — | — | stdio, SSE transports |
| `SkillManager` register/execute | — | ✅ | — | — | ✅ | — | — | sandbox, permissions |
| `TaskScheduler` cron/jobs | — | ✅ | — | — | — | — | — | AlarmManager, WorkManager |
| `BuildEnvironmentManager` env vars | — | ✅ | — | — | — | — | — | Docker/proot detection |
| `SandboxController` session mgmt | — | — | — | — | — | ✅ | — | proot, termux, firejail |
| `KaiToolExecutor` tool dispatch | — | ✅ | — | — | ✅ | — | — | param validation, error surfacing |
| `KaiProviderSelector` provider pick | — | ✅ | — | — | — | — | — | fallback order, latency |
| `RemoteDataRepository` / `FakeBlackboxRepository` | — | ✅ | — | — | — | — | — | Room-backed repo |
| Compose UI: theme, models, chat | — | — | — | ✅ | — | — | — | state hoisting, preview |
| `KaiServiceCatalog` service list | — | ✅ | — | — | — | — | — | completeness against manifest |

### ADT (`:modules:adt`)
| Feature | T0 | T1 | T2 | T3 | T4 | T5 | T6 | Notes |
|---|---|---|---|---|---|---|---|---|
| `AdtModuleImpl` lifecycle + `AdtModuleLoader` | — | ✅ | ✅ | — | ✅ | — | — | startAll/stopAll services |
| `AdtNativeLoader` extraction | — | — | — | — | — | ✅ | ✅ | APK assets, lib paths, ABI |
| `AdtManifestMapper` services/receivers | — | ✅ | — | — | — | — | — | completeness vs decompiled smali |
| `AdtBootReceiver` BOOT_COMPLETED | — | — | ✅ | — | — | — | — | manifest receiver exported |
| `ZimDownloadReceiver` VIEW/DOWNLOAD_COMPLETE | — | — | ✅ | — | — | — | — | download manager integration |
| `LlamaService` foreground service | — | — | ✅ | — | — | — | — | notification, model load |
| `WhisperService` foreground service | — | — | ✅ | — | — | — | — | audio permission, model |
| `StableDiffusionService` binder IPC | — | — | ✅ | — | — | — | — | img2img, text2img, upscale |
| `VideoUpscalerService` foreground | — | — | ✅ | — | — | — | — | video I/O, memory |
| `ModelDownloadService` progress | — | — | ✅ | — | — | — | — | resume, checksum, storage |
| `AgentForegroundService` runtime | — | — | ✅ | — | — | — | — | agent loop, tool calls |
| `AdventureForegroundService` runtime | — | — | ✅ | — | — | — | — | media playback, state |
| `AiToolServerService` MCP/tool server | — | — | ✅ | — | — | — | — | HTTP, stdio, schema validation |
| `ZimShareService` share/mount | — | — | ✅ | — | — | — | — | local HTTP, file access |
| `LiteRtLmWorkerService` inference worker | — | — | ✅ | — | — | — | — | messenger IPC, model load |

### App (`:app`)
| Feature | T0 | T1 | T2 | T3 | T4 | T5 | T6 | Notes |
|---|---|---|---|---|---|---|---|---|
| `MainActivity` launch, permissions | — | — | ✅ | ✅ | — | — | — | cold start, permissions, state |
| `BlackboxApp` Application onCreate | — | — | ✅ | — | ✅ | — | — | module registration, notification channel |
| `PermissionCoordinator` request/map | — | — | ✅ | — | — | — | — | all Android permissions |
| `BlackboxAssistantService` VoiceInteractionService | — | — | ✅ | — | ✅ | — | — | assist structure, routing |
| `AssistantIntentRouter` route dispatch | — | ✅ | — | — | ✅ | — | — | module bus delivery |
| `AssistTextExtractor` text extraction | — | ✅ | — | — | — | — | — | view hierarchy, web views |
| `ProotSupervisor` state machine | — | ✅ | — | — | — | ✅ | — | start/stop/health transitions |
| `BlackboxPreferences` all preference keys | — | — | ✅ | — | — | — | ✅ | read/write every key |
| `UnifiedDaos` channel/assistant CRUD | — | — | ✅ | — | ✅ | — | — | Room flow emissions |
| Compose navigation (5 screens) | — | — | — | ✅ | — | — | — | back stack, state restore |
| `BridgeCommandRouter` dispatch | — | ✅ | — | — | ✅ | — | — | AnyClaw/Kai/ADT bridges |

---

## Test Infrastructure
```
app/src/test/           ← JVM unit tests (T1)
app/src/androidTest/    ← Instrumented tests (T2-T4)
.github/workflows/
  test.yml              ← CI matrix: lint + unit + instrumented + ui
TEST-GAPS.md            ← Deferred tests with blockers
```

### CI Workflow (`test.yml`)
- **Lint job**: `./gradlew lintDebug detekt`
- **Unit job**: `./gradlew testDebugUnitTest`
- **Instrumented job**: `./gradlew connectedDebugAndroidTest` (API 26, 34 emulator)
- **UI job**: `./gradlew connectedDebugAndroidTest` with Compose tests
- **Merge gate**: all jobs must pass

### Test Categories to Implement
1. **Module lifecycle tests**: register, load, unregister, verify `onLoad`/`onUnload` called exactly once per module, verify services started/stopped.
2. **ModuleBus tests**: publish/subscribe, multiple subscribers, event ordering, lifecycle cleanup.
3. **Data layer tests**: Room DAO CRUD, DataStore read/write, SecretStore encrypt/decrypt roundtrip, migration v1→v2.
4. **Service tests**: start each ADT/AnyClaw/Kai service, verify `onCreate`/`onStartCommand`, stop, verify `onDestroy`.
5. **Receiver tests**: simulate BOOT_COMPLETED, DOWNLOAD_COMPLETE, verify receivers fire.
6. **Assistant tests**: synthetic `AssistStructure`, verify extraction and routing.
7. **Compose UI tests**: `createComposeRule`, verify all 5 screens render, navigation flows, back button.
8. **Bridge tests**: mock Discord/Telegram/WhatsApp endpoints, verify send/receive through `ServiceBridge`.
9. **Email/SMS tests**: mock IMAP/SMTP/SMS provider, verify `ImapClient`, `SmtpClient`, `SmsReader` flows.
10. **Native loader tests**: mock APK assets, verify `AdtNativeLoader` extraction, lib discovery.
11. **Security tests**: `SecretStore` key storage, no plaintext secrets in logs, network security config.
12. **Permission tests**: request each dangerous permission, verify graceful denial handling.

---

## Execution Order
1. Scaffold test directories and CI workflow.
2. T0: Add lint/detekt/ktlint plugins and configs.
3. T1: Write unit tests for Core module (ModuleBus, ModuleRegistry, BlackboxPreferences, BlackboxDatabase, SecretStore).
4. T1: Write unit tests for AnyClaw auth, proot, archive utils, bridge stubs.
5. T1: Write unit tests for Kai data models, email clients (mocked), MCP, skill manager, tool executor.
6. T1: Write unit tests for ADT manifest mapper, module loader, native loader.
7. T1: Write unit tests for App layer (AssistantIntentRouter, ProotSupervisor, BridgeCommandRouter).
8. T2: Write instrumented tests for service start/stop lifecycle.
9. T2: Write instrumented tests for receivers (BOOT_COMPLETED, DOWNLOAD_COMPLETE).
10. T2: Write instrumented tests for DataStore and Room database on device.
11. T3: Write Compose UI tests for all 5 screens and navigation.
12. T4: Write integration tests for full module lifecycle (register → load → bus → unload).
13. T5: Manual device test checklist for ADT native libs, AnyClaw bridges, Kai email/SMS.
14. T6: Security audit checklist.
15. T7: Performance baseline.
16. Populate `TEST-GAPS.md` with any blockers.

---

## Success Criteria
- **Zero skipped or disabled tests** in CI.
- **All T0–T4 tiers green** on GitHub Actions before merge.
- **T5–T7 completed and documented** in `TEST-GAPS.md` or `PHASE-3-REPORT.md`.
- Every module has at least one test per public class/method that contains logic (not just empty stubs).
- Every manifest-declared service/receiver has a lifecycle test.
- Every preference key has a read/write test.
- Every bridge command has a unit test.
- Every security-sensitive class (`SecretStore`, `OpenRouterAuth`, `AdtNativeLoader`) has a security-focused test.

---

## Out of Scope (Deferred)
- Actual GPU inference performance benchmarking (requires device with GPU delegate).
- Long-running stability soak tests (days).
- Localization beyond English strings.
- Accessibility scanner pass.
- Fuzzing of native libraries.
