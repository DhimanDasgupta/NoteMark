package com.dhimandasgupta.notemark.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dhimandasgupta.notemark.app.di.AppBackgroundDispatcher
import com.dhimandasgupta.notemark.common.getCurrentIso8601Timestamp
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.SyncRepository
import com.dhimandasgupta.notemark.data.UserRepository
import com.dhimandasgupta.notemark.data.remote.api.AuthenticationException
import com.dhimandasgupta.notemark.data.remote.model.Note
import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.database.NoteEntity
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

private const val DELAY_IN_BETWEEN_EVERY_NOTE = 10L

class NoteSyncWorker
@AssistedInject
constructor(
  @Assisted context: Context,
  @Assisted workerParameters: WorkerParameters,
  @AppBackgroundDispatcher private val applicationDispatcher: CoroutineDispatcher,
  private val syncRepository: SyncRepository,
  private val noteMarkRepository: NoteMarkRepository,
  private val userRepository: UserRepository,
) : CoroutineWorker(appContext = context, params = workerParameters) {

  override suspend fun doWork(): Result =
    withContext(applicationDispatcher) {
      try {
        syncRepository.saveSyncing(isSyncing = true)

        // Fetch all Remote notes.
        noteMarkRepository
          .getRemoteNotes()
          .fold(
            onSuccess = { noteResponse ->
              executeSuccess(noteResponse)
              Result.success()
            },
            onFailure = { throwable ->
              if (throwable is AuthenticationException) {
                userRepository.deleteUser()
              }
              Result.failure()
            },
          )
      } catch (_: Exception) {
        coroutineContext.ensureActive()
        Result.failure()
      } finally {
        syncRepository.saveSyncing(isSyncing = false)
      }
    }

  private suspend fun executeSuccess(noteResponse: NoteResponse) = supervisorScope {

    // Delete all Remote notes waiting to be deleted.
    val deleteNotesDeferred = async {
      val toBeDeletedNotes = noteMarkRepository.getAllMarkedAsDeletedNotes()
      deleteNotes(toBeDeletedNotes)
    }

    // Update or Upload all Local notes.
    val updateOrUploadNotesDeferred = async {
      val toBeSyncedNotes = noteMarkRepository.getAllNonSyncedNotes()
      updateOrUploadNotes(
        remoteNotes = noteResponse.notes,
        notes = toBeSyncedNotes,
      )
    }

    deleteNotesDeferred.await()
    updateOrUploadNotesDeferred.await()

    // Insert all Remote notes.
    noteMarkRepository.getRemoteNotesAndSaveInDB()

    syncRepository.saveLastUploadedTime(uploadedTime = getCurrentIso8601Timestamp())
    syncRepository.saveLastDownloadedTime(downLoadedTime = getCurrentIso8601Timestamp())
  }

  private suspend fun updateOrUploadNotes(
    remoteNotes: List<Note>,
    notes: List<NoteEntity>,
  ) {
    notes.forEach { note ->
      when (remoteNotes.find { remoteNote -> remoteNote.uuid == note.uuid }) {
        null -> uploadNote(note)
        else -> updateNote(note)
      }
      delay(timeMillis = DELAY_IN_BETWEEN_EVERY_NOTE) // Just some delay for testing
    }
  }

  private suspend fun updateNote(note: NoteEntity) = supervisorScope {
    val uploaded =
      noteMarkRepository.updateRemoteNote(
        title = note.title,
        content = note.content,
        lastEditedAt = note.lastEditedAt,
        noteEntity = note,
      )
    if (uploaded) {
      noteMarkRepository.updateLocalNote(
        title = note.title,
        content = note.content,
        lastEditedAt = note.lastEditedAt,
        noteEntity = note.copy(synced = true),
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
        noteEntity = note.copy(synced = true),
      )
    }
  }

  private suspend fun deleteNotes(notes: List<NoteEntity>) {
    notes.forEach { note ->
      deleteNote(note)
      delay(timeMillis = DELAY_IN_BETWEEN_EVERY_NOTE) // Just some delay for testing
    }
  }

  private suspend fun deleteNote(note: NoteEntity) = supervisorScope {
    val deleted = noteMarkRepository.deleteRemoteNote(noteEntity = note)
    if (deleted) {
      noteMarkRepository.deleteLocalNote(noteEntity = note)
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(context: Context, workerParameters: WorkerParameters): NoteSyncWorker
  }
}
