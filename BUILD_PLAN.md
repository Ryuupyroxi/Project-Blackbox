# Blackbox — Full Build Plan (DRAFT)

> Author: Hermes (reins handed over by Kevin). 2026-08-01.
> Grounded in: repo inspection + GitHub `Project-Blackbox` + ADT upstream comparison.
> Status: PLAN ONLY. Do not execute until SDK install confirmed by Kevin.

## 0. Critical finding (read first)
The extracted/bundled repo is a **partial snapshot** of AI-Doomsday-Toolbox. `app/build.gradle.kts`
references files that are NOT in the repo (neither bundle nor GitHub `Project-Blackbox`):
- `gradlew` + `gradle-wrapper.jar`  → MISSING (CI regenerates via checkout+chmod, but not committed)
- `app/src/main/cpp/CMakeLists.txt` (declared in `externalNativeBuild`) → MISSING
- `tools/tama_dialog_excel.py` (Gradle task runs it) → MISSING
- `app/src/main/tama-dialogs/pet_dialogs.xlsx` (input to that task) → MISSING
- dynamic feature modules (`feature_*`, `asset_upscaler`) → MISSING (and `settings.gradle.kts`
  already disables them: "TEMP: disabled - no source")

ADT upstream HAS: gradlew, gradle-wrapper.jar, cpp/CMakeLists.txt, tools/tama_dialog_excel.py,
tama-dialogs/pet_dialogs.xlsx. So the fix is to **restore the missing build-support files from
upstream ADT** (Apache-2.0, compatible) and disable the dynamic-feature + asset-pack blocks that
have no source. Then build debug APK.

## 1. Toolchain (what Kevin is installing)
- Android SDK (cmdline-tools, platforms-35, build-tools) — IN PROGRESS
- Plus we must add (after SDK lands):
  - JDK 17 (Temurin) — CI uses '17'; `libs.versions.toml` AGP 8.6 needs JDK 17
  - NDK 29.0.14206865 (exact version pinned in build.gradle; `sdkmanager "ndk;29.0.14206865"`)
  - CMake 3.22.1 (pinned; `sdkmanager "cmake;3.22.1"`)
  - Python 3 (for the Tama dialog Excel→JSON task)
  - ~4–6 GB download total

## 2. Restore missing build-support files from ADT upstream
```bash
UP=https://github.com/ManuXD32/AI-Doomsday-Toolbox
# wrapper (so ./gradlew works)
gh api repos/ManuXD32/AI-Doomsday-Toolbox/contents/gradlew --jq '.content' | base64 -d > gradlew
gh api repos/ManuXD32/AI-Doomsday-Toolbox/contents/gradle/wrapper/gradle-wrapper.jar --jq '.content' | base64 -d > gradle/wrapper/gradle-wrapper.jar
chmod +x gradlew
# native build (CPU feature detection via arm64 headers)
mkdir -p app/src/main/cpp && (download cpp/CMakeLists.txt + sources from $UP/app/src/main/cpp)
# Tama dialog generator
mkdir -p tools && (download tools/tama_dialog_excel.py from $UP)
mkdir -p app/src/main/tama-dialogs && (download app/src/main/tama-dialogs/pet_dialogs.xlsx from $UP)
```
> Verify each restored file is byte-identical to upstream where Blackbox made no changes.
> If Blackbox DID modify any of these, prefer the Blackbox version (none found yet).

## 3. Disable blocks with no source (avoid build failure)
In `app/build.gradle.kts`, neutralize (comment out) the dynamic-feature + asset-pack references
since the modules don't exist:
- `assetPacks += setOf(":asset_upscaler")`  (line ~141)
- `dynamicFeatures += setOf(...)`  (lines ~146-152)
(They reference modules that are absent; leaving them in fails config resolution.)
Keep `externalNativeBuild { cmake ... }` — it has source after step 2.

## 4. Environment
```bash
export ANDROID_HOME=$HOME/Android/Sdk      # or wherever Kevin installs
export JAVA_HOME=$(path to JDK17)
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
sdkmanager "platforms;android-35" "build-tools;35.0.0" "ndk;29.0.14206865" "cmake;3.22.1"
```

## 5. Build
```bash
cd ~/projects/blackbox
./gradlew assembleDebug --no-daemon --stacktrace
# output: app/build/outputs/apk/debug/app-debug.apk
```
If CMake native build complains (no `.so` produced), the app still compiles Kotlin; native piece
is only CPU-feature detection (`CpuFeatures.kt`) — low risk.

## 6. Risks / gotchas
- **Gradle version**: wrapper pins 8.11.1. If network to services.gradle.org is blocked, fall
  back to system Gradle 8.11.1 if installable, else adjust wrapper.
- **First build downloads ~hundreds of Maven deps** (ONNX 1.21, LiteRT-LM, parquet, ML Kit,
  play-delivery, etc.) — needs internet; large (first sync ~1–2 GB).
- **play-delivery / asset-delivery** libs are fine for debug; only matter for Play publishing.
- **`AppContainer` is an empty stub** (DI not wired) — may or may not break runtime; compile is fine.
- **NDK exact version 29.0.14206865** must match or CMake config fails.

## 7. Acceptance (done when)
- [ ] `./gradlew assembleDebug` succeeds
- [ ] `app-debug.apk` produced
- [ ] APK installs + opens on arm64 device/emulator (runtime check later)
- [ ] Rebrand strings verified present (Blackbox, no Doomsday/llamadroid)

## 8. After build works
Then tackle the real Blackbox differentiator: **B1 self-contained coding runtime** (Node24/ARM64,
no Termux) layered beside ADT's Termux path. Separate plan.

## Open decisions for Kevin
- D1: OK to restore build-support files (gradlew, cpp/, tools/, tama-dialogs/) from upstream ADT
  (Apache-2.0, same license) to make the build work? (Recommended — they're build infra, not app logic)
- D2: Confirm SDK install path so I set ANDROID_HOME correctly.
- D3: Where to install JDK17/NDK/CMake — system or a local toolchain dir under ~/projects/blackbox/.toolchain?
