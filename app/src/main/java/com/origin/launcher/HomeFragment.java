package com.origin.launcher;

import android.content.DialogInterface;
import org.jetbrains.annotations.NotNull;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import androidx.core.content.FileProvider;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Build;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import android.os.Looper;
import android.content.res.ColorStateList;
import android.graphics.Color;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.origin.launcher.ThemeManager;
import com.origin.launcher.ThemeUtils;
import android.util.Log;
import android.widget.Toast;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.origin.launcher.Launcher.MinecraftLauncher;
import com.origin.launcher.versions.GameVersion;
import com.origin.launcher.versions.VersionManager;
import com.origin.launcher.databinding.ActivityMainBinding;
import com.origin.launcher.FeatureSettings;
import com.origin.launcher.animation.DynamicAnim;

public class HomeFragment extends BaseThemedFragment {

    private static final String TAG = "HomeFragment";
    private TextView listener;
    private Button mbl2_button;
    private Button versions_button;
    private com.google.android.material.button.MaterialButton shareLogsButton;
    private MinecraftLauncher minecraftLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        listener = view.findViewById(R.id.listener);
        mbl2_button = view.findViewById(R.id.mbl2_load);
        versions_button = view.findViewById(R.id.versions_button);
        shareLogsButton = view.findViewById(R.id.share_logs_button);
        Handler handler = new Handler(Looper.getMainLooper());

        // Apply initial theme
        applyInitialTheme(view);
        
        mbl2_button.setOnClickListener(v -> launchGame());

