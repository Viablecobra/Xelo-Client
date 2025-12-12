package com.origin.launcher.Launcher

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import android.content.Intent
import android.os.Environment
import com.origin.launcher.BuildConfig
import com.origin.launcher.CrashActivity
import xcrash.ICrashCallback
import xcrash.XCrash
import java.io.File

class LauncherApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        
        val callback: ICrashCallback = ICrashCallback { logPath, emergency ->
    try {
        val i = Intent(applicationContext, CrashActivity::class.java).apply {
            putExtra("LOG_PATH", logPath)
            putExtra("EMERGENCY", emergency)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        applicationContext.startActivity(i)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
    XCrash.init(this, XCrash.InitParameters().apply {
    setAppVersion(BuildConfig.VERSION_NAME)
    setLogDir(
        File(
            Environment.getExternalStorageDirectory(),
            "games/xelo_client/crash_logs"
        ).absolutePath
    )
    setNativeCallback(callback)
    setJavaCallback(callback)
    setAnrCallback(callback)
    setJavaRethrow(false)
    setNativeRethrow(false)
    setAnrRethrow(false)
}) 
      
                try {
            System.loadLibrary("xelo_init")
          } catch (e: Exception) {
            e.printStackTrace()
        }
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    companion object {
        @JvmStatic
        lateinit var context: Context
            private set

        @JvmStatic
        lateinit var preferences: SharedPreferences
            private set
    }
}