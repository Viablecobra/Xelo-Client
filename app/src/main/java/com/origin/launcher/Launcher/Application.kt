package com.origin.launcher.Launcher

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors

class LauncherApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
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