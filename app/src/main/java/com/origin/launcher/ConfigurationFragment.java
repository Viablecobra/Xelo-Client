package com.origin.launcher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.util.Log;
import com.google.android.material.card.MaterialCardView;
import com.origin.launcher.R;
import com.origin.launcher.FeatureSettings;
import com.origin.launcher.animation.DynamicAnim;
import com.origin.launcher.LogcatOverlayManager;

public class ConfigurationFragment extends BaseThemedFragment {

private LinearLayout settingsItemsContainer;
    private RecyclerView settingsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        DynamicAnim.applyPressScaleRecursively(findViewById(android.R.id.content));

        ImageButton backButton = findViewById(R.id.back_button);
        if (backButton != null) backButton.setOnClickListener(v -> finish());

        settingsRecyclerView = findViewById(R.id.settings_recycler);
        settingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        settingsRecyclerView.setAdapter(new SettingsAdapter(container -> {
            settingsItemsContainer = container;

            FeatureSettings fs = FeatureSettings.getInstance();
            addSwitchItem(getString(R.string.version_isolation), fs.isVersionIsolationEnabled(), (btn, checked) -> fs.setVersionIsolationEnabled(checked));
            addSwitchItem(getString(R.string.launcher_managed_mc_login), fs.isLauncherManagedMcLoginEnabled(), (btn, checked) -> fs.setLauncherManagedMcLoginEnabled(checked));
            addSwitchItem(getString(R.string.show_logcat_overlay), fs.isLogcatOverlayEnabled(), (btn, checked) -> {
                fs.setLogcatOverlayEnabled(checked);
                try {
                    LogcatOverlayManager mgr = LogcatOverlayManager.getInstance();
                    if (mgr != null) mgr.refreshVisibility();
                } catch (Throwable ignored) {}
            });
        }));

        settingsRecyclerView.post(() -> DynamicAnim.staggerRecyclerChildren(settingsRecyclerView));
    }

    private void addSwitchItem(String label, boolean defChecked, Switch.OnCheckedChangeListener listener) {
        View ll = LayoutInflater.from(this).inflate(R.layout.item_settings_switch, settingsItemsContainer, false);
        ((TextView) ll.findViewById(R.id.tv_title)).setText(label);
        Switch sw = ll.findViewById(R.id.switch_value);
        sw.setChecked(defChecked);
        if (listener != null) sw.setOnCheckedChangeListener(listener);
        settingsItemsContainer.addView(ll);
    }

    private Spinner addSpinnerItem(String label, String[] options, int defaultIdx) {
        View ll = LayoutInflater.from(this).inflate(R.layout.item_settings_spinner, settingsItemsContainer, false);
        ((TextView) ll.findViewById(R.id.tv_title)).setText(label);
        Spinner spinner = ll.findViewById(R.id.spinner_value);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, options);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setPopupBackgroundResource(R.drawable.bg_popup_menu_rounded);
        DynamicAnim.applyPressScale(spinner);
        spinner.setSelection(defaultIdx);
        settingsItemsContainer.addView(ll);
        return spinner;
    }

    private void addActionButton(String label, String buttonText, View.OnClickListener listener) {
        View ll = LayoutInflater.from(this).inflate(R.layout.item_settings_button, settingsItemsContainer, false);
        ((TextView) ll.findViewById(R.id.tv_title)).setText(label);
        Button btn = ll.findViewById(R.id.btn_action);
        btn.setText(buttonText);
        btn.setOnClickListener(listener);
        settingsItemsContainer.addView(ll);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuration, container, false);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        DiscordRPCHelper.getInstance().updateMenuPresence("Configuration");
    }

    @Override
    public void onPause() {
        super.onPause();
        DiscordRPCHelper.getInstance().updateIdlePresence();
    }
}