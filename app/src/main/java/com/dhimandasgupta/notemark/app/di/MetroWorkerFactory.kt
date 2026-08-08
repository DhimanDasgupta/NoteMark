package com.dhimandasgupta.notemark.app.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.dhimandasgupta.notemark.app.work.NoteSyncWorker
import dev.zacsweers.metro.Inject

@Inject
class MetroWorkerFactory(private val noteSyncWorkerFactory: NoteSyncWorker.Factory) :
  WorkerFactory() {
  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters,
  ): ListenableWorker? {
    return when (workerClassName) {
      NoteSyncWorker::class.java.name -> noteSyncWorkerFactory.create(appContext, workerParameters)
      else -> null
    }
  }
}
