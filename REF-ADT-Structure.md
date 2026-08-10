# REF-ADT-Structure.md
**AI-Doomsday-Toolbox v0.948 — Complete Component Inventory**  
**Source:** `/home/Ryuu/Project-Blackbox-artifacts/adt_latest.apk` → `/home/Ryuu/Project-Blackbox-worktree/adt-decompiled/`  
**Date:** 2026-08-09  
**Status:** Verified — valid APK, decompiled with apktool 2.7.0

---

## 1. Identity

| Field | Value |
|---|---|
| Package | `com.manuxd32.aidoomsdaytoolbox` |
| Version | 0.948 |
| Compile SDK | 35 (Android 15) |
| Min SDK | 26 |
| Main Activity | `com.example.llamadroid.MainActivity` |
| App Theme | `Theme.LlamaDroid` |

**Note:** Uses `com.example.llamadroid` as implementation package.

---

## 2. Size Breakdown

| Component | Value |
|---|---|
| Total smali files | 40,308 |
| App-specific classes (`com/example/llamadroid`) | 3,756 |
| Native libraries (.so) | 62 |
| Dex files | 3+ (smali, smali_classes2, smali_classes3) |

---

## 3. Permissions (16 explicit + 1 custom)

| Permission | Purpose |
|---|---|
| `INTERNET` | Network access |
| `FOREGROUND_SERVICE` | Runtime service |
| `FOREGROUND_SERVICE_DATA_SYNC` | Data sync |
| `FOREGROUND_SERVICE_MEDIA_PROCESSING` | Media processing |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Media playback |
| `FOREGROUND_SERVICE_MICROPHONE` | Voice recording |
| `POST_NOTIFICATIONS` | Notifications |
| `VIBRATE` | Haptic feedback |
| `USE_FULL_SCREEN_INTENT` | Alarm/notification full screen |
| `WAKE_LOCK` | Keep device awake |
| `RECEIVE_BOOT_COMPLETED` | Auto-start on boot |
| `RECORD_AUDIO` | Voice recording |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Prevent Doze |
| `ACCESS_NETWORK_STATE` | Network check |
| `ACCESS_WIFI_STATE` | WiFi state |
| `CHANGE_WIFI_MULTICAST_STATE` | Multicast |
| `com.manuxd32.aidoomsdaytoolbox.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | Internal receiver protection |

**Note:** NO billing, NO ads, NO license check permissions.

---

## 4. Activities

| Activity | Purpose |
|---|---|
| `com.example.llamadroid.MainActivity` | Main entry point |
| `com.example.llamadroid.service.OrganizerAlarmRingActivity` | Alarm ring UI |

---

## 5. Services (33 total)

### AI Core
| Service | Foreground Type | Purpose |
|---|---|---|
| `com.example.llamadroid.service.LlamaService` | dataSync | Core AI runtime |
| `com.example.llamadroid.service.LiteRtLmWorkerService` | — | LiteRT local model worker (separate process) |
| `com.example.llamadroid.service.LlamaClientService` | dataSync | Llama client |

### Media/Audio
| Service | Foreground Type | Purpose |
|---|---|---|
| `com.example.llamadroid.service.LlamaCallService` | mediaPlayback\|microphone | Voice calls |
| `com.example.llamadroid.service.LiveTranslatorService` | mediaPlayback\|microphone | Real-time translation |
| `com.example.llamadroid.service.WhisperService` | dataSync | Whisper STT |

### Image/Video Generation
| Service | Foreground Type | Purpose |
|---|---|---|
| `com.example.llamadroid.service.StableDiffusionService` | — | Stable Diffusion |
| `com.example.llamadroid.service.OnnxImageGenerationService` | — | ONNX image gen |
| `com.example.llamadroid.service.OnnxTtsGenerationService` | — | TTS generation |
| `com.example.llamadroid.service.OnnxBackgroundRemovalService` | — | Background removal (separate process) |
| `com.example.llamadroid.service.TamaArtworkGenerationService` | — | Tama artwork |
| `com.example.llamadroid.service.TamaDeepDreamService` | dataSync | Deep Dream |
| `com.example.llamadroid.service.VideoGenerationService` | — | Video generation |
| `com.example.llamadroid.service.OrganizerAlarmRingingService` | mediaPlayback | Alarm ringing |
| `com.example.llamadroid.service.VideoUpscalerService` | dataSync | Video upscaler |
| `com.example.llamadroid.service.SubtitleBurnService` | dataSync | Subtitle burning |

### Data/Tools
| Service | Foreground Type | Purpose |
|---|---|---|
| `com.example.llamadroid.service.DownloadService` | dataSync | Downloads |
| `com.example.llamadroid.service.FileServerService` | dataSync | File server |
| `com.example.llamadroid.service.ModelShareService` | dataSync | Model sharing |
| `com.example.llamadroid.service.ZimShareService` | dataSync | ZIM sharing |
| `com.example.llamadroid.service.AiToolServerService` | dataSync | AI tool server |
| `com.example.llamadroid.service.SDEnvironmentService` | dataSync | SD environment |

### ZIM/Wiki
| Service | Foreground Type | Purpose |
|---|---|---|
| `com.example.llamadroid.service.KiwixService` | dataSync | Kiwix ZIM wiki |

### ML Training
| Service | Foreground Type | Purpose |
|---|---|---|
| `com.example.llamadroid.service.QuadtrixTrainingService` | dataSync | ML training |
| `com.example.llamadroid.service.DistributedService` | dataSync | Distributed training |
| `com.example.llamadroid.service.SdDistributedService` | dataSync | SD distributed |

### Agent/Game
| Service | Foreground Type | Purpose |
|---|---|---|
| `com.example.llamadroid.service.AgentForegroundService` | dataSync | Agent runtime |
| `com.example.llamadroid.service.AdventureForegroundService` | dataSync | Adventure game |
| `com.example.llamadroid.service.DatasetForegroundService` | dataSync | Dataset management |

### Knowledge/Search
| Service | Foreground Type | Purpose |
|---|---|---|
| `com.example.llamadroid.service.KnowledgeBaseIndexingService` | dataSync | RAG indexing |
| `com.example.llamadroid.service.DeepResearchService` | dataSync | Deep research |
| `com.example.llamadroid.service.MediaTranslationForegroundService` | dataSync | Media translation |
| `com.example.llamadroid.service.MangaTranslationForegroundService` | dataSync | Manga translation |
| `com.example.llamadroid.service.LlamaScheduledTaskService` | dataSync | Scheduled tasks |

### WorkManager System
| Service | Purpose |
|---|---|
| `androidx.work.impl.foreground.SystemForegroundService` | WorkManager foreground |

---

## 6. Receivers

| Receiver | Purpose |
|---|---|
| `com.example.llamadroid.service.ZimDownloadReceiver` | ZIM download events (exported=true) |
| `com.example.llamadroid.service.AiRuntimeBootReceiver` | **Core boot receiver** (exported=true) |
| `com.example.llamadroid.tama.notifications.TamaNotificationReceiver` | Tama notifications |
| `com.example.llamadroid.service.OrganizerAlarmReceiver` | Alarm events |
| `com.example.llamadroid.service.OrganizerAlarmBootReceiver` | Alarm boot |
| `com.example.llamadroid.service.LlamaScheduledTaskReceiver` | Scheduled tasks |
| `com.example.llamadroid.service.LlamaScheduledTaskBootReceiver` | Task boot |

---

## 7. Widgets (6)

| Widget | Type |
|---|---|
| `OrganizerCalendarWidgetProvider` | Calendar widget |
| `OrganizerUpcomingEventsWidgetProvider` | Events widget |
| `NoteDisplayWidgetProvider` | Notes widget |
| `NoteDisplayWidgetConfigActivity` | Widget config |
| `NoteDisplayWidgetService` | Widget remote views |
| `OrganizerCalendarUpcomingEventsService` | Widget remote views |
| `TamaPetWidgetProvider` | Tama pet widget |
| `TamaFarmWidgetProvider` | Tama farm widget |
| `TamaFarmWidgetConfigActivity` | Widget config |

---

## 8. Providers

| Provider | Purpose |
|---|---|
| `com.manuxd32.aidoomsdaytoolbox.fileprovider` | File sharing |
| `com.manuxd32.aidoomsdaytoolbox.mlkitinitprovider` | ML Kit init |
| `com.manuxd32.aidoomsdaytoolbox.androidx-startup` | AndroidX startup |
| `androidx.core.content.FileProvider` | File sharing |
| `androidx.startup.InitializationProvider` | AndroidX startup |
| `androidx.room.MultiInstanceInvalidationService` | Room DB |
| `androidx.profileinstaller.ProfileInstallReceiver` | Profile install |
| `com.google.android.datatransport.runtime.backends.TransportBackendDiscovery` | DataTransport |
| `com.google.android.datatransport.runtime.scheduling.jobscheduling.JobInfoSchedulerService` | Job scheduling |
| `com.google.mlkit.common.internal.MlKitComponentDiscoveryService` | ML Kit |
| `com.google.android.gms.common.api.GoogleApiActivity` | Google Play Services |
| `com.google.android.play.core.assetpacks.AssetPackExtractionService` | Play asset packs (enabled=false) |
| `com.google.android.play.core.assetpacks.ExtractionForegroundService` | Play asset packs (enabled=false) |

---

## 9. Key App Packages (3,756 classes)

### com/example/llamadroid/
- `MainActivity` — App entry point
- Build config, extensions, utilities

### com/example/llamadroid/data/
- `api/` — API clients
- `backup/` — Backup system
- `binary/` — Binary repository
- `dao/` — Data Access Objects
- `db/` — Database layer
- `model/` — Data models
- `proot/` — Proot deployment system
- `repository/` — Repository implementations

### com/example/llamadroid/service/
- `LlamaService` — Core AI runtime
- `LiteRtLmWorkerService` — LiteRT worker
- `AiToolServerService` — Tool server
- `KiwixService` — ZIM wiki
- All 33 services listed above

### com/example/llamadroid/tama/
- `adventure/` — Adventure game
- `data/` — Tama data models
- `db/` — Tama database
- `game/` — Game logic
- `notifications/` — Tama notifications
- `rpg/` — RPG elements
- `ui/` — Tama UI

### com/example/llamadroid/onnx/
- `onnxruntime/platform/` — ONNX platform
- `onnxruntime/providers/` — ONNX providers

### com/example/llamadroid/ui/
- `ai/llama/` — Llama AI UI

### com/example/llamadroid/util/
- Utilities

### com/example/llamadroid/widget/
- Calendar, events, notes, Tama widgets
- Widget config activities
- Widget remote view services

---

## 10. Native Libraries (62 .so files)

### LiteRT / LLM
- `libLiteRt.so`, `libLiteRtClGlAccelerator.so`, `liblitertlm_jni.so`
- `libllama.so`
- `libllama-server_armv9.so`, `libllama-server_baseline.so`, `libllama-server_dotprod.so`, `libllama-server_snapdragon_opencl.so`
- `libllama-bench_armv9.so`, `libllama-bench_baseline.so`, `libllama-bench_dotprod.so`, `libllama-bench_snapdragon_opencl.so`
- `libggml.so`, `libggml-cpu.so`, `libggml-base.so`
- `libmtmd_armv9.so`, `libmtmd_baseline.so`, `libmtmd_dotprod.so`
- `libwhisper-cli_armv9.so`, `libwhisper-cli_baseline.so`, `libwhisper-cli_dotprod.so`
- `libwhisper.so.1.so`

### ONNX / ML
- `libonnxruntime.so`, `libonnxruntime4j_jni.so`
- `libncnn.so`
- `libmlkit_google_ocr_pipeline.so`
- `libAIDOCL.so`
- `libandroidx.graphics.path.so`

### Stable Diffusion / Image
- `libsd_armv9.so`, `libsd_baseline.so`, `libsd_dotprod.so`, `libsd_snapdragon_vulkan.so`
- `libsd-rpc-server_armv9.so`, `libsd-rpc-server_baseline.so`, `libsd-rpc-server_dotprod.so`
- `librpc-server_armv9.so`, `librpc-server_baseline.so`, `librpc-server_dotprod.so`
- `librealcugan-ncnn.so`, `librealsr-ncnn.so`

### Kiwix / ZIM
- `libkiwix-manage_armv9.so`, `libkiwix-manage_baseline.so`, `libkiwix-manage_dotprod.so`
- `libkiwix-serve_armv9.so`, `libkiwix-serve_baseline.so`, `libkiwix-serve_dotprod.so`

### ML Training
- `libquadtrix_trainer_armv9.so`, `libquadtrix_trainer_baseline.so`, `libquadtrix_trainer_dotprod.so`

### Media
- `libffmpeg_armv9.so`, `libffmpeg_baseline.so`, `libffmpeg_dotprod.so`
- `libffprobe_armv9.so`, `libffprobe_baseline.so`, `libffprobe_dotprod.so`
- `libx264.so`, `libx264.so.164.so`
- `libasound.so`

### System
- `libc++_shared.so`, `libcpufeatures.so`, `libiconv.so`, `libomp.so`

---

## 11. Boot Sequence (Verified)

```
AiRuntimeBootReceiver (exported=true)
├── Boot completed
└── startForegroundService(LlamaService)
    └── LlamaService.onCreate()
        ├── Create foreground notification
        ├── Start LiteRtLmWorkerService (separate process)
        ├── Initialize proot layer
        ├── Start KiwixService
        └── Start KnowledgeBaseIndexingService
