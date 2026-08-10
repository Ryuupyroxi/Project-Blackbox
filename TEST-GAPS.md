# Test Gaps and Blockers
**Generated:** Phase 3 completion  
**Policy:** No test is skipped without a recorded blocker. Fix the blocker, not the test.

## Blocked Tests (Require Fix Before CI Can Pass)

| Feature | Blocker | Owner | Status |
|---|---|---|---|
| Android instrumented tests | No local Android SDK/emulator; requires GitHub Actions runner with API 26+ | CI | Blocked |
| Compose UI tests on API 34 | Same as above; API 34 emulator image must be available | CI | Blocked |
| DataStore/SecretStore instrumented tests | Requires Android runtime for DataStore + EncryptedSharedPreferences | CI | Blocked |
| Service lifecycle tests (GatewayService, LlamaService, etc.) | Requires started service + notification channel on device | CI | Blocked |
| AnyClaw bridge end-to-end | Requires live Discord/Telegram/WhatsApp credentials or mock server | Network | Blocked |
| Kai email IMAP/SMTP | Requires live email server or mock IMAP/SMTP | Network | Blocked |
| Kai SMS reader/sender | Requires real SMS permissions and carrier/emulator | Device | Blocked |
| ADT native library extraction | Requires real APK with native libs and filesystem access | APK | Blocked |
| LiteRT inference | Requires quantized TFLite model file and GPU delegate | Model | Blocked |
| StableDiffusion generation | Requires real model file (huge) and GPU | Model | Blocked |
| Whisper transcription | Requires model file + audio input | Model | Blocked |
| ZIM share/mount | Requires real ZIM file | Data | Blocked |

## Deferred to Later Phase

| Feature | Reason | Planned Phase |
|---|---|---|
| Performance soak tests | Requires device lab + hours of runtime | Phase 5 |
| Localization tests | Requires translation files | Phase 4 |
| Accessibility scanner | Requires TalkBack + accessibility toolkit | Phase 4 |
| Fuzzing/native lib tests | Requires AFL/LibFuzzer + native build chain | Phase 5 |
| Long-running stability | Requires days of uptime on device | Phase 5 |

## Running Tests Locally

```bash
# Unit tests (JVM, no emulator)
./gradlew testDebugUnitTest

# All lint
./gradlew lintDebug

# Instrumented tests (requires emulator/device)
./gradlew connectedDebugAndroidTest
```

## CI Status

Tests must be green on all 4 jobs (lint, unit, instrumented, UI) before merge.
