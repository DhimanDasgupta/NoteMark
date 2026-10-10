package com.dhimandasgupta.notemark.app.extension

import android.content.Context
import com.dhimandasgupta.notemark.common.android.SyncScheduler
import dev.zacsweers.metro.Inject
import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow

@Inject
class WorkManagerSyncScheduler(private val context: Context) : SyncScheduler {
  override fun triggerOneTimeSync() = context.triggerOneTimeSync()

  override fun schedulePeriodicSync(interval: Duration) =
    context.schedulePeriodicSync(interval = interval)

  override fun cancelPeriodicSync() = context.cancelPeriodicSync()

  override fun observeSyncRunning(): Flow<Boolean> = context.observeSyncRunning()

  override suspend fun cancelSyncWork() = context.cancelSyncWork()
}
