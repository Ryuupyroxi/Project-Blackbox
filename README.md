# Blackbox

**Local-first, private AI — in one Android app.**

Blackbox merges four proven projects into a single self-contained runtime:

| Source | What it contributes |
|---|---|
| [AI-Doomsday-Toolbox](https://github.com/ManuXD32/AI-Doomsday-Toolbox) | Local AI core: llama.cpp chat, model hub, media generation, voice, Kiwix offline Wikipedia, Organizer, Tama/P.E.T., Termux + SSH tools, PDF/dataset tools |
| [OpenClaw Android](https://github.com/friuns2/openclaw-android-assistant) | Self-contained coding runtime: embedded Termux bootstrap, Node.js, proot, Codex CLI, OpenClaw gateway |
| [Kai](https://github.com/SimonSchubert/Kai) | Voice-first assistant: background daemon, `ACTION_ASSIST` entry, task scheduler, calendar, TTS/STT |
| [termux-agents-hub](https://github.com/Ryuupyroxi/termux-agents-hub) | Agent manager: install/launch/health/session-history for Hermes, Codex CLI, OpenClaw |

Everything runs **on-device by default**. Cloud/API keys are optional — add them in the Agents tab only if you want hosted models.

---

## ✨ Features

- **Unified AI channels** — local llama.cpp / Ollama server, OpenAI, OpenRouter, or Anthropic, all behind one chat engine.
- **Embedded LOCAL runtime** — zero-setup coding runtime (Termux bootstrap + Node.js + Codex CLI) extracted into the app sandbox. No Termux or SSH install needed. Includes a Codex login flow, an OpenAI-compatible local server, and the OpenClaw Control UI (port 19001).
- **Agent Hub** — workspaces (Local/SSH/Kai channels), API keys, quick agent chat with voice round-trip, runtime agents (Hermes / Codex CLI / OpenClaw), and a Kai-style assistant daemon toggle.
- **Kai-style daemon** — foreground assistant service with heartbeat, scheduled tasks, calendar access, and `ACTION_ASSIST` routing into the Agents tab.
- **Full ADT suite** — local LLM chat, Stable Diffusion / ONNX media generation, Whisper transcription, Kiwix offline Wikipedia, notes & knowledge bases, Termux/SSH tooling, and more.

## 📥 Download

Grab the latest APK from the [Releases](https://github.com/Ryuupyroxi/Project-Blackbox/releases) page.

- Current: **v1.0.3** (`com.blackbox.ai`, versionName `1.0.3`)
- Debug build (~124 MB arm64); install with "Install unknown apps" allowed for your browser/file manager.

> ⚠️ This is a beta. It ships as a debug build for early testing.

## 🏗️ Build

Compilation happens on GitHub Actions — pushing to `main` triggers the `Build Blackbox APK` workflow (`assembleDebug`), and the APK is uploaded as the `blackbox-debug` artifact.

To build locally you need JDK 17, the Android SDK, and NDK `29.0.14206865` + CMake `3.22.1`:

```bash
./gradlew assembleDebug
```

The embedded LOCAL runtime bootstrap is downloaded into assets by `tools/download-bootstrap.sh` before the build.

## 🚀 Quick start

1. Install the APK and open **Blackbox**.
2. Open the **Agents** tab.
3. Either:
   - enable a **local llama.cpp / Ollama server** (fully offline), or
   - add an **API key** (OpenAI / OpenRouter / Anthropic), or
   - tap **Install** on the Embedded LOCAL Runtime card for the zero-setup Codex runtime.
4. Pick a workspace (Local / SSH / Kai) and start chatting with the agent.

To use the voice assistant, set **Blackbox** as your default Digital Assistant in Android settings, then long-press the home button.

## 🔒 Privacy

Local-first by design:

- Models, chat history, workspaces, and agent runtimes live **on-device** in the app sandbox.
- No account required; no telemetry.
- Cloud/API keys are stored locally (EncryptedSharedPreferences) and only used when you explicitly enable a hosted channel.

## 🗂️ Architecture (merge code)

| Path | Purpose |
|---|---|
| `app/src/main/java/com/blackbox/ai/engine/` | Unified channels: `EngineKeysStore`, `ChatChannel` |
| `app/src/main/java/com/blackbox/ai/agent/workspace/` | `WorkspaceStore`, `FeatureAccessStore` |
| `app/src/main/java/com/blackbox/ai/agent/runtime/` | `RuntimeAgent`, `AgentRuntimeManager`, `EmbeddedRuntimeManager` |
| `app/src/main/java/com/blackbox/ai/ui/agent/` | `AgentHubScreen`, `AgentRuntimeScreen` |
| `app/src/main/java/com/blackbox/ai/service/` | `AssistantDaemonService`, `BlackboxAssistService` |

## ⚖️ License

Apache License 2.0. See [NOTICE](NOTICE) for full attribution.

Blackbox is a fork of AI-Doomsday-Toolbox (© 2025-2026 ManuXD32, Apache-2.0) and includes work from llama.cpp, whisper.cpp, stable-diffusion.cpp, Kiwix, Real-ESRGAN, ONNX Runtime, LiteRT, Ollama, Quadtrix, Termux, and other upstream projects, each under its own license.
