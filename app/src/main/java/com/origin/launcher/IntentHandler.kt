package com.origin.launcher

import com.origin.launcher.Launcher.MinecraftActivity
import com.origin.launcher.Launcher.MinecraftActivityState
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log

class IntentHandler : BaseThemedActivity() {
    companion object {
        private const val TAG = "IntentHandler"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {  
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    @SuppressLint("IntentReset")
    private fun handleDeepLink(originalIntent: Intent) {
        val newIntent = Intent(originalIntent)
        if (isMinecraftActivityRunning()) {
            newIntent.setClass(this, MinecraftActivity::class.java)
            newIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        } else {
            if (isMinecraftResourceFile(originalIntent)) {
                newIntent.setClassName(this, "com.origin.launcher.MainActivity")
            } else {
                if (isMcRunning()) {
                    newIntent.setClassName(this, "com.mojang.minecraftpe.Launcher")
                } else {
                    newIntent.setClassName(this, "com.origin.launcher.MainActivity")
                }
            }
        }
        startActivity(newIntent)
        finish()
    }

    private fun isMinecraftResourceFile(intent: Intent): Boolean {
        val data = intent.data ?: return false
        val path = data.path ?: return false
        val lowerPath = path.lowercase()
        return lowerPath.endsWith(".mcworld") ||
                lowerPath.endsWith(".mcpack") ||
                lowerPath.endsWith(".mcaddon") ||
                lowerPath.endsWith(".mctemplate")
    }

    private fun isMinecraftActivityRunning(): Boolean {
        if (MinecraftActivityState.isRunning()) return true
        
        return try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
            val tasks = activityManager.getAppTasks()
            tasks.any { task ->
                val taskInfo = task.taskInfo
               
                (taskInfo.baseActivity?.className == MinecraftActivity::class.java.name) ||
                (taskInfo.topActivity?.className == MinecraftActivity::class.java.name)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if MinecraftActivity is running", e)
            false
        }
    }

    private fun isMcRunning(): Boolean {
        return try {
            Class.forName("com.mojang.minecraftpe.Launcher", false, classLoader)
            Log.d(TAG, "Minecraft PE Launcher class exists!")
            true
        } catch (e: ClassNotFoundException) {
            Log.d(TAG, "Minecraft PE Launcher class not found.")
            false
        }
    }
}