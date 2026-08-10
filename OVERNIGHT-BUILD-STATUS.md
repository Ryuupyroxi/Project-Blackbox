# Overnight Build Status — v1.1-beta

_Last updated: 2026-08-10 ~06:15 UTC. Maintained by the build watchdog cron._

## SUMMARY

| Phase | State |
|---|---|
| **PUSH** | ✅ DONE — `v1.1-beta` on origin @ `d240e4a5b`+ (4 additional fix commits) |
| **BUILD-WORKFLOW** | ✅ PRESENT — `.github/workflows/build-apk.yml` (assembleDebug + upload-artifact) |
| **CI** | ❌ FAILED — compile errors in `:modules:core` + missing `drawable/ic_launcher` |
| **APK-RELEASED** | ❌ NO — no APK produced yet, release `v1.1-beta` does not exist |

Last run: [31360653375](https://github.com/Ryuupyroxi/Project-Blackbox/actions/runs/31360653375) — `Build Debug APK`, conclusion **failure**.

## KEY DECISION (why the branch looks different from local history)
The full local history (`9515011fe` and earlier) contained 110,750 `*-decompiled/`
reference files. Pushing that history failed twice with HTTP 408. **Nothing was lost:**
all decompiled trees remain on disk (untracked/gitignored) and the complete history is
preserved locally in branch **`v1.1-beta-full`**. The pushed `v1.1-beta` contains all
source, modules, tests, docs, gradle wrapper and CI config.

## FIXES APPLIED THIS RUN (all additive / minimal, no history rewritten)

1. **`gradlew` — made portable.** The committed `gradlew` was a 29-line hand-written
   stub with the author's absolute path hardcoded
   (`WRAPPER_JAR="/home/Ryuu/Project-Blackbox-worktree/..."`), plus a typo
   (`$WRADDER_JAR`), and it invoked `java -jar gradle-wrapper.jar` (that jar has no
   Main-Class). CI died in 37s with "Gradle wrapper JAR missing at /home/Ryuu/...".
   Replaced with a standard POSIX launcher that resolves `APP_HOME` relative to `$0`
   and runs `-classpath gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain`.
   Verified `GradleWrapperMain.class` is present in the committed jar.
   → commit `fix: make gradlew portable ...`

2. **Removed `org.jetbrains.kotlin.plugin.compose`** from root `build.gradle.kts`.
   That plugin only exists for Kotlin **2.0+**; the project pins Kotlin 1.9.24, so
   plugin resolution failed outright. It was declared `apply false` and applied
   nowhere (verified by grep), and Compose is already configured correctly via
   `composeOptions { kotlinCompilerExtensionVersion = "1.7.0" }` in `app/build.gradle.kts`.
   Pure removal of a dead, broken declaration.
   → commit `fix: drop kotlin.plugin.compose (2.0-only) ...`

3. **Gradle wrapper 8.7 → 8.9.** AGP 8.7.3 refuses to apply on Gradle < 8.9
   ("Minimum supported Gradle version is 8.9"). Bumped `distributionUrl` only.
   → commit `fix: bump Gradle wrapper to 8.9 ...`

4. **Added `gradle.properties`** (the file did not exist) with
   `android.useAndroidX=true` — `:app:checkDebugAarMetadata` hard-failed because every
   dependency is AndroidX. Also set jvmargs/parallel/caching/nonTransitiveRClass.
   → commit `fix: add gradle.properties with android.useAndroidX=true ...`

Each of these unblocked a distinct stage; the build now gets all the way to
`compileDebugKotlin` / `processDebugResources`. **What remains is genuine source-level
breakage, not CI configuration**, so per the watchdog's own rules I stopped editing
rather than guess.

## REMAINING BLOCKERS (require a human decision — 33 compile errors + 1 resource error)

### BLOCKER A — architectural: `:modules:core` imports classes that live in `:modules:adt`
`modules/core/.../module/BlackboxModules.kt` and `ModuleLoader.kt` import:
```
com.blackbox.core.module.adt.AdtModuleImpl
com.blackbox.core.module.adt.bridge.AdtManifestMapper
com.blackbox.core.module.adt.model.AdtReceiverDefinition
com.blackbox.core.module.adt.model.AdtServiceDefinition
com.blackbox.core.module.adt.runtime.AdtModuleLoader
```
Those types **do exist**, but under package `com.blackbox.module.adt.*` in the
**`:modules:adt`** project — note the import path is also wrong (`core.module.adt`
vs `module.adt`).

**This cannot be fixed by adding a dependency:** `modules/adt/build.gradle.kts`
already has `implementation(project(":modules:core"))`, so `core → adt` would be a
**circular dependency** and Gradle will reject it.

Two possible resolutions (a design choice, hence not made automatically):
- **(a)** Move `BlackboxModules.kt` and `ModuleLoader.kt` out of `:modules:core` into
  `:modules:adt` (they are ADT-specific), fixing the package/import to `com.blackbox.module.adt.*`; or
- **(b)** Move the shared contracts (`AdtServiceDefinition`, `AdtReceiverDefinition`,
  `AdtManifestMapper`, the module interface) *down* into `:modules:core` and have
  `:modules:adt` implement them.

(b) is the cleaner layering. Either way `AdtModuleImpl` also fails
"not abstract and does not implement abstract member 'onUnload'" — the module
interface gained an `onUnload` member that the impl never got.

### BLOCKER B — `UnifiedDaos.kt`: missing `import androidx.room.Query`
6 errors, all `Unresolved reference 'Query'` at lines 39/45/51/57/63/69.
The file imports `Dao`, `Entity`, `Insert`, `PrimaryKey` but not `Query`.
Note also: `:modules:core` declares `room-runtime`/`room-ktx` but **no KSP/annotation
processor** (`ksp("androidx.room:room-compiler")`) and does not apply the KSP plugin,
even though KSP 1.9.24-1.0.20 is declared in the root build file. Room DAOs will not
generate implementations until that is wired up — expect a follow-on failure here.

### BLOCKER C — `ModuleLoader.kt`: missing `import java.util.zip.ZipInputStream`
Causes a cascade of ~8 errors (`nextEntry`, `closeEntry`, `name`, `isDirectory`,
plus an `InputStream.readBytes` overload-resolution ambiguity that disappears once
the type is known).

### BLOCKER D — `BlackboxPreferences.kt:133` parameter shadowing
```kotlin
suspend fun setSelectedModelContext(context: String) {
    context.blackboxStore.edit { ... }   // <-- `context` is the String param
}
```
The `String` parameter shadows the `Context` receiver, so the
`val Context.blackboxStore` extension does not apply. Rename the parameter
(e.g. `contextValue`) — but confirm intent before touching it.

### BLOCKER E — `:app:processDebugResources`: missing launcher icon
```
app/src/main/AndroidManifest.xml:22: AAPT: error: resource drawable/ic_launcher not found
```
`app/src/main/res/` contains only `values/` and `xml/` — there is no `mipmap-*` or
`drawable/` launcher icon at all. Needs either a real icon added or
`android:icon` pointed at an existing resource.

## RECOMMENDED NEXT STEPS (in order)
1. Decide Blocker A's layering, then move/repackage the two core files.
2. Add `import androidx.room.Query` and wire KSP + `room-compiler` into `:modules:core`.
3. Add `import java.util.zip.ZipInputStream` to `ModuleLoader.kt`.
4. Rename the shadowing `context: String` parameter.
5. Add a launcher icon (or fix `android:icon`).
6. Re-push; `build-apk.yml` will run automatically and, once green, the watchdog will
   download `app-debug-apk` and cut the `v1.1-beta` release.

## NOTES
- `test.yml` (Exhaustive Test Suite) also runs on this branch; its instrumented/UI
  jobs lack emulator SDK setup and fail for CI-config reasons independent of the APK.
- `app/build.gradle.kts`: compileSdk 35, minSdk 26, targetSdk 35,
  versionName `1.1.0-beta`, versionCode 11.
- No history was rewritten, nothing force-pushed, no tracked files deleted.
