package com.origin.launcher;

import android.app.Application;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.origin.launcher.CrashActivity;
import java.io.File;

import xcrash.ICrashCallback;
import xcrash.XCrash;

public class XeloApplication extends Application {
    private static final String TAG = "XeloApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Initializing Xelo Application");

// ---- xCrash setup ----
        ICrashCallback callback = (logPath, emergency) -> {
            try {
                Intent i = new Intent(getApplicationContext(), CrashActivity.class);
                i.putExtra("LOG_PATH", logPath);
                i.putExtra("EMERGENCY", emergency);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getApplicationContext().startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        XCrash.init(this, new XCrash.InitParameters()
                .setAppVersion(BuildConfig.VERSION_NAME)
                .setLogDir(new File(
                        Environment.getExternalStorageDirectory(),
                        "games/xelo_client/crash_logs"
                ).getAbsolutePath())
                .setJavaCallback(callback)
                .setNativeCallback(callback)
                .setAnrCallback(callback)
                .setJavaRethrow(false)
                .setNativeRethrow(false)
                .setAnrRethrow(false)
        );
        // ---- end xCrash setup ----

        // Initialize ThemeManager globally
        ThemeManager.getInstance(this);

        Log.d(TAG, "ThemeManager initialized");

try {
            System.loadLibrary("xelo_init");
        } catch (UnsatisfiedLinkError | Exception e) {
            e.printStackTrace();
        }

    }
}