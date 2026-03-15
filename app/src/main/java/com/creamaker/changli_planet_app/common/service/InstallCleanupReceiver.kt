package com.creamaker.changli_planet_app.common.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.io.File

class InstallCleanupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        runCatching {
            val apkFile = File(context.externalCacheDir, DownloadService.APK_FILE_NAME)
            if (apkFile.exists()) {
                apkFile.delete()
            }
        }
    }
}
