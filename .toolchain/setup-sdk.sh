#!/usr/bin/env bash
# Blackbox build environment setup. Run after cmdline-tools download finishes.
set -e
export ANDROID_HOME=$HOME/Android
export SDK_ROOT=$HOME/Android/Sdk
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator
CM=$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager

# Accept licenses non-interactively
yes | $CM --sdk_root=$SDK_ROOT licenses >/dev/null 2>&1 || true

# Core packages needed by Blackbox build
$CM --sdk_root=$SDK_ROOT "platforms;android-35" "build-tools;35.0.0" "ndk;29.0.14206865" "cmake;3.22.1"
echo "SDK packages installed."
