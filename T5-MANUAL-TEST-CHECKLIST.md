# Tier 5 — Manual Device Verification Checklist
**App:** Blackbox Unified (`com.blackbox`)  
**Modules:** AnyClaw, Kai, ADT  
**Environment:** Physical Android device or emulator with API 26+  
**Blockers:** Model files, network credentials, native libs (documented in `TEST-GAPS.md`)

---

## T5.1 ADT Native Library Extraction (`AdtNativeLoader`)
| Step | Action | Expected Result | Pass/Fail | Notes |
|---|---|---|---|---|
| 1 | Install debug APK on device | App launches without crash | | |
| 2 | Open Modules screen → ADT section | ADT module listed as “loaded” or “available” | | |
| 3 | Trigger native lib extraction from ADT runtime | `AdtNativeLoader` extracts ABI-matched `.so` files from APK assets to app private storage | | Requires APK built with `jniLibs` |
| 4 | Inspect extracted lib paths via `adb shell run-as com.blackbox ls /data/data/com.blackbox/files/adt-native/` | `.so` files present for device ABI | | Blocker: no native libs packaged yet |
| 5 | Delete extracted libs → re-extract | Idempotent: extraction succeeds again | | |

**Blocker:** No native `.so` files packaged in current build. Test is gated until ADT native assets are added.

---

## T5.2 ADT Runtime Services (Foreground + Bound)
| Step | Action | Expected Result | Pass/Fail | Notes |
|---|---|---|---|---|
| 1 | Grant all requested permissions (notification, storage, mic, camera) | Permissions dialog dismissed, grants recorded | | |
| 2 | Start ADT module from Modules screen | `AdtUnifiedRuntimeService` starts, notification channel visible in status bar | | |
| 3 | Open notification shade | Notification titled “Blackbox ADT Runtime” present | | |
| 4 | Bind to `LlamaService` via `AdtModuleLoader` | `onBind` returns non-null `IBinder` | | Blocker: no model file |
| 5 | Send inference request to `LlamaService` | Service returns generated text or graceful “no model” error | | Blocker: `llama-model.gguf` missing |
| 6 | Start `WhisperService` with audio intent | Service starts, requests RECORD_AUDIO if not granted | | Blocker: no whisper model |
| 7 | Start `StableDiffusionService` with text prompt | Service returns bitmap or error | | Blocker: no SD model |
| 8 | Trigger `AgentForegroundService` tool call | Agent loop executes tool, returns JSON result | | |
| 9 | Trigger `AdventureForegroundService` media playback | Audio/music plays, state persists across pause/resume | | |
| 10 | Stop all ADT services | `onDestroy` called, notifications removed | | |

**Blocker:** Model files (`llama-model.gguf`, `whisper.tflite`, `sd-model`) not packaged. Inference services will crash or no-op until models present.

---

## T5.3 AnyClaw Bridge End-to-End
| Step | Action | Expected Result | Pass/Fail | Notes |
|---|---|---|---|---|
| 1 | Open AnyClaw module settings | `PreferencesManager` shows bridge toggles for Discord, Telegram, WhatsApp | | |
| 2 | Enable Discord bridge | `GatewayService` starts, notification “Blackbox Gateway Running” appears | | |
| 3 | Send message to Discord bot from external client | Message appears in `ChannelMessageEntity` via `BlackboxRepository` | | Blocker: no bot token |
| 4 | Enable Telegram bridge | `GatewayService` starts Telegram polling | | Blocker: no bot token |
| 5 | Enable WhatsApp bridge | `GatewayService` starts WhatsApp session | | Blocker: no WhatsApp Business API credentials |
| 6 | Send message to AnyClaw from WhatsApp | Message routed to `BridgeCommandRouter` → ModuleBus | | |
| 7 | Send `/help` via Discord bridge | `DeviceBridge.handleCommand("help")` returns command list | | |
| 8 | Trigger audio capture via Discord bridge | `DeviceBridge.AudioHelper` records, publishes audio event | | |
| 9 | Trigger camera capture via Discord bridge | `DeviceBridge.CameraHelper` takes photo, publishes image event | | |
| 10 | Disable all bridges | `GatewayService` stops, no active connections | | |

**Blocker:** No live bot tokens or API credentials. Bridge connectivity cannot be verified end-to-end without real accounts.

---

