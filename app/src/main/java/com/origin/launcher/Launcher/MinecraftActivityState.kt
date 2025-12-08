package com.origin.launcher.Launcher

import android.app.Activity
import java.lang.ref.WeakReference

object MinecraftActivityState {
    @Volatile
    private var running: Boolean = false
    
    @Volatile
    private var resumed: Boolean = false
    
    private var currentActivityRef: WeakReference<Activity>? = null

    @JvmStatic
    fun onCreated(activity: Activity) {
        running = true
        currentActivityRef = WeakReference(activity)
    }

    @JvmStatic
    fun onResumed() {
        resumed = true
    }

    @JvmStatic
    fun onPaused() {
        resumed = false
    }

    @JvmStatic
    fun onDestroyed() {
        running = false
        resumed = false
        currentActivityRef = null
    }

    @JvmStatic
    fun isRunning(): Boolean = running

    @JvmStatic
    fun isResumed(): Boolean = resumed

    @JvmStatic
    fun getCurrentActivity(): Activity? = currentActivityRef?.get()
}