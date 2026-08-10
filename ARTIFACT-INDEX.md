# Project Blackbox: Artifact Index
**Permanent storage:** `/home/Ryuu/Project-Blackbox-worktree/`  
**APK binaries:** `/home/Ryuu/Project-Blackbox-artifacts/`

---

## Source APKs
| File | Size | Package | Version |
|---|---|---|---|
| `/home/Ryuu/Project-Blackbox-artifacts/anyclaw.apk` | 66 MB | `gptos.intelligence.assistant` | 2.1.565 |
| `/home/Ryuu/Project-Blackbox-artifacts/kai9000.apk` | 24 MB | `com.inspiredandroid.kai` | 3.0.0 |
| `/home/Ryuu/Project-Blackbox-artifacts/adt_latest.apk` | 609 MB | `com.manuxd32.aidoomsdaytoolbox` | 0.948 |

---

## Decompiled Sources
| Directory | App | Classes | Smali Total | Dex Files |
|---|---|---|---|---|
| `/home/Ryuu/Project-Blackbox-worktree/anyclaw-decompiled/` | AnyClaw | 1,400 | 47,036 | 5 |
| `/home/Ryuu/Project-Blackbox-worktree/kai9000-decompiled/` | Kai 9000 | 1,809 | 19,151 | 3 |
| `/home/Ryuu/Project-Blackbox-worktree/adt-decompiled/` | ADT | 3,756 | 40,308 | 3+ |

---

## Class Inventories
| File | App | Count |
|---|---|---|
| `/home/Ryuu/Project-Blackbox-worktree/anyclaw-app-classes.txt` | AnyClaw | 1,400 |
| `/home/Ryuu/Project-Blackbox-worktree/anyclaw-full-classes.txt` | AnyClaw | 47,036 |
| `/home/Ryuu/Project-Blackbox-worktree/kai9000-app-classes.txt` | Kai 9000 | 1,809 |
| `/home/Ryuu/Project-Blackbox-worktree/anyclaw-classes-normalized.txt` | AnyClaw | normalized |

---

## Reference Documentation
| File | Purpose |
|---|---|
| `/home/Ryuu/Project-Blackbox-worktree/REF-AnyClaw-Structure.md` | AnyClaw component inventory |
| `/home/Ryuu/Project-Blackbox-worktree/REF-Kai9000-Structure.md` | Kai 9000 component inventory |
| `/home/Ryuu/Project-Blackbox-worktree/REF-ADT-Structure.md` | ADT component inventory |
| `/home/Ryuu/Project-Blackbox-worktree/INTEGRATION-DESIGN.md` | Functional integration design |
| `/home/Ryuu/Project-Blackbox-worktree/INTEGRATION-PATTERNS.md` | Code-level integration patterns |
| `/home/Ryuu/Project-Blackbox-worktree/COMBINATION-STRATEGY.md` | Modular shell + module loader strategy |
| `/home/Ryuu/Project-Blackbox-worktree/STRIP-POLICY.md` | Bloat stripping rules |

---

## Build Artifacts
| Directory | Purpose |
|---|---|
| `/home/Ryuu/Project-Blackbox-artifacts/` | APK binaries, build outputs |
| `/home/Ryuu/Project-Blackbox-worktree/` | Source, docs, decompiled trees |

---

## Key Integration Targets
- AnyClaw: proot, bridges, auth, process manager
- Kai 9000: RemoteDataRepository, ToolExecutor, McpServerManager, LiteRT, email/SMS
- ADT: 33 services, 62 .so libs, media/ML/ZIM/Tama

---

*Last updated: 2026-08-09*
