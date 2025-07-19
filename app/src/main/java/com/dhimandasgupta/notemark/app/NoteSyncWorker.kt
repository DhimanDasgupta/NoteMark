package com.dhimandasgupta.notemark.app

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.SyncRepository
import com.dhimandasgupta.notemark.data.UserRepository
import kotlinx.coroutines.delay

class NoteSyncWorker(
    context: Context,
    workerParameters: WorkerParameters,
    private val userRepository: UserRepository,
    private val syncRepository: SyncRepository,
    private val noteMarkRepository: NoteMarkRepository
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        delay(1000)
        return Result.success()
    }
}