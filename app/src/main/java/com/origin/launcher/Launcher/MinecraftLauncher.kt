package com.origin.launcher.Launcher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.origin.launcher.LoadingDialog
import com.origin.launcher.versions.GameVersion
import com.origin.launcher.Launcher.GamePackageManager
import java.io.File

class MinecraftLauncher(private val context: Context) {

    companion object {
    private const val TAG = "MinecraftLauncher"
const val MC_PACKAGE_NAME = "com.mojang.minecraftpe"
    const val PREF_PACKAGE_NAME = "mc_package_name"

    fun abiToSystemLibDir(abi: String): String {
        return when (abi) {
            "arm64-v8a" -> "arm64"
            "armeabi-v7a" -> "arm"
            else -> abi
        }
    }
}

    private var gameManager: GamePackageManager? = null
    private var loadingDialog: LoadingDialog? = null

    fun createFakeApplicationInfo(version: GameVersion, packageName: String): ApplicationInfo {
        val fakeInfo = ApplicationInfo()
        val apkFile = File(version.versionDir, "base.apk.xelo")

        fakeInfo.sourceDir = apkFile.absolutePath
        fakeInfo.publicSourceDir = fakeInfo.sourceDir

        val systemAbi = abiToSystemLibDir(Build.SUPPORTED_ABIS[0])
        val dstLibDir = File(context.dataDir, "minecraft/${version.directoryName}/lib/$systemAbi")

        fakeInfo.nativeLibraryDir = dstLibDir.absolutePath
        fakeInfo.packageName = packageName
        fakeInfo.dataDir = version.versionDir.absolutePath

        val splitsFolder = File(version.versionDir, "splits")
        if (splitsFolder.exists() && splitsFolder.isDirectory) {
            val splits = splitsFolder.listFiles()
            splits?.let {
                val splitPathList = it.filter { f ->
                    f.isFile && f.name.endsWith(".apk.xelo")
                }.map(File::getAbsolutePath)

                if (splitPathList.isNotEmpty()) {
                    fakeInfo.splitSourceDirs = splitPathList.toTypedArray()
                }
            }
        }
        return fakeInfo
    }

    fun launch(sourceIntent: Intent, version: GameVersion?) {
        val activity = context as Activity

        try {
            if (version == null) {
                Log.e(TAG, "No version selected")
                showLaunchErrorOnUi("No version selected")
                return
            }

            activity.runOnUiThread {
                dismissLoading()
                loadingDialog = LoadingDialog(activity).apply { show() }
            }

            gameManager = GamePackageManager.getInstance(context.applicationContext, version)
            fillIntentWithMcPath(sourceIntent, version)
            launchMinecraftActivity(sourceIntent, version, modsEnabled = false)

        } catch (e: Exception) {
            Log.e(TAG, "Launch failed: ${e.message}", e)
            dismissLoading()
            showLaunchErrorOnUi("Launch failed: ${e.message}")
        }
    }

    private fun fillIntentWithMcPath(sourceIntent: Intent, version: GameVersion) {
        sourceIntent.putExtra("MC_PATH", "")
        sourceIntent.putExtra("IS_INSTALLED", false)
    }

private fun getConfiguredPackageName(): String {
    val prefs = context.getSharedPreferences("settings", 0)
    val packageName = prefs.getString(PREF_PACKAGE_NAME, null)
    
    return packageName ?: MC_PACKAGE_NAME  // Use constant as fallback
}

    private fun launchMinecraftActivity(sourceIntent: Intent, version: GameVersion, modsEnabled: Boolean) {
    val activity = context as Activity

    Log.d(TAG, "Launch attempt: isInstalled=${version.isInstalled}, versionDir=${version.versionDir.absolutePath}")
    Log.d(TAG, "Package from settings: ${getConfiguredPackageName()}")
    if (version.isInstalled) {
        Log.d(TAG, "Using real package context")
    } else {
        Log.d(TAG, "Using fake info - base.apk.xelo exists: ${File(version.versionDir, "base.apk.xelo").exists()}")
        Log.d(TAG, "Lib dir exists: ${File(context.dataDir, "minecraft/${version.directoryName}/lib").exists()}")
    }

    Thread {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                sourceIntent.putExtra("DISABLE_SPLASH_SCREEN", true)
            }

            sourceIntent.setClass(context, MinecraftActivity::class.java)

            val mcInfo = try {
    Log.d(TAG, "Always using fake ApplicationInfo")
    val pkg = getConfiguredPackageName()
    createFakeApplicationInfo(version, pkg)
} catch (e: Exception) {
    Log.e(TAG, "Can't create fake ApplicationInfo - ${e.message}")
    null
}

            if (mcInfo == null) {
                Log.e(TAG, "Can't detect Minecraft - mcInfo is null, aborting launch")
                activity.runOnUiThread {
                    dismissLoading()
                    Toast.makeText(context, "Can't detect Minecraft - check settings", Toast.LENGTH_LONG).show()
                }
                return@Thread
            }

            mcInfo.let {
                sourceIntent.putExtra("MC_SRC", it.sourceDir)
                it.splitSourceDirs?.let { splits ->
                    sourceIntent.putExtra("MC_SPLIT_SRC", ArrayList(splits.asList()))
                }
            }

            sourceIntent.putExtra("MODS_ENABLED", modsEnabled)
            sourceIntent.putExtra("MINECRAFT_VERSION", version.versionCode)
            sourceIntent.putExtra("MINECRAFT_VERSION_DIR", version.directoryName)

            if (shouldLoadMaesdk(version)) {
                gameManager?.loadAllLibraries()
            } else {
                gameManager?.apply {
                    loadLibrary("c++_shared")
                    loadLibrary("fmod")
                    loadLibrary("MediaDecoders_Android")
                    loadLibrary("minecraftpe")
                }
            }

            activity.runOnUiThread {
                dismissLoading()
                activity.startActivity(sourceIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch Minecraft activity: ${e.message}", e)
            activity.runOnUiThread {
                dismissLoading()
                Toast.makeText(context, "Failed to launch: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }.start()
}

    private fun shouldLoadMaesdk(version: GameVersion?): Boolean {
        val versionCode = version?.versionCode ?: return false
        val targetVersion = if (versionCode.contains("beta")) "1.21.110.22" else "1.21.110"
        return isVersionAtLeast(versionCode, targetVersion)
    }

    private fun isVersionAtLeast(currentVersion: String, targetVersion: String): Boolean {
        return try {
            val current = currentVersion.replace("[^0-9.]".toRegex(), "").split(".")
            val target = targetVersion.split(".")
            val maxLength = maxOf(current.size, target.size)

            for (i in 0 until maxLength) {
                val currentPart = current.getOrNull(i)?.toIntOrNull() ?: 0
                val targetPart = target.getOrNull(i)?.toIntOrNull() ?: 0
                when {
                    currentPart > targetPart -> return true
                    currentPart < targetPart -> return false
                }
            }
            true
        } catch (_: NumberFormatException) {
            false
        }
    }

    private fun dismissLoading() {
        try {
            loadingDialog?.takeIf { it.isShowing }?.dismiss()
        } catch (_: Exception) {
        } finally {
            loadingDialog = null
        }
    }

    private fun showLaunchErrorOnUi(message: String) {
        val activity = context as Activity
        activity.runOnUiThread {
            Toast.makeText(activity, "Failed to launch Minecraft: $message", Toast.LENGTH_LONG).show()
        }
    }
}