#!/bin/bash

# test_apk.sh - Test ID34 APK installation and functionality
# Usage: ./test_apk.sh [apk_path]

set -e

# Configuration
ANDROID_HOME="/home/michael/android-sdk-linux/adt-bundle-linux-x86/sdk"
APK_PATH="${1:-release/id34v2.1.apk}"
PACKAGE_NAME="com.promethylhosting.id34"
MAIN_ACTIVITY="${PACKAGE_NAME}/.Splash"

# Check if APK exists
if [ ! -f "$APK_PATH" ]; then
    echo "❌ APK not found: $APK_PATH"
    echo "Usage: ./test_apk.sh [apk_path]"
    exit 1
fi

echo "🚀 Testing ID34 APK: $APK_PATH"
echo ""

# Check ADB connection
echo "📱 Checking ADB devices..."
DEVICES=$($ANDROID_HOME/platform-tools/adb devices | grep -E "device$|emulator$" | wc -l)

if [ "$DEVICES" -eq 0 ]; then
    echo "❌ No Android devices or emulators found"
    echo "💡 Make sure an emulator is running or a device is connected"
    exit 1
fi

echo "✅ Found $DEVICES device(s)"
$ANDROID_HOME/platform-tools/adb devices
echo ""

# Get first available device
DEVICE_ID=$($ANDROID_HOME/platform-tools/adb devices | grep -E "device$|emulator$" | head -1 | awk '{print $1}')
echo "🎯 Using device: $DEVICE_ID"

# Install APK
echo "📦 Installing APK..."
if $ANDROID_HOME/platform-tools/adb -s "$DEVICE_ID" install -r "$APK_PATH" 2>/dev/null; then
    echo "✅ APK installed successfully"
else
    echo "❌ Failed to install APK"
    exit 1
fi

# Verify installation
echo ""
echo "🔍 Verifying installation..."
if $ANDROID_HOME/platform-tools/adb -s "$DEVICE_ID" shell pm list packages | grep -q "$PACKAGE_NAME"; then
    echo "✅ Package found: $PACKAGE_NAME"
else
    echo "❌ Package not found after installation"
    exit 1
fi

# Get app info
echo ""
echo "📊 App Information:"
APP_INFO=$($ANDROID_HOME/platform-tools/adb -s "$DEVICE_ID" shell dumpsys package "$PACKAGE_NAME" | grep -E "(versionName|versionCode|targetSdk)")
echo "$APP_INFO"

# Launch app
echo ""
echo "🚀 Launching application..."
if $ANDROID_HOME/platform-tools/adb -s "$DEVICE_ID" shell am start -n "$MAIN_ACTIVITY" 2>/dev/null; then
    echo "✅ App launched successfully"
    echo "💡 Activity: $MAIN_ACTIVITY"
else
    echo "❌ Failed to launch app"
    exit 1
fi

# Wait a moment for app to start
sleep 3

# Check if app is running
echo ""
echo "🔍 Checking app status..."
if $ANDROID_HOME/platform-tools/adb -s "$DEVICE_ID" shell pidof "$PACKAGE_NAME" > /dev/null 2>&1; then
    PID=$($ANDROID_HOME/platform-tools/adb -s "$DEVICE_ID" shell pidof "$PACKAGE_NAME")
    echo "✅ App is running with PID: $PID"
else
    echo "⚠️  App may not be running (could have crashed or exited)"
fi

# Get recent logs
echo ""
echo "📝 Recent app logs (last 10 lines):"
$ANDROID_HOME/platform-tools/adb -s "$DEVICE_ID" shell logcat -d | grep -i "$PACKAGE_NAME" | tail -10 || echo "   No recent logs found"

echo ""
echo "🎯 APK testing completed!"
echo "💡 Use 'adb shell logcat | grep $PACKAGE_NAME' to monitor real-time logs"