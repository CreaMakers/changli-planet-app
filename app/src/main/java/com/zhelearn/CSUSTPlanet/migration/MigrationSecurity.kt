package com.zhelearn.CSUSTPlanet.migration

import android.content.Context
import android.content.pm.PackageManager
import android.os.Binder

object MigrationSecurity {
    fun isInstalledWithMatchingSignature(context: Context, packageName: String): Boolean {
        return runCatching {
            context.packageManager.getPackageInfo(packageName, 0)
            context.packageManager.checkSignatures(context.packageName, packageName) ==
                PackageManager.SIGNATURE_MATCH
        }.getOrDefault(false)
    }

    fun enforceTargetCaller(context: Context) {
        val callerPackages = context.packageManager.getPackagesForUid(Binder.getCallingUid()).orEmpty()
        if (MigrationContract.TARGET_PACKAGE !in callerPackages) {
            throw SecurityException("Untrusted migration caller")
        }
        if (context.packageManager.checkSignatures(
                context.packageName,
                MigrationContract.TARGET_PACKAGE
            ) != PackageManager.SIGNATURE_MATCH
        ) {
            throw SecurityException("Migration signature mismatch")
        }
    }
}
