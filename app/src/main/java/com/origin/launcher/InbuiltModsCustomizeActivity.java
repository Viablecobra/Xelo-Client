package com.origin.launcher;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.origin.launcher.Launcher.inbuilt.manager.InbuiltModSizeStore;

import java.util.HashMap;
import java.util.Map;

public class InbuiltModsCustomizeActivity extends BaseThemedActivity {

    private View lastSelectedButton;
    private View sliderContainer;
    private SeekBar sizeSeekBar;
    private final Map<String, Float> modScales = new HashMap<>();
    private String lastSelectedId = null;

    private static final float MIN_SCALE = 1.0f;
    private static final float MAX_SCALE = 3.5f;

    private int scaleToProgress(float scale) {
        return Math.round((scale - MIN_SCALE) / (MAX_SCALE - MIN_SCALE) * sizeSeekBar.getMax());
    }

    private float progressToScale(int progress) {
        float t = progress / (float) sizeSeekBar.getMax();
        t = t * t;
        return MIN_SCALE + t * (MAX_SCALE - MIN_SCALE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbuilt_mods_customize);

        Button resetButton = findViewById(R.id.reset_button);
        Button doneButton = findViewById(R.id.done_button);
        GridLayout grid = findViewById(R.id.inbuilt_buttons_grid);
        sliderContainer = findViewById(R.id.slider_container);
        sizeSeekBar = findViewById(R.id.size_seekbar);
        sizeSeekBar.setMax(200);

        addModButton(grid, R.drawable.ic_sprint, "auto_sprint");
        addModButton(grid, R.drawable.ic_quick_drop, "quick_drop");
        addModButton(grid, R.drawable.ic_hud, "toggle_hud");
        addModButton(grid, R.drawable.ic_camera, "camera_perspective");

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
                float scale = progressToScale(progress);
                lastSelectedButton.setScaleX(scale);
                lastSelectedButton.setScaleY(scale);
                modScales.put(lastSelectedId, scale);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void addModButton(GridLayout grid, int iconResId, String id) {
        ImageButton btn = new ImageButton(this);
        btn.setImageResource(iconResId);
        btn.setBackgroundResource(R.drawable.bg_overlay_button);
        btn.setScaleType(ImageButton.ScaleType.FIT_CENTER);

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(8, 8, 8, 8);
        btn.setLayoutParams(lp);

        float savedScale = InbuiltModSizeStore.getInstance().getScale(id);
        modScales.put(id, savedScale);
        btn.setScaleX(savedScale);
        btn.setScaleY(savedScale);

        btn.setOnClickListener(v -> {
            lastSelectedButton = v;
            lastSelectedId = id;
            float scale = modScales.get(id);
            v.setScaleX(scale);
            v.setScaleY(scale);
            sizeSeekBar.setProgress(scaleToProgress(scale));
            sliderContainer.setVisibility(View.VISIBLE);
        });

        btn.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        view.setX(event.getRawX() + dX);
                        view.setY(event.getRawY() + dY);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        });

        grid.addView(btn);
    }

    private void resetSizes(GridLayout grid) {
        for (int i = 0; i < grid.getChildCount(); i++) {
            View c = grid.getChildAt(i);
            c.setScaleX(MIN_SCALE);
            c.setScaleY(MIN_SCALE);
        }
        for (String key : modScales.keySet()) {
            modScales.put(key, MIN_SCALE);
            InbuiltModSizeStore.getInstance().setScale(key, MIN_SCALE);
        }
        lastSelectedButton = null;
        lastSelectedId = null;
        sliderContainer.setVisibility(View.GONE);
        sizeSeekBar.setProgress(scaleToProgress(MIN_SCALE));
    }
}