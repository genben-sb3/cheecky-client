#!/usr/bin/env bash
# Baut Cheecky für alle unterstützten Minecraft-Versionen nach build/libs/.
set -euo pipefail
cd "$(dirname "$0")"

./gradlew build -Pminecraft_version=26.3 -Pfabric_api_version=0.164.0+26.3
./gradlew build -Pminecraft_version=26.2 -Pfabric_api_version=0.161.0+26.2
ls build/libs/
