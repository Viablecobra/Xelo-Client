package com.origin.launcher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.net.Uri;
import com.bumptech.glide.Glide;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction; 
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.graphics.Typeface;

public class MainActivity extends BaseThemedActivity {
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "app_preferences";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_DISCLAIMER_SHOWN = "disclaimer_shown";
    private static final String KEY_THEMES_DIALOG_SHOWN = "themes_dialog_shown";
private static final String KEY_CREDITS_SHOWN = "credits_shown";
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
    boolean creditsShown = prefs.getBoolean(KEY_CREDITS_SHOWN, false);

    if (isFirstLaunch) {
        showFirstLaunchDialog(prefs, disclaimerShown, themesDialogShown, creditsShown);
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
    } else if (!disclaimerShown) {
        showDisclaimerDialog(prefs);
    } else if (!creditsShown) {
        showThanksDialog(prefs);
    } else if (!themesDialogShown) {
        showThemesDialog(prefs, disclaimerShown);
    }
}

private void showFirstLaunchDialog(SharedPreferences prefs,
                                   boolean disclaimerShown,
                                   boolean themesDialogShown,
                                   boolean creditsShown) {
    new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("Welcome to Xelo Client")
            .setMessage("Launch Minecraft once before doing anything, to make the config load properly")
            .setIcon(R.drawable.ic_info)
            .setPositiveButton("Proceed", (dialog, which) -> {
                dialog.dismiss();
                if (!disclaimerShown) {
                    showDisclaimerDialog(prefs);
                } else if (!creditsShown) {
                    showThanksDialog(prefs);
                } else if (!themesDialogShown) {
                    showThemesDialog(prefs, disclaimerShown);
                }
            })
            .setCancelable(false)
            .show();
}

private void showDisclaimerDialog(SharedPreferences prefs) {
    new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("Important Disclaimer")
            .setMessage("This application is not affiliated with, endorsed by, or related to Mojang Studios, Microsoft Corporation, or any of their subsidiaries." 
+
                       "Minecraft is a trademark of Mojang Studios. This is an independent third-party launcher." 
+
                       "By clicking 'I Understand', you acknowledge that you use this launcher at your own risk and that the developers are not responsible for any issues that may arise.")
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton("I Understand", (dialog, which) -> {
                dialog.dismiss();
                prefs.edit().putBoolean(KEY_DISCLAIMER_SHOWN, true).apply();
               
                boolean creditsShown = prefs.getBoolean(KEY_CREDITS_SHOWN, false);
                boolean themesDialogShown = prefs.getBoolean(KEY_THEMES_DIALOG_SHOWN, false);
                if (!creditsShown) {
                    showThanksDialog(prefs);
                } else if (!themesDialogShown) {
                    showThemesDialog(prefs, true);
                }
            })
            .setCancelable(false)
            .show();
}

private void showThanksDialog(SharedPreferences prefs) {
    LayoutInflater inflater = LayoutInflater.from(this);
    View customView = inflater.inflate(R.layout.dialog_credits, null);
    
    LinearLayout creditsContainer = customView.findViewById(R.id.credits_container);
    
    
    addCreditCard(creditsContainer, "VCX", "Viablecobra", "https://avatars.githubusercontent.com/u/88580298?v=4", "I am viable👍🏻");
    
   
    addCreditCard(creditsContainer, "Light", "RadiantByte", "https://avatars.githubusercontent.com/u/198057285?v=4", "💭");
    
    
    addCreditCard(creditsContainer, "Kitsuri", "Kitsuri-Studios", "https://avatars.githubusercontent.com/u/220755073?v=4", "One Place For All Case: Native Development, Reverse Engineering, Game Development");
    
    
    addCreditCard(creditsContainer, "GX", "dreamguxiang", "https://avatars.githubusercontent.com/u/62042544?v=4", "No Tag line Needed");

    new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
        .setView(customView)
        .setPositiveButton("Continue", (dialog, which) -> {
            dialog.dismiss();
            prefs.edit().putBoolean(KEY_CREDITS_SHOWN, true).apply();
            showThemesDialog(prefs, true);
        })
        .setCancelable(false)
        .show();
}

private void addCreditCard(LinearLayout container, String handle, String username, String picUrl, String tagline) {
    LayoutInflater inflater = LayoutInflater.from(this);
    View card = inflater.inflate(R.layout.credit_card_item, container, false);
    
    ImageView profilePic = card.findViewById(R.id.profile_pic);
    TextView profileName = card.findViewById(R.id.profile_name);
    TextView profileTagline = card.findViewById(R.id.profile_tagline);
    
    
    Glide.with(this).load(picUrl).circleCrop().into(profilePic);
    
    profileName.setText(username);
    profileTagline.setText(tagline);
    
    
    card.setOnClickListener(v -> {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/" + username));
        startActivity(browserIntent);
    });
    
    container.addView(card);
}

private void showThemesDialog(SharedPreferences prefs, boolean disclaimerShown) {
    new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("THEMES!!🎉")
            .setMessage("xelo client now supports custom themes! download themes from https://themes.xeloclient.in or make your own themes from https://docs.xeloclient.com")
            .setIcon(R.drawable.ic_info)
            .setPositiveButton("Proceed", (dialog, which) -> {
                dialog.dismiss();
                prefs.edit().putBoolean(KEY_THEMES_DIALOG_SHOWN, true).apply();
                
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