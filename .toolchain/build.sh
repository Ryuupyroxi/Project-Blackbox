#!/usr/bin/env bash
# Blackbox build runner. Requires: Google cmdline-tools + SDK packages installed
# (see setup-sdk.sh) and OpenJDK 17 (AGP 8.6 rejects JDK 25).
set -e
export ANDROID_HOME=$HOME/Android/Sdk
export SDK_ROOT=$HOME/Android/Sdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH
export GRADLE_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=256m"
# Cap Kotlin compile daemon + Gradle workers so the 3.6GB box doesn't swap-death-spiral
export KOTLIN_DAEMON_JVMARGS="-Xmx768m"
export ORG_GRADLE_PROJECT_kotlin_daemon_jvm_args="-Xmx768m"

cd "$(dirname "$0")/.."
chmod +x gradlew
./gradlew assembleDebug --no-daemon --max-workers=2 --stacktrace "$@" 2>&1 | tee /tmp/blackbox_build.log
