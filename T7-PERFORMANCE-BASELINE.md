# Tier 7 — Performance Baseline
**App:** Blackbox Unified (`com.blackbox`)  
**Modules:** AnyClaw, Kai, ADT, Core  
**Date:** 2026-08-09  

---

## 7.1 Performance Targets

| Metric | Target | Measurement Method | Current Status |
|---|---|---|---|
| Cold start time | < 2s | `adb shell am start -W` | Not yet measured |
| Warm start time | < 1s | `adb shell am start -W` after kill | Not yet measured |
| Module registration | < 500ms | `System.nanoTime()` around `ModuleRegistry` | Verified stub |
| ModuleBus publish latency | < 50ms | Flow emission timestamp delta | Verified JVM |
| Room query (conversations) | < 10ms | `System.nanoTime()` around DAO | Not yet measured |
| Room query (messages) | < 10ms | `System.nanoTime()` around DAO | Not yet measured |
| DataStore read | < 20ms | Flow collection latency | Not yet measured |
| Memory footprint (idle) | < 200MB | `adb shell dumpsys meminfo` | Not yet measured |
| Memory after service start | < 350MB | `adb shell dumpsys meminfo` | Not yet measured |
| Proot supervisor health check | < 100ms | `System.nanoTime()` around `healthCheck()` | Verified stub |

---

## 7.2 Startup Time Measurement Script

```bash
# Cold start (app not in memory)
adb shell am force-stop com.blackbox
sleep 2
adb shell am start -W -n com.blackbox/.MainActivity | grep -E "TotalTime|WaitTime"

# Warm start
adb shell am start -W -n com.blackbox/.MainActivity | grep -E "TotalTime|WaitTime"

# Background start
adb shell am start -W -n com.blackbox/.MainActivity | grep -E "TotalTime|WaitTime"
```

**Expected:** Cold start < 2000ms on API 26+ device.

---

## 7.3 Memory Footprint Measurement

```bash
# Idle memory
adb shell dumpsys meminfo com.blackbox | grep "TOTAL"

# After starting ADT services
adb shell service call com.blackbox ...  # trigger ADT start
sleep 3
adb shell dumpsys meminfo com.blackbox | grep "TOTAL"

# After starting AnyClaw Gateway
adb shell service call com.blackbox ...  # trigger GatewayService
sleep 3
adb shell dumpsys meminfo com.blackbox | grep "TOTAL"
```

**Expected:** Idle < 200MB, services < 350MB.

---

## 7.4 Service Restart Behavior

| Service | Restart Trigger | Expected Behavior | Measured |
|---|---|---|---|
| `GatewayService` | `stopSelf()` | `START_STICKY` → restarts within 5s | Not yet |
| `LlamaService` | Crash | System restarts, notification recreated | Not yet |
| `WhisperService` | Crash | System restarts, mic re-acquired | Not yet |
| `AdtUnifiedRuntimeService` | Crash | System restarts, all ADT services re-started | Not yet |

---

## 7.5 Database Performance

| Operation | Target | JVM Verified | Device Verified |
|---|---|---|---|
| Insert conversation | < 5ms | — | Not yet |
| Insert 100 messages | < 50ms | — | Not yet |
| Query all conversations | < 10ms | — | Not yet |
| Query messages for conversation | < 10ms | — | Not yet |
| Delete conversation + cascade | < 20ms | — | Not yet |

---

## 7.6 ModuleBus Throughput

| Metric | Target | JVM Verified |
|---|---|---|
| Publish/subscribe latency | < 50ms | ✅ |
| 1000 events/sec sustained | No drops | Not yet |
| Memory leak (1000 events) | < 1MB growth | Not yet |

---

## 7.7 Proot Supervisor Performance

| Metric | Target | JVM Verified | Device Verified |
|---|---|---|---|
| `healthCheck()` latency | < 100ms | ✅ (stub) | Not yet |
| `start()` completion | < 5s | — | Not yet |
| `stop()` completion | < 2s | — | Not yet |
| Zombie process count after stop | 0 | — | Not yet |

---

## 7.8 Performance Test Implementation (JVM)

**File:** `app/src/test/java/com/blackbox/core/PerformanceBaselineTest.kt`

Tests verify:
- `ModuleRegistry` registration under 1000 modules completes in < 100ms
- `ModuleBus` publish/subscribe latency under load
- `BlackboxDatabase` Room query performance with 10,000 rows
- `SecretStore` encrypt/decrypt throughput

---

## 7.9 Performance Test Implementation (Instrumented)

**File:** `app/src/androidTest/java/com/blackbox/core/PerformanceInstrumentedTest.kt`

Tests verify (on device):
- Cold/warm start time via `ActivityTestRule`
- Service start latency via `ServiceConnection`
- Memory footprint via `Debug.MemoryInfo`
- Database query latency via `System.nanoTime()`

---

## 7.10 Performance Findings

| Finding | Severity | Action Required |
|---|---|---|
| No performance tests yet | Medium | Implement T7 tests on device |
| Proot supervisor is stub | Low | Measure real proot startup time |
| Room DAO queries not benchmarked | Low | Add 10K-row stress test |

---

## Next Steps
1. Implement `PerformanceBaselineTest` JVM tests
2. Implement `PerformanceInstrumentedTest` device tests
3. Run baseline measurements on physical device
4. Document actual numbers in this file
5. Set CI regression alerts if performance degrades > 20%
