package com.origin.launcher.Launcher

import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.mojang.minecraftpe.MainActivity
import com.origin.launcher.versions.GameVersion
import com.origin.launcher.SettingsFragment
import java.io.File

class MinecraftActivity : MainActivity() {

    private lateinit var gameManager: GamePackageManager
    
    val prefs = getSharedPreferences("settings", MODE_PRIVATE)
val mcPackageName = prefs.getString(
    "mc_package_name",
    "com.mojang.minecraftpe"
)!!

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            val versionDir = intent.getStringExtra("MC_PATH")
            val versionCode = intent.getStringExtra("MINECRAFT_VERSION") ?: ""
            val versionDirName = intent.getStringExtra("MINECRAFT_VERSION_DIR") ?: ""
            val isInstalled = intent.getBooleanExtra("IS_INSTALLED", false)

val prefs = getSharedPreferences("settings", MODE_PRIVATE)
val mcPackageName = prefs.getString(
    "mc_package_name",
    "com.mojang.minecraftpe"
)!!

try {
    packageManager.getPackageInfo(mcPackageName, 0)
} catch (e: Exception) {
    Toast.makeText(
        this,
        "Minecraft package not installed: $mcPackageName",
        Toast.LENGTH_LONG
    ).show()
    finish()
    return
}

            val version = if (!versionDir.isNullOrEmpty()) {
                GameVersion(
                    versionDirName,
                    versionCode,
                    versionCode,
                    File(versionDir),
                    isInstalled,
                    mcPackageName,
                    ""
                )
            } else if (!versionCode.isNullOrEmpty()) {
                GameVersion(
                    versionDirName,
                    versionCode,
                    versionCode,
                    File(versionDir ?: ""),
                    true,
                    mcPackageName,
                    ""
                )
            } else {
                null
            }

            gameManager = GamePackageManager.getInstance(applicationContext, version)

            try {
                System.loadLibrary("preloader")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load preloader: ${e.message}")
            }

            if (!gameManager.loadLibrary("minecraftpe")) {
                throw RuntimeException("Failed to load libminecraftpe.so")
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load game: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        super.onCreate(savedInstanceState)
        MinecraftActivityState.onCreated(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        MinecraftActivityState.onResumed()
    }

    override fun onPause() {
        MinecraftActivityState.onPaused()
        super.onPause()
    }

    override fun onDestroy() {
        MinecraftActivityState.onDestroyed()
        super.onDestroy()

        val intent = Intent(applicationContext, com.origin.launcher.HomeFragment::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)

        finishAndRemoveTask()
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    override fun getAssets(): AssetManager {
        return if (::gameManager.isInitialized) {
            gameManager.getAssets()
        } else {
            super.getAssets()
        }
    }

    override fun getFilesDir(): File {
        return super.getFilesDir()
    }

    override fun getDataDir(): File {
        return super.getDataDir()
    }

    override fun getExternalFilesDir(type: String?): File? {
        return super.getExternalFilesDir(type)
    }

    override fun getDatabasePath(name: String): File {
         return super.getDatabasePath(name)
    }

    override fun getCacheDir(): File {
        return super.getCacheDir()
    }

    companion object {
        private const val TAG = "MinecraftActivity"
    }
}