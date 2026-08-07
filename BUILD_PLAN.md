# Blackbox — Master Build Plan (LIVING DOCUMENT)

> Maintained continuously as the merge progresses. Updated: 2026-08-06.
> This is the single source of truth for what Blackbox is, what has been built,
> what is next, and where the code lives. The debugger/tester agent reads this
> file first, then `AGENTS.md`.

---

## 1. Goal

**One app.** Merge the capabilities of four proven projects into a single,
private, local-first Android application:

| Source | What it contributes | Repo |
|---|---|---|
| AI-Doomsday-Toolbox (ADT) | Local AI core: llama.cpp chat, models, media gen, voice, Kiwix, Organizer, Tama/P.E.T., Termux+SSH tools, PDF/dataset tools | `ManuXD32/AI-Doomsday-Toolbox` (already the base of this repo) |
| AnyClaw / OpenClaw-android | Self-contained coding runtime: Termux bootstrap in app sandbox, Node.js, proot, Codex CLI, OpenClaw gateway | `OpenClawAndroid/openclaw-android-assistant` |
| Kai | Voice-first assistant: background daemon, `ACTION_ASSIST` entry, task scheduler, calendar, TTS/STT | `SimonSchubert/Kai` |
| termux-agents-hub | Agent manager: install/launch/health/session-history for Hermes, Codex, OpenClaw | `Ryuupyroxi/termux-agents-hub` |

**Hard constraints**
- Repo stays **private** until the app is finished. No Play Store push yet.
- No local JDK/SDK on the dev box → **compile verification happens on GitHub Actions** (push to private `main` triggers `build.yml`). Use CI runs sparingly.
- Do **not** lose essential ADT code when merging. Everything new is additive; ADT services/screens remain untouched unless explicitly noted.

---

## 2. Current Status (2026-08-07)

- [x] Agents bottom tab added (route `Screen.Agents`, label `nav_agents`) + new routes `Screen.AgentRuntime`, `Screen.Agents`
- [x] `EngineKeysStore` — API keys (OpenAI / OpenRouter / Anthropic) + local server + Termux SSH settings
- [x] `ChatChannel` + `ChatChannelClient` — unified chat engine: local llama/Ollama OR any API key
- [x] `WorkspaceStore` — multi-workspace support (add/switch/delete, per-workspace channel Local/SSH/Kai)
- [x] `FeatureAccessStore` — assistant feature authorization grants (Organizer, Notes, Calendar, Kiwix, PDF, Tama, Models, Chat)
- [x] `RuntimeAgent` catalog + `AgentRuntimeManager` — Hermes / Codex CLI / OpenClaw over the local Termux/Ubuntu SSH channel (commands from termux-agents-hub)
- [x] `AgentHubScreen` — channel status, API keys, workspace manager, quick agent chat, daemon toggle
- [x] `AgentRuntimeScreen` — install/start/stop/health/logs/web-UI for runtime agents
- [x] `AssistantDaemonService` (Kai-style foreground daemon) + `ACTION_ASSIST` manifest filter + MainActivity hook → opens agent chat
- [x] ProGuard rules renamed to `com.blackbox.ai.*` (was stale `com.example.llamadroid`)
- [x] Deep integration: route existing `AgentService` tool calls through the unified channels
- [x] Assistant feature dispatch: `FeatureDispatch` enforces `FeatureAccessStore` grants
- [x] Workspace-aware agent sessions: `WorkspaceAgentSession` (per-workspace channel/context)
- [x] Task scheduler loop under `AssistantDaemonService` (Kai `TaskScheduler` pattern, 60s poll)
- [x] Port OpenClaw `BootstrapInstaller` (zero-Termux embedded runtime) + `download-bootstrap.sh`
- [x] Port OpenClaw `CodexServerManager` (Node.js, Codex CLI, platform binary, CONNECT proxy, OpenClaw gateway)
- [x] `EmbeddedRuntimeManager` — LOCAL channel wraps bootstrap + Codex runtime; Agent Hub card added
- [x] CI downloads Termux bootstrap into assets before `assembleDebug` (`.gitignore`d, not committed)
- [x] TTS/STT voice round-trip in agent chat (`BlackboxVoice` + mic / speak-replies in Quick Agent Chat)
- [x] Heartbeat + scheduled tasks: daemon tick refreshes ADT `LlamaScheduledTaskScheduler` alarms + records heartbeat
- [x] Calendar integration: `CalendarAccess` (create events, ISO parsing, reminders) gated by `FeatureAccessStore.FEATURE_CALENDAR`
- [ ] LOCAL channel full UI: progress stream, login flow, web UI (Control UI port 19001)
- [ ] OpenClaw gateway/control-UI startup from the Agent Hub (ports 18789 / 19001)

