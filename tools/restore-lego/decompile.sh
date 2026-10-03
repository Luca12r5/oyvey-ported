#!/usr/bin/env bash
# Decompiles the remapped LEGO Client jar into ../../lego-client-decompiled.
# Requires: ./gradlew -p tools/restore-lego remapLego (run first), Java 21.
set -euo pipefail
cd "$(dirname "$0")"
VF_VERSION=1.11.1
OUT=build/decompiled
mkdir -p build
[ -f build/vineflower.jar ] || curl -sSfL -o build/vineflower.jar \
  "https://repo1.maven.org/maven2/org/vineflower/vineflower/${VF_VERSION}/vineflower-${VF_VERSION}.jar"
rm -rf "$OUT" && mkdir -p "$OUT"
LIBS=()
for j in build/restore/libs/*.jar; do LIBS+=("-e=$j"); done
java -Xmx3G -jar build/vineflower.jar -dgs=1 -rsy=1 -log=WARN "${LIBS[@]}" \
  build/restore/legoclient-named.jar "$OUT/"
echo "decompiled into $OUT"
