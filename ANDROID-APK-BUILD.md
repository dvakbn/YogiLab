# YogiLab Android APK build

The web app remains the same in UI, theme, layout, structure, and existing functionality. The Settings screen's daily/weekly reminder controls are now exposed as **Daily Notification** and **Weekly Notification**. In the Android wrapper, those controls schedule native Android notifications, which are re-scheduled after reboot, app update, clock changes, and timezone changes.

The supplied YogiLab icon image is used for the Android launcher icon and the web/PWA icons.

## Build with Android Studio

Open the `android` folder as a project. Let Android Studio install/sync the required Android SDK and Gradle components, then choose `Build > Build Bundle(s) / APK(s) > Build APK(s)` for the `debug` variant.

The APK will be:

`android/app/build/outputs/apk/debug/app-debug.apk`

## Build in GitHub Actions

The repository-level workflow `.github/workflows/build-apk.yml` builds the debug APK on demand or after changes under `android/` and uploads the APK as a workflow artifact.

A debug APK is installable on an Android phone for personal use. A Play Store release should be built as a signed release APK/AAB with a release keystore.
