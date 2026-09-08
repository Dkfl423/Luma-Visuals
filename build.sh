#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
if [[ -n "${JAVA_HOME:-}" ]]; then export PATH="$JAVA_HOME/bin:$PATH"; fi
if ! command -v javac >/dev/null 2>&1 || ! javac -version 2>&1 | grep -Eq '^javac 21([.[:space:]]|$)'; then
  printf '%s\n' 'Install JDK 21 (not just a JRE), then set JAVA_HOME. https://adoptium.net/temurin/releases/?version=21' >&2
  exit 1
fi
version=9.2.1
gradle="$PWD/.tools/gradle-$version/bin/gradle"
if [[ ! -x "$gradle" ]]; then
  command -v curl >/dev/null || { echo 'curl is required.' >&2; exit 1; }
  command -v unzip >/dev/null || { echo 'unzip is required.' >&2; exit 1; }
  mkdir -p .tools
  zipfile=".tools/gradle-$version-bin.zip"
  url="https://services.gradle.org/distributions/gradle-$version-bin.zip"
  curl --fail --location --retry 2 --connect-timeout 20 --max-time 600 "$url" -o "$zipfile"
  expected="$(curl --fail --location --retry 2 --connect-timeout 20 --max-time 60 "$url.sha256")"
  [[ "$expected" =~ ^[0-9a-fA-F]{64}$ ]] || { echo 'Invalid checksum response.' >&2; exit 1; }
  if command -v sha256sum >/dev/null; then actual="$(sha256sum "$zipfile" | awk '{print $1}')";
  else actual="$(shasum -a 256 "$zipfile" | awk '{print $1}')"; fi
  [[ "$actual" == "$expected" ]] || { rm -f "$zipfile"; echo 'Gradle checksum mismatch.' >&2; exit 1; }
  unzip -q -o "$zipfile" -d .tools
  rm -f "$zipfile"
fi
"$gradle" --no-daemon --console=plain clean build
printf '\n%s\n' 'SUCCESS: build/libs/luma-visuals-0.1.0+mc1.21.11.jar' 'Install the normal JAR and Fabric API for 1.21.11.'
