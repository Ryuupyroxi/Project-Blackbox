# Blackbox — Vision Spec (DRAFT v0.3)

> Draft for review. Kevin's vision, captured 2026-08-01. NOT committed yet.
> v0.3: full ADT feature inventory from the real README; AnyClaw researched
> (two distinct apps — ambiguity flagged). No shortcuts.

## North Star
**A true all-in-one, local-first AI workstation on Android** — the offline-AI power of
AI Doomsday Toolbox + the on-phone AI coding/agent capability of AnyClaw + the agent
context of SID-OS. Everything runs on-device; cloud is optional, never required.

## Lineage (verified)
- Blackbox is a **fork of AI-Doomsday-Toolbox** (github.com/ManuXD32/AI-Doomsday-Toolbox,
  Apache-2.0). Same version 0.945. Codex rebranded ADT -> LlamaDroid -> Blackbox and left
  stray "doomsday"/LlamaDroid strings. ADT was itself "built with the help of codex".
- Apache-2.0 => we can pull upstream fixes. Upstream actively maintained (last commit
  2026-07-19, 103 stars, 18 forks, Play beta).
- Codex ADDED the SID-OS agent bridge (AgentImporter) on top of ADT. That is the unique bit.

---

## Reference A — AI Doomsday Toolbox (inherited core, FULL inventory)

### App surfaces (navigation hubs)
- **Dashboard** — server start/stop, knowledge bases, local file sharing (upload + folder),
  Kiwix, distributed inference entry points
- **AI HUB** — AI Servers, studios, workflows, datasets, native chat, agent tools
- **Models** — LLM / SD / ONNX / LiteRT / Whisper / shared; import, download, export,
  rename, share; vision-projector handling for compatible setups
- **Settings** — runtime controls, backup/restore (full app + native-chat/Organizer ZIP),
  system prompts, output folders, acceleration, thread count, saved prompts
- **Organizer** — notes (Markdown), calendar, alarms, home/lock widgets
- **P.E.T.** — Tamagotchi-like pet: home, chat, gallery, farm, store, adventures, dream
  recaps, artwork, room/decoration, Adventure Gate; long-term memory

### Feature inventory
1. **Local AI Chat** — llama.cpp GGUF, Ollama, llama-server backends; native chat UI;
   OpenAI-compatible local server on :8080; model switching; optional LAN-visible server
2. **Ollama Manager** — add/manage servers; pull/inspect/copy/delete; edit Modelfiles;
   create derived models in-app
3. **AI Servers Hub** — browser-facing local web UIs (Image Studio, Video Studio,
   Workflows, Voice Studio, Video Upscale, Docs/Datasets, Llama Chat) with QR + LAN access
4. **Distributed / phone-cluster inference** — master-worker across LAN devices (mDNS/NSD);
   monitor flows; share models/services over LAN; reuse old phones as edge cluster
5. **Image generation** — SD 1.5 / 2.1 / SDXL / FLUX via stable-diffusion.cpp; txt2img,
   img2img; steps/CFG/dimensions/sampler/seed/tiling/diffusion-caching
6. **Video generation** — txt2vid / img2vid (stable-diffusion.cpp video models); FLUX
   `vid_gen` badge; 480x832 default; AVI+MP4 + metadata; dedicated video gallery
7. **Upscaling** — RealESRGAN image+video, multiple scale factors; FastSDCPU / A1111 alt
8. **Background removal** — ONNX
9. **Text-to-speech** — ONNX TTS + gallery
10. **Audio transcription** — Whisper (multi-language, multi-size); from share intents
11. **Live translator** — turn-based bilingual voice (mic+playback)
12. **Voice call** — LlamaCall (mic + media playback)
13. **Audio/video/subtitle tools** — Whisper transcribe, video summarize, FFmpeg audio
    extract, burn subtitles (font/color/position), share-intent processing
14. **PDF / summary tools** — text extract w/ OCR fallback; summarize PDF/video/transcript;
    Ollama/llama.cpp remote backends; per-workflow prompt/context/length tuning
15. **Dataset Creator** — import .txt/.pdf; chunk; clean; 5 Q/chunk w/ neighbor context;
    generate+rate answers; Alpaca JSON export; customizable prompts
16. **Training** — Quadtrix native LLM training (QuadtrixTrainer + WebUI)
17. **Termux + proot + Ubuntu SSH** — connect to Ubuntu SSH (port 8025); one-button install
    of Ollama, Open WebUI, Big-AGI, Oobabooga textgen, FastSDCPU, experimental A1111
    (Python 3.11, mirrored SD deps, MCP runtime shared); in-app webview; file manager;
    optional LAN expose
18. **AI Agent Workspace** — custom tools + custom agents; project workspace memory;
    reusable automation flows (Termux + Ollama backends)
19. **LiteRT-LM** — on-device LiteRT inference backend (separate model family)
20. **Offline knowledge** — Kiwix/ZIM (catalog download + import; Kiwix server :8888; LAN);
    model/ZIM LAN sharing (web UI + QR); auto-notes from summaries/transcripts