**Compile status (2026-08-07):** the "green" CI runs since Phase 2 were **false
positives** — `build.yml` had `continue-on-error: true` on the build step and the
APK upload used `if-no-files-found: warn`, so runs showed success while
`:app:compileDebugKotlin` failed with 34 errors and no `blackbox-debug` APK was
produced (last real APK artifact: 2026-08-06 10:56). The debugger batch below fixed
all 34 errors and hardened `build.yml` (compile failures now fail the run; missing
APK fails the upload step). **Verified green on run 31140699510 (`17ecdfe`)** —
`assembleDebug` SUCCESS and `blackbox-debug` APK uploaded (124 MB arm64, includes
`assets/bootstrap-aarch64.zip` 30 MB + all ADT dex/native libs).

---

## 3. Architecture (added layers)


```
Blackbox (ADT core, unchanged)
└── Agents tab (bottom nav)                        → ui/agent/AgentHubScreen.kt
    ├── Workspaces (multi, per-workspace channel)  → agent/workspace/WorkspaceStore.kt
    ├── Engine channels (local OR any API key)     → engine/EngineKeysStore.kt, engine/ChatChannel.kt
    ├── Runtime agents (Termux/SSH channel)        → agent/runtime/RuntimeAgent.kt, AgentRuntimeManager.kt
    ├── Runtime manager screen                     → ui/agent/AgentRuntimeScreen.kt
    └── Assistant daemon (Kai-style)               → service/AssistantDaemonService.kt
                                                     + MainActivity ACTION_ASSIST → Screen.Agent
```

**Unified engine concept** — every feature can run through:
1. **Local**: on-device OpenAI-compatible server (llama.cpp / Ollama), URL editable in Agent Hub
2. **SSH**: local Termux/Ubuntu runtime (ADT style, default `127.0.0.1:8025`)
3. **Kai**: assistant daemon (voice-first, `ACTION_ASSIST`)
4. **Any API key**: OpenAI / OpenRouter / Anthropic — stored in `blackbox_engine` prefs, testable in the hub

**Workspaces** — `WorkspaceStore` seeds ADT's `default_project` so nothing is lost.
`AgentService.setCurrentProjectFolder(folder)` switches the active workspace
(brain path `/workspace/<folder>`). Each workspace remembers its execution channel.

---

## 4. Step-by-Step Tasks

### Phase 1 — Foundation (DONE, needs CI compile)
- [x] Add `Screen.Agents` + `Screen.AgentRuntime` routes
- [x] Add `nav_agents` string
- [x] `EngineKeysStore` (API keys + local + Termux settings)
- [x] `ChatChannel` / `ChatChannelClient` (local + OpenAI + OpenRouter + Anthropic)
- [x] `WorkspaceStore` / `FeatureAccessStore`
- [x] `RuntimeAgent` catalog + `AgentRuntimeManager` (SSH-driven)
- [x] `AgentHubScreen` / `AgentRuntimeScreen`
- [x] `AssistantDaemonService` + manifest + MainActivity assist hook
- [x] Bottom nav: Agents tab wired in `BlackboxApp.kt`
- [x] ProGuard package renames
- [ ] Push to private repo → CI compile check → fix errors

### Phase 2 — Deep integration
- [ ] Wire `AgentService` tool calls through `ChatChannel` (local → key → SSH fallback)
- [x] Assistant feature dispatch: assistant can open/act on granted features (Organizer, Kiwix, PDF, Tama) with user authorization
- [x] Workspace-aware agent sessions (each workspace keeps its own conversation/context)
- [x] Task scheduler (Kai `TaskScheduler` pattern) under `AssistantDaemonService`

### Phase 3 — Self-contained coding runtime (AnyClaw/OpenClaw)
- [ ] Port `BootstrapInstaller` (extract Termux bootstrap into app sandbox, fix apt/dpkg paths)
- [ ] Add `scripts/download-bootstrap.sh` + bundle `bootstrap-aarch64.zip` at build time
- [ ] Node.js + Codex CLI install flow (`CodexServerManager` pattern)
- [ ] OpenClaw gateway + control UI ports (18789 / 19001)
- [ ] Workspace channel `LOCAL` uses this embedded runtime

### Phase 4 — Voice assistant (Kai deep)
- [ ] TTS/STT voice round-trip in agent chat
- [ ] Heartbeat deep-link + scheduled tasks
- [ ] Calendar integration (read/write, user-gated)

