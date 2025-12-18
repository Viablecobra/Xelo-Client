package com.origin.launcher.Launcher.inbuilt.overlay;

import android.app.Activity;
import android.view.KeyEvent;
import android.widget.ImageButton;

import com.origin.launcher.R;
import com.origin.launcher.Launcher.inbuilt.manager.InbuiltModSizeStore;

public class CameraPerspectiveOverlay extends BaseOverlayButton {
    private static final String MOD_ID = "camera_perspective";

    private static final float MIN_SCALE = 0.7f;
    private static final float MAX_SCALE = 1.8f;
    private static final float DEFAULT_SCALE = 1.0f;

    private float clampScale(float s) {
        return Math.max(MIN_SCALE, Math.min(s, MAX_SCALE));
    }

    public CameraPerspectiveOverlay(Activity activity) {
        super(activity);
    }

    @Override
    protected int getIconResource() {
        return R.drawable.ic_camera;
    }

    @Override
    protected void onOverlayViewCreated(ImageButton btn) {
        float scale = InbuiltModSizeStore.getInstance().getScale(MOD_ID);
        if (scale <= 0f) scale = DEFAULT_SCALE;
        scale = clampScale(scale);
        btn.setScaleX(scale);
        btn.setScaleY(scale);
    }

    @Override
    protected void onButtonClick() {
        sendKey(KeyEvent.KEYCODE_F5);
    }
}