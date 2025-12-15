package com.origin.launcher;

import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.util.Log;
import com.google.android.material.card.MaterialCardView;

public class ConfigurationFragment {
    private static volatile ConfigurationFragment INSTANCE;
    private static Context appContext;
    private boolean versionIsolationEnabled = false;
    private boolean logcatOverlayEnabled = false;

    public enum StorageType {
        INTERNAL,
        EXTERNAL,
        VERSION_ISOLATION
    }

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static ConfigurationFragment getInstance() {
        if (INSTANCE == null) {
            synchronized (ConfigurationFragment.class) {
                if (INSTANCE == null) {
                    INSTANCE = SettingsStorage.load(appContext);
                    if (INSTANCE == null) {
                        INSTANCE = new ConfigurationFragment();
                    }
                }
            }
        }
        return INSTANCE;
    }

    public boolean isVersionIsolationEnabled() { return versionIsolationEnabled; }
    public void setVersionIsolationEnabled(boolean enabled) { this.versionIsolationEnabled = enabled; autoSave(); }

    public boolean isLogcatOverlayEnabled() { return logcatOverlayEnabled; }
    public void setLogcatOverlayEnabled(boolean enabled) { this.logcatOverlayEnabled = enabled; autoSave(); }

    private void autoSave() {
        if (appContext != null) {
            SettingsStorage.save(appContext, this);
        }
    }
}