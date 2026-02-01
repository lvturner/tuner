# Rokid Glasses (Android) Guitar Tuner

A minimalist guitar tuner Android app designed specifically for Rokid Glasses with monochromatic displays and limited input capabilities. It may also work on other Android devices or other AR/XR/AI glasses.

## Purpose

Real-time pitch detection using YIN algorithm, note detection for any tuning (chromatic), and visual tuning indicator with status display.

## Environment Setup

This project has only been tested on Linux.

Source the environment setup script to configure Java, Android SDK paths, and provide useful aliases:

```bash
source tools/setup-env.sh
```

Note that this depends on having the Android SDK installed.

After sourcing, run `./tools/env-check.sh` to verify your environment.

## Building and Deploying

Use the provided aliases after sourcing `setup-env.sh`:

- `tuner-build` - Build debug APK
- `tuner-assemble` - Build release APK
- `tuner-deploy` - Deploy to connected device

Alternatively, use the standard Gradle wrapper:

```bash
./gradlew assembleDebug    # Debug APK
./gradlew assembleRelease  # Release APK
```

The deploy script automatically builds the APK if needed and installs it to a connected Android device or emulator.

# Support
[![Tip in Crypto](https://tip.md/badge.svg)](https://tip.md/lvturner)
