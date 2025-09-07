package com.dhimandasgupta.notemark.common.extensions

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.core.view.WindowCompat

fun Activity.setDarkStatusBarIcons(isLight: Boolean) = WindowCompat.getInsetsController(window, window.decorView).apply {
    isAppearanceLightStatusBars = if (isSystemInDarkTheme()) {
        false
    } else {
        isLight
    }
}

private fun Context.isSystemInDarkTheme(): Boolean {
    return resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}