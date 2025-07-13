package com.dhimandasgupta.notemark.app

import android.app.Application
import android.os.StrictMode
import com.dhimandasgupta.notemark.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class NoteMarkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            // enableStrictMode()
        }

        startKoin {
            androidLogger()
            androidContext(this@NoteMarkApp)
            modules(appModule)
        }
    }

    private fun enableStrictMode() {
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )
    }
}