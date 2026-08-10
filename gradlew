#!/bin/sh
# Gradle wrapper script
# Adapted from standard Gradle wrapper

APP_NAME="Gradle"
GRADLE_VERSION="8.7"
DIST_URL="https://services.gradle.org/distributions/gradle-8.7-bin.zip"
WRAPPER_JAR="/home/Ryuu/Project-Blackbox-worktree/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_PROPS="/home/Ryuu/Project-Blackbox-worktree/gradle/wrapper/gradle-wrapper.properties"

die() { echo "$*" >&2; exit 1; }

GRADLE_CMD="$0"
GRADLE_HOME="${GRADLE_CMD%/*}"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"

if [ ! -f "$WRAPPER_JAR" ]; then
  mkdir -p "$(dirname "$WRADDER_JAR")" 2>/dev/null || true
  echo "Gradle wrapper JAR missing at $WRAPPER_JAR" >&2
  echo "Download Gradle $GRADLE_VERSION and place gradle-wrapper.jar in gradle/wrapper/" >&2
  exit 1
fi

JAVA_CMD="java"
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
fi

exec "$JAVA_CMD" -jar "$WRAPPER_JAR" "$@"
