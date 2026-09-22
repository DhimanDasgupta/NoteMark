package com.dhimandasgupta.notemark.common.extensions.android

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.app.work.NoteSyncWorker
import com.dhimandasgupta.notemark.ui.activity.MainActivity
import java.time.Duration

private const val ONE_TIME_SYNC_WORK = "one_time_sync_work"
private const val PERIODIC_SYNC_WORK = "delayed_sync_work"
private const val SHORTCUT_ID_NEW_NOTE = "new_note"

fun Context.getAppVersionName(): String {
  require(value = applicationContext is Application) { "Context must be an Application" }
  return runCatching {
      val packageInfo = packageManager.getPackageInfo(packageName, 0)
      packageInfo.versionName ?: "Unknown"
    }
    .getOrDefault("Unknown")
}

fun Context.cancelPreviousAndTriggerNewWork(duration: Duration = Duration.ZERO) {
  require(value = applicationContext is Application) { "Context must be an Application" }
  require(value = duration >= Duration.ZERO) { "Duration must be non-negative" }

  val workManager = WorkManager.getInstance(context = this)

  val constraints =
    Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .setRequiresBatteryNotLow(true)
      .setRequiresStorageNotLow(true)
      .build()

  when (duration) {
    Duration.ZERO -> {
      workManager.cancelAllWorkByTag(tag = ONE_TIME_SYNC_WORK)
      workManager.enqueueUniqueWork(
        uniqueWorkName = ONE_TIME_SYNC_WORK,
        existingWorkPolicy = ExistingWorkPolicy.REPLACE,
        request = OneTimeWorkRequestBuilder<NoteSyncWorker>().setConstraints(constraints).build(),
      )
    }
    else -> {
      workManager.enqueueUniquePeriodicWork(
        uniqueWorkName = PERIODIC_SYNC_WORK,
        existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.REPLACE,
        request =
          PeriodicWorkRequestBuilder<NoteSyncWorker>(
              repeatInterval = duration,
              flexTimeInterval = Duration.ofMinutes(5), // 5 mins earlier or after the schedule
            )
            .setConstraints(constraints)
            .build(),
      )
    }
  }
}

fun Context.addCreateNewNoteShortcut() {
  require(value = applicationContext is Application) { "Context must be an Application" }
  require(value = ShortcutManagerCompat.isRequestPinShortcutSupported(this)) {
    "Pin shortcut is not supported"
  }

  runCatching {
    val shortcut =
      ShortcutInfoCompat.Builder(this, SHORTCUT_ID_NEW_NOTE)
        .setShortLabel(getString(R.string.shortcut_new_note_short))
        .setLongLabel(getString(R.string.shortcut_new_note_long))
        .setDisabledMessage(getString(R.string.shortcut_new_note_disabled))
        .setIcon(IconCompat.createWithResource(this, R.drawable.ic_plus_icon))
        .setIntent(
          Intent(Intent.ACTION_VIEW, "notemark://notes/new".toUri()).apply {
            setClass(this@addCreateNewNoteShortcut, MainActivity::class.java)
          }
        )
        .setCategories(setOf(Intent.CATEGORY_DEFAULT))
        .build()

    ShortcutManagerCompat.pushDynamicShortcut(this, shortcut)
  }
}

fun Context.removeCreateNewNoteShortcut() {
  require(value = applicationContext is Application) { "Context must be an Application" }
  require(value = ShortcutManagerCompat.isRequestPinShortcutSupported(this)) {
    "Pin shortcut is not supported"
  }

  runCatching {
    ShortcutManagerCompat.removeDynamicShortcuts(this, listOf(SHORTCUT_ID_NEW_NOTE))
  }
}
