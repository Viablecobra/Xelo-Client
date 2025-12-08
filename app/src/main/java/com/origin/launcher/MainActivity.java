package com.origin.launcher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction; 
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.graphics.Color;

public class MainActivity extends BaseThemedActivity {
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "app_preferences";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_DISCLAIMER_SHOWN = "disclaimer_shown";
    private static final String KEY_THEMES_DIALOG_SHOWN = "themes_dialog_shown";
    private SettingsFragment settingsFragment;
    private int currentFragmentIndex = 0;
    private LinearProgressIndicator globalProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);


        checkFirstLaunch();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        ThemeUtils.applyThemeToBottomNavigation(bottomNavigationView);
        
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String presenceActivity = "";
            int newIndex = -1;
            
            if (item.getItemId() == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
                presenceActivity = "In Home";
                newIndex = 0;
            } else if (item.getItemId() == R.id.navigation_dashboard) {
                selectedFragment = new DashboardFragment();
                presenceActivity = "In Dashboard";
                newIndex = 1;
            } else if (item.getItemId() == R.id.navigation_settings) {
                if (settingsFragment == null) {
                    settingsFragment = new SettingsFragment();
                }
                selectedFragment = settingsFragment;
                presenceActivity = "In Settings";
                newIndex = 2;
            }

            if (selectedFragment != null) {
                // Determine direction based on tab indices
                boolean isForward = newIndex > getCurrentFragmentIndex();
                
                navigateToFragmentWithAnimation(selectedFragment, isForward);
                
                setCurrentFragmentIndex(newIndex);
                
                DiscordRPCHelper.getInstance().updatePresence(presenceActivity, "Using the best MCPE Client");
                
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
            setCurrentFragmentIndex(0);
        }
    }
    
    private int getCurrentFragmentIndex() {
        return currentFragmentIndex;
    }

    private void setCurrentFragmentIndex(int index) {
        this.currentFragmentIndex = index;
    }

    private void navigateToFragmentWithAnimation(Fragment fragment, boolean isForward) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        
        if (isForward) {
            transaction.setCustomAnimations(
                R.anim.slide_in_right, 
                R.anim.slide_out_left, 
                R.anim.slide_in_left,  
                R.anim.slide_out_right 
            );
        } else {
            transaction.setCustomAnimations(
                R.anim.slide_in_left,  
                R.anim.slide_out_right,  
                R.anim.slide_in_right,  
                R.anim.slide_out_left 
            );
        }
        
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void checkFirstLaunch() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true);
        boolean disclaimerShown = prefs.getBoolean(KEY_DISCLAIMER_SHOWN, false);
        boolean themesDialogShown = prefs.getBoolean(KEY_THEMES_DIALOG_SHOWN, false);
        
        if (isFirstLaunch) {
            showFirstLaunchDialog(prefs, disclaimerShown, themesDialogShown);
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
        } else if (!themesDialogShown) {
            showThemesDialog(prefs, disclaimerShown);
        } else if (!disclaimerShown) {
            showDisclaimerDialog(prefs);
        }
    }

    private void showFirstLaunchDialog(SharedPreferences prefs, boolean disclaimerShown, boolean themesDialogShown) {
        new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle("Welcome to Xelo Client")
                .setMessage("Launch Minecraft once before doing anything, to make the config load properly")
                .setIcon(R.drawable.ic_info)
                .setPositiveButton("Proceed", (dialog, which) -> {
                    dialog.dismiss();
                    if (!disclaimerShown) {
                        showDisclaimerDialog(prefs);
                    }
                })
                .setCancelable(false)
                .show();
    }
    
    private void showThemesDialog(SharedPreferences prefs, boolean disclaimerShown) {
        new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle("THEMES!!🎉")
                .setMessage("xelo client now supports custom themes! download themes from https://themes.xeloclient.in or make your own themes from https://docs.xeloclient.com")
                .setIcon(R.drawable.ic_info)
                .setPositiveButton("Proceed", (dialog, which) -> {
                    dialog.dismiss();
                    prefs.edit().putBoolean(KEY_THEMES_DIALOG_SHOWN, true).apply();
                    if (!disclaimerShown) {
                        showDisclaimerDialog(prefs);
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showDisclaimerDialog(SharedPreferences prefs) {
        new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle("Important Disclaimer")
                .setMessage("This application is not affiliated with, endorsed by, or related to Mojang Studios, Microsoft Corporation, or any of their subsidiaries. " +
                           "Minecraft is a trademark of Mojang Studios. This is an independent third-party launcher. " +
                           "\n\nBy clicking 'I Understand', you acknowledge that you use this launcher at your own risk and that the developers are not responsible for any issues that may arise.")
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton("I Understand", (dialog, which) -> {
                    dialog.dismiss();
                    prefs.edit().putBoolean(KEY_DISCLAIMER_SHOWN, true).apply();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        Log.d(TAG, "MainActivity onActivityResult: requestCode=" + requestCode + 
              ", resultCode=" + resultCode + ", data=" + (data != null ? "present" : "null"));
        
        if (requestCode == DiscordLoginActivity.DISCORD_LOGIN_REQUEST_CODE && settingsFragment != null) {
            Log.d(TAG, "Forwarding Discord login result to SettingsFragment");
            settingsFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        DiscordRPCHelper.getInstance().updatePresence("Using Xelo Client", "Using the best MCPE Client");
    }
    
    @Override
    protected void onApplyTheme() {
        super.onApplyTheme();
        
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            try {
                int currentBackground = Color.parseColor("#141414"); 
                if (bottomNavigationView.getBackground() != null) {
                    try {
                        currentBackground = ((android.graphics.drawable.ColorDrawable) bottomNavigationView.getBackground()).getColor();
                    } catch (Exception e) {
                    }
                }
                
                int targetBackground = ThemeManager.getInstance().getColor("surface");
                
                ThemeUtils.animateBackgroundColorTransition(bottomNavigationView, currentBackground, targetBackground, 300);
                
                ThemeUtils.applyThemeToBottomNavigation(bottomNavigationView);
            } catch (Exception e) {
                ThemeUtils.applyThemeToBottomNavigation(bottomNavigationView);
            }
        }
    }

    public void showGlobalProgress(int max) {
        if (globalProgress == null) {
            globalProgress = findViewById(R.id.global_download_progress);
        }
        if (globalProgress != null) {
            if (max > 0) {
                globalProgress.setIndeterminate(false);
                globalProgress.setMax(max);
                globalProgress.setProgress(0);
            } else {
                globalProgress.setIndeterminate(true);
            }
            globalProgress.setVisibility(View.VISIBLE);
            globalProgress.bringToFront();
        }
    }

    public void updateGlobalProgress(int value) {
        if (globalProgress != null) {
            globalProgress.setIndeterminate(false);
            globalProgress.setProgressCompat(value, true);
        }
    }

    public void hideGlobalProgress() {
        if (globalProgress != null) {
            globalProgress.setVisibility(View.GONE);
            globalProgress.setIndeterminate(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        DiscordRPCHelper.getInstance().updatePresence("Xelo Client", "Using the best MCPE Client");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DiscordRPCHelper.getInstance().cleanup();
    }
}