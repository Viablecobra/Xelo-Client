package com.origin.launcher;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

public class InbuiltModsCustomizeActivity extends BaseThemedActivity {

    private View lastSelectedButton;
    private View sliderContainer;
    private SeekBar sizeSeekBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbuilt_mods_customize);

        Button resetButton = findViewById(R.id.reset_button);
        Button doneButton = findViewById(R.id.done_button);
        GridLayout grid = findViewById(R.id.inbuilt_buttons_grid);
        sliderContainer = findViewById(R.id.slider_container);
        sizeSeekBar = findViewById(R.id.size_seekbar);

        // Add buttons for inbuilt mods
        addModButton(grid, "Q", "auto_sprint");
        addModButton(grid, "Drop", "quick_drop");
        addModButton(grid, "HUD", "toggle_hud");
        addModButton(grid, "Cam", "camera_perspective");

        resetButton.setOnClickListener(v -> resetSizes(grid));
        doneButton.setOnClickListener(v -> finish());

        sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (lastSelectedButton == null) return;
                float scale = 0.5f + progress / 200f;
                lastSelectedButton.setScaleX(scale);
                lastSelectedButton.setScaleY(scale);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void addModButton(GridLayout grid, String label, String id) {
        Button btn = new Button(this);
        btn.setText(label);

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(8, 8, 8, 8);
        btn.setLayoutParams(lp);

        btn.setOnClickListener(v -> {
            lastSelectedButton = v;
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
        lastSelectedButton = null;
        sliderContainer.setVisibility(View.GONE);
        sizeSeekBar.setProgress(0);
    }
}