package com.origin.launcher.Launcher

import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.mojang.minecraftpe.MainActivity
import java.io.File

class MinecraftActivity : MainActivity() {

    companion object {
        var globalGameManager: GamePackageManager? = null
        private const val TAG = "MinecraftActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // NO BLOCKING! Launcher preloaded everything
        
        super.onCreate(savedInstanceState)
        MinecraftActivityState.onCreated(this)
        
        Log.d(TAG, "MinecraftActivity started - libs preloaded by launcher")
    }

    override fun getAssets(): AssetManager {
        return globalGameManager?.getAssets() ?: super.getAssets()
    }

    override fun getFilesDir(): File {
        val mcPath = intent.getStringExtra("MC_PATH")
        return if (!mcPath.isNullOrEmpty()) {
            val filesDir = File(mcPath, "games/com.mojang")
            if (!filesDir.exists()) filesDir.mkdirs()
            filesDir
        } else {
            super.getFilesDir()
        }
    }

    override fun getDataDir(): File {
        val mcPath = intent.getStringExtra("MC_PATH")
        return if (!mcPath.isNullOrEmpty()) {
            val dataDir = File(mcPath)
            if (!dataDir.exists()) dataDir.mkdirs()
            dataDir
        } else {
            super.getDataDir()
        }
    }

    override fun getExternalFilesDir(type: String?): File? {
        val mcPath = intent.getStringExtra("MC_PATH")
        return if (!mcPath.isNullOrEmpty()) {
            val externalDir = if (type != null) {
                File(mcPath, "games/com.mojang/$type")
            } else {
                File(mcPath, "games/com.mojang")
            }
            if (!externalDir.exists()) externalDir.mkdirs()
            externalDir
        } else {
            super.getExternalFilesDir(type)
        }
    }

    override fun getDatabasePath(name: String): File {
        val mcPath = intent.getStringExtra("MC_PATH")
        return if (!mcPath.isNullOrEmpty()) {
            val dbDir = File(mcPath, "databases")
            if (!dbDir.exists()) dbDir.mkdirs()
            File(dbDir, name)
        } else {
            super.getDatabasePath(name)
        }
    }

    override fun getCacheDir(): File {
        val mcPath = intent.getStringExtra("MC_PATH")
        return if (!mcPath.isNullOrEmpty()) {
            val cacheDir = File(mcPath, "cache")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            cacheDir
        } else {
            super.getCacheDir()
        }
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
}