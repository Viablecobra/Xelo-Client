package com.origin.launcher

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.origin.launcher.ThemeManager
import com.origin.launcher.ThemeUtils
import com.origin.launcher.LoadingDialog
import java.io.*
import java.nio.file.Files
import java.util.*
import java.util.concurrent.Executors
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import kotlin.collections.ArrayList
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.origin.launcher.Launcher.MinecraftLauncher

class HomeFragment : BaseThemedFragment() {

    companion object {
        private const val TAG = "HomeFragment"
    }

    private lateinit var listener: TextView
    private lateinit var mbl2_button: Button
    private lateinit var versions_button: Button
    private lateinit var shareLogsButton: MaterialButton
private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        listener = view.findViewById(R.id.listener)
        mbl2_button = view.findViewById(R.id.mbl2_load)
        versions_button = view.findViewById(R.id.versions_button)
        shareLogsButton = view.findViewById(R.id.share_logs_button)
        val handler = Handler(Looper.getMainLooper())

        applyInitialTheme(view)

        mbl2_button.setOnClickListener {
    mbl2_button.isEnabled = false
    listener.text = "Starting Minecraft launcher..."
    
    val handler = Handler(Looper.getMainLooper())
    val packageName = getPackageNameFromSettings()
    
    Executors.newSingleThreadExecutor().execute {
        try {
            val mcInfo = getMinecraftInfo(packageName)
            val pathList = getPathList(requireActivity().classLoader)
            
            if (processNativeLibraries(mcInfo, pathList, handler, listener)) {
                handler.postDelayed({
                    try {
                        val intent = Intent(requireContext(), MinecraftLauncher::class.java)
                        intent.putExtra("MODS_ENABLED", true)
                        startActivity(intent)
listener.append("""
-> MinecraftActivity launching!
""".trimIndent())
                    } catch (e: Exception) {
                        listener.text = "Activity launch failed: ${e.message}"
                        mbl2_button.isEnabled = true
                    }
                }, 1000)
            } else {
                handler.post { mbl2_button.isEnabled = true }
            }
        } catch (e: Exception) {
            handler.post {
                listener.text = "Launch failed: ${e.message}"
                mbl2_button.isEnabled = true
            }
        }
    }
}


        mbl2_button.setOnLongClickListener {
            clearSelectedApk()
            true
        }

        versions_button.setOnClickListener {
            try {
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_fade_in_right,
                        R.anim.slide_out_right,
                        R.anim.slide_in_left,
                        R.anim.slide_out_left
                    )
                    .replace(android.R.id.content, VersionsFragment())
                    .addToBackStack(null)
                    .commit()

                Log.d(TAG, "Opening themes fragment")
            } catch (e: Exception) {
                Log.e(TAG, "Error opening themes", e)
                Toast.makeText(context, "Unable to open themes", Toast.LENGTH_SHORT).show()
            }
        }

        listener.text = "Ready to launch Minecraft"
        updateSelectionStatus()

        shareLogsButton.setOnClickListener { shareLogs() }

        return view
    }

