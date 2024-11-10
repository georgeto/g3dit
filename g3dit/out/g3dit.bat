@ECHO OFF
SETLOCAL

for %%Q in ("%~dp0\.") DO set "G3DIT_DIR=%%~fQ"

start /D "%G3DIT_DIR%" /B javaw ^
  -XX:+IgnoreUnrecognizedVMOptions ^
  -XX:+UseG1GC ^
  -XX:MinHeapFreeRatio=5 ^
  -XX:MaxHeapFreeRatio=20 ^
  --add-opens=java.desktop/java.awt=ALL-UNNAMED ^
  --add-opens=java.desktop/javax.swing=ALL-UNNAMED ^
  --add-opens=java.desktop/com.sun.java.swing.plaf.windows=ALL-UNNAMED ^
  --add-opens=java.base/sun.nio.fs=ALL-UNNAMED ^
  --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED ^
  -Dg3dit.exefile="%G3DIT_DIR%\g3dit.bat" ^
  -Djava.library.path="%G3DIT_DIR%:%G3DIT_DIR%\libs" ^
  -jar "%G3DIT_DIR%\g3dit.jar" ^
  %*
