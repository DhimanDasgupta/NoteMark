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
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import com.dhimandasgupta.notemark.R
import com.dhimandasgupta.notemark.app.work.NoteSyncWorker
import com.dhimandasgupta.notemark.ui.activity.MainActivity
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

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

private val syncConstraints: Constraints
  get() =
    Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .setRequiresBatteryNotLow(true)
      .setRequiresStorageNotLow(true)
      .build()

/** Queues one sync. Does nothing if a one-time sync is already queued or running. */
fun Context.triggerOneTimeSync() {
  require(value = applicationContext is Application) { "Context must be an Application" }

  WorkManager.getInstance(context = this)
    .enqueueUniqueWork(
      uniqueWorkName = ONE_TIME_SYNC_WORK,
      existingWorkPolicy = ExistingWorkPolicy.KEEP,
      request = OneTimeWorkRequestBuilder<NoteSyncWorker>().setConstraints(syncConstraints).build(),
    )
}

/** Syncs every [interval], replacing the interval of an already scheduled periodic sync. */
fun Context.schedulePeriodicSync(interval: Duration) {
  require(value = applicationContext is Application) { "Context must be an Application" }
  require(value = interval >= 5.minutes) { "WorkManager can't repeat more often than 5 minutes" }

  WorkManager.getInstance(context = this)
    .enqueueUniquePeriodicWork(
      uniqueWorkName = PERIODIC_SYNC_WORK,
      existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
      request =
        PeriodicWorkRequestBuilder<NoteSyncWorker>(
            repeatInterval = interval.toJavaDuration(),
            flexTimeInterval = 5.minutes.toJavaDuration(), // 5 mins earlier or after the schedule
          )
          .setConstraints(syncConstraints)
          .build(),
    )
}

fun Context.cancelPeriodicSync() {
  require(value = applicationContext is Application) { "Context must be an Application" }

  WorkManager.getInstance(context = this).cancelUniqueWork(uniqueWorkName = PERIODIC_SYNC_WORK)
}

/**
 * Whether a one-time or periodic sync is running right now. Read from WorkManager rather than a
 * stored flag, so a process killed mid-sync can't leave it stuck at true.
 */
fun Context.observeSyncRunning(): Flow<Boolean> {
  require(value = applicationContext is Application) { "Context must be an Application" }

  val workManager = WorkManager.getInstance(context = this)
  return combine(
      workManager.getWorkInfosForUniqueWorkFlow(uniqueWorkName = ONE_TIME_SYNC_WORK),
      workManager.getWorkInfosForUniqueWorkFlow(uniqueWorkName = PERIODIC_SYNC_WORK),
    ) { oneTimeWorkInfos, periodicWorkInfos ->
      (oneTimeWorkInfos + periodicWorkInfos).any { workInfo ->
        workInfo.state == WorkInfo.State.RUNNING
      }
    }
    .distinctUntilChanged()
}

suspend fun Context.cancelSyncWork() {
  require(value = applicationContext is Application) { "Context must be an Application" }

  val workManager = WorkManager.getInstance(context = this)
  workManager.cancelUniqueWork(uniqueWorkName = ONE_TIME_SYNC_WORK).await()
  workManager.cancelUniqueWork(uniqueWorkName = PERIODIC_SYNC_WORK).await()
}

fun Context.addCreateNewNoteShortcut() {
  require(value = applicationContext is Application) { "Context must be an Application" }
  if (!ShortcutManagerCompat.isRequestPinShortcutSupported(this)) return

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
  if (!ShortcutManagerCompat.isRequestPinShortcutSupported(this)) return

  runCatching {
    ShortcutManagerCompat.removeDynamicShortcuts(this, listOf(SHORTCUT_ID_NEW_NOTE))
  }
}
