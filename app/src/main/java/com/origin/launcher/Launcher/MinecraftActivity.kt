package com.origin.launcher.Launcher

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.mojang.minecraftpe.MainActivity
import org.conscrypt.Conscrypt
import java.security.Security

/**
 * Self-preloading Minecraft activity (ANR-safe)
 */
class MinecraftActivity : MainActivity() {

    private lateinit var gameManager: GamePackageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Keep work before super.onCreate() minimal
        try {
            Log.d(TAG, "Initializing game manager (light)...")
            gameManager = GamePackageManager.getInstance(applicationContext)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init GamePackageManager", e)
            Toast.makeText(
                this,
                "Failed to init game: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        // Let MainActivity create its window ASAP
        super.onCreate(savedInstanceState)

        // Do all heavy stuff off the UI thread
        Thread {
            try {
                Log.d(TAG, "Setting up security provider...")
                try {
                    Security.insertProviderAt(Conscrypt.newProvider(), 1)
                } catch (e: Exception) {
                    Log.w(TAG, "Conscrypt init failed: ${e.message}")
                }

                Log.d(TAG, "Loading native libraries...")

                try {
                    System.loadLibrary("preloader")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to load preloader: ${e.message}")
                }

                // Load all additional libs (can be slow)
                gameManager.loadAllLibraries()

                // Load launcher core if mods disabled
                val modsEnabled = intent.getBooleanExtra("MODS_ENABLED", true)
                if (!modsEnabled) {
                    Log.d(TAG, "Loading game core...")
                    System.loadLibrary("mtbinloader2")

                    val pkgCtx = gameManager.getPackageContext().applicationInfo
                    val libPath =
                        if (pkgCtx.splitPublicSourceDirs?.isNotEmpty() == true) {
                            // App bundle
                            "${applicationContext.cacheDir.path}/lib/${android.os.Build.CPU_ABI}/libminecraftpe.so"
                        } else {
                            // Standard APK
                            "${gameManager.getPackageContext().applicationInfo.nativeLibraryDir}/libminecraftpe.so"
                        }

                    nativeOnLauncherLoaded(libPath)
                }

                Log.i(TAG, "Game initialized successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize game in background", e)
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Failed to load game: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }.start()
    }

    override fun getAssets(): AssetManager {
        return if (::gameManager.isInitialized) {
            gameManager.getAssets()
        } else {
            super.getAssets()
        }
    }

    private external fun nativeOnLauncherLoaded(libPath: String)

    companion object {
        private const val TAG = "MinecraftActivity"
    }
}