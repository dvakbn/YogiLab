package com.yogilab.sadhanapada;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.pm.PackageManager;

public class NotificationReceiver extends BroadcastReceiver {
    public static final int TYPE_DAILY = 1;
    public static final int TYPE_WEEKLY = 2;
    private static final int DAILY_ID = 5101;
    private static final int WEEKLY_ID = 5102;

    @Override
    public void onReceive(Context context, Intent intent) {
        int type = intent != null ? intent.getIntExtra("type", TYPE_DAILY) : TYPE_DAILY;
        String title = type == TYPE_WEEKLY ? "Weekly Notification · YogiLab" : "Daily Notification · YogiLab";
        String body = type == TYPE_WEEKLY ? "Your weekly check-in is ready." : "Your daily check-in is ready.";

        if (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                Intent open = new Intent(context, MainActivity.class);
                open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pi = PendingIntent.getActivity(context, type, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                Notification.Builder b = Build.VERSION.SDK_INT >= 26
                    ? new Notification.Builder(context, MainActivity.CHANNEL_ID)
                    : new Notification.Builder(context);
                b.setSmallIcon(com.yogilab.sadhanapada.R.drawable.ic_stat_notification)
                 .setContentTitle(title)
                 .setContentText(body)
                 .setAutoCancel(true)
                 .setContentIntent(pi)
                 .setPriority(Notification.PRIORITY_DEFAULT);
                nm.notify(type == TYPE_WEEKLY ? WEEKLY_ID : DAILY_ID, b.build());
            }
        }

        MainActivity.rescheduleSaved(context);
    }
}
