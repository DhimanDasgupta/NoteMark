package com.dhimandasgupta.notemark.common.android

import android.app.Application
import android.content.Context

fun Context.getAppVersionName(): String {
  require(value = applicationContext is Application) { "Context must be an Application" }
  return runCatching {
      val packageInfo = packageManager.getPackageInfo(packageName, 0)
      packageInfo.versionName ?: "Unknown"
    }
    .getOrDefault("Unknown")
}
