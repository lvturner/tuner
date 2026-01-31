#!/bin/bash
# Environment setup script for Guitar Tuner Android project
# Run with: source setup-env.sh

# Project root directory
export TUNER_PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "TUNER_PROJECT_ROOT set to: $TUNER_PROJECT_ROOT"

# Java Configuration
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
echo "JAVA_HOME set to: $JAVA_HOME"

# Android SDK Configuration
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
echo "ANDROID_HOME set to: $ANDROID_HOME"

# Add Android platform-tools to PATH (for adb, fastboot)
export PATH="$ANDROID_HOME/platform-tools:$PATH"

# Add Android build-tools to PATH
BUILD_TOOLS_DIR=$(ls -d $ANDROID_HOME/build-tools/*/ 2>/dev/null | sort -V | tail -n1)
if [ -n "$BUILD_TOOLS_DIR" ]; then
    export PATH="$BUILD_TOOLS_DIR:$PATH"
    echo "Build tools added from: $BUILD_TOOLS_DIR"
fi

# Set Gradle properties to use our Java installation
export GRADLE_OPTS="-Dorg.gradle.java.home=$JAVA_HOME"

# Project-specific aliases
alias tuner-build="./gradlew assembleDebug"
alias tuner-clean="./gradlew clean"
alias tuner-assemble="./gradlew assembleRelease"
alias tuner-test="./gradlew test"
alias tuner-lint="./gradlew lint"
alias tuner-deploy="$TUNER_PROJECT_ROOT/tools/deploy.sh"
alias tuner-logcat="adb logcat | grep com.rokid.tuner"
alias tuner-devices="adb devices"

# Git aliases for this project
alias tuner-status="git status"
alias tuner-commit="git commit"
alias tuner-push="git push"

echo ""
echo "Environment setup complete for Guitar Tuner!"
echo ""
echo "Available commands:"
echo "  tuner-build      - Build debug APK"
echo "  tuner-clean      - Clean project"
echo "  tuner-assemble   - Build release APK"
echo "  tuner-test       - Run tests"
echo "  tuner-lint       - Run lint checks"
echo "  tuner-deploy     - Deploy to connected device"
echo "  tuner-logcat     - View app logs"
echo "  tuner-devices    - List connected devices"
echo "  tuner-status     - Git status"
echo "  tuner-commit     - Git commit"
echo "  tuner-push       - Git push"
echo ""
echo "To verify:"
echo "  java -version"
echo "  adb version"
echo "  ./gradlew --version"
echo "  ./env-check.sh    # Detailed environment check"
