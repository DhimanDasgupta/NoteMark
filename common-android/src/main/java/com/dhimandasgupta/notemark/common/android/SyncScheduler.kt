package com.dhimandasgupta.notemark.common.android

import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow

/** Schedules note syncs. The app module provides the implementation backed by its sync worker. */
interface SyncScheduler {
  /** Queues one sync. Does nothing if a one-time sync is already queued or running. */
  fun triggerOneTimeSync()

  /** Syncs every [interval], replacing the interval of an already scheduled periodic sync. */
  fun schedulePeriodicSync(interval: Duration)

  fun cancelPeriodicSync()

  /** Whether a one-time or periodic sync is running right now. */
  fun observeSyncRunning(): Flow<Boolean>

  suspend fun cancelSyncWork()
}
