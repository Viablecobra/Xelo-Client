package com.origin.launcher;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.origin.launcher.R;
import com.origin.launcher.Launcher.inbuilt.manager.InbuiltModManager;
import com.origin.launcher.Launcher.inbuilt.model.InbuiltMod;
import com.origin.launcher.Adapter.InbuiltModsAdapter;
import com.origin.launcher.animation.DynamicAnim;

import java.util.List;

public class InbuiltModsActivity extends BaseThemedActivity {

    private RecyclerView recyclerView;
    private InbuiltModsAdapter adapter;
    private InbuiltModManager modManager;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbuilt_mods);

        View root = findViewById(android.R.id.content);
        if (root != null) {
            DynamicAnim.applyPressScaleRecursively(root);
        }

        modManager = InbuiltModManager.getInstance(this);
        setupViews();
        loadMods();
    }

    private void setupViews() {
        ImageButton closeButton = findViewById(R.id.close_inbuilt_button);
        closeButton.setOnClickListener(v -> finish());
        DynamicAnim.applyPressScale(closeButton);

        recyclerView = findViewById(R.id.inbuilt_mods_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        emptyText = findViewById(R.id.empty_inbuilt_text);

        adapter = new InbuiltModsAdapter(modManager);
        adapter.setOnToggleClickListener((mod, enable) -> {
            if (enable) {
                modManager.addMod(mod.getId());
                Toast.makeText(this,
                        getString(R.string.inbuilt_mod_added, mod.getName()),
                        Toast.LENGTH_SHORT).show();
            } else {
                modManager.removeMod(mod.getId());
                Toast.makeText(this,
                        getString(R.string.inbuilt_mod_removed, mod.getName()),
                        Toast.LENGTH_SHORT).show();
            }
            loadMods();
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadMods() {
        List<InbuiltMod> mods = modManager.getAvailableMods(this);
        adapter.updateMods(mods);
        emptyText.setVisibility(mods.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(mods.isEmpty() ? View.GONE : View.VISIBLE);
        recyclerView.post(() -> DynamicAnim.staggerRecyclerChildren(recyclerView));
    }
}