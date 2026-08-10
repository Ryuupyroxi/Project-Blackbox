# Project Blackbox: Combination Strategy Research
**Date:** 2026-08-09  
**Status:** Strategy draft — pending audit corrections  
**Location:** `/home/Ryuu/Project-Blackbox-worktree/`

---

## 1. Why Direct APK Merge Is Wrong

Naive approach: merge all dex files, all manifests, all resources into one fat APK.

**Problems:**
- Combined smali: ~66,000+ files → method count explosion, multidex required
- 3 different Application classes: `AnyClawApp`, `com.pairip.application.Application`, ADT equivalent
- 3 different package names → R class collisions
- 3 different build configs, compile SDKs, dependency sets
- Conflicting permissions (e.g., `CHECK_LICENSE` from Kai must be stripped, but removing it from merged manifest requires patching)
- Service name collisions in merged manifest
- Resource ID conflicts across merged resources.arsc files
- Pairip license framework cannot coexist with stripped version

**Verdict:** Do not attempt direct APK merge via apktool.

---

## 2. Correct Strategy: Modular Runtime Shell

Treat Blackbox as a **runtime orchestrator**, not a monolithic merged APK.

### Architecture
```
Blackbox APK (shell)
├── Core runtime: BlackboxRuntimeService
├── Module loader: loads feature modules from internal storage
├── Bridge server: JSON-over-file IPC in app-private storage
├── Shared data layer: Room DB, DataStore, encrypted secrets
└── UI layer: unified Compose shell

Modules (downloaded/extracted on first run)
├── anyclaw-module/
│   ├── anyclaw.dex → smali_classes2 merge
│   ├── assets/ → AdManager, Analytics (bloat stripped at build time)
│   ├── proot/ → OpenClaw rootfs, Codex UI
│   └── bridges/ → Discord/Telegram/WhatsApp
│
├── kai-module/
│   ├── kai.dex → smali_classes2 merge (Pairip stripped)
│   ├── proot/ → BuildEnvironmentManager rootfs
│   ├── data/ → Room schemas, Email/SMS/Notification stacks
│   ├── ui/ → Dynamic UI renderer, markdown, sandbox
│   ├── inference/ → LiteRT engine, model download
│   ├── mcp/ → McpServerManager
│   └── skills/ → Skills marketplace
│
└── adt-module/
    ├── adt.dex → smali_classes2 merge
    ├── proot/ → BinaryRepository deployments
    ├── services/ → Media/ML/Image/ZIM/Tama services
    ├── native/ → 31 .so libraries
    └── models/ → Upscaler, Stable Diffusion weights
```

### Why This Works
1. **No method-count explosion in base APK**: base shell stays under 64K methods; modules loaded dynamically
2. **No manifest conflicts**: only one Application class, one MainActivity
3. **Clean license stripping**: Kai's Pairip stripped from module before packaging, not patched post-merge
4. **Proot isolation**: each module's proot environment is self-contained
5. **OTA updates**: modules can be updated independently
6. **Opt-in loading**: user enables AnyClaw bridges, Kai inference, ADT media as needed

---

## 3. Implementation Path

### Phase 1: Base Shell (Week 1-2)
- Create new Blackbox Android project
- Single Application class: `BlackboxApp`
- Single MainActivity with bottom nav: Chat | Bridges | Settings | Terminal
- Core service: `BlackboxRuntimeService`
- Data layer: Room DB + DataStore + EncryptedSharedPreferences
- Bridge server: file-based IPC in `files/bridge/`
- Proot supervisor: manages proot lifecycle

### Phase 2: Module Loader (Week 3)
- Module format: ZIP with `module.json`, dex files, assets, native libs
- Loader: `ModuleManager` that extracts, validates, loads dex
- DEX merging: use `InMemoryDexClassLoader` or `PathClassLoader` with appended classpaths
- Isolation: each module's classes loaded in its own ClassLoader, preventing package conflicts
- Validation: checksum module.json, reject tampered modules

### Phase 3: Kai Module (Week 4-5)
- Strip Pairip from decompiled smali
- Merge Kai's `com.inspiredandroid.kai` classes into module.dex
- Port `RemoteDataRepository`, `ToolExecutor`, `McpServerManager`
- Port `LiteRTInferenceEngine`, `ModelDownloadService`
- Port email/SMS/notification stacks
- Port dynamic UI renderer
- Port skills marketplace
- Port BuildEnvironmentManager proot layer
- Merge Room schemas into Blackbox DB

### Phase 4: AnyClaw Module (Week 6)
- Strip ads/analytics/billing from decompiled smali
- Merge `app.anyclaw` classes into module.dex
- Port `GatewayService` → absorb into BlackboxRuntimeService
- Port `ProcessManager` → bridge to BlackboxProotManager
- Port `SetupManager` → module's rootfs installer
- Port messaging bridges: Discord, Telegram, WhatsApp
- Strip AdManager, AnalyticsManager, Firebase, Play Core

