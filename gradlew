#!/bin/sh
# Gradle start-up script (POSIX). Resolves APP_HOME relative to this script so it
# works on any machine (CI included), not just the original author's checkout.

PRG="$0"
# Resolve symlinks
while [ -h "$PRG" ]; do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  case $link in
    /*) PRG="$link" ;;
    *)  PRG=$(dirname "$PRG")/"$link" ;;
  esac
done

APP_HOME=$(cd -P "$(dirname "$PRG")" > /dev/null && pwd -P)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$CLASSPATH" ]; then
  echo "Gradle wrapper JAR missing at $CLASSPATH" >&2
  exit 1
fi

JAVACMD="java"
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
fi

exec "$JAVACMD" $JAVA_OPTS $GRADLE_OPTS \
  -classpath "$CLASSPATH" \
  -Dorg.gradle.appname="$(basename "$0")" \
  org.gradle.wrapper.GradleWrapperMain "$@"
