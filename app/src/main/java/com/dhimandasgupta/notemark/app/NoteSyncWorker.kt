package com.dhimandasgupta.notemark.app

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dhimandasgupta.notemark.common.getCurrentIso8601Timestamp
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.SyncRepository
import com.dhimandasgupta.notemark.data.remote.model.Note
import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlinx.coroutines.delay
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

            // Fetch all Remote notes.
            val allRemoteNotes = noteMarkRepository.getRemoteNotesAndSaveInDB().getOrElse { NoteResponse(notes = emptyList(), total = 0) }

            // Delete all Remote notes waiting to be deleted.
            val toBeDeletedNotes = noteMarkRepository.getAllMarkedAsDeletedNotes()
            deleteNotes(toBeDeletedNotes)

            // Update or Upload all Local notes.
            val toBeSyncedNotes = noteMarkRepository.getAllNonSyncedNotes()
            updateOrUploadNotes(
                remoteNotes = allRemoteNotes.notes,
                notes = toBeSyncedNotes
            )

            syncRepository.saveLastUploadedTime(getCurrentIso8601Timestamp())
            syncRepository.saveSyncing(false)

            return Result.success()
        } catch (_: Exception) {
            coroutineContext.ensureActive()
            return Result.failure()
        }
    }

    private suspend fun updateOrUploadNotes(
        remoteNotes: List<Note>,
        notes: List<NoteEntity>
    ) {
        notes.forEach { note ->
            when (remoteNotes.find { it.uuid == note.uuid }) {
                null -> uploadNote(note)
                else -> updateNote(note)
            }
            delay(1000L)
        }
    }

    private suspend fun updateNote(note: NoteEntity) = supervisorScope {
        val uploaded = noteMarkRepository.updateRemoteNote(
            title = note.title,
            content = note.content,
            lastEditedAt = note.lastEditedAt,
            noteEntity = note
        )
        if (uploaded) {
            noteMarkRepository.updateLocalNote(
                title = note.title,
                content = note.content,
                lastEditedAt = note.lastEditedAt,
                noteEntity = note.copy(synced = true)
            )
        }
    }

    private suspend fun uploadNote(note: NoteEntity) = supervisorScope {
        val uploaded = noteMarkRepository.createNewRemoteNote(note)
        if (uploaded) {
            noteMarkRepository.updateLocalNote(
                title = note.title,
                content = note.content,
                lastEditedAt = note.lastEditedAt,
                noteEntity = note.copy(synced = true)
            )
        }
    }

    private suspend fun deleteNotes(notes: List<NoteEntity>) {
        notes.forEach { note ->
            deleteNote(note)
            delay(1000L)
        }
    }

    private suspend fun deleteNote(note: NoteEntity) = supervisorScope {
        val deleted = noteMarkRepository.deleteRemoteNote(note)
        if (deleted) {
            noteMarkRepository.deleteLocalNote(note)
        }
    }
}