package com.origin.launcher.Launcher

import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.mojang.minecraftpe.MainActivity
import java.io.File
import org.conscrypt.Conscrypt
import java.security.Security

class MinecraftActivity : MainActivity() {

    private lateinit var gameManager: GamePackageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            gameManager = GamePackageManager.getInstance(applicationContext)
            
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
            
            val modsEnabled = intent.getBooleanExtra("MODS_ENABLED", true)
            if (!modsEnabled) {
                Log.d(TAG, "Loading game core...")
                System.loadLibrary("mtbinloader2")
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

        val intent = Intent(applicationContext, com.origin.launcher.MainActivity::class.java)
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