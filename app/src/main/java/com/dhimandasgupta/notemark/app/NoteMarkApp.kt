package com.dhimandasgupta.notemark.app

import android.app.Application
import android.os.StrictMode
import android.util.Log.DEBUG
import android.util.Log.ERROR
import androidx.compose.runtime.Composer
import androidx.compose.runtime.tooling.ComposeStackTraceMode
import androidx.work.Configuration
import androidx.work.WorkManager
import com.dhimandasgupta.notemark.BuildConfig
import com.dhimandasgupta.notemark.app.di.NoteMarkGraph
import com.skydoves.compose.stability.runtime.ComposeStabilityAnalyzer
import dev.zacsweers.metro.createGraphFactory
import timber.log.Timber

class NoteMarkApp : Application() {
  private lateinit var graph: NoteMarkGraph

  override fun onCreate() {
    super.onCreate()

    graph = createGraphFactory<NoteMarkGraph.Factory>().create(context = this)

    ComposeStabilityAnalyzer.setEnabled(BuildConfig.DEBUG)
    if (BuildConfig.DEBUG) {
      Timber.plant(tree = Timber.DebugTree())
      enableStrictMode()
      Composer.setDiagnosticStackTraceMode(ComposeStackTraceMode.SourceInformation)
    } else {
      Composer.setDiagnosticStackTraceMode(ComposeStackTraceMode.None)
    }

    val config =
      Configuration.Builder()
        .setMinimumLoggingLevel(if (BuildConfig.DEBUG) DEBUG else ERROR)
        .setWorkerFactory(workerFactory = graph.workerFactory())
        .build()

    WorkManager.initialize(context = this, configuration = config)
  }

  fun getGraph(): NoteMarkGraph {
    require(::graph.isInitialized)
    return graph
  }

  private fun enableStrictMode() {
    StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build())

    StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
  }
}
