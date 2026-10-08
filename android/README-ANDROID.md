# YogiLab Android wrapper

This Android project packages the supplied YogiLab web app as a native WebView APK without changing the web UI/theme/structure. The supplied icon image is used for the launcher icon.

## Notifications

The Settings screen now labels the existing daily/weekly reminder controls as **Daily Notification** and **Weekly Notification**. On Android, changing those controls schedules native Android notifications via AlarmManager. Notifications are re-scheduled after device reboot, app update, time changes, and timezone changes.

Android 13+ asks for notification permission when the user changes a notification setting.

## Build

Open the `android` folder in Android Studio and build `app` in the Debug variant. The APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

A GitHub Actions workflow is also included at `.github/workflows/build-apk.yml`; it builds and uploads the debug APK when run manually or when Android project files are pushed.
