#!/bin/sh

G3DIT_DIR="$(dirname $0)"

java \
  -XX:+IgnoreUnrecognizedVMOptions \
  -XX:+UseG1GC \
  -XX:MinHeapFreeRatio=5 \
  -XX:MaxHeapFreeRatio=20 \
  --add-opens=java.desktop/java.awt=ALL-UNNAMED \
  --add-opens=java.desktop/javax.swing=ALL-UNNAMED \
  --add-opens=java.base/sun.nio.fs=ALL-UNNAMED \
  --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED \
  -Dg3dit.mode=console \
  -Djava.library.path="${G3DIT_DIR}:${G3DIT_DIR}/libs" \
  -jar "${G3DIT_DIR}/g3dit.jar" \
  $@
