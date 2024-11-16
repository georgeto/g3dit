#!/bin/sh

G3DIT_DIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

( cd "${G3DIT_DIR}" && java \
  -XX:+IgnoreUnrecognizedVMOptions \
  -XX:+UseG1GC \
  -XX:MinHeapFreeRatio=5 \
  -XX:MaxHeapFreeRatio=20 \
  --add-opens=java.desktop/java.awt=ALL-UNNAMED \
  --add-opens=java.desktop/javax.swing=ALL-UNNAMED \
  --add-opens=java.base/sun.nio.fs=ALL-UNNAMED \
  --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED \
  -Dg3dit.mode=console \
  -Dg3dit.exefile="${G3DIT_DIR}/g3dit.sh" \
  -Djava.library.path="${G3DIT_DIR}:${G3DIT_DIR}/libs" \
  -jar "${G3DIT_DIR}/g3dit.jar" \
  $@ )
