#!/bin/bash

# monitor_save_crash.sh - Monitor ID34 app for save-related crashes
# Usage: ./monitor_save_crash.sh

export ANDROID_HOME="/home/michael/android-sdk-linux/adt-bundle-linux-x86/sdk"
PACKAGE_NAME="com.promethylhosting.id34"

echo "🔍 Monitoring ID34 app for save-related issues..."
echo "📱 Package: $PACKAGE_NAME"
echo "⏰ Starting monitoring at $(date)"
echo ""

# Check if device is connected
if ! $ANDROID_HOME/platform-tools/adb devices | grep -q device; then
    echo "❌ No Android device found"
    exit 1
fi

echo "👀 Monitoring logcat for save operations and crashes..."
echo "🛑 Press Ctrl+C to stop monitoring"
echo "───────────────────────────────────────────────────"

# Clear logcat buffer first
$ANDROID_HOME/platform-tools/adb logcat -c

# Monitor for various save-related patterns
$ANDROID_HOME/platform-tools/adb logcat | grep --line-buffered -E \
    "(id34|ID34|insertIdea|insertCat|replace|SQLite|database|FATAL|AndroidRuntime|crash|save|updateDB)" | \
while read line; do
    # Color coding for different types of messages
    if echo "$line" | grep -q "FATAL\|AndroidRuntime"; then
        echo -e "\033[31m🚨 CRASH: $line\033[0m"  # Red
    elif echo "$line" | grep -q "ERROR\|Exception"; then
        echo -e "\033[91m❌ ERROR: $line\033[0m"   # Light red
    elif echo "$line" | grep -q "insertIdea\|insertCat\|replace\|updateDB"; then
        echo -e "\033[32m💾 SAVE: $line\033[0m"    # Green
    elif echo "$line" | grep -q "SQLite\|database"; then
        echo -e "\033[33m🗃️  DB: $line\033[0m"      # Yellow
    elif echo "$line" | grep -q "id34\|ID34"; then
        echo -e "\033[36m📱 APP: $line\033[0m"     # Cyan
    else
        echo "$line"
    fi
done