package com.origin.launcher;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.content.Intent;
import android.widget.ImageView;

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
        GridLayout grid = findViewById(R.id.inbuilt_buttons_grid);
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
    btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

    GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED, 1f),
            GridLayout.spec(GridLayout.UNDEFINED, 1f)
    );
    int size = dpToPx(48);
    lp.width = size;
    lp.height = size;
    lp.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
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
                float newX = event.getRawX() + dX;
                float newY = event.getRawY() + dY;

                int[] loc = new int[2];
                dragBoundsView.getLocationOnScreen(loc);
                float left = loc[0];
                float top = loc[1];
                float right = left + dragBoundsView.getWidth() - view.getWidth();
                float bottom = top + dragBoundsView.getHeight() - view.getHeight();

                // clamp to (almost) full screen
                newX = Math.max(left, Math.min(newX, right));
                newY = Math.max(top, Math.min(newY, bottom));

                view.setX(newX);
                view.setY(newY);
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