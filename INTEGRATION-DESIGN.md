# Project Blackbox: Integration Design
**Status:** Design with Verified Audit Findings  
**Sources:** AnyClaw 2.1.565 + Kai 9000 3.0.0 + ADT 0.948 decompilation  
**Location:** `/home/Ryuu/Project-Blackbox-worktree/`

---

## 1. Verified Class Counts

| App | App-specific classes | Total smali files |
|---|---|---|
| AnyClaw | 1,400 | 47,036 |
| Kai 9000 | 1,809 | 19,151 |
| ADT | 3,756 | 40,308 |

**Prior estimates were low.** Real footprint is significantly larger.

---

## 2. License Check Status

| App | License Check | Action |
|---|---|---|
| AnyClaw | No | Keep |
| Kai 9000 | **Yes — Pairip + CHECK_LICENSE** | **MUST STRIP** |
| ADT | No | Keep |

Kai uses `com.pairip.licensecheck.LicenseActivity` and `com.android.vending.CHECK_LICENSE`. This must be removed before integration.

---

## 3. Proot Unification

- AnyClaw: `ProotManager`, `ProcessManager`, `SetupManager`, `TarInstaller`, `AssetInstaller`
- Kai: `BuildEnvironmentManager`, `BuildProotExecutor`, `DebianRootfsInstaller`, `BuildFileBrowser`
- ADT: `BinaryRepository`, proot layer in `data/proot/`

**Strategy:** Merge into single `BlackboxProotManager` with Kai's overlay approach + AnyClaw's bundle update policy + ADT's binary deployment.

---

## 4. AI Provider Merger

- AnyClaw: OpenRouter OAuth, custom OpenAI-compatible, Brave Search
- Kai: 26 providers via `Service` enum, `RemoteDataRepository`, `ToolExecutor`, `McpServerManager`
- ADT: Ollama, LiteRT, HuggingFace, local inference

**Strategy:** Keep Kai's `RemoteDataRepository` + `Service` enum, add ADT's Ollama/LiteRT/HuggingFace as additional providers, merge model catalogs.

---

## 5. Messaging Bridges

- AnyClaw: Discord, Telegram, WhatsApp (all in `PreferencesManager`)
- Kai: Email (IMAP/SMTP), SMS, notifications
- ADT: No messaging bridges

**Strategy:** Keep all AnyClaw bridges + Kai email/SMS/notifications.

---

## 6. Strippable Components

| Component | Source | Action |
|---|---|---|
| Pairip license framework | Kai | STRIP |
| CHECK_LICENSE permission | Kai | STRIP |
| Play Core | Kai | STRIP |
| AdManager, AdRemoteConfigManager | AnyClaw | STRIP |
| AnalyticsManager | AnyClaw | STRIP |
| PremiumBillingManager | AnyClaw | STRIP |
| Firebase | AnyClaw | STRIP |
| Google Mobile Ads | AnyClaw | STRIP |

---

## 7. Services to Keep

### Core Runtime
- BlackboxRuntimeService (unified)
- DaemonService (absorbed)
- GatewayService (absorbed)
- LlamaService (absorbed)

### AI/ML
- LiteRtLmWorkerService
- AiToolServerService
- LlamaClientService
- ModelDownloadService

### Media
- LlamaCallService
- LiveTranslatorService
- WhisperService
- OnnxTtsGenerationService
- MediaTranslationForegroundService
- MangaTranslationForegroundService

### Image/Video
- StableDiffusionService
- OnnxImageGenerationService
- OnnxBackgroundRemovalService
- VideoGenerationService
- VideoUpscalerService
- SubtitleBurnService
- TamaArtworkGenerationService
- TamaDeepDreamService

### Data/Tools
- DownloadService
- FileServerService
- ModelShareService
- KiwixService
- ZimShareService
- KnowledgeBaseIndexingService
- DeepResearchService

### Agent/Game
- AgentForegroundService
- AdventureForegroundService
- DatasetForegroundService

### ML Training
- QuadtrixTrainingService
- DistributedService
- SdDistributedService

### Email/SMS
- EmailPoller (via RemoteDataRepository)
- SmsReader/SmsSender/SmsPoller

---

## 8. Implementation Phases

### Phase 1: Runtime (Week 1)
- Create BlackboxRuntimeService
- Merge proot layers
- Strip license/ads/billing
- Create unified manifest

### Phase 2: Data Layer (Week 2)
- Merge Room schemas
- Migrate settings from 3 sources
- Create unified chat DB

### Phase 3: AI + MCP (Week 3)
- Merge provider enums
- Port McpServerManager
- Add Ollama/LiteRT providers
- Merge model catalogs

### Phase 4: Chat + Tools (Week 4)
- Port RemoteDataRepository
- Port ToolExecutor
- Merge dynamic UI renderer
- Port conversation flow

### Phase 5: Background (Week 5)
- Merge TaskSchedulers
- Port email/SMS/notifications
- Merge heartbeat
- Port scheduled tasks

### Phase 6: Bridges + Extras (Week 6)
- Port messaging bridges
- Port build environment
- Port skills marketplace
- Port Splinterlands

### Phase 7: Testing + Polish (Week 7)
- Unit tests
- Integration tests
- Device testing
- APK optimization

---

## 9. Risks

- **Pairip license:** Must be fully stripped or integration will fail
- **APK size:** 47K + 19K + 40K smali files = massive method count, multidex required
- **Proot compatibility:** 3.6 GB device may struggle with full merged rootfs
- **Battery drain:** 30+ foreground services
- **Memory pressure:** Multiple ML runtimes competing for RAM

---

## 10. Open Questions

1. Can Pairip license be cleanly stripped without breaking app initialization?
2. Does Kai's `Application` class have side effects beyond license checking?
3. How does ADT's `BinaryRepository` interact with Kai's `BuildEnvironmentManager`?
4. Can we use Kai's `RemoteDataRepository` as-is, or does it need refactoring for unified provider model?
