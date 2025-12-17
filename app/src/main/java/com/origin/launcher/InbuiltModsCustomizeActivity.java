package com.origin.launcher;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.SeekBar;
import android.content.Intent;
import android.widget.ImageButton;
import java.util.HashMap;
import java.util.Map;
import androidx.annotation.Nullable;

public class InbuiltModsCustomizeActivity extends BaseThemedActivity {

    private View lastSelectedButton;
    private View sliderContainer;
    private SeekBar sizeSeekBar;
    private final Map<String, Float> modScales = new HashMap<>();
private String lastSelectedId = null;
private static final float MIN_SCALE = 0.5f;
private static final float MAX_SCALE = 1.5f;
private int scaleToProgress(float scale) {
    return Math.round((scale - MIN_SCALE) / (MAX_SCALE - MIN_SCALE) * sizeSeekBar.getMax());
}

private float progressToScale(int progress) {
    return MIN_SCALE + (progress / (float) sizeSeekBar.getMax()) * (MAX_SCALE - MIN_SCALE);
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

        // Add buttons for inbuilt mods
        addModButton(grid, R.drawable.ic_sprint,       "auto_sprint");
addModButton(grid, R.drawable.ic_quick_drop,   "quick_drop");
addModButton(grid, R.drawable.ic_hud,          "toggle_hud");
addModButton(grid, R.drawable.ic_camera,       "camera_perspective");

        resetButton.setOnClickListener(v -> resetSizes(grid));
        doneButton.setOnClickListener(v -> {
    Intent result = new Intent();
    for (Map.Entry<String, Float> e : modScales.entrySet()) {
        result.putExtra("scale_" + e.getKey(), e.getValue());
    }
    setResult(RESULT_OK, result);
    finish();
});

        

sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (lastSelectedButton == null || lastSelectedId == null) return;
        float scale = 0.5f + (progress / 200f);
        lastSelectedButton.setScaleX(scale);
        lastSelectedButton.setScaleY(scale);
        modScales.put(lastSelectedId, scale);
    }
    @Override public void onStartTrackingTouch(SeekBar seekBar) {}
    @Override public void onStopTrackingTouch(SeekBar seekBar) {}
});
    }

    private void addModButton(GridLayout grid, int iconResId, String id) {
    ImageButton btn = new ImageButton(this);
    btn.setImageResource(iconResId);
    btn.setBackground(null);
    btn.setScaleType(ImageButton.ScaleType.FIT_CENTER);

    GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
    lp.width = 0;
    lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
    lp.setMargins(8, 8, 8, 8);
    btn.setLayoutParams(lp);

    modScales.put(id, 1.0f); // default

    btn.setOnClickListener(v -> {
        lastSelectedButton = v;
        lastSelectedId = id;
        float scale = modScales.get(id);
        v.setScaleX(scale);
        v.setScaleY(scale);
        sizeSeekBar.setProgress(scaleToProgress(scale));
        sliderContainer.setVisibility(View.VISIBLE);
    });

    grid.addView(btn);
}

    private void resetSizes(GridLayout grid) {
    for (int i = 0; i < grid.getChildCount(); i++) {
        View c = grid.getChildAt(i);
        c.setScaleX(1f);
        c.setScaleY(1f);
    }
    for (String key : modScales.keySet()) {
        modScales.put(key, 1.0f);
    }
    lastSelectedButton = null;
    lastSelectedId = null;
    sliderContainer.setVisibility(View.GONE);
    sizeSeekBar.setProgress(scaleToProgress(1.0f));
}
}