---

## 5. Reference Source (cloned locally at `/tmp/ref/`)

| App | Key files to steal patterns from |
|---|---|
| ADT | `SSHService.kt`, `TermuxTools.kt`, `AgentService.kt` (already in this repo) |
| OpenClaw | `BootstrapInstaller.kt`, `CodexServerManager.kt`, `CodexForegroundService.kt`, `scripts/download-bootstrap.sh` |
| Kai | `DaemonService.kt`, `DaemonController.android.kt`, `TaskScheduler.kt`, `DataRepository.kt` (assist/heartbeat) |
| termux-agents-hub | `termux-agents-hub.sh` (install/launch/health commands, ports, session history) |

---

## 6. Known Risks / Open Items

- **CI run budget**: only a few GitHub Actions runs/day. Batch pushes; do not push per-file.
- **Compose compile risks** in new screens: verify icons used exist in material-icons-extended (`SmartToy`, `MonitorHeart`); avoid composables-in-LaunchedEffect; keep Material3 API usage standard.
- **SSH availability**: runtime agents require the Termux-hosted Ubuntu SSH channel (port 8025) or Termux SSH (8022) — the app already ships the tooling (`SSHService`); user config lives in Agent Hub.
- **Large-file merge discipline**: never rewrite `AgentService.kt` wholesale; add adapter layers instead.
- **Release**: keystore/secrets, version bump, and Play readiness are intentionally deferred until the merge is finished.

---

## 7. Handoff to Debugger/Tester Agent

See `AGENTS.md` (repo root) for orientation. Key focus areas for the next agent:
1. Compile the private `main` on GitHub Actions after the next push; fix Kotlin/Compose errors.
2. Audit new files for API misuse: `agent/runtime/`, `agent/workspace/`, `engine/`, `ui/agent/AgentHubScreen.kt`, `ui/agent/AgentRuntimeScreen.kt`, `service/AssistantDaemonService.kt`.
3. Confirm no ADT functionality was removed (diff scope is purely additive).
4. Suggest improvements — record them in this file under "Decisions & Improvements" below.

## 8. Decisions & Improvements (append-only)

- 2026-08-07: Compile now verified green (run 31140699510, `17ecdfe`) — `assembleDebug`
  SUCCESS and `blackbox-debug` APK artifact uploaded (124 MB arm64). Remaining follow-ups
  for the next debugger/tester cycle: `assist_voice_interaction` requires the user to pick
  Blackbox as the default Digital Assistant (needs on-device validation of the
  VoiceInteraction flow); LOCAL embedded runtime needs an on-device install/start test;
  `FeatureDispatch`/grants UI wiring; quick-agent-chat channel fallback order; daemon
  running-flag derived from real FGS.

- 2026-08-07: Debugger batch — root-caused and fixed ALL 34 `:app:compileDebugKotlin`
  errors from run 31128671993 (these had been silently failing CI since Phase 2; the
  "green" runs were false positives caused by `continue-on-error` + `if-no-files-found: warn`):
  - `AgentRuntimeManager.kt` — deleted duplicate `import ...SSHService` (introduced by the
    earlier "fix" commit `0a0e891`, which made the build worse).
  - `WorkspaceAgentSession.kt` — `MutableStateFlow` lives in `kotlinx.coroutines.flow` (was
    imported from `kotlinx.coroutines`); `SSHConfig` is a **top-level** type in package
    `com.blackbox.ai.engine` (was wrongly imported as `EngineKeysStore.SSHConfig`).
  - `AgentEngineAdapter.kt` — `AgentTool` is a **top-level** type in `com.blackbox.ai.service`
    (was imported as `OllamaService.AgentTool`); `JSONObject` has no `toRequestBody` extension
    (use `body.toString().toRequestBody(...)`); `parseAnthropicResponse` /
    `parseOpenAiStreamingResponse` now take `channel` so `backend = channel.label` resolves.
  - `EngineKeysStore.kt` — `MasterKey.Builder` does not exist in `security-crypto:1.0.0`
    (verified in the AAR: only `MasterKeys`); 1.0.0 `EncryptedSharedPreferences.create`
    signature is `(masterKeyAlias: String, fileName: String, context: Context, ...)` —
    rewritten to `MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)` + the old arg order;
    deleted duplicate `import android.content.Context`.
  - `AssistantDaemonService.kt` — removed unused `import kotlin.time.Clock` (stdlib lacks it
    here) + duplicate `import android.content.Context`.
  - `BlackboxAssistService.kt` — `android.service.assist.AssistService` **does not exist** in
    the SDK (verified against API 35 `android.jar`; that package only has
    `classification.FieldClassification`). Rewrote as the real assist-gesture stack:
    `VoiceInteractionService` + `VoiceInteractionSessionService` + `VoiceInteractionSession`
    (new `res/xml/blackbox_voice_interaction.xml`, manifest now uses `BIND_VOICE_INTERACTION`
    + `android.service.voice.VoiceInteractionService` action + `android.voice_interaction`
    metadata). User must pick Blackbox as the default Digital Assistant.
  - `build.yml` — removed `continue-on-error: true` from the build step and switched the APK
    upload to `if-no-files-found: error` so future compile breaks fail the run visibly.
  - FIXES.md P0 (`connected = true` in `AgentRuntimeScreen.kt`) was already resolved in the
    current tree — no change needed.

