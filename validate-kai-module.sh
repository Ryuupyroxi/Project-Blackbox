#!/bin/bash
set -euo pipefail
MODULE_DIR="/home/Ryuu/Project-Blackbox-worktree/modules/kai/src/main/java/com/blackbox/module/kai"
ERRORS=0
missing() { echo "FAIL $1"; ERRORS=$((ERRORS+1)); }
ok() { echo "OK   $1"; }

echo "== Kai module structure =="
check() { [ -f "$2" ] && ok "$1" || missing "$1: $2"; }
check "Root module impl" "$MODULE_DIR/KaiModuleImpl.kt"
check "Module catalog"    "$MODULE_DIR/KaiServiceCatalog.kt"
check "Tool executor"     "$MODULE_DIR/KaiToolExecutor.kt"
check "Provider"          "$MODULE_DIR/chat/KaiAiProvider.kt"
check "Repository"        "$MODULE_DIR/data/BlackboxRepository.kt"
check "FakeRepository"    "$MODULE_DIR/data/FakeBlackboxRepository.kt"
check "Provider selector" "$MODULE_DIR/data/KaiProviderSelector.kt"
check "Message model"     "$MODULE_DIR/model/BlackboxMessage.kt"
check "Role enum"         "$MODULE_DIR/model/Role.kt"
check "Service model"     "$MODULE_DIR/model/Service.kt"
check "Model model"       "$MODULE_DIR/model/Model.kt"
check "ServiceEntry"      "$MODULE_DIR/model/ServiceEntry.kt"
check "HTTP client"       "$MODULE_DIR/net/KaiHttpClient.kt"

echo
echo "== Basic Kotlin sanity =="
for f in $(find "$MODULE_DIR" -name "*.kt" | sort); do
  open=$(grep -o '{' "$f" | wc -l)
  close=$(grep -o '}' "$f" | wc -l)
  [ "$open" -eq "$close" ] && ok "braces: $f" || { missing "brace mismatch: $f (open=$open close=$close)"; }
  pkg=$(grep -m1 '^package ' "$f" | awk '{print $2}' | tr -d ';')
  if [ -n "$pkg" ]; then
    expected_dir=$(printf "%s" "$pkg" | tr '.' '/')
    case "$f" in
      *"/$expected_dir/"*) ok "package path: $f" ;;
      *) missing "package path mismatch: $f (package=$pkg)" ;;
    esac
  fi
done

echo
echo "== Cross-reference checks =="
grep -Rq "KaiServiceCatalog" "$MODULE_DIR" && ok "KaiServiceCatalog referenced" || echo "WARN KaiServiceCatalog not referenced"

echo
[ "$ERRORS" -eq 0 ] && { echo "VALIDATION PASSED"; exit 0; } || { echo "VALIDATION FAILED: $ERRORS issue(s)"; exit 1; }
