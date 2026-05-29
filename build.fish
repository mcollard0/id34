#!/usr/bin/env fish

set -l project_root (dirname (status --current-filename))
set -l keystore_path /run/media/michael/FAST_ARCHIVE/Keystore/michaelcollard-pro.keystore
set -l key_alias michaelcollard-pro

if not test -f $keystore_path
    echo "Keystore not found: $keystore_path"
    exit 1
end

if not test -x "$project_root/gradlew"
    chmod +x "$project_root/gradlew"
end

if test -d /usr/lib/jvm/java-21-openjdk
    set -gx JAVA_HOME /usr/lib/jvm/java-21-openjdk
else if test -d /usr/lib/jvm/java-17-openjdk
    set -gx JAVA_HOME /usr/lib/jvm/java-17-openjdk
else
    echo "No compatible Java found. Install Java 17 or Java 21."
    exit 1
end

set -gx PATH "$JAVA_HOME/bin" $PATH

if not test -x "$JAVA_HOME/bin/java"
    echo "JAVA_HOME is set but invalid: $JAVA_HOME"
    exit 1
end

read --silent --prompt-str "Keystore password (used for store + key): " signing_password
echo
if test -z "$signing_password"
    echo "Password cannot be empty."
    exit 1
end

set -lx ID34_KEYSTORE_PASSWORD "$signing_password"
set -lx ID34_KEY_PASSWORD "$signing_password"
set -lx ID34_KEY_ALIAS "$key_alias"

echo "Building signed debug APK..."
"$project_root/gradlew" -p "$project_root" --no-daemon -Dorg.gradle.java.home="$JAVA_HOME" :app:assembleDebug
or exit $status

echo "Building signed release APK..."
"$project_root/gradlew" -p "$project_root" --no-daemon -Dorg.gradle.java.home="$JAVA_HOME" :app:assembleRelease
or exit $status

echo "Done."
echo "Debug APK: app/build/outputs/apk/debug/"
echo "Release APK: app/build/outputs/apk/release/"
