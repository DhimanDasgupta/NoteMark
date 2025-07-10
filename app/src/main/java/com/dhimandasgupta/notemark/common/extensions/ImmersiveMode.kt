package com.dhimandasgupta.notemark.common.extensions

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

fun Activity.turnOnImmersiveMode() = WindowCompat.getInsetsController(window, window.decorView).apply {
    hide(WindowInsetsCompat.Type.systemBars())
}

fun Activity.turnOffImmersiveMode() = WindowCompat.getInsetsController(window, window.decorView).apply {
    show(WindowInsetsCompat.Type.systemBars())
}

fun Activity.lockToLandscape() = setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

fun Activity.unlockOrientation() = setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR)