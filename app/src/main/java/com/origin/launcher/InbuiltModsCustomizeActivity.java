package com.origin.launcher;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.origin.launcher.Launcher.inbuilt.manager.InbuiltModSizeStore;

import java.util.HashMap;
import java.util.Map;

public class InbuiltModsCustomizeActivity extends BaseThemedActivity {

    private View lastSelectedButton;
    private View sliderContainer;
    private SeekBar sizeSeekBar;
    private View dragBoundsView;
    private final Map<String, Integer> modSizes = new HashMap<>();
    private final Map<String, View> modButtons = new HashMap<>();
    private String lastSelectedId = null;

    private static final int MIN_SIZE_DP = 32;
    private static final int MAX_SIZE_DP = 96;
    private static final int DEFAULT_SIZE_DP = 40;

    private int sizeToProgress(int sizeDp) {
        float t = (sizeDp - MIN_SIZE_DP) / (float) (MAX_SIZE_DP - MIN_SIZE_DP);
        return Math.round(t * sizeSeekBar.getMax());
    }

    private int progressToSize(int progress) {
        float t = progress / (float) sizeSeekBar.getMax();
        return MIN_SIZE_DP + Math.round(t * (MAX_SIZE_DP - MIN_SIZE_DP));
    }

    private int clampSize(int s) {
        return Math.max(MIN_SIZE_DP, Math.min(s, MAX_SIZE_DP));
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbuilt_mods_customize);

        dragBoundsView = findViewById(R.id.customize_root);
        Button resetButton = findViewById(R.id.reset_button);
        Button doneButton = findViewById(R.id.done_button);
        FrameLayout grid = findViewById(R.id.inbuilt_buttons_grid);
        sliderContainer = findViewById(R.id.slider_container);
        sizeSeekBar = findViewById(R.id.size_seekbar);
        sizeSeekBar.setMax(100);

