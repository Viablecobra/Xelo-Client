package com.origin.launcher.Launcher.inbuilt.overlay;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.ViewGroup;
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
        btn.setBackgroundResource(R.drawable.bg_overlay_button);
        btn.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);

        int sizeDp = InbuiltModSizeStore.getInstance().getSize(MOD_ID);
        if (sizeDp <= 0) sizeDp = 40;
        int sizePx = dpToPx(btn.getContext(), sizeDp);

        ViewGroup.LayoutParams lp = btn.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(sizePx, sizePx);
        } else {
            lp.width = sizePx;
            lp.height = sizePx;
        }
        btn.setLayoutParams(lp);
    }

    private int dpToPx(Context c, int dp) {
        return Math.round(dp * c.getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onButtonClick() {
        sendKey(KeyEvent.KEYCODE_F1);
    }
}