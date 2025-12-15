package com.origin.launcher.Launcher.inbuilt.overlay;

import android.app.Activity;

import com.origin.launcher.Launcher.inbuilt.manager.InbuiltModManager;
import com.origin.launcher.Launcher.inbuilt.model.ModIds;
import com.origin.launcher.ConfigurationFragment;

import java.util.ArrayList;
import java.util.List;

public class InbuiltOverlayManager {
    private static volatile InbuiltOverlayManager instance;
    private final Activity activity;
    private final List<BaseOverlayButton> overlays = new ArrayList<>();
    private int nextY = 150;
    private static final int SPACING = 70;
    private static final int START_X = 50;

    public InbuiltOverlayManager(Activity activity) {
        this.activity = activity;
        instance = this;
    }

    public static InbuiltOverlayManager getInstance() {
        return instance;
    }

    public void showEnabledOverlays() {
        InbuiltModManager manager = InbuiltModManager.getInstance(activity);
        nextY = 150;

        if (manager.isModAdded(ModIds.QUICK_DROP)) {
            QuickDropOverlay overlay = new QuickDropOverlay(activity);
            overlay.show(START_X, nextY);
            overlays.add(overlay);
            nextY += SPACING;
        }
        if (manager.isModAdded(ModIds.CAMERA_PERSPECTIVE)) {
            CameraPerspectiveOverlay overlay = new CameraPerspectiveOverlay(activity);
            overlay.show(START_X, nextY);
            overlays.add(overlay);
            nextY += SPACING;
        }
        if (manager.isModAdded(ModIds.TOGGLE_HUD)) {
            ToggleHudOverlay overlay = new ToggleHudOverlay(activity);
            overlay.show(START_X, nextY);
            overlays.add(overlay);
            nextY += SPACING;
        }
        if (manager.isModAdded(ModIds.AUTO_SPRINT)) {
            AutoSprintOverlay overlay = new AutoSprintOverlay(activity, manager.getAutoSprintKey());
            overlay.show(START_X, nextY);
            overlays.add(overlay);
            nextY += SPACING;
        }
    }

    public void hideAllOverlays() {
        for (BaseOverlayButton overlay : overlays) {
            overlay.hide();
        }
        overlays.clear();
        instance = null;
    }
}