        // Long press to clear APK selection
        mbl2_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clearSelectedApk();
                return true;
            }
        });

        versions_button.setOnClickListener(v -> {
            try {
                requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_fade_in_right,  
                        R.anim.slide_out_right, 
                        R.anim.slide_in_left,   
                        R.anim.slide_out_left 
                    )
                    .replace(android.R.id.content, new VersionsFragment())
                    .addToBackStack(null)
                    .commit();

                Log.d(TAG, "Opening themes fragment");
            } catch (Exception e) {
                Log.e(TAG, "Error opening themes", e);
                Toast.makeText(getContext(), "Unable to open themes", Toast.LENGTH_SHORT).show();
            }
        });

        // Set initial log text
        listener.setText("Ready to launch Minecraft");

        // Show current selection status
        updateSelectionStatus();

        // Set up share button
        shareLogsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareLogs();
            }
        });

        return view;
    }

    /**
     * Apply initial theme to all views
     */
    private void applyInitialTheme(View view) {
        try {
            ThemeManager themeManager = ThemeManager.getInstance();
            if (themeManager != null && themeManager.isThemeLoaded()) {
                // Apply theme to main button
                if (mbl2_button instanceof MaterialButton) {
                    ThemeUtils.applyThemeToButton((MaterialButton) mbl2_button, requireContext());
                }

                // Apply theme to share button (remove background, make it text button)
                if (shareLogsButton != null) {
                    ThemeUtils.applyThemeToButton(shareLogsButton, requireContext());
                    // Remove background and make it transparent
                    shareLogsButton.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
                    shareLogsButton.setStrokeWidth(0);
                }

                // Ensure versions nav button is transparent like share button
                if (versions_button instanceof MaterialButton) {
                    MaterialButton vb = (MaterialButton) versions_button;
                    ThemeUtils.applyThemeToButton(vb, requireContext());
                    vb.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
                    vb.setStrokeWidth(0);
                    try {
                        vb.setIconTint(ColorStateList.valueOf(themeManager.getColor("onSurfaceVariant")));
                    } catch (Exception ignored) {}
                }

                // Apply theme to log text area
                if (listener != null) {
                    listener.setTextColor(themeManager.getColor("onSurfaceVariant"));
                    // Set background color for the log text area
                    View logCard = view.findViewById(R.id.logCard);
                    if (logCard instanceof MaterialCardView) {
                        MaterialCardView card = (MaterialCardView) logCard;
                        card.setCardBackgroundColor(themeManager.getColor("surfaceVariant"));
                        card.setStrokeColor(themeManager.getColor("outline"));
                    }
                }
            }
        } catch (Exception e) {
            // Handle error gracefully
        }
    }
    
  private void setupManagersAndHandlers() {
        versionManager.loadAllVersions();
        minecraftLauncher = new MinecraftLauncher(this);
    }

   private void checkResourcepack() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        new ResourcepackHandler(
                this, minecraftLauncher, executorService
        ).checkIntentForResourcepack();
    }

    private void launchGame() {
        binding.mbl2_button.setEnabled(false);

        GameVersion version = versionManager != null ? versionManager.getSelectedVersion() : null;

        if (version == null) {
            binding.mbl2_button.setEnabled(true);
            new CustomAlertDialog(this)
                    .setTitleText(getString(R.string.dialog_title_no_version))
                    .setMessage(getString(R.string.dialog_message_no_version))
                    .setPositiveButton(getString(R.string.dialog_positive_ok), null)
                    .show();
            return;
        }

        if (!version.isInstalled && !FeatureSettings.getInstance().isVersionIsolationEnabled()) {
            binding.mbl2_button.setEnabled(true);
            new CustomAlertDialog(this)
                    .setTitleText(getString(R.string.dialog_title_version_isolation))
                    .setMessage(getString(R.string.dialog_message_version_isolation))
                    .setPositiveButton(getString(R.string.dialog_positive_enable), v -> {
                        FeatureSettings.getInstance().setVersionIsolationEnabled(true);
                        launchGame();
                    })
                    .setNegativeButton(getString(R.string.dialog_negative_cancel), null)
                    .show();
            return;
        }
        new Thread(() -> {
            try {
                minecraftLauncher.launch(getIntent(), version);
                runOnUiThread(() -> {
                    binding.mbl2_button.setEnabled(true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    binding.mbl2_button.setEnabled(true);
                    new CustomAlertDialog(this)
                            .setTitleText(getString(R.string.dialog_title_launch_failed))
                            .setMessage(getString(R.string.dialog_message_launch_failed, e.getMessage()))
                            .setPositiveButton(getString(R.string.dialog_positive_ok), null)
                            .show();
                });
            }
        }).start();
    }
  
      private boolean animatembl2_button(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            SpringAnimation sx = new SpringAnimation(v, SpringAnimation.SCALE_X, 0.95f);
            SpringAnimation sy = new SpringAnimation(v, SpringAnimation.SCALE_Y, 0.95f);
            SpringForce spring = new SpringForce(0.95f)
                    .setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY)
                    .setStiffness(SpringForce.STIFFNESS_MEDIUM);
            sx.setSpring(spring);
            sy.setSpring(spring);
            sx.start();
            sy.start();
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            SpringAnimation sx = new SpringAnimation(v, SpringAnimation.SCALE_X, 1f);
            SpringAnimation sy = new SpringAnimation(v, SpringAnimation.SCALE_Y, 1f);
            SpringForce spring = new SpringForce(1f)
                    .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
                    .setStiffness(SpringForce.STIFFNESS_LOW);
            sx.setSpring(spring);
            sy.setSpring(spring);
            sx.start();
            sy.start();
        }
        return false;
    }

    @Override
    protected void onApplyTheme() {
        super.onApplyTheme();

        View view = getView();
        if (view != null) {
            // Refresh all theme elements
            applyInitialTheme(view);
        }
    }

    private String getPackageNameFromSettings() {
        SharedPreferences prefs = requireContext().getSharedPreferences("settings", 0);
        return prefs.getString("mc_package_name", "com.mojang.minecraftpe");
    }

    private String getSelectedApkPath() {
        SharedPreferences prefs = requireContext().getSharedPreferences("selected_apk", 0);
        return prefs.getString("apk_path", null);
    }

    private void updateSelectionStatus() {
        String selectedApkPath = getSelectedApkPath();
        if (selectedApkPath != null && new File(selectedApkPath).exists()) {
            String fileName = new File(selectedApkPath).getName();
            listener.setText("Ready to launch Minecraft\nSelected APK: " + fileName);
        } else {
            listener.setText("Ready to launch Minecraft");
        }
    }

    private void clearSelectedApk() {
        SharedPreferences prefs = requireContext().getSharedPreferences("selected_apk", 0);
        prefs.edit().remove("apk_path").apply();
        updateSelectionStatus();
        Toast.makeText(requireContext(), "Cleared APK selection", Toast.LENGTH_SHORT).show();
    }

    private void shareLogs() {
        try {
            // Get the current log text
            String logText = listener.getText().toString();

            // Create a temporary file
            File logFile = new File(requireContext().getCacheDir(), "latestlog.txt");
            FileWriter writer = new FileWriter(logFile);
            writer.write(logText);
            writer.close();

            // Create the sharing intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            // Get the file URI using FileProvider
            android.net.Uri fileUri = FileProvider.getUriForFile(
                requireContext(),
                "com.origin.launcher.fileprovider",
                logFile
            );

            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Xelo Client Logs");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Xelo Client Latest Logs");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Start the sharing activity
            startActivity(Intent.createChooser(shareIntent, "Share Logs"));

        } catch (Exception e) {
            // Show error message
            android.widget.Toast.makeText(requireContext(), "Failed to share logs: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetTextI18n")

    private Object getPathList(@NotNull ClassLoader classLoader) throws Exception {
        Field pathListField = Objects.requireNonNull(classLoader.getClass().getSuperclass()).getDeclaredField("pathList");
        pathListField.setAccessible(true);
        return pathListField.get(classLoader);
    }

    private boolean processNativeLibraries(ApplicationInfo mcInfo, @NotNull Object pathList, @NotNull Handler handler, TextView listener) throws Exception {
        FileInputStream inStream = new FileInputStream(getApkWithLibs(mcInfo));
        BufferedInputStream bufInStream = new BufferedInputStream(inStream);
        ZipInputStream inZipStream = new ZipInputStream(bufInStream);
        if (!checkLibCompatibility(inZipStream)) {
            handler.post(() -> alertAndExit("Wrong minecraft architecture", "The minecraft you have installed does not support the same main architecture (" + Build.SUPPORTED_ABIS[0] + ") your device uses, Xelo client cant work with it"));
            return false;
        }                     
        Method addNativePath = pathList.getClass().getDeclaredMethod("addNativePath", Collection.class);
        ArrayList<String> libDirList = new ArrayList<>();
        File libdir = new File(mcInfo.nativeLibraryDir);
        if (libdir.list() == null || libdir.list().length == 0 
         || (mcInfo.flags & ApplicationInfo.FLAG_EXTRACT_NATIVE_LIBS) != ApplicationInfo.FLAG_EXTRACT_NATIVE_LIBS) {
            loadUnextractedLibs(mcInfo);
            libDirList.add(requireActivity().getCodeCacheDir().getAbsolutePath() + "/");
        } else {
            libDirList.add(mcInfo.nativeLibraryDir);
        }
        addNativePath.invoke(pathList, libDirList);
        handler.post(() -> listener.append("\n-> " + mcInfo.nativeLibraryDir + " added to native library directory path"));
        return true;
    }

    private static Boolean checkLibCompatibility(ZipInputStream zip) throws Exception{
         ZipEntry ze = null;
         String requiredLibDir = "lib/" + Build.SUPPORTED_ABIS[0] + "/";
         while ((ze = zip.getNextEntry()) != null) {
             if (ze.getName().startsWith(requiredLibDir)) {
                 return true;
             }
         }
         zip.close();
         return false;
     }

     private void alertAndExit(String issue, String description) {
        AlertDialog alertDialog = new AlertDialog.Builder(requireActivity()).create();
        alertDialog.setTitle(issue);
        alertDialog.setMessage(description);
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Exit",
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                requireActivity().finish();
            }
        });
        alertDialog.show();         
     }

    private void loadUnextractedLibs(ApplicationInfo appInfo) throws Exception {
        FileInputStream inStream = new FileInputStream(getApkWithLibs(appInfo));
        BufferedInputStream bufInStream = new BufferedInputStream(inStream);
        ZipInputStream inZipStream = new ZipInputStream(bufInStream);
        String zipPath = "lib/" + Build.SUPPORTED_ABIS[0] + "/";
        String outPath = requireActivity().getCodeCacheDir().getAbsolutePath() + "/";
        File dir = new File(outPath);
        dir.mkdir();
        extractDir(appInfo, inZipStream, zipPath, outPath);
    }

    public String getApkWithLibs(ApplicationInfo pkg) throws PackageManager.NameNotFoundException {
        String[] sn=pkg.splitSourceDirs;
        if (sn != null && sn.length > 0) {
            String cur_abi = Build.SUPPORTED_ABIS[0].replace('-','_');
            for(String n:sn){
                if(n.contains(cur_abi)){
                    return n;
                }
            }
        }
        return pkg.sourceDir;
    }

    private static void extractDir(ApplicationInfo mcInfo, ZipInputStream zip, String zip_folder, String out_folder ) throws Exception{
        ZipEntry ze = null;
        while ((ze = zip.getNextEntry()) != null) {
            if (ze.getName().startsWith(zip_folder) && !ze.getName().contains("c++_shared")) {
                String strippedName = ze.getName().substring(zip_folder.length());
                String path = out_folder + "/" + strippedName;
                OutputStream out = new FileOutputStream(path);
                BufferedOutputStream outBuf = new BufferedOutputStream(out);
                byte[] buffer = new byte[9000];
                int len;
                while ((len = zip.read(buffer)) != -1) {
                    outBuf.write(buffer, 0, len);
                }
                outBuf.close();
            }
        }
        zip.close();
    }

    private static void copyFile(InputStream from, @NotNull File to) throws IOException {
        File parentDir = to.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Failed to create directories");
        }
        if (!to.exists() && !to.createNewFile()) {
            throw new IOException("Failed to create new file");
        }
        try (BufferedInputStream input = new BufferedInputStream(from);
             BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(to.toPath()))) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DiscordRPCHelper.getInstance().updateMenuPresence("Playing");
    }

    @Override
    public void onPause() {
        super.onPause();
        DiscordRPCHelper.getInstance().updateIdlePresence();
    }
}