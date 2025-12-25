package com.dhimandasgupta.notemark.common.extensions

import android.content.Context

fun Context.getAppVersionName(): String = runCatching {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    packageInfo.versionName ?: "Unknown"
}.getOrDefault("Unknown")