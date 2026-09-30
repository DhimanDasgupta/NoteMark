package com.dhimandasgupta.notemark.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dhimandasgupta.notemark.app.di.AppBackgroundDispatcher
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/** Attempts per run before a retryable failure gives up until the next trigger. */
private const val MAX_RUN_ATTEMPTS = 3

@AssistedInject
class NoteSyncWorker(
  @Assisted context: Context,
  @Assisted workerParameters: WorkerParameters,
  @AppBackgroundDispatcher private val applicationDispatcher: CoroutineDispatcher,
  private val noteSyncer: NoteSyncer,
) : CoroutineWorker(appContext = context, params = workerParameters) {

  override suspend fun doWork(): Result =
    withContext(applicationDispatcher) {
      when (noteSyncer.sync()) {
        SyncOutcome.Success -> Result.success()
        SyncOutcome.Retry ->
          if (runAttemptCount + 1 < MAX_RUN_ATTEMPTS) Result.retry() else Result.failure()
        SyncOutcome.Failure -> Result.failure()
      }
    }

  @AssistedFactory
  interface Factory {
    fun create(context: Context, workerParameters: WorkerParameters): NoteSyncWorker
  }
}
