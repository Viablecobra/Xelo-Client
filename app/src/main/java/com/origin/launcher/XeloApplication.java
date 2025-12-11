package com.origin.launcher;

import android.app.Application;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import xcrash.XCrash;

public class XeloApplication extends Application {

    private static final String TAG = "XeloApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Initializing Xelo Application");

        // Initialize xCrash for crash reporting
        initializeXCrash();

        // Initialize ThemeManager asynchronously to avoid blocking the main thread
        new Thread(() -> {
            try {
                ThemeManager.getInstance(XeloApplication.this);
                Log.d(TAG, "ThemeManager initialized in background thread");
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize ThemeManager", e);
            }
        }).start();
    }

    private void initializeXCrash() {
        try {
            // Use old external storage directory
            File crashDir = new File(
                    Environment.getExternalStorageDirectory(),
                    "games/xelo_client/crash_logs"
            );

            if (!crashDir.exists()) {
                boolean created = crashDir.mkdirs();
                Log.d(TAG, "Crash log directory created: " + created + " at " + crashDir.getAbsolutePath());
            }

            // Configure xCrash (3.1.0) without IAnrCallback
            XCrash.InitParameters parameters = new XCrash.InitParameters()
                    .setLogDir(crashDir.getAbsolutePath())
                    .setJavaRethrow(true)
                    .setNativeRethrow(true)
                    .setAnrRethrow(true); // ANR reporting enabled

            // Initialize xCrash
            XCrash.init(this, parameters);

            Log.d(TAG, "xCrash initialized successfully at " + crashDir.getAbsolutePath());

        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize xCrash", e);
        }
    }
}