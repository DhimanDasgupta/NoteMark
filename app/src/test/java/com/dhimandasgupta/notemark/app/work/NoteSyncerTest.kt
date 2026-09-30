package com.dhimandasgupta.notemark.app.work

import com.dhimandasgupta.notemark.data.FakeSuccessfulNoteRepository
import com.dhimandasgupta.notemark.data.FakeSuccessfulUserRepository
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.SyncRepository
import com.dhimandasgupta.notemark.data.UserRepository
import com.dhimandasgupta.notemark.data.remote.api.AuthenticationException
import com.dhimandasgupta.notemark.data.remote.model.Note
import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.database.NoteEntity
import com.dhimandasgupta.notemark.proto.Sync
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NoteSyncerTest {
  private val noteRepository = InMemoryNoteRepository()
  private val syncRepository = RecordingSyncRepository()
  private val userRepository = RecordingUserRepository()
  private val syncer =
    NoteSyncer(
      noteMarkRepository = noteRepository,
      syncRepository = syncRepository,
      userRepository = userRepository,
    )

  @Test
  fun `new local note is uploaded and marked synced`() = runTest {
    noteRepository.local["a"] = localNote(uuid = "a", lastEditedAt = T1, synced = false)

    assertEquals(SyncOutcome.Success, syncer.sync())

    assertEquals(listOf("create a"), noteRepository.remoteCalls)
    assertTrue(noteRepository.local.getValue("a").synced)
    assertEquals(T1, noteRepository.remote.getValue("a").lastEditedAt)
    assertTrue(syncRepository.lastUploadedTime.isNotEmpty())
  }

  @Test
  fun `note edited during upload stays unsynced with the edit kept`() = runTest {
    noteRepository.local["a"] = localNote(uuid = "a", lastEditedAt = T1, synced = false)
    noteRepository.onUpload = { note ->
      noteRepository.local[note.uuid] =
        note.copy(content = "edited meanwhile", lastEditedAt = T2, synced = false)
    }

    syncer.sync()

    val local = noteRepository.local.getValue("a")
    assertFalse(local.synced)
    assertEquals("edited meanwhile", local.content)
  }

  @Test
  fun `remote newer edit wins over an unsynced local edit`() = runTest {
    noteRepository.local["a"] = localNote(uuid = "a", lastEditedAt = T1, synced = false)
    noteRepository.remote["a"] = remoteNote(uuid = "a", lastEditedAt = T2, content = "server")

    assertEquals(SyncOutcome.Success, syncer.sync())

    assertTrue(noteRepository.remoteCalls.isEmpty())
    val local = noteRepository.local.getValue("a")
    assertEquals("server", local.content)
    assertEquals(T2, local.lastEditedAt)
    assertTrue(local.synced)
  }

  @Test
  fun `timestamps with different UTC offsets compare by instant`() = runTest {
    // 09:30-02:00 is 11:30Z, so the local edit is newer although it sorts first as text.
    noteRepository.local["a"] =
      localNote(uuid = "a", lastEditedAt = "2025-07-01T09:30:00-02:00", synced = false)
    noteRepository.remote["a"] = remoteNote(uuid = "a", lastEditedAt = "2025-07-01T10:00:00Z")

    syncer.sync()

    assertEquals(listOf("update a"), noteRepository.remoteCalls)
    assertTrue(noteRepository.local.getValue("a").synced)
  }

  @Test
  fun `server note is downloaded as synced`() = runTest {
    noteRepository.remote["b"] = remoteNote(uuid = "b", lastEditedAt = T1)

    syncer.sync()

    assertTrue(noteRepository.local.getValue("b").synced)
  }

  @Test
  fun `synced note missing from the server is deleted locally`() = runTest {
    noteRepository.local["a"] = localNote(uuid = "a", lastEditedAt = T1, synced = true)

    syncer.sync()

    assertNull(noteRepository.local["a"])
  }

  @Test
  fun `deleted note that never reached the server is removed without a remote call`() = runTest {
    noteRepository.local["a"] =
      localNote(uuid = "a", lastEditedAt = T1, synced = false, markAsDeleted = true)

    assertEquals(SyncOutcome.Success, syncer.sync())

    assertTrue(noteRepository.remoteCalls.isEmpty())
    assertNull(noteRepository.local["a"])
  }

  @Test
  fun `deleted note on the server is deleted remotely then locally`() = runTest {
    noteRepository.local["a"] =
      localNote(uuid = "a", lastEditedAt = T1, synced = true, markAsDeleted = true)
    noteRepository.remote["a"] = remoteNote(uuid = "a", lastEditedAt = T1)

    syncer.sync()

    assertEquals(listOf("delete a"), noteRepository.remoteCalls)
    assertNull(noteRepository.local["a"])
    assertNull(noteRepository.remote["a"])
  }

  @Test
  fun `network failure retries and leaves the sync times unchanged`() = runTest {
    noteRepository.remoteFailure = IOException("offline")

    assertEquals(SyncOutcome.Retry, syncer.sync())

    assertEquals("", syncRepository.lastUploadedTime)
    assertEquals("", syncRepository.lastDownloadedTime)
  }

  @Test
  fun `failed upload retries and leaves the note unsynced`() = runTest {
    noteRepository.local["a"] = localNote(uuid = "a", lastEditedAt = T1, synced = false)
    noteRepository.failUploads = true

    assertEquals(SyncOutcome.Retry, syncer.sync())

    assertFalse(noteRepository.local.getValue("a").synced)
    assertEquals("", syncRepository.lastUploadedTime)
  }

  @Test
  fun `authentication failure logs the user out without retrying`() = runTest {
    noteRepository.remoteFailure = AuthenticationException(message = "expired")

    assertEquals(SyncOutcome.Failure, syncer.sync())

    assertTrue(userRepository.deleted)
  }
}

