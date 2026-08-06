# AGENTS.md — Blackbox repo orientation

Read `BUILD_PLAN.md` first — it is the living master plan and is updated as work
progresses. This file tells any agent (builder, debugger, tester) how to operate
safely in this repo.

## Project
Blackbox is a private Android app: a merge of AI-Doomsday-Toolbox (base),
OpenClaw/AnyClaw (self-contained coding runtime), Kai (voice assistant), and
termux-agents-hub (agent manager). Local-first; cloud/API keys are optional.

## Critical rules
1. **Do not make the repo public** and do not prepare a Play Store release until
   the merge is finished. It stays private.
2. **Compile on GitHub Actions**, not locally (no JDK/SDK on the dev box).
   Pushing to private `main` triggers `.github/workflows/build.yml` (assembleDebug).
   CI runs are limited — batch commits; do not push per-file.
3. **Merge discipline**: ADT code is huge and working. Never rewrite
   `AgentService.kt`, `SSHService.kt`, or other core services wholesale. Add new
   files and adapters; keep changes additive.
4. Do not lose essential ADT functionality. If a change would alter ADT behavior,
   flag it in `BUILD_PLAN.md` first.
5. Update `BUILD_PLAN.md` (status checkboxes + Decisions & Improvements section)
   whenever work is done, so the debugger/tester agent always has fresh context.

## Layout (new merge code)
- `app/src/main/java/com/blackbox/ai/engine/` — unified channels: `EngineKeysStore`, `ChatChannel`
- `app/src/main/java/com/blackbox/ai/agent/workspace/` — `WorkspaceStore`, `FeatureAccessStore`
- `app/src/main/java/com/blackbox/ai/agent/runtime/` — `RuntimeAgent`, `AgentRuntimeManager`
- `app/src/main/java/com/blackbox/ai/ui/agent/` — `AgentHubScreen`, `AgentRuntimeScreen`
- `app/src/main/java/com/blackbox/ai/service/AssistantDaemonService.kt` — Kai-style daemon
- Navigation: `ui/navigation/Screen.kt` (routes), `ui/BlackboxApp.kt` (bottom tabs)

## Reference clones
Reference source trees live at `/tmp/ref/` (adt, kai, openclaw, tah) on the dev
box — useful for borrowing exact patterns (not committed).

## Debugger/tester focus
- After each push, watch the Actions run (`gh run watch`) and fix Kotlin/Compose errors.
- Audit new files for API misuse and UI regressions.
- Confirm the APK still contains ADT features (compare against the earlier green build).
- Suggest improvements by appending to `BUILD_PLAN.md` §8.
