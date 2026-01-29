#!/bin/bash
# Environment check script for Guitar Tuner Android project

echo "🔍 Checking Guitar Tuner development environment..."
echo "=================================================="

# 1. Java check
echo "1. Java:"
if [ -n "$JAVA_HOME" ]; then
    echo "   JAVA_HOME=$JAVA_HOME"
    if [ -f "$JAVA_HOME/bin/java" ]; then
        "$JAVA_HOME/bin/java" -version 2>&1 | head -3 | sed 's/^/   /'
    else
        echo "   ❌ java executable not found in JAVA_HOME/bin"
    fi
else
    echo "   ❌ JAVA_HOME not set"
fi

# 2. Android SDK check
echo ""
echo "2. Android SDK:"
if [ -n "$ANDROID_HOME" ]; then
    echo "   ANDROID_HOME=$ANDROID_HOME"
    if [ -d "$ANDROID_HOME" ]; then
        echo "   ✅ Android SDK directory exists"
        # Check for platform-tools
        if [ -d "$ANDROID_HOME/platform-tools" ]; then
            echo "   ✅ platform-tools found"
            # Check adb version
            if command -v adb &> /dev/null; then
                adb version | head -1 | sed 's/^/   /'
            else
                echo "   ❌ adb not in PATH"
            fi
        else
            echo "   ❌ platform-tools missing"
        fi
    else
        echo "   ❌ ANDROID_HOME directory does not exist"
    fi
else
    echo "   ❌ ANDROID_HOME not set"
fi

# 3. Gradle check
echo ""
echo "3. Gradle:"
if [ -f "./gradlew" ]; then
    echo "   ✅ gradlew wrapper present"
    ./gradlew --version | head -1 | sed 's/^/   /'
else
    echo "   ❌ gradlew not found in project root"
fi

# 4. Project structure
echo ""
echo "4. Project structure:"
if [ -d "app/src/main" ]; then
    echo "   ✅ Android app module present"
else
    echo "   ❌ app/src/main directory missing"
fi

# 5. Build tools detection
echo ""
echo "5. Build tools:"
BUILD_TOOLS_DIR=$(ls -d $ANDROID_HOME/build-tools/*/ 2>/dev/null | sort -V | tail -n1)
if [ -n "$BUILD_TOOLS_DIR" ]; then
    echo "   ✅ Latest build tools: $(basename "$BUILD_TOOLS_DIR")"
else
    echo "   ❌ No build tools found in $ANDROID_HOME/build-tools/"
fi

echo ""
echo "=================================================="
echo "Environment check complete."