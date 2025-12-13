package com.origin.launcher.Launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.origin.launcher.Launcher.GamePackageManager;
import com.origin.launcher.Launcher.MinecraftActivity;
import com.origin.launcher.versions.GameVersion;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MinecraftLauncher {
    private static final String TAG = "MinecraftLauncher";
    private final Context context;
    private GamePackageManager gameManager;
    public static final String MC_PACKAGE_NAME = "com.mojang.minecraftpe";

    public MinecraftLauncher(Context context) {
        this.context = context;
    }

    public static String abiToSystemLibDir(String abi) {
        if ("arm64-v8a".equals(abi)) return "arm64";
        if ("armeabi-v7a".equals(abi)) return "arm";
        return abi;
    }

    public ApplicationInfo createFakeApplicationInfo(GameVersion version, String packageName) {
        ApplicationInfo fakeInfo = new ApplicationInfo();
        File apkFile = new File(version.versionDir, "base.apk.xelo");  // YOUR XELO
        fakeInfo.sourceDir = apkFile.getAbsolutePath();
        fakeInfo.publicSourceDir = fakeInfo.sourceDir;
        String systemAbi = abiToSystemLibDir(Build.SUPPORTED_ABIS[0]);
        File dstLibDir = new File(version.versionDir, "lib/" + systemAbi);
        fakeInfo.nativeLibraryDir = dstLibDir.getAbsolutePath();
        fakeInfo.packageName = packageName;
        fakeInfo.dataDir = version.versionDir.getAbsolutePath();

        File splitsFolder = new File(version.versionDir, "splits");
        if (splitsFolder.exists() && splitsFolder.isDirectory()) {
            File[] splits = splitsFolder.listFiles();
            if (splits != null) {
                ArrayList<String> splitPathList = new ArrayList<>();
                for (File f : splits) {
                    if (f.isFile() && f.getName().endsWith(".apk.xelo")) {
                        splitPathList.add(f.getAbsolutePath());
                    }
                }
                if (!splitPathList.isEmpty()) {
                    fakeInfo.splitSourceDirs = splitPathList.toArray(new String[0]);
                }
            }
        }
        Log.d(TAG, "Fake APK: " + apkFile.getAbsolutePath() + " exists=" + apkFile.exists());
        return fakeInfo;
    }

    public void launch(Intent sourceIntent, GameVersion version) {
        Activity activity = (Activity) context;
        try {
            if (version == null) {
                Log.e(TAG, "No version selected");
                showLaunchErrorOnUi("No version selected");
                return;
            }

            // EXACT REFERENCE FLOW
            showLoading(activity);
            gameManager = GamePackageManager.getInstance(context.getApplicationContext(), version);
            fillIntentWithMcPath(sourceIntent, version);
            launchMinecraftActivity(sourceIntent, version, false);
        } catch (Exception e) {
            Log.e(TAG, "Launch failed: " + e.getMessage(), e);
            dismissLoading();
            showLaunchErrorOnUi("Launch failed: " + e.getMessage());
        }
    }

    private void fillIntentWithMcPath(Intent sourceIntent, GameVersion version) {
        sourceIntent.putExtra("MC_PATH", version.versionDir.getAbsolutePath());
        sourceIntent.putExtra("IS_INSTALLED", version.isInstalled);
        sourceIntent.putExtra("MINECRAFT_VERSION", version.versionCode);
        sourceIntent.putExtra("MINECRAFT_VERSION_DIR", version.directoryName);
    }

    private void launchMinecraftActivity(Intent sourceIntent, GameVersion version, boolean modsEnabled) {
        Activity activity = (Activity) context;
        new Thread(() -> {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    sourceIntent.putExtra("DISABLE_SPLASH_SCREEN", true);
                }
                sourceIntent.setClass(context, MinecraftActivity.class);

                ApplicationInfo mcInfo = version.isInstalled ?
                    gameManager.getPackageContext().getApplicationInfo() :
                    createFakeApplicationInfo(version, MC_PACKAGE_NAME);
                    
                sourceIntent.putExtra("MC_SRC", mcInfo.sourceDir);
                if (mcInfo.splitSourceDirs != null) {
                    sourceIntent.putExtra("MC_SPLIT_SRC", 
                        new ArrayList<>(Arrays.asList(mcInfo.splitSourceDirs)));
                }
                sourceIntent.putExtra("MODS_ENABLED", modsEnabled);

                // EXACT REFERENCE: Load libs BEFORE Activity starts
                gameManager.loadLibrary("c++_shared");
                gameManager.loadLibrary("fmod");
                gameManager.loadLibrary("MediaDecoders_Android");
                gameManager.loadLibrary("minecraftpe");
                gameManager.loadLibrary("gxcore");

                // PASS GPM to Activity
                MinecraftActivity.globalGameManager = gameManager;

                activity.runOnUiThread(() -> {
                    dismissLoading();
                    activity.startActivity(sourceIntent);
                    Log.d(TAG, "Minecraft launch requested - libs preloaded");
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to launch: " + e.getMessage(), e);
                activity.runOnUiThread(() -> {
                    dismissLoading();
                    showLaunchErrorOnUi("Failed to launch: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showLoading(Activity activity) {
        // REPLACE WITH YOUR LoadingDialog
        activity.runOnUiThread(() -> {
            // LoadingDialog.show() - your implementation
            Log.d(TAG, "Showing loading dialog");
        });
    }

    private void dismissLoading() {
        // REPLACE WITH YOUR LoadingDialog
        Log.d(TAG, "Dismissed loading dialog");
    }

    private void showLaunchErrorOnUi(String message) {
        ((Activity) context).runOnUiThread(() -> 
            Toast.makeText(context, "Failed to launch Minecraft: " + message, Toast.LENGTH_LONG).show());
    }
}