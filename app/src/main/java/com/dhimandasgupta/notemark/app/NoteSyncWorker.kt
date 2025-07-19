package com.dhimandasgupta.notemark.app

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.SyncRepository
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.supervisorScope
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject

class NoteSyncWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters), KoinComponent {
    private val syncRepository: SyncRepository by inject(SyncRepository::class.java)
    private val noteMarkRepository: NoteMarkRepository by inject(NoteMarkRepository::class.java)

    override suspend fun doWork(): Result {
        try {
            syncRepository.saveSyncing(true)

            val deletedNotes = noteMarkRepository.getAllMarkedAsDeletedNotes().filter { it.markAsDeleted }
            deleteNotes(deletedNotes)

            val nonSyncedNotes = noteMarkRepository.getAllNonSyncedNotes().filter { it.synced.not() }
            uploadNotes(nonSyncedNotes)

            syncRepository.saveSyncing(false)
            syncRepository.saveLastUploadedTime(System.currentTimeMillis())
            return Result.success()
        } catch (_: Exception) {
            coroutineContext.ensureActive()
            return Result.failure()
        }
    }

    private suspend fun uploadNotes(notes: List<NoteEntity>) {
        notes.forEach { note -> uploadNote(note) }
    }

    private suspend fun uploadNote(note: NoteEntity) = supervisorScope {
        val uploaded = noteMarkRepository.uploadNote(note)
        if (uploaded) {
            noteMarkRepository.updateNote(
                title = note.title,
                content = note.content,
                lastEditedAt = note.lastEditedAt,
                noteEntity = note.copy(synced = true)
            )
        }
    }

    private suspend fun deleteNotes(notes: List<NoteEntity>) {
        notes.forEach { note -> deleteNote(note) }
    }

    private suspend fun deleteNote(note: NoteEntity) = supervisorScope {
        val deleted = noteMarkRepository.deleteRemoteNote(note)
        if (deleted) {
            noteMarkRepository.deleteLocalNote(note)
        }
    }
}