private fun getMinecraftInfo(packageName: String): ApplicationInfo {
    val selected = getSelectedApkPath()
    return if (selected != null && File(selected).exists()) {
        val mcInfo = requireActivity().packageManager.getApplicationInfo(packageName, 0)
        mcInfo.sourceDir = selected
        mcInfo
    } else {
        requireActivity().packageManager.getApplicationInfo(packageName, 0)
    }
}

    private fun applyInitialTheme(view: View) {
        try {
            val themeManager = ThemeManager.getInstance()
            if (themeManager != null && themeManager.isThemeLoaded) {

                if (mbl2_button is MaterialButton) {
                    ThemeUtils.applyThemeToButton(mbl2_button as MaterialButton, requireContext())
                }

                ThemeUtils.applyThemeToButton(shareLogsButton, requireContext())
                shareLogsButton.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                shareLogsButton.strokeWidth = 0

                if (versions_button is MaterialButton) {
                    val vb = versions_button as MaterialButton
                    ThemeUtils.applyThemeToButton(vb, requireContext())
                    vb.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                    vb.strokeWidth = 0
                    try {
                        vb.iconTint = ColorStateList.valueOf(themeManager.getColor("onSurfaceVariant"))
                    } catch (_: Exception) {}
                }

                listener.setTextColor(themeManager.getColor("onSurfaceVariant"))

                val logCard = view.findViewById<View>(R.id.logCard)
                if (logCard is MaterialCardView) {
                    logCard.setCardBackgroundColor(themeManager.getColor("surfaceVariant"))
                    logCard.strokeColor = themeManager.getColor("outline")
                }
            }
        } catch (_: Exception) {
        }
    }

    override fun onApplyTheme() {
        super.onApplyTheme()
        view?.let { applyInitialTheme(it) }
    }

    private fun getPackageNameFromSettings(): String {
        val prefs = requireContext().getSharedPreferences("settings", 0)
        return prefs.getString("mc_package_name", "com.mojang.minecraftpe")!!
    }

    private fun getSelectedApkPath(): String? {
        val prefs = requireContext().getSharedPreferences("selected_apk", 0)
        return prefs.getString("apk_path", null)
    }

    private fun updateSelectionStatus() {
        val path = getSelectedApkPath()
        if (path != null && File(path).exists()) {
            val name = File(path).name
            listener.text = "Ready to launch Minecraft\nSelected APK: $name"
        } else {
            listener.text = "Ready to launch Minecraft"
        }
    }

    private fun clearSelectedApk() {
        val prefs = requireContext().getSharedPreferences("selected_apk", 0)
        prefs.edit().remove("apk_path").apply()
        updateSelectionStatus()
        Toast.makeText(requireContext(), "Cleared APK selection", Toast.LENGTH_SHORT).show()
    }

    private fun shareLogs() {
        try {
            val logText = listener.text.toString()
            val logFile = File(requireContext().cacheDir, "latestlog.txt")

            FileWriter(logFile).apply {
                write(logText)
                close()
            }

            val fileUri = FileProvider.getUriForFile(
                requireContext(),
                "com.origin.launcher.fileprovider",
                logFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "Xelo Client Logs")
                putExtra(Intent.EXTRA_TEXT, "Xelo Client Latest Logs")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Share Logs"))

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to share logs: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    

    private fun getPathList(classLoader: ClassLoader): Any {
        val field = classLoader.javaClass.superclass!!.getDeclaredField("pathList")
        field.isAccessible = true
        return field.get(classLoader)
    }

    private fun processNativeLibraries(
        mcInfo: ApplicationInfo,
        pathList: Any,
        handler: Handler,
        listener: TextView
    ): Boolean {
        val zip = ZipInputStream(FileInputStream(getApkWithLibs(mcInfo)))

        if (!checkLibCompatibility(zip)) {
            handler.post {
                alertAndExit(
                    "Wrong architecture",
                    "Minecraft ABI doesn't match device (${Build.SUPPORTED_ABIS[0]})"
                )
            }
            return false
        }

        val addNativePath = pathList.javaClass.getDeclaredMethod("addNativePath", Collection::class.java)

        val libDirs = ArrayList<String>()

        val libdir = File(mcInfo.nativeLibraryDir)

        if (libdir.list() == null ||
            libdir.list().isEmpty() ||
            (mcInfo.flags and ApplicationInfo.FLAG_EXTRACT_NATIVE_LIBS) != ApplicationInfo.FLAG_EXTRACT_NATIVE_LIBS
        ) {
            loadUnextractedLibs(mcInfo)
            libDirs.add(requireActivity().codeCacheDir.absolutePath + "/")
        } else {
            libDirs.add(mcInfo.nativeLibraryDir)
        }

        addNativePath.invoke(pathList, libDirs)

        handler.post { listener.append("\n-> ${mcInfo.nativeLibraryDir} added to native dirs") }
        return true
    }

    private fun checkLibCompatibility(zip: ZipInputStream): Boolean {
        val req = "lib/${Build.SUPPORTED_ABIS[0]}/"
        var entry: ZipEntry?

        while (true) {
            entry = zip.nextEntry ?: break
            if (entry.name.startsWith(req)) return true
        }

        zip.close()
        return false
    }

    private fun alertAndExit(title: String, msg: String) {
        val dialog = AlertDialog.Builder(requireActivity()).create()
        dialog.setTitle(title)
        dialog.setMessage(msg)
        dialog.setCancelable(false)
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Exit") { _: DialogInterface?, _: Int ->
            requireActivity().finish()
        }
        dialog.show()
    }

    private fun loadUnextractedLibs(appInfo: ApplicationInfo) {
        val zip = ZipInputStream(FileInputStream(getApkWithLibs(appInfo)))
        val zipPath = "lib/${Build.SUPPORTED_ABIS[0]}/"
        val out = requireActivity().codeCacheDir.absolutePath + "/"

        File(out).mkdir()
        extractDir(appInfo, zip, zipPath, out)
    }

    fun getApkWithLibs(pkg: ApplicationInfo): String {
        pkg.splitSourceDirs?.let {
            val abi = Build.SUPPORTED_ABIS[0].replace('-', '_')
            for (path in it) {
                if (path.contains(abi)) return path
            }
        }
        return pkg.sourceDir
    }

    private fun extractDir(
        mcInfo: ApplicationInfo,
        zip: ZipInputStream,
        zip_folder: String,
        out_folder: String
    ) {
        var entry: ZipEntry?

        while (true) {
            entry = zip.nextEntry ?: break
            if (entry.name.startsWith(zip_folder) && !entry.name.contains("c++_shared")) {
                val stripped = entry.name.substring(zip_folder.length)
                val out = File(out_folder + stripped)
                val outStream = BufferedOutputStream(FileOutputStream(out))

                val buffer = ByteArray(9000)
                var len: Int

                while (zip.read(buffer).also { len = it } != -1) {
                    outStream.write(buffer, 0, len)
                }

                outStream.close()
            }
        }

        zip.close()
    }

    override fun onResume() {
        super.onResume()
loadingDialog?.dismiss()
    loadingDialog = null
        DiscordRPCHelper.getInstance().updateMenuPresence("Playing")
    }

    override fun onPause() {
        super.onPause()
        DiscordRPCHelper.getInstance().updateIdlePresence()
    }
}