## T5.4 Kai Email, SMS, Notifications, Sandbox
| Step | Action | Expected Result | Pass/Fail | Notes |
|---|---|---|---|---|
| 1 | Add IMAP/SMTP account in Kai settings | `ImapClient` connects, folder list returned | | Blocker: no email server |
| 2 | Send test email via Kai | `SmtpClient` sends, email appears in recipient inbox | | Blocker: no SMTP relay |
| 3 | Receive email on device | `EmailPoller` fetches new message, stores in `KaiDatabase` | | Blocker: no IMAP server |
| 4 | Grant SMS permission, send test SMS via Kai | SMS appears in default SMS app | | Blocker: carrier/emulator SMS |
| 5 | Trigger SMS poller | `SmsPoller` reads recent SMS, publishes to ModuleBus | | Blocker: no SMS provider |
| 6 | Trigger notification listener | `KaiNotificationListenerService` captures notification text | | Blocker: needs NotificationListener permission + app |
| 7 | Start sandbox session (proot) | `ProotSupervisor` starts proot, shell prompt available | | Blocker: no rootfs image |
| 8 | Run command in sandbox | Output captured, published to `TerminalScreen` | | |
| 9 | Kill sandbox session | `ProotSupervisor` stops proot, no zombie processes | | |

**Blockers:** No IMAP/SMTP server, no SMS carrier/emulator config, no proot rootfs image, no notification source app.

---

## T5.5 ADT Boot Receiver & Proot Auto-Start
| Step | Action | Expected Result | Pass/Fail | Notes |
|---|---|---|---|---|
| 1 | Enable ADT auto-start in Settings | `AdtBootReceiver` registered, `SharedPreferences` flag set | | |
| 2 | Reboot device | After boot, `AdtBootReceiver` fires, starts ADT foreground services | | |
| 3 | Verify notification after boot | ADT notification visible without manual app launch | | |
| 4 | Disable auto-start, reboot | No ADT services start automatically | | |

**Blocker:** Requires physical device reboot; emulator boot time + ADB delay may mask timing issues.

---

## T5.6 Android Assistant Layer
| Step | Action | Expected Result | Pass/Fail | Notes |
|---|---|---|---|---|
| 1 | Set Blackbox as default voice assistant | `BlackboxAssistantService` listed in voice assistant picker | | |
| 2 | Trigger assistant via long-press home or “Hey Google” fallback | `onAssistStructure` called, text extracted | | |
| 3 | Speak query “send message to discord” | `AssistantIntentRouter` routes to AnyClaw module via `ModuleBus` | | |
| 4 | Verify `AssistantSessionEntity` stored | Session appears in `BlackboxDatabase.assistantSessionDao()` | | |
| 5 | Trigger assistant from `ChatScreen` FAB | Same routing path executed | | |

**Blocker:** VoiceInteractionService requires physical device + assistant enrollment; emulator may not support full assistant flow.

---

## T5.7 Unified UI Navigation & State
| Step | Action | Expected Result | Pass/Fail | Notes |
|---|---|---|---|---|
| 1 | Cold-start app | `BlackboxApp.onCreate` initializes modules, shows Dashboard | | |
| 2 | Navigate Chat → Modules → Terminal → Settings → Dashboard | Bottom nav switches screens, no crashes | | |
| 3 | Rotate device | Screen state preserved, ModuleBus still active | | |
| 4 | Background app, return | App resumes to same screen, services still running | | |
| 5 | Kill app from recents | Services stopped, ModuleBus cleared | | |

**Blocker:** None — executable on emulator or device.

---

## T5.8 Permissions & Graceful Denial
| Step | Action | Expected Result | Pass/Fail | Notes |
|---|---|---|---|---|
| 1 | Deny all permissions at first prompt | App shows permission rationale screen | | |
| 2 | Grant permissions one-by-one | `PermissionCoordinator` updates state, enables features | | |
| 3 | Revoke permissions in system settings | App detects revocation, disables dependent features | | |

**Blocker:** None — executable on emulator or device.

---

## Execution Log
Use this table to record actual results during manual testing.

| T5.x | Date | Device | Tester | Result | Notes |
|---|---|---|---|---|---|
| T5.1 | | | | ☐ Pass ☐ Fail ☐ Blocked | |
| T5.2 | | | | ☐ Pass ☐ Fail ☐ Blocked | |
| T5.3 | | | | ☐ Pass ☐ Fail ☐ Blocked | |
| T5.4 | | | | ☐ Pass ☐ Fail ☐ Blocked | |
| T5.5 | | | | ☐ Pass ☐ Fail ☐ Blocked | |
| T5.6 | | | | ☐ Pass ☐ Fail ☐ Blocked | |
| T5.7 | | | | ☐ Pass ☐ Fail ☐ Blocked | |
| T5.8 | | | | ☐ Pass ☐ Fail ☐ Blocked | |

---

## Blocker Summary for T5
1. No native `.so` files packaged → ADT native lib extraction untestable
2. No model files (`llama-model.gguf`, `whisper.tflite`, `sd-model`) → inference services untestable
3. No Discord/Telegram/WhatsApp bot tokens → bridge end-to-end untestable
4. No IMAP/SMTP server → Kai email untestable
5. No SMS carrier/emulator config → Kai SMS untestable
6. No proot rootfs image → Kai sandbox untestable
7. No physical device for boot receiver + assistant layer → some steps emulator-gated

See `TEST-GAPS.md` for formal blocker tracking.
