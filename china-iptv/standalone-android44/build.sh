#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$ROOT/../.." && pwd)"
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [ -z "$SDK" ]; then echo "ANDROID_HOME / ANDROID_SDK_ROOT is required" >&2; exit 1; fi
TOOLS="$SDK/build-tools/33.0.2"
PLATFORM="$SDK/platforms/android-33/android.jar"
test -f "$PLATFORM"
test -x "$TOOLS/aapt"
test -x "$TOOLS/d8"
mkdir -p "$ROOT/assets" "$ROOT/build/gen" "$ROOT/build/classes" "$ROOT/build/dex" "$ROOT/build/output"
# Bundle the current full user-managed list so first install already shows channels even offline.
cp "$REPO_ROOT/china-iptv/live.txt" "$ROOT/assets/fallback_live.txt"

"$TOOLS/aapt" package -f -m -J "$ROOT/build/gen" \
  -M "$ROOT/AndroidManifest.xml" -S "$ROOT/res" -A "$ROOT/assets" \
  -I "$PLATFORM"

find "$ROOT/src" "$ROOT/build/gen" -type f -name '*.java' > "$ROOT/build/sources.txt"
javac -source 7 -target 7 -encoding UTF-8 \
  -classpath "$PLATFORM" -d "$ROOT/build/classes" @"$ROOT/build/sources.txt"

find "$ROOT/build/classes" -type f -name '*.class' > "$ROOT/build/classes.txt"
mapfile -t class_files < "$ROOT/build/classes.txt"
"$TOOLS/d8" --min-api 19 --lib "$PLATFORM" \
  --output "$ROOT/build/dex" "${class_files[@]}"

"$TOOLS/aapt" package -f \
  -M "$ROOT/AndroidManifest.xml" -S "$ROOT/res" -A "$ROOT/assets" \
  -I "$PLATFORM" \
  -F "$ROOT/build/output/china-unsigned-unaligned.apk"
(
  cd "$ROOT/build/dex"
  "$TOOLS/aapt" add "$ROOT/build/output/china-unsigned-unaligned.apk" classes.dex
)
"$TOOLS/zipalign" -f 4 \
  "$ROOT/build/output/china-unsigned-unaligned.apk" \
  "$ROOT/build/output/china-unsigned-aligned.apk"

KEYSTORE="$ROOT/build/output/temporary-signing.jks"
KEYSTORE_PASS="$(openssl rand -hex 14)"
keytool -genkeypair -noprompt -keystore "$KEYSTORE" \
  -alias china -storepass "$KEYSTORE_PASS" -keypass "$KEYSTORE_PASS" \
  -keyalg RSA -keysize 2048 -validity 3650 \
  -dname "CN=China IPTV 4.4,O=Community,C=CN" >/dev/null

APK="$ROOT/build/output/China-IPTV-Android-4.4.apk"
"$TOOLS/apksigner" sign \
  --ks "$KEYSTORE" --ks-key-alias china \
  --ks-pass "pass:$KEYSTORE_PASS" --key-pass "pass:$KEYSTORE_PASS" \
  --v1-signing-enabled true --v2-signing-enabled true \
  --min-sdk-version 19 --out "$APK" \
  "$ROOT/build/output/china-unsigned-aligned.apk"

"$TOOLS/aapt" dump badging "$APK" | grep -E "package:|sdkVersion:|targetSdkVersion:|native-code:"
"$TOOLS/zipalign" -c -v 4 "$APK" | tail -n 3
"$TOOLS/apksigner" verify --verbose --min-sdk-version 19 "$APK"
mkdir -p "$REPO_ROOT/artifacts"
cp "$APK" "$REPO_ROOT/artifacts/China-IPTV-Android-4.4.apk"
sha256sum "$REPO_ROOT/artifacts/China-IPTV-Android-4.4.apk" > "$REPO_ROOT/artifacts/SHA256-Android44.txt"
printf '\nOutput: %s\n' "$REPO_ROOT/artifacts/China-IPTV-Android-4.4.apk"
