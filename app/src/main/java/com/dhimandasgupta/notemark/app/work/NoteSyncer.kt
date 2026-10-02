package com.dhimandasgupta.notemark.app.work

import com.dhimandasgupta.notemark.common.extensions.kotlin.getCurrentIso8601Timestamp
import com.dhimandasgupta.notemark.common.extensions.kotlin.isIsoTimestampAfter
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.SyncRepository
import com.dhimandasgupta.notemark.data.UserRepository
import com.dhimandasgupta.notemark.data.remote.api.AuthenticationException
import com.dhimandasgupta.notemark.data.remote.model.Note
import com.dhimandasgupta.notemark.database.NoteEntity
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex

enum class SyncOutcome {
  /** Everything synced, or another sync was already running. */
  Success,

  /** Something failed that may succeed later, such as the network or a single note. */
  Retry,

  /** The user is no longer authenticated; retrying won't help. */
  Failure,
}

/**
 * Two-way sync of the notes table with the server.
 *
 * The sync reads the server list and a snapshot of every local row once, then works from those. The
 * local writes it makes only apply while the row's `lastEditedAt` still matches the snapshot, so a
 * note edited while the sync runs keeps the edit and stays unsynced for the next run. When a note
 * was changed on both sides, the newer `lastEditedAt` wins.
 */
@Inject
class NoteSyncer(
  private val noteMarkRepository: NoteMarkRepository,
  private val syncRepository: SyncRepository,
  private val userRepository: UserRepository,
) {
  private val syncMutex = Mutex()

  suspend fun sync(): SyncOutcome {
    // The one-time and periodic workers are separate WorkManager jobs; only one may sync at a time.
    if (!syncMutex.tryLock()) return SyncOutcome.Success
    syncRepository.saveSyncing(true)
    return try {
      syncLocked()
    } catch (_: Exception) {
      currentCoroutineContext().ensureActive()
      SyncOutcome.Retry
    } finally {
      syncRepository.saveSyncing(false)
      syncMutex.unlock()
    }
  }

  private suspend fun syncLocked(): SyncOutcome {
    val remoteNotes =
      noteMarkRepository
        .getRemoteNotes()
        .getOrElse { throwable ->
          currentCoroutineContext().ensureActive()
          if (throwable is AuthenticationException) {
            userRepository.deleteUser()
            return SyncOutcome.Failure
          }
          return SyncOutcome.Retry
        }
        .notes
        .associateBy { note -> note.uuid }
    val localNotes = noteMarkRepository.getAllNotesForSync()

    val deletesSucceeded = pushDeletes(localNotes = localNotes, remoteNotes = remoteNotes)
    val uploadsSucceeded = pushChanges(localNotes = localNotes, remoteNotes = remoteNotes)
    pullChanges(localNotes = localNotes, remoteNotes = remoteNotes)

    if (!deletesSucceeded || !uploadsSucceeded) return SyncOutcome.Retry

    val syncedAt = getCurrentIso8601Timestamp()
    syncRepository.saveLastUploadedTime(uploadedTime = syncedAt)
    syncRepository.saveLastDownloadedTime(downLoadedTime = syncedAt)
    return SyncOutcome.Success
  }

  /** Deletes notes marked as deleted, first on the server, then locally. */
  private suspend fun pushDeletes(
    localNotes: List<NoteEntity>,
    remoteNotes: Map<String, Note>,
  ): Boolean {
    var allSucceeded = true
    localNotes
      .asSequence()
      .filter { note -> note.markAsDeleted }
      .forEach { note ->
        // A note that never reached the server only needs removing here.
        val deletedRemotely = note.uuid !in remoteNotes || noteMarkRepository.deleteRemoteNote(note)
        if (deletedRemotely) {
          noteMarkRepository.deleteLocalNote(noteEntity = note)
        } else {
          allSucceeded = false
        }
      }
    return allSucceeded
  }

  /** Uploads local changes, unless the server has a newer edit of the same note. */
  private suspend fun pushChanges(
    localNotes: List<NoteEntity>,
    remoteNotes: Map<String, Note>,
  ): Boolean {
    var allSucceeded = true
    localNotes
      .asSequence()
      .filter { note -> !note.synced && !note.markAsDeleted }
      .forEach { note ->
        val remoteNote = remoteNotes[note.uuid]
        val uploaded =
          when {
            remoteNote == null -> noteMarkRepository.createNewRemoteNote(noteEntity = note)
            // The server copy is newer; pullChanges takes it.
            isIsoTimestampAfter(first = remoteNote.lastEditedAt, second = note.lastEditedAt) ->
              return@forEach
            else ->
              noteMarkRepository.updateRemoteNote(
                title = note.title,
                content = note.content,
                lastEditedAt = note.lastEditedAt,
                noteEntity = note,
              )
          }
        if (uploaded) {
          noteMarkRepository.markSyncedIfUnchanged(
            uuid = note.uuid,
            lastEditedAt = note.lastEditedAt,
          )
        } else {
          allSucceeded = false
        }
      }
    return allSucceeded
  }

  /** Applies new, newer and deleted server notes to the local table. */
  private suspend fun pullChanges(
    localNotes: List<NoteEntity>,
    remoteNotes: Map<String, Note>,
  ) {
    val localNotesByUuid = localNotes.associateBy { note -> note.uuid }

    noteMarkRepository.insertRemoteNotesIfMissing(
      remoteNotes = remoteNotes.values.filter { note -> note.uuid !in localNotesByUuid }
    )

    remoteNotes.values.forEach { remoteNote ->
      val localNote = localNotesByUuid[remoteNote.uuid] ?: return@forEach
      if (localNote.markAsDeleted) return@forEach
      if (isIsoTimestampAfter(first = remoteNote.lastEditedAt, second = localNote.lastEditedAt)) {
        noteMarkRepository.replaceWithRemoteNote(
          remoteNote = remoteNote,
          expectedLastEditedAt = localNote.lastEditedAt,
        )
      }
    }

    // A note that was synced before but is gone from the server was deleted on another device.
    localNotes
      .asSequence()
      .filter { note -> note.synced && !note.markAsDeleted && note.uuid !in remoteNotes }
      .forEach { note ->
        noteMarkRepository.deleteSyncedNoteIfUnchanged(
          uuid = note.uuid,
          lastEditedAt = note.lastEditedAt,
        )
      }
  }
}