private const val T1 = "2025-07-01T10:00:00Z"
private const val T2 = "2025-07-01T11:00:00Z"

private fun localNote(
  uuid: String,
  lastEditedAt: String,
  synced: Boolean,
  markAsDeleted: Boolean = false,
) =
  NoteEntity(
    id = 0,
    uuid = uuid,
    title = "title",
    content = "local",
    createdAt = T1,
    lastEditedAt = lastEditedAt,
    synced = synced,
    markAsDeleted = markAsDeleted,
  )

private fun remoteNote(
  uuid: String,
  lastEditedAt: String,
  content: String = "remote",
) =
  Note(
    uuid = uuid,
    title = "title",
    content = content,
    createdAt = T1,
    lastEditedAt = lastEditedAt,
  )

/** Local table and server in memory. Guarded writes follow the same rules as the SQL queries. */
private class InMemoryNoteRepository(
  private val delegate: NoteMarkRepository = FakeSuccessfulNoteRepository()
) : NoteMarkRepository by delegate {
  val local = mutableMapOf<String, NoteEntity>()
  val remote = mutableMapOf<String, Note>()
  val remoteCalls = mutableListOf<String>()
  var remoteFailure: Throwable? = null
  var failUploads = false
  var onUpload: (NoteEntity) -> Unit = {}

  override suspend fun getRemoteNotes(page: Int, size: Int): Result<NoteResponse> =
    remoteFailure?.let { throwable -> Result.failure(throwable) }
      ?: Result.success(NoteResponse(notes = remote.values.toList(), total = remote.size))

  override suspend fun getAllNotesForSync(): List<NoteEntity> = local.values.toList()

  override suspend fun createNewRemoteNote(noteEntity: NoteEntity): Boolean =
    upload(call = "create", noteEntity = noteEntity)

  override suspend fun updateRemoteNote(
    title: String,
    content: String,
    lastEditedAt: String,
    noteEntity: NoteEntity,
  ): Boolean = upload(call = "update", noteEntity = noteEntity)

  private fun upload(call: String, noteEntity: NoteEntity): Boolean {
    remoteCalls += "$call ${noteEntity.uuid}"
    if (failUploads) return false
    remote[noteEntity.uuid] =
      Note(
        uuid = noteEntity.uuid,
        title = noteEntity.title,
        content = noteEntity.content,
        createdAt = noteEntity.createdAt,
        lastEditedAt = noteEntity.lastEditedAt,
      )
    onUpload(noteEntity)
    return true
  }

  override suspend fun deleteRemoteNote(noteEntity: NoteEntity): Boolean {
    remoteCalls += "delete ${noteEntity.uuid}"
    return remote.remove(noteEntity.uuid) != null
  }

  override suspend fun deleteLocalNote(noteEntity: NoteEntity): Boolean =
    local.remove(noteEntity.uuid) != null

  override suspend fun insertRemoteNotesIfMissing(remoteNotes: List<Note>): Boolean {
    remoteNotes.forEach { note ->
      local.putIfAbsent(
        note.uuid,
        NoteEntity(
          id = 0,
          uuid = note.uuid,
          title = note.title,
          content = note.content,
          createdAt = note.createdAt,
          lastEditedAt = note.lastEditedAt,
          synced = true,
          markAsDeleted = false,
        ),
      )
    }
    return true
  }

  override suspend fun replaceWithRemoteNote(
    remoteNote: Note,
    expectedLastEditedAt: String,
  ): Boolean {
    val note = local[remoteNote.uuid] ?: return false
    if (note.lastEditedAt != expectedLastEditedAt || note.markAsDeleted) return false
    local[remoteNote.uuid] =
      note.copy(
        title = remoteNote.title,
        content = remoteNote.content,
        lastEditedAt = remoteNote.lastEditedAt,
        synced = true,
      )
    return true
  }

  override suspend fun markSyncedIfUnchanged(uuid: String, lastEditedAt: String): Boolean {
    val note = local[uuid] ?: return false
    if (note.lastEditedAt != lastEditedAt) return false
    local[uuid] = note.copy(synced = true)
    return true
  }

  override suspend fun deleteSyncedNoteIfUnchanged(uuid: String, lastEditedAt: String): Boolean {
    val note = local[uuid] ?: return false
    if (note.lastEditedAt != lastEditedAt || !note.synced || note.markAsDeleted) return false
    local.remove(uuid)
    return true
  }
}

private class RecordingSyncRepository : SyncRepository {
  var lastUploadedTime = ""
  var lastDownloadedTime = ""

  override fun getSync(): Flow<Sync> = emptyFlow()

  override suspend fun saveSyncing(isSyncing: Boolean) = Unit

  override suspend fun saveSyncDuration(syncDuration: Sync.SyncDuration) = Unit

  override suspend fun saveLastDownloadedTime(downLoadedTime: String) {
    lastDownloadedTime = downLoadedTime
  }

  override suspend fun saveLastUploadedTime(uploadedTime: String) {
    lastUploadedTime = uploadedTime
  }

  override suspend fun saveDeleteLocalNotesOnLogout(deleteLocalNotesOnLogout: Boolean) = Unit

  override suspend fun reset() = Unit
}

private class RecordingUserRepository(
  private val delegate: UserRepository = FakeSuccessfulUserRepository()
) : UserRepository by delegate {
  var deleted = false

  override suspend fun deleteUser() {
    deleted = true
  }
}