```

---

## 12. Key Classes

### Data Layer
- `BinaryRepository` — Proot binary deployment
- `KnowledgeBaseRepository` — RAG knowledge base
- Backup system
- DAOs for Room DB

### Proot Layer
- `com.example.llamadroid.data.proot` — Proot deployment

### Tama System
- `TamaNotificationReceiver` — Notification handling
- Adventure/RPG game logic
- Tama UI

### Widget Layer
- Calendar widget
- Upcoming events widget
- Notes widget
- Tama pet widget
- Tama farm widget

---

## 13. Strippable Components

| Component | Action |
|---|---|
| Play Core asset packs (disabled) | STRIP |
| ML Kit discovery service | STRIP if not using ML Kit Vision |
| Google Play Services activity | STRIP if not using GMS |
| DataTransport backends | STRIP if not using analytics |
| WorkManager system alarms | STRIP if using own scheduler |
| AndroidX profile installer | STRIP |

**Note:** No billing, no ads, no license check to strip.

---

## 14. Integration Points

### AI Runtime
- `LlamaService` → absorb into BlackboxRuntimeService
- `LiteRtLmWorkerService` → keep as separate process
- `LlamaClientService` → merge with Kai's RemoteDataRepository
- `AiToolServerService` → merge with Kai's McpServerManager

### Media
- `WhisperService` → keep
- `OnnxTtsGenerationService` → keep
- `LiveTranslatorService` → keep
- `StableDiffusionService` → keep
- `OnnxImageGenerationService` → keep
- `VideoGenerationService` → keep
- `VideoUpscalerService` → keep
- `SubtitleBurnService` → keep

### ZIM/Wiki
- `KiwixService` → keep
- `ZimShareService` → keep
- `ZimDownloadReceiver` → keep

### Knowledge
- `KnowledgeBaseIndexingService` → merge with Kai's HeartbeatManager
- `DeepResearchService` → keep

### Tama
- `AgentForegroundService` → keep
- `AdventureForegroundService` → keep
- `DatasetForegroundService` → keep
- Tama widgets → keep

### ML Training
- `QuadtrixTrainingService` → keep
- `DistributedService` → keep
- `SdDistributedService` → keep

---

## 15. Open Questions for ADT

1. Does `BinaryRepository` handle model downloads, or only proot binaries?
2. How does `KnowledgeBaseRepository` index content — local embedding model or remote?
3. Are the 62 .so libraries all required, or can unused variants be stripped?
4. Does `MainActivity` contain actual UI, or is it just a stub?
