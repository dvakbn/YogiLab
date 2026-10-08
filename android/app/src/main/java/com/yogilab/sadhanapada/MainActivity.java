package com.yogilab.sadhanapada;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Calendar;

public class MainActivity extends Activity {
    static final String CHANNEL_ID = "yogilab_notifications";
    static final int NOTIFICATION_PERMISSION_REQUEST = 1001;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();

        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);
        s.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new AndroidNotificationsBridge(this), "AndroidNotifications");
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(com.yogilab.sadhanapada.R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(getString(com.yogilab.sadhanapada.R.string.notification_channel_description));
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    public class AndroidNotificationsBridge {
        private final Context context;
        AndroidNotificationsBridge(Context context) { this.context = context; }

        @JavascriptInterface
        public void requestPermission() {
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
            }
        }

        @JavascriptInterface
        public void scheduleDaily(int hour, int minute) {
            saveDaily(hour, minute, true);
            scheduleAlarm(NotificationReceiver.TYPE_DAILY, hour, minute, -1);
        }

        @JavascriptInterface
        public void scheduleWeekly(int dayOfWeek, int hour, int minute) {
            saveWeekly(dayOfWeek, hour, minute, true);
            scheduleAlarm(NotificationReceiver.TYPE_WEEKLY, hour, minute, dayOfWeek);
        }

        private void saveDaily(int hour, int minute, boolean enabled) {
            getSharedPreferences("notifications", MODE_PRIVATE).edit()
                .putBoolean("daily_enabled", enabled)
                .putInt("daily_hour", hour)
                .putInt("daily_minute", minute)
                .apply();
        }

        private void saveWeekly(int day, int hour, int minute, boolean enabled) {
            getSharedPreferences("notifications", MODE_PRIVATE).edit()
                .putBoolean("weekly_enabled", enabled)
                .putInt("weekly_day", day)
                .putInt("weekly_hour", hour)
                .putInt("weekly_minute", minute)
                .apply();
        }
    }

    public static void scheduleAlarm(Context context, int type, int hour, int minute, int dayOfWeek) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Calendar now = Calendar.getInstance();
        Calendar next = (Calendar) now.clone();
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, minute);

        if (type == NotificationReceiver.TYPE_WEEKLY) {
            int current = next.get(Calendar.DAY_OF_WEEK);
            int target = dayOfWeek + 1; // JS: 0=Sunday..6=Saturday; Calendar: 1..7
            int delta = (target - current + 7) % 7;
            if (delta == 0 && !next.after(now)) delta = 7;
            next.add(Calendar.DAY_OF_YEAR, delta);
        } else if (!next.after(now)) {
            next.add(Calendar.DAY_OF_YEAR, 1);
        }

        int requestCode = type == NotificationReceiver.TYPE_DAILY ? 4101 : 4102;
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(type == NotificationReceiver.TYPE_DAILY ? "YOGILAB_DAILY" : "YOGILAB_WEEKLY");
        intent.putExtra("type", type);
        PendingIntent pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pi);
        // Inexact alarms avoid requiring the special exact-alarm permission.
        if (Build.VERSION.SDK_INT >= 23) alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
        else alarmManager.set(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
    }

    public static void rescheduleSaved(Context context) {
        android.content.SharedPreferences p = context.getSharedPreferences("notifications", Context.MODE_PRIVATE);
        if (p.getBoolean("daily_enabled", false)) {
            scheduleAlarm(context, NotificationReceiver.TYPE_DAILY, p.getInt("daily_hour",20), p.getInt("daily_minute",30), -1);
        }
        if (p.getBoolean("weekly_enabled", false)) {
            scheduleAlarm(context, NotificationReceiver.TYPE_WEEKLY, p.getInt("weekly_hour",20), p.getInt("weekly_minute",30), p.getInt("weekly_day",0));
        }
    }
}
