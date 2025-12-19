package com.origin.launcher.Launcher.inbuilt.overlay;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.origin.launcher.R;
import com.origin.launcher.Launcher.inbuilt.manager.InbuiltModSizeStore;

public class AutoSprintOverlay extends BaseOverlayButton {
    private static final String MOD_ID = "auto_sprint";
    private boolean isActive = false;
    private int sprintKey;

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

        int sizeDp = InbuiltModSizeStore.getInstance().getSize(MOD_ID);
        if (sizeDp <= 0) sizeDp = 40;
        int sizePx = dpToPx(btn.getContext(), sizeDp);

        ViewGroup.LayoutParams lp = btn.getLayoutParams();
        lp.width = sizePx;
        lp.height = sizePx;
        btn.setLayoutParams(lp);
    }

    private int dpToPx(Context c, int dp) {
        return Math.round(dp * c.getResources().getDisplayMetrics().density);
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