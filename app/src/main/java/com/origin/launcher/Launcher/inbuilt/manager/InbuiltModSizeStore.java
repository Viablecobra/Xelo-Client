package com.origin.launcher.Launcher.inbuilt.manager;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class InbuiltModSizeStore {

    private static InbuiltModSizeStore instance;

    private static final String PREFS = "inbuilt_mod_sizes";

    private SharedPreferences prefs;
    private final Map<String, Float> sizes = new HashMap<>();

    private InbuiltModSizeStore() {
    }

    public static InbuiltModSizeStore getInstance() {
        if (instance == null) {
            instance = new InbuiltModSizeStore();
        }
        return instance;
    }

    public void init(Context appContext) {
        if (prefs != null) return;

        prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        Map<String, ?> all = prefs.getAll();
        for (Map.Entry<String, ?> e : all.entrySet()) {
            Object value = e.getValue();
            if (value instanceof Float) {
                sizes.put(e.getKey(), (Float) value);
            } else if (value instanceof Double) {
                sizes.put(e.getKey(), ((Double) value).floatValue());
            }
        }
    }

    public float getScale(String id) {
        Float v = sizes.get(id);
        if (v != null) return v;
        if (prefs != null) {
            float stored = prefs.getFloat(id, 1.0f);
            sizes.put(id, stored);
            return stored;
        }
        return 1.0f;
    }

    public void setScale(String id, float scale) {
        sizes.put(id, scale);
        if (prefs != null) {
            prefs.edit().putFloat(id, scale).apply();
        }
    }
}