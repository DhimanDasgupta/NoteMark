package com.dhimandasgupta.notemark.app

import android.app.Application
import android.os.StrictMode
import android.util.Log.DEBUG
import android.util.Log.ERROR
import androidx.work.Configuration
import androidx.work.WorkManager
import com.dhimandasgupta.notemark.BuildConfig
import com.dhimandasgupta.notemark.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class NoteMarkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) DEBUG else ERROR)
            .build()

        WorkManager.initialize(context = this, configuration = config)

        startKoin {
            androidLogger()
            androidContext(androidContext = this@NoteMarkApp)
            modules(modules = appModule)
        }
    }

    private fun enableStrictMode() {
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                //.penaltyDeath() // Koin causes crash on this
                .build()
        )

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                //.penaltyDeath()  // Koin causes crash on this
                .build()
        )
    }
}