package com.origin.launcher.Launcher

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.mojang.minecraftpe.MainActivity
import org.conscrypt.Conscrypt
import java.security.Security

/**
 * Self-preloading Minecraft activity
 */
class MinecraftActivity : MainActivity() {

    private lateinit var gameManager: GamePackageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Load everything BEFORE super.onCreate()
        try {
            Log.d(TAG, "Initializing game manager...")
            gameManager = GamePackageManager.getInstance(applicationContext)

            Log.d(TAG, "Setting up security provider...")
            try {
                Security.insertProviderAt(Conscrypt.newProvider(), 1)
            } catch (e: Exception) {
                Log.w(TAG, "Conscrypt init failed: ${e.message}")
            }

            Log.d(TAG, "Loading native libraries...")
            gameManager.loadAllLibraries()

            // Load launcher core
            val modsEnabled = intent.getBooleanExtra("MODS_ENABLED", false)
            if (!modsEnabled) {
                Log.d(TAG, "Loading game core...")
                System.loadLibrary("xelo_init")

                val libPath = if (gameManager.getPackageContext().applicationInfo.splitPublicSourceDirs?.isNotEmpty() == true) {
                    // App bundle
                    "${applicationContext.cacheDir.path}/lib/${android.os.Build.CPU_ABI}/libminecraftpe.so"
                } else {
                    // Standard APK
                    "${gameManager.getPackageContext().applicationInfo.nativeLibraryDir}/libminecraftpe.so"
                }
                nativeOnLauncherLoaded(libPath)
            }

            Log.i(TAG, "Game initialized successfully, calling super.onCreate()")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize game", e)
            Toast.makeText(
                this,
                "Failed to load game: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        // Now call super.onCreate() after everything is loaded
        super.onCreate(savedInstanceState)
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