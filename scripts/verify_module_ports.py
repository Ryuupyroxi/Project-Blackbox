#!/usr/bin/env python3
import re, sys
from pathlib import Path

ROOT = Path("/home/Ryuu/Project-Blackbox-worktree")
MODULES = ROOT / "modules"
ALLOWED_PACKAGES = {"com.blackbox.core", "com.blackbox.module"}
APP_INTERNAL = {
    "com.blackbox.ai",
    "com.blackbox.runtime",
    "com.blackbox.data",
    "com.blackbox.bridge",
    "com.blackbox.ui",
}

def first_pkg(text: str):
    m = re.search(r"^package\s+([\w.]+)", text, re.M)
    return m.group(1) if m else None

def module_local_model_ok(pkg: str) -> bool:
    # Allow com.blackbox.module.<module>.model as module-local model types
    return re.fullmatch(r"com\.blackbox\.module\.[^.]+\.model", pkg) is not None

def check_files():
    failures = []
    for kts in MODULES.rglob("build.gradle.kts"):
        text = kts.read_text()
        if 'project(":app")' in text:
            failures.append(f"{kts}: cyclic dependency on :app")

    for kt in MODULES.rglob("*.kt"):
        rel = kt.relative_to(ROOT)
        text = kt.read_text()
        pkg = first_pkg(text)
        if pkg:
            ok = any(
                pkg == p or pkg.startswith(p + ".")
                for p in ALLOWED_PACKAGES
            ) or module_local_model_ok(pkg)
            if not ok:
                failures.append(f"{rel}: package {pkg} is outside allowed module packages")
        if "***" in text:
            failures.append(f"{rel}: contains placeholder token '***'")
        for internal in APP_INTERNAL:
            if internal in text:
                failures.append(f"{rel}: references app-internal package {internal}")

    kai_build = MODULES / "kai" / "build.gradle.kts"
    anyclaw_build = MODULES / "anyclaw" / "build.gradle.kts"
    if kai_build.exists() and 'project(":modules:core")' not in kai_build.read_text():
        failures.append("modules/kai/build.gradle.kts missing dependency on :modules:core")
    if anyclaw_build.exists() and 'project(":modules:core")' not in anyclaw_build.read_text():
        failures.append("modules/anyclaw/build.gradle.kts missing dependency on :modules:core")

    app_build = ROOT / "app" / "build.gradle.kts"
    if app_build.exists() and 'project(":modules:core")' not in app_build.read_text():
        failures.append("app/build.gradle.kts missing dependency on :modules:core")

    settings = ROOT / "settings.gradle.kts"
    if settings.exists() and 'include(":modules:core")' not in settings.read_text():
        failures.append("settings.gradle.kts missing :modules:core include")

    return failures

if __name__ == "__main__":
    failures = check_files()
    if failures:
        print("VERIFICATION FAILED")
        for f in failures:
            print(f"- {f}")
        sys.exit(1)
    print("VERIFICATION OK")
    sys.exit(0)
