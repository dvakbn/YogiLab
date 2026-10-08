@echo off
setlocal
if not exist gradlew.bat (
  echo Gradle wrapper is not included in this source-only package.
  echo Open this folder in Android Studio and let it sync, or install Gradle 8.10.2 and run:
  echo gradle wrapper --gradle-version 8.10.2
  exit /b 1
)
gradlew.bat assembleDebug
echo APK: app\build\outputs\apk\debug\app-debug.apk
