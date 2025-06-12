package com.dhimandasgupta.notemark

import android.app.Application
import android.os.StrictMode
import com.dhimandasgupta.notemark.network.NoteMarkApi
import com.dhimandasgupta.notemark.network.storage.InMemoryTokenStorage
import com.dhimandasgupta.notemark.network.storage.TokenStorage
import com.dhimandasgupta.notemark.presenter.AppPresenter
import com.dhimandasgupta.notemark.statemachine.AppStateMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent

class NoteMarkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }

        startKoin {
            androidLogger()
            androidContext(this@NoteMarkApp)
            modules(appModule)
        }

        // Pre-warm I/O-heavy dependencies on a background thread
        // This prevents them from being initialized on the main thread later.
        CoroutineScope(Dispatchers.IO).launch {
            KoinJavaComponent.getKoin().get<TokenStorage>()
            KoinJavaComponent.getKoin().get<NoteMarkApi>()
            KoinJavaComponent.getKoin().get<AppStateMachine>()
            KoinJavaComponent.getKoin().get<AppPresenter>()
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