package com.dhimandasgupta.notemark.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dhimandasgupta.notemark.app.di.APP_BACKGROUND_SCOPE
import com.dhimandasgupta.notemark.common.getCurrentIso8601Timestamp
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.SyncRepository
import com.dhimandasgupta.notemark.data.remote.model.Note
import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent

class NoteSyncWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext = context, params = workerParameters), KoinComponent {
    private val applicationScope: CoroutineScope by inject(qualifier = named(name = APP_BACKGROUND_SCOPE))
    private val syncRepository: SyncRepository by KoinJavaComponent.inject(clazz = SyncRepository::class.java)
    private val noteMarkRepository: NoteMarkRepository by KoinJavaComponent.inject(
        clazz = NoteMarkRepository::class.java
    )

    override suspend fun doWork(): Result = withContext(applicationScope.coroutineContext) {
        try {
            syncRepository.saveSyncing(isSyncing = true)

            // Fetch all Remote notes.
            val allRemoteNotes = noteMarkRepository.getRemoteNotesAndSaveInDB()
                .getOrElse { NoteResponse(notes = emptyList(), total = 0) }

            // Delete all Remote notes waiting to be deleted.
            val toBeDeletedNotes = noteMarkRepository.getAllMarkedAsDeletedNotes()
            deleteNotes(toBeDeletedNotes)

            // Update or Upload all Local notes.
            val toBeSyncedNotes = noteMarkRepository.getAllNonSyncedNotes()
            updateOrUploadNotes(
                remoteNotes = allRemoteNotes.notes,
                notes = toBeSyncedNotes
            )

            syncRepository.saveLastUploadedTime(uploadedTime = getCurrentIso8601Timestamp())
            syncRepository.saveLastDownloadedTime(downLoadedTime = getCurrentIso8601Timestamp())

            Result.success()
        } catch (_: Exception) {
            coroutineContext.ensureActive()
            Result.failure()
        } finally {
            syncRepository.saveSyncing(isSyncing = false)
        }
    }

    private suspend fun updateOrUploadNotes(
        remoteNotes: List<Note>,
        notes: List<NoteEntity>
    ) {
        notes.forEach { note ->
            when (remoteNotes.find { remoteNote -> remoteNote.uuid == note.uuid }) {
                null -> uploadNote(note)
                else -> updateNote(note)
            }
            delay(timeMillis = 1000L) // Just some delay for testing
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
        val uploaded = noteMarkRepository.createNewRemoteNote(noteEntity = note)
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
            delay(timeMillis = 1000L) // Just some delay for testing
        }
    }

    private suspend fun deleteNote(note: NoteEntity) = supervisorScope {
        val deleted = noteMarkRepository.deleteRemoteNote(noteEntity = note)
        if (deleted) {
            noteMarkRepository.deleteLocalNote(noteEntity = note)
        }
    }
}