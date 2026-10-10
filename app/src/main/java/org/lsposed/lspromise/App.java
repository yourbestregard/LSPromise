package org.lsposed.lspromise; 
import android.app.Application;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        installFutureLib();
    }

    private void installFutureLib() {
        File dir = new File(getApplicationInfo().dataDir, "ksud");
        File target = new File(dir, "libksud.so");

        long currentVersion = getVersionCode();
        SharedPreferences prefs = getSharedPreferences("ksud_lib", MODE_PRIVATE);

        // Lewati jika sudah ada dan versi aplikasi belum berubah
        if (target.exists() && prefs.getLong("ver", -1) == currentVersion) return;

        if (!dir.exists() && !dir.mkdirs()) {
            Log.e("App", "Gagal membuat folder " + dir);
            return;
        }

        File tmp = new File(dir, "libksud.so.tmp");
        try (InputStream in = getAssets().open("libksud.so");
             OutputStream out = new FileOutputStream(tmp)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
        } catch (IOException e) {
            Log.e("App", "Gagal menyalin libksud.so", e);
            tmp.delete();
            return;
        }

        if (target.exists()) target.delete();
        if (tmp.renameTo(target)) {
            target.setReadOnly();
            prefs.edit().putLong("ver", currentVersion).apply();
        }
    }

    @SuppressWarnings("deprecation")
    private long getVersionCode() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                    ? info.getLongVersionCode()
                    : info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }
}