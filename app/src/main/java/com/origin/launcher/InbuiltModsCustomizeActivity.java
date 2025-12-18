package com.origin.launcher;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
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
    private final Map<String, Float> modScales = new HashMap<>();
    private String lastSelectedId = null;

    private static final float MIN_SCALE = 0.7f;
    private static final float MAX_SCALE = 2.2f;
    private static final float DEFAULT_SCALE = 1.0f;

    private int scaleToProgress(float scale) {
        return Math.round((scale - MIN_SCALE) / (MAX_SCALE - MIN_SCALE) * sizeSeekBar.getMax());
    }

    private float progressToScale(int progress) {
        float t = progress / (float) sizeSeekBar.getMax();
        t = t * t;
        return MIN_SCALE + t * (MAX_SCALE - MIN_SCALE);
    }

    private float clampScale(float s) {
        return Math.max(MIN_SCALE, Math.min(s, MAX_SCALE));
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
        sizeSeekBar.setMax(200);

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

        for (Map.Entry<String, Float> e : modScales.entrySet()) {
            float s = e.getValue();
            if (s <= 0f) s = DEFAULT_SCALE;
            s = clampScale(s);
            e.setValue(s);
        }

        float initialScale = clampScale(DEFAULT_SCALE);
        sizeSeekBar.setProgress(scaleToProgress(initialScale));
        sliderContainer.setVisibility(View.GONE);
        lastSelectedButton = null;
        lastSelectedId = null;

        resetButton.setOnClickListener(v -> resetSizes(grid));

        doneButton.setOnClickListener(v -> {
            Intent result = new Intent();
            for (Map.Entry<String, Float> e : modScales.entrySet()) {
                InbuiltModSizeStore.getInstance().setScale(e.getKey(), e.getValue());
                result.putExtra("scale_" + e.getKey(), e.getValue());
            }
            setResult(RESULT_OK, result);
            finish();
        });

        sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (lastSelectedButton == null || lastSelectedId == null) return;
                float scale = clampScale(progressToScale(progress));
                lastSelectedButton.setScaleX(scale);
                lastSelectedButton.setScaleY(scale);
                modScales.put(lastSelectedId, scale);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void addModButton(FrameLayout grid, int iconResId, String id) {
        ImageButton btn = new ImageButton(this);
        btn.setImageResource(iconResId);
        btn.setBackgroundResource(R.drawable.bg_overlay_button);
        btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        int size = dpToPx(40);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
        lp.leftMargin = dpToPx(8);
        lp.topMargin = dpToPx(8);
        btn.setLayoutParams(lp);

        float savedScale = InbuiltModSizeStore.getInstance().getScale(id);
        if (savedScale <= 0f) savedScale = DEFAULT_SCALE;
        savedScale = clampScale(savedScale);
        modScales.put(id, savedScale);
        btn.setScaleX(savedScale);
        btn.setScaleY(savedScale);

        btn.setOnClickListener(v -> {
            lastSelectedButton = v;
            lastSelectedId = id;

            Float scaleObj = modScales.get(id);
            float scale = (scaleObj == null || scaleObj <= 0f)
                    ? DEFAULT_SCALE
                    : clampScale(scaleObj);

            v.setScaleX(scale);
            v.setScaleY(scale);
            modScales.put(id, scale);
            sizeSeekBar.setProgress(scaleToProgress(scale));
            sliderContainer.setVisibility(View.VISIBLE);
        });

        btn.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;
            boolean moved;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                View parent = (View) view.getParent();

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

                        float left = 0f;
                        float top = 0f;
                        float right = parent.getWidth() - view.getWidth();
                        float bottom = parent.getHeight() - view.getHeight();

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
        float defaultScale = clampScale(DEFAULT_SCALE);
        for (int i = 0; i < grid.getChildCount(); i++) {
            View c = grid.getChildAt(i);
            c.setScaleX(defaultScale);
            c.setScaleY(defaultScale);
        }
        for (String key : modScales.keySet()) {
            modScales.put(key, defaultScale);
        }
        lastSelectedButton = null;
        lastSelectedId = null;
        sliderContainer.setVisibility(View.GONE);
        sizeSeekBar.setProgress(scaleToProgress(defaultScale));
    }
}