package com.dhimandasgupta.notemark.common.extensions

import android.app.Activity
import androidx.core.view.WindowCompat

fun Activity.setDarkStatusBarIcons(isLight: Boolean) = WindowCompat.getInsetsController(window, window.decorView).apply {
    isAppearanceLightStatusBars = isLight
}