21. **Benchmarking** — thread-count comparison; save/compare history; real llama bench output
22. **Remote summary / remote backends** — orchestrate off-device OpenAI-compat servers
23. **Share-intent ingest** — PDF/video/image/audio into processing flows
24. **Tama / P.E.T.** virtual pet (see surfaces above) — CORE, not cruft
25. **Online hub** — HuggingFace/GitHub/remote LLM (optional convenience)

### Engine / native stack
- llama.cpp, whisper.cpp, stable-diffusion.cpp, FFmpeg, Kiwix-tools, Real-ESRGAN
- ONNX Runtime, LiteRT-LM, Ollama, Quadtrix
- Accelerators: OpenCL/QNN/NPU dynamic feature packs (arm64-v8a only)
- NanoHTTPD (embedded servers), ZXing (QR), ML Kit (OCR), Apache PDFBox

---

## Reference B — AnyClaw B1 (LOCKED: the coding reference)
**"AnyClaw: 5-in-1 AI Coding"** — `gptos.intelligence.assistant` (BrutalStrike), Play Store.
Selected over B2 (gateway companion). Traits to fold into Blackbox:

- **Self-contained local coding** on Android — **no Termux dependency, no root**
- **5 AI coding agents** + full terminal environment
- **Built-in Linux runtime**: Node.js 24, npm, ARM64 native support
- **Multi-agent routing**, skills, sessions, dashboard
- **Background execution** via foreground service
- Built on **OpenClaw + OpenAI Codex CLI + Hermes Agent (Nous Research) + OpenCode**
- Matches the "build an app from your phone in minutes" reference

### Why B1 and not B2
B2 (`sh.anyclaw.app`, A8E Group) is a *gateway companion client* (QR-pair, ClawKey auth,
24h guest keys, multi-gateway dashboard) — a remote control, not a coder. It overlaps
Blackbox's existing AI Servers Hub and is NOT the "AI coding" half Kevin described.

### Architectural note (B1 vs ADT's Termux approach)
ADT's agent coding runs on **Termux + proot + Ubuntu SSH** (A-17). B1 instead ships a
**self-contained Linux runtime** (no Termux, no root). The Blackbox plan is to offer BOTH:
keep ADT's Termux path for heavy toolchains, AND add a B1-style self-contained coding
runtime so agent coding works with zero setup. This is the key differentiator vs stock ADT.

---

## Reference C — SID-OS (your project, the Blackbox edge)
- Blackbox already has `AgentImporter` to pull agents from SID-OS context
- This is the differentiator vs. stock ADT: Blackbox is the on-phone face of your
  AI-OS ecosystem (SID-OS + termux-agents-hub). Goal: deepen that bridge.

---

## Core Pillars (consolidated — every pillar maps to a Reference above)
1. Local AI core (A-1,2,3,19)
2. Generative media: image/video/upscale/bg-removal (A-5,6,7,8)
3. Voice: STT/TTS/translator/call (A-9,10,11,12)
4. AI coding & agents: ADT workspace (A-18) + AnyClaw-B1 self-contained coding runtime
   (no Termux/root, 5 agents, built-in Node24/ARM64) + SID-OS import (C)
5. Distributed inference / phone cluster (A-4)
6. Offline knowledge: Kiwix/RAG/dataset (A-15,20)
7. Organizer: notes/calendar/alarms/widgets (A-Organizer)
8. Tama / P.E.T. virtual pet (A-P.E.T.) — CORE
9. Utilities/toolbox: Termux+SSH, device toolkit, PDF/video/subtitle, file sharing (A-13,14,17,23)

## Open Questions (need Kevin's call)
- Q1: ~~Which AnyClaw?~~ RESOLVED → **B1** (5-in-1 AI Coding). See Reference B.
- Q2: ~~Distribution~~ RESOLVED → **GitHub-hosted, open-source** (keep ADT's Apache-2.0
  license). Public project. Polish/permissions held to a shippable bar, but no Play-store
  gatekeeping required. Note: app currently requests MANAGE_EXTERNAL_STORAGE +
  REQUEST_IGNORE_BATTERY_OPTIMIZATIONS — acceptable for local-first power tool, revisit if
  a stricter public store path is ever wanted.
- Q3: ~~Cloud/online hub~~ RESOLVED → **local-first. Online AI API / HuggingFace / GitHub
  download / remote LLM are OPTIONAL conveniences only. App must fully function offline.**
- Q4: **Coding runtime choice** — RESOLVED → offer BOTH: keep ADT's Termux+proot path for
  heavy toolchains AND add a B1-style self-contained Linux runtime (Node24/ARM64) so agent
  coding works zero-setup. This is the Blackbox differentiator vs stock ADT.

## Out of Scope (proposed)
- Server you don't control / telemetry / account lock-in