### Phase 5: ADT Module (Week 7-8)
- Merge ADT classes into module.dex
- Merge 31 native .so libraries
- Port media services: Whisper, TTS, StableDiffusion, video upscaler
- Port ML services: LiteRT, training, dataset
- Port ZIM/Kiwix service
- Port Tama system
- Merge BinaryRepository with Kai's BuildEnvironmentManager

### Phase 6: UI Integration (Week 9)
- Unified chat screen using Kai's `ChatScreenKt` + dynamic UI renderer
- Bridge management screen using AnyClaw's settings patterns
- Terminal screen using Kai's VT terminal
- Settings screen merging all three preference systems
- Model selection using Kai's `ModelSelectionSheetKt`
- Notification/email/SMS UI

### Phase 7: Testing + Polish (Week 10)
- Unit tests per module
- Integration tests for bridge protocols
- Device testing on target hardware
- APK size optimization
- OTA module update mechanism

---

## 4. Key Technical Decisions

### DEX Loading Strategy
**Decision:** Use `InMemoryDexClassLoader` or multi-dex with `DexPathList` append.

**Rationale:**
- Allows loading module dex files at runtime without rebuilding base APK
- Each module gets its own ClassLoader, preventing classpath collisions
- `BlackboxApp` loads base classes; `ModuleClassLoader` loads module classes
- Shared interfaces in base APK, implementations in modules

### Service Strategy
**Decision:** Single `BlackboxRuntimeService` with subsystem dispatchers.

**Rationale:**
- Android 14 limits foreground service types; multiple services requires multiple notification channels
- Single service with `startForeground` using `dataSync` + `mediaPlayback` + `camera` combined
- Subsystems run as coroutines within service, not separate services
- Reduces notification clutter and battery impact

### Proot Strategy
**Decision:** Merge AnyClaw's `SetupManager` + Kai's `BuildEnvironmentManager` + ADT's `BinaryRepository`.

**Rationale:**
- All three use proot; unified approach reduces disk duplication
- Kai's overlay approach for incremental updates
- AnyClaw's bundle update policy for versioned rootfs
- ADT's binary deployment for native tools
- Shared proot binary at `files/proot/`

### License Check Strategy
**Decision:** Strip Pairip at build time using smali patch before module packaging.

**Rationale:**
- Remove `com.pairip.*` classes from module.dex
- Remove `LicenseActivity` from merged manifest
- Remove `CHECK_LICENSE` permission
- Remove `com.pairip.application.Application` as Application class
- Replace with `BlackboxApp`

### Data Layer Strategy
**Decision:** Single Room DB with tables from all three apps.

**Rationale:**
- Kai already uses Room; ADT likely uses Room or SQLite
- Merge schemas: `conversations`, `messages`, `emails`, `sms`, `notifications`, `tasks`, `memories`, `models`, `mcp_servers`, `skills`
- Migration from AnyClaw SharedPreferences at first run
- Migration from Kai AppSettings at first run
- Encrypted columns for API keys, bot tokens

---

## 5. Risk Mitigation

| Risk | Mitigation |
|---|---|
| Method count overflow | Base shell < 64K; modules loaded dynamically |
| Pairip breakage | Strip at build time; validate with test build |
| Proot incompatibility | Test on target device (3.6 GB RAM, ARM64) |
| Battery drain | Single foreground service; optimize wake locks |
| APK size | Base shell ~50 MB; modules downloaded on demand |
| Module tampering | SHA-256 checksums in module.json; signature validation |
| ClassLoader leaks | WeakReference caches; explicit unload on module disable |
| Data migration | Versioned migrations; backup before migration |

---

## 6. Open Research Items

1. Can `InMemoryDexClassLoader` handle 1,400-class AnyClaw module on 3.6 GB device?
2. Does Kai's `RemoteDataRepository` depend on Koin/Hilt DI, or can it run standalone?
3. How does AnyClaw's `rish` Shizuku wrapper interact with proot — can it be abstracted?
4. What's the actual method count for merged Kai + AnyClaw + ADT classes?
5. Does ADT's `BinaryRepository` require specific native library loading order?

---

## 7. Next Actions

1. **Wait for audit subagents** to correct reference docs
2. **Extract Kai DI config** — check for Koin/Hilt modules in `AppModuleKt`
3. **Calculate exact method count** for each module after stripping
4. **Test InMemoryDexClassLoader** with 1,400-class payload on target device
5. **Build proof-of-concept base shell** with module loader skeleton
6. **Strip Pairip from Kai module** and verify boot without license check
7. **Merge Room schemas** and write migration from AnyClaw SharedPreferences
