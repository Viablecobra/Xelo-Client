package com.origin.launcher.Launcher.inbuilt.overlay;

import android.app.Activity;
import android.view.KeyEvent;
import android.widget.ImageButton;

import com.origin.launcher.R;
import com.origin.launcher.Launcher.inbuilt.manager.InbuiltModSizeStore;

public class ToggleHudOverlay extends BaseOverlayButton {
    private static final String MOD_ID = "toggle_hud";

    public ToggleHudOverlay(Activity activity) {
        super(activity);
    }

    @Override
    protected int getIconResource() {
        return R.drawable.ic_hud;
    }

    @Override
    protected void onOverlayViewCreated(ImageButton btn) {
        float scale = InbuiltModSizeStore.getInstance().getScale(MOD_ID);
        btn.setScaleX(scale);
        btn.setScaleY(scale);
    }

    @Override
    protected void onButtonClick() {
        sendKey(KeyEvent.KEYCODE_F1);
    }
}