package com.origin.launcher.Launcher.inbuilt.overlay;

import android.app.Activity;
import android.widget.ImageButton;

import com.origin.launcher.R;
import com.origin.launcher.Launcher.inbuilt.manager.InbuiltModSizeStore;

public class AutoSprintOverlay extends BaseOverlayButton {
    private static final String MOD_ID = "auto_sprint";
    private boolean isActive = false;
    private int sprintKey;

    private static final float MIN_SCALE = 1.0f;
    private static final float MAX_SCALE = 2.5f;
    private static final float DEFAULT_SCALE = 1.0f;

    private float clampScale(float s) {
        return Math.max(MIN_SCALE, Math.min(s, MAX_SCALE));
    }

    public AutoSprintOverlay(Activity activity, int sprintKey) {
        super(activity);
        this.sprintKey = sprintKey;
    }

    @Override
    protected int getIconResource() {
        return R.drawable.ic_sprint;
    }

    @Override
    protected void onOverlayViewCreated(ImageButton btn) {
        btn.setBackgroundResource(R.drawable.bg_overlay_button);
        btn.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);

        float scale = InbuiltModSizeStore.getInstance().getScale(MOD_ID);
        if (scale <= 0f) scale = DEFAULT_SCALE;
        scale = clampScale(scale);
        btn.setScaleX(scale);
        btn.setScaleY(scale);
    }

    @Override
    protected void onButtonClick() {
        isActive = !isActive;
        if (isActive) {
            sendKeyDown(sprintKey);
            updateButtonState(true);
        } else {
            sendKeyUp(sprintKey);
            updateButtonState(false);
        }
    }

    private void updateButtonState(boolean active) {
        if (overlayView != null) {
            ImageButton btn = overlayView.findViewById(R.id.mod_overlay_button);
            if (btn != null) {
                btn.setAlpha(active ? 1.0f : 0.6f);
                btn.setBackgroundResource(
                        active ? R.drawable.bg_overlay_button_active
                               : R.drawable.bg_overlay_button
                );
            }
        }
    }

    @Override
    public void hide() {
        if (isActive) {
            sendKeyUp(sprintKey);
            isActive = false;
        }
        super.hide();
    }
}