        View root = findViewById(R.id.customize_root);
        root.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastSelectedButton = null;
                lastSelectedId = null;
                sliderContainer.setVisibility(View.GONE);
            }
            return false;
        });

        addModButton(grid, R.drawable.ic_sprint, "auto_sprint");
        addModButton(grid, R.drawable.ic_quick_drop, "quick_drop");
        addModButton(grid, R.drawable.ic_hud, "toggle_hud");
        addModButton(grid, R.drawable.ic_camera, "camera_perspective");

        for (Map.Entry<String, Integer> e : modSizes.entrySet()) {
            int s = e.getValue();
            s = clampSize(s <= 0 ? DEFAULT_SIZE_DP : s);
            e.setValue(s);
        }

        int initialSize = clampSize(DEFAULT_SIZE_DP);
        sizeSeekBar.setProgress(sizeToProgress(initialSize));
        sliderContainer.setVisibility(View.GONE);
        lastSelectedButton = null;
        lastSelectedId = null;

        resetButton.setOnClickListener(v -> resetSizes(grid));

        doneButton.setOnClickListener(v -> {
            Intent result = new Intent();
            for (Map.Entry<String, Integer> e : modSizes.entrySet()) {
                String id = e.getKey();
                int sizeDp = e.getValue();
                InbuiltModSizeStore.getInstance().setSize(id, sizeDp);
                result.putExtra("size_" + id, sizeDp);

                View btn = modButtons.get(id);
                if (btn != null) {
                    float x = btn.getX();
                    float y = btn.getY();
                    InbuiltModSizeStore.getInstance().setPositionX(id, x);
                    InbuiltModSizeStore.getInstance().setPositionY(id, y);
                }
            }
            setResult(RESULT_OK, result);
            finish();
        });

        sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (lastSelectedButton == null || lastSelectedId == null) return;
                int sizeDp = clampSize(progressToSize(progress));
                int sizePx = dpToPx(sizeDp);

                ViewGroup.LayoutParams lp = lastSelectedButton.getLayoutParams();
                lp.width = sizePx;
                lp.height = sizePx;
                lastSelectedButton.setLayoutParams(lp);

                modSizes.put(lastSelectedId, sizeDp);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        sizeSeekBar.post(() -> {
            int h = sizeSeekBar.getHeight();
            int padV = h / 4;
            int padH = h / 3;
            sizeSeekBar.setPadding(padH, padV, padH, padV);

            ViewGroup.MarginLayoutParams lp =
                    (ViewGroup.MarginLayoutParams) sizeSeekBar.getLayoutParams();
            int marginV = h / 4;
            int marginH = h / 4;
            lp.setMargins(marginH, marginV, marginH, marginV);
            sizeSeekBar.setLayoutParams(lp);
        });

        resetButton.post(() -> {
            int h = resetButton.getHeight();
            int padV = h / 4;
            int padH = h / 3;
            resetButton.setPadding(padH, padV, padH, padV);

            ViewGroup.MarginLayoutParams lp =
                    (ViewGroup.MarginLayoutParams) resetButton.getLayoutParams();
            int marginV = h / 4;
            int marginH = h / 4;
            lp.setMargins(marginH, marginV, marginH, marginV);
            resetButton.setLayoutParams(lp);
        });

        doneButton.post(() -> {
            int h = doneButton.getHeight();
            int padV = h / 4;
            int padH = h / 3;
            doneButton.setPadding(padH, padV, padH, padV);

            ViewGroup.MarginLayoutParams lp =
                    (ViewGroup.MarginLayoutParams) doneButton.getLayoutParams();
            int marginV = h / 4;
            int marginH = h / 4;
            lp.setMargins(marginH, marginV, marginH, marginV);
            doneButton.setLayoutParams(lp);
        });
    }

    private void addModButton(FrameLayout grid, int iconResId, String id) {
        ImageButton btn = new ImageButton(this);
        btn.setImageResource(iconResId);
        btn.setBackgroundResource(R.drawable.bg_overlay_button);
        btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        int savedSizeDp = InbuiltModSizeStore.getInstance().getSize(id);
        if (savedSizeDp <= 0) savedSizeDp = DEFAULT_SIZE_DP;
        savedSizeDp = clampSize(savedSizeDp);
        int sizePx = dpToPx(savedSizeDp);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(sizePx, sizePx);
        lp.leftMargin = dpToPx(8);
        lp.topMargin = dpToPx(8);
        btn.setLayoutParams(lp);

        modSizes.put(id, savedSizeDp);

        float savedX = InbuiltModSizeStore.getInstance().getPositionX(id);
        float savedY = InbuiltModSizeStore.getInstance().getPositionY(id);
        if (savedX >= 0f && savedY >= 0f) {
            btn.setX(savedX);
            btn.setY(savedY);
        }

        modButtons.put(id, btn);

        btn.setOnClickListener(v -> {
            lastSelectedButton = v;
            lastSelectedId = id;

            Integer sizeObj = modSizes.get(id);
            int sizeDp = sizeObj == null || sizeObj <= 0 ? DEFAULT_SIZE_DP : clampSize(sizeObj);
            int sizePx2 = dpToPx(sizeDp);

            ViewGroup.LayoutParams lp2 = v.getLayoutParams();
            lp2.width = sizePx2;
            lp2.height = sizePx2;
            v.setLayoutParams(lp2);

            modSizes.put(id, sizeDp);
            sizeSeekBar.setProgress(sizeToProgress(sizeDp));
            sliderContainer.setVisibility(View.VISIBLE);
        });

        btn.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;
            boolean moved;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        view.bringToFront();
                        dX = event.getRawX() - view.getX();
                        dY = event.getRawY() - view.getY();
                        moved = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() - dX;
                        float newY = event.getRawY() - dY;

                        View bg = findViewById(R.id.customize_background);
                        float left = 0f;
                        float top = 0f;
                        float right = bg.getWidth() - view.getWidth();
                        float bottom = bg.getHeight() - view.getHeight();

                        if (newX < left) newX = left;
                        if (newX > right) newX = right;
                        if (newY < top) newY = top;
                        if (newY > bottom) newY = bottom;

                        view.setX(newX);
                        view.setY(newY);
                        moved = true;
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!moved) view.performClick();
                        return true;
                }
                return false;
            }
        });

        grid.addView(btn);
    }

    private void resetSizes(FrameLayout grid) {
        int defaultSizeDp = clampSize(DEFAULT_SIZE_DP);
        int defaultSizePx = dpToPx(defaultSizeDp);
        for (int i = 0; i < grid.getChildCount(); i++) {
            View c = grid.getChildAt(i);
            ViewGroup.LayoutParams lp = c.getLayoutParams();
            lp.width = defaultSizePx;
            lp.height = defaultSizePx;
            c.setLayoutParams(lp);
            c.setX(0f);
            c.setY(0f);
        }
        for (String key : modSizes.keySet()) {
            modSizes.put(key, defaultSizeDp);
            InbuiltModSizeStore.getInstance().setPositionX(key, -1f);
            InbuiltModSizeStore.getInstance().setPositionY(key, -1f);
        }
        lastSelectedButton = null;
        lastSelectedId = null;
        sliderContainer.setVisibility(View.GONE);
        sizeSeekBar.setProgress(sizeToProgress(defaultSizeDp));
    }
}