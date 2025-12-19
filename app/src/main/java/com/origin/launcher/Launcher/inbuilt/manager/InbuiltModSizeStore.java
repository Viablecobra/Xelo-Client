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
    private final Map<String, Float> posX = new HashMap<>();
    private final Map<String, Float> posY = new HashMap<>();

    private InbuiltModSizeStore() { }

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
                String key = e.getKey();
                float f = (Float) value;
                if (key.startsWith("pos_x_")) {
                    posX.put(key.substring("pos_x_".length()), f);
                } else if (key.startsWith("pos_y_")) {
                    posY.put(key.substring("pos_y_".length()), f);
                } else {
                    sizes.put(key, f);
                }
            } else if (value instanceof Double) {
                String key = e.getKey();
                float f = ((Double) value).floatValue();
                if (key.startsWith("pos_x_")) {
                    posX.put(key.substring("pos_x_".length()), f);
                } else if (key.startsWith("pos_y_")) {
                    posY.put(key.substring("pos_y_".length()), f);
                } else {
                    sizes.put(key, f);
                }
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

    public void setPositionX(String id, float x) {
        posX.put(id, x);
        if (prefs != null) {
            prefs.edit().putFloat("pos_x_" + id, x).apply();
        }
    }

    public void setPositionY(String id, float y) {
        posY.put(id, y);
        if (prefs != null) {
            prefs.edit().putFloat("pos_y_" + id, y).apply();
        }
    }

    public float getPositionX(String id) {
        Float v = posX.get(id);
        if (v != null) return v;
        if (prefs != null && prefs.contains("pos_x_" + id)) {
            float stored = prefs.getFloat("pos_x_" + id, -1f);
            posX.put(id, stored);
            return stored;
        }
        return -1f;
    }

    public float getPositionY(String id) {
        Float v = posY.get(id);
        if (v != null) return v;
        if (prefs != null && prefs.contains("pos_y_" + id)) {
            float stored = prefs.getFloat("pos_y_" + id, -1f);
            posY.put(id, stored);
            return stored;
        }
        return -1f;
    }
}