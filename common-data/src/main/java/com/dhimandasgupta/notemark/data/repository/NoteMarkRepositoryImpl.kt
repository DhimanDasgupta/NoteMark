package com.dhimandasgupta.notemark.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dhimandasgupta.notemark.data.local.datasource.NoteMarkLocalDataSource
import com.dhimandasgupta.notemark.data.remote.datasource.NoteMarkApiDataSource
import com.dhimandasgupta.notemark.data.remote.model.Note
import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.data.remote.model.extension.toNoteEntity
import com.dhimandasgupta.notemark.database.NoteEntity
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow

@Inject
class NoteMarkRepositoryImpl(
  localDataSource: Lazy<NoteMarkLocalDataSource>,
  remoteDataSource: Lazy<NoteMarkApiDataSource>,
) : NoteMarkRepository {
  private val localDataSource by localDataSource
  private val remoteDataSource by remoteDataSource

  override fun getPagedNotes(pageSize: Int): Flow<PagingData<NoteEntity>> =
    Pager(
        config =
          PagingConfig(
            pageSize = pageSize,
            enablePlaceholders = true,
            initialLoadSize = pageSize * 2,
          ),
        pagingSourceFactory = { localDataSource.notesPagingSource() },
      )
      .flow

  override fun getNotesFromOffSetWithLimitAsList(
    limit: Long,
    offset: Long,
  ): List<NoteEntity> =
    localDataSource.getNotesFromOffSetWithLimitAsList(limit = limit, offset = offset)

  override fun getNotesFromOffSetWithLimit(
    limit: Long,
    offset: Long,
  ): Flow<List<NoteEntity>> =
    localDataSource.getNotesFromOffSetWithLimit(limit = limit, offset = offset)

  override fun getAllNotes(): Flow<List<NoteEntity>> = localDataSource.getAllNotes()

  override suspend fun getAllNonSyncedNotes(): List<NoteEntity> =
    localDataSource.getAllNonSyncedNotes()

  override suspend fun getAllMarkedAsDeletedNotes(): List<NoteEntity> =
    localDataSource.getAllMarkedAsDeletedNotes()

  override suspend fun getRemoteNotes(page: Int, size: Int): Result<NoteResponse> =
    remoteDataSource.getAllNotes(page = page, size = size)

  override suspend fun getRemoteNotesAndSaveInDB(page: Int, size: Int): Result<NoteResponse> {
    val remoteNotes = remoteDataSource.getAllNotes(page = page, size = size)
    remoteNotes.getOrNull()?.notes?.let { note ->
      val notesToBeSavedInDB = note.map { note -> note.toNoteEntity(synced = true) }
      return if (localDataSource.insertNotes(noteEntities = notesToBeSavedInDB)) {
        remoteNotes
      } else Result.failure(Exception("Failed to fetch notes from remote"))
    }
    return Result.failure(Exception("Failed to fetch notes from remote"))
  }

  override suspend fun getAllNotesForSync(): List<NoteEntity> = localDataSource.getAllNotesForSync()

  override suspend fun getNoteById(noteId: Long) = localDataSource.getNoteById(noteId = noteId)

  override suspend fun getNoteByUUID(uuid: String) = localDataSource.getNoteByUUID(uuid = uuid)

  override suspend fun createNote(noteEntity: NoteEntity): NoteEntity? =
    localDataSource.createNote(noteEntity = noteEntity.copy(synced = false))

  override suspend fun updateLocalNote(
    title: String,
    content: String,
    lastEditedAt: String,
    noteEntity: NoteEntity,
  ): NoteEntity? =
    localDataSource.updateNote(
      title = title,
      content = content,
      lastEditedAt = lastEditedAt,
      uuid = noteEntity.uuid,
      synced = noteEntity.synced,
    )

  override suspend fun insertNotes(noteEntities: List<NoteEntity>) =
    localDataSource.insertNotes(noteEntities = noteEntities)

  override suspend fun createNewRemoteNote(noteEntity: NoteEntity): Boolean {
    val noteCreatedRemotely = remoteDataSource.createNote(noteEntity = noteEntity)
    return noteCreatedRemotely.getOrNull() != null
  }

  override suspend fun updateRemoteNote(
    title: String,
    content: String,
    lastEditedAt: String,
    noteEntity: NoteEntity,
  ): Boolean {
    val noteUpdatedRemotely =
      remoteDataSource.updateNote(
        title = title,
        content = content,
        lastEditedAt = lastEditedAt,
        noteEntity = noteEntity,
      )
    return noteUpdatedRemotely.getOrNull() != null
  }

  override suspend fun deleteRemoteNote(noteEntity: NoteEntity): Boolean {
    val noteDeletedRemotely = remoteDataSource.deleteNote(noteEntity = noteEntity)
    return noteDeletedRemotely.getOrNull() == Unit
  }

  override suspend fun deleteLocalNote(noteEntity: NoteEntity): Boolean {
    val noteDeletedLocally = localDataSource.deleteNote(noteEntity = noteEntity)
    return noteDeletedLocally
  }

  override suspend fun markAsDeleted(noteEntity: NoteEntity): Boolean =
    localDataSource.markAsDeleted(noteEntity = noteEntity)

  override suspend fun deleteAllLocalNotes() =
    try {
      localDataSource.deleteAllNotes()
    } catch (_: Exception) {
      currentCoroutineContext().ensureActive()
      false
    }

  override suspend fun insertRemoteNotesIfMissing(remoteNotes: List<Note>): Boolean =
    localDataSource.insertRemoteNotesIfMissing(
      noteEntities = remoteNotes.map { note -> note.toNoteEntity(synced = true) }
    )

  override suspend fun replaceWithRemoteNote(
    remoteNote: Note,
    expectedLastEditedAt: String,
  ): Boolean =
    localDataSource.replaceWithRemoteNote(
      remoteNote = remoteNote.toNoteEntity(synced = true),
      expectedLastEditedAt = expectedLastEditedAt,
    )

  override suspend fun markSyncedIfUnchanged(
    uuid: String,
    lastEditedAt: String,
  ): Boolean = localDataSource.markSyncedIfUnchanged(uuid = uuid, lastEditedAt = lastEditedAt)

  override suspend fun deleteSyncedNoteIfUnchanged(
    uuid: String,
    lastEditedAt: String,
  ): Boolean = localDataSource.deleteSyncedNoteIfUnchanged(uuid = uuid, lastEditedAt = lastEditedAt)

  override suspend fun logout(request: RefreshRequest): Result<Unit> =
    remoteDataSource.logout(request = request)
}
