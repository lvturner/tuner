#!/bin/bash
# Deploy script for Guitar Tuner Android project
# Installs the debug APK to connected device/emulator

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"

echo "🔧 Building and deploying Guitar Tuner..."

# Check if APK exists, if not build it
if [ ! -f "$APK_PATH" ]; then
    echo "APK not found at $APK_PATH, building..."
    ../gradlew assembleDebug
fi

# Check if adb is available
if ! command -v adb &> /dev/null; then
    echo "❌ adb not found in PATH. Please ensure Android SDK is installed and configured."
    exit 1
fi

# Check for connected devices
DEVICES=$(adb devices | grep -v "List of devices" | grep -v "^$" | wc -l)
if [ "$DEVICES" -eq 0 ]; then
    echo "❌ No connected Android devices/emulators found."
    echo "   Please connect a device or start an emulator."
    exit 1
fi

echo "📱 Found $DEVICES device(s). Installing APK..."

# Install APK
adb install -r "$APK_PATH"

if [ $? -eq 0 ]; then
    echo "✅ APK installed successfully!"
    echo "   Launch the app manually or use: adb shell am start -n com.rokid.tuner/.MainActivity"
else
    echo "❌ Installation failed."
    exit 1
fi