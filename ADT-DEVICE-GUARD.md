# ADT Device Guard
**Status:** Reference  
**Date:** 2026-08-09  
**Source:** ADT decompilation + Project Blackbox integration policy

---

## Goal
Define the device/runtime boundary for ADT-derived functionality in Project Blackbox, so we don’t accidentally mount dangerous system components or unrestricted host access during integration.

## Scope
This guard applies to:
- ADT services ported into Blackbox
- Proot/host-bridge runtime interactions
- Background daemons spawned from ADT modules
- Any tool/runtime surface that can affect the Android device, filesystem, or host shell

## Guardrails

### 1. No Unrestricted Host Shell by Default
- All `shellExec` / `proot` commands run through a single supervisor
- Default session is non-root unless explicitly elevated
- Dangerous command prefixes are rejected by default:
  - `rm -rf /`
  - `mkfs`
  - `dd if=`
  - `shutdown`
  - `reboot`
  - `reboot now`
  - `poweroff`
- A user-visible override exists but is opt-in and recorded in logs

### 2. Foreground Service Requirements
- Any ADT service that needs long-running work must declare `foregroundServiceType`
- Supported types in Blackbox:
  - `dataSync`
  - `mediaPlayback`
  - `microphone`
  - `specialUse`
- Missing type → build-time lint warning in module validation

### 3. Permission Request Cadence
- Request permissions only when the feature is enabled
- Do not request all ADT permissions on first launch
- Keep a permission gate map in `modules/anyclaw/permission/`:
  - feature → required permissions
  - rationale text for runtime prompt
  - fallback behavior if user denies

### 4. Boot Receivers
- Blackbox owns boot lifecycle
- ADT boot receivers are ported into a single `BlackboxBootController`
- No dynamic receivers without explicit registration in the controller

### 5. Native Libraries
- ADT ships OpenCL/OpenCL-pixel/OpenCL-car/libcdsprpc/libvndksupport stubs
- Blackbox loads optional native libs via explicit checks, not auto-load
- Missing native libs degrade gracefully:
  - OpenCL → CPU inference fallback
  - MLKit text recognition → system Tesseract or no-op fallback

### 6. Network Security
- ADT uses `android:usesCleartextTraffic="true"`
- Blackbox keeps cleartext disabled by default
- Local proot/bridge traffic stays on `localhost` only
- External provider traffic must use HTTPS unless user enables local-only debug mode

### 7. Asset Packs / Play Core
- ADT uses `AssetPackExtractionService`
- Blackbox does not use Play Core asset packs
- Large model bundles are delivered via direct download or local import
- Asset-pack classes are stripped unless explicitly whitelisted

### 8. Privacy and Telemetry
- ADT may include analytics/measurement libraries
- Blackbox strip policy removes:
  - Firebase Analytics
  - Firebase Measurement
  - Play Services Ads
  - Review/Billing
  - Crashlytics-style reporters
- No telemetry is sent without explicit user opt-in

## Device Capability Matrix

| Capability | Allowed | Notes |
|---|---|---|
| Local shell / proot | Yes | gated by permission gate |
| Host activity launch | Yes | gated by bridge permissions |
| Broadcasts | Yes | limited allowlist |
| Audio recording | Optional | requires runtime permission |
| Camera | Optional | QR scan / image capture only |
| Notifications | Yes | listener optional |
| Foreground services | Yes | type-restricted |
| Boot auto-start | Yes | unified boot controller |
| Native acceleration | Optional | graceful fallback |
| Cleartext traffic | No | localhost only |
| External asset packs | No | direct download only |
| Telemetry | No | opt-in only |

## Integration Decision Log

| Decision | Rationale |
|---|---|
| Replace multiple ADT services with `BlackboxRuntimeService` | Single ownership, easier recovery |
| Keep ADT bridge protocol shape, move to `BlackboxBridge` | Reuse proven JSON-RPC pattern |
| Strip ADT application class, use Blackbox `Application` | Unified DI/initialization |
| Strip ADT Play Core receivers | No Play Store dependency |
| Keep ADT MLKit init but lazy-load | Avoid startup cost |