- 2026-08-06: Debugger audit produced `FIXES.md` (plan only, no code). Summary:
  - **P0 (compile blocker):** `ui/agent/AgentRuntimeScreen.kt:86` — `connected = true` on a
    `val ...collectAsState()` delegate (State has no setter) → fail `assembleDebug`. Delete the line.
  - **P1:** assist gesture needs a real `AssistService` + `singleTop` (manifest filter alone doesn't
    hook the system gesture); Quick Chat should fall through to next enabled channel instead of
    always hitting local-first; `AssistantDaemonService` running flag goes stale (derive from real
    FGS, use `stopService`); SSH exec has no timeout (wrap install/start); tighten stop patterns +
    DEAD vs MISSING health; wire (or hide) `WorkspaceChannel`/grants/daemon.
  - **P2:** encrypt `EngineKeysStore` secrets + exclude `blackbox_engine` from backups.
  - **P3:** strings/indent, 7-item nav, Anthropic role mapping, install console copy.
  - Suggested order for one batched CI push: P0 → P1 → P2 → P3.
- 2026-08-06: Reference sources re-audited (`/tmp/ref/{adt,kai,openclaw,tah}`) for the fix
  plan. Key deltas captured in `FIXES.md` §"Reference context": runtime agents must pass
  `EngineKeysStore` keys at launch (tah `launch_background`), install base deps once, use
  `pgrep` health (Codex has no HTTP port), free-port fallback; Phase 3 ports
  `BootstrapInstaller.kt` + `download-bootstrap.sh` with package paths remapped
  `com.codex.mobile` → `com.blackbox.ai`; Kai `DaemonService` should try/catch
  `startForeground`, add `onTimeout`, and host a `TaskScheduler`-style long-lived scope.
- 2026-08-06: SID-OS integration deferred — no new SID work this cycle (`AgentImporter` stays as-is).
- 2026-08-06: No Play Store until the merge is finished.
- 2026-08-06: Unified channel order for quick agent chat: local → OpenRouter → OpenAI → Anthropic.

- 2026-08-06: FIXES.md (Coder's audit) fully triaged — P0/P1/P2/P3 implemented in one batch, pending CI compile.
- 2026-08-06: Phase 2 shipped in `0c7faa4` and compiled green on Actions (run 31127686809):
  `AgentEngineAdapter` (AgentService → unified ChatChannel routing), `FeatureDispatch`
  (grant enforcement), `WorkspaceAgentSession` (per-workspace channel/context),
  `AssistantDaemonService` scheduler loop (Kai TaskScheduler pattern).
- 2026-08-06: Phase 3 ported from OpenClaw/android: `BootstrapInstaller.kt`,
  `CodexServerManager.kt`, assets (`proxy.js`, `bionic-compat.js`, `setup-codex.sh`),
  `tools/download-bootstrap.sh`, and `EmbeddedRuntimeManager` for the LOCAL channel.
  CI now downloads the pinned Termux bootstrap into `app/src/main/assets/` before
  `assembleDebug`; the zip is `.gitignore`d to keep the repo small. Agent Hub shows
  an "Embedded LOCAL Runtime" card (Install / Start / Stop / Health).
- 2026-08-06: Phase 4 core drafted (uncommitted at time of writing): `BlackboxVoice`
  (TTS/STT round-trip), mic + speak-replies in Quick Agent Chat, `CalendarAccess`
  (Kai CalendarRepository port, user-gated), and daemon heartbeat that re-asserts
  ADT scheduled-task alarms every poll. Pushed together with Phase 3 to conserve
  CI runs after the runner queue cancelled the Phase 3-only dispatch.
