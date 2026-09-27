package com.dhimandasgupta.notemark.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dhimandasgupta.notemark.data.local.datasource.NoteMarkLocalDataSource
import com.dhimandasgupta.notemark.data.remote.datasource.NoteMarkApiDataSource
import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.database.NoteEntity
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow

@Inject
class NoteMarkRepositoryImpl(
  private val localDataSource: Lazy<NoteMarkLocalDataSource>,
  private val remoteDataSource: Lazy<NoteMarkApiDataSource>,
) : NoteMarkRepository {
  override fun getPagedNotes(pageSize: Int): Flow<PagingData<NoteEntity>> =
    Pager(
        config =
          PagingConfig(
            pageSize = pageSize,
            enablePlaceholders = true,
            initialLoadSize = pageSize * 2,
          ),
        pagingSourceFactory = { localDataSource.value.notesPagingSource() },
      )
      .flow

  override fun getNotesFromOffSetWithLimitAsList(
    limit: Long,
    offset: Long,
  ): List<NoteEntity> =
    localDataSource.value.getNotesFromOffSetWithLimitAsList(limit = limit, offset = offset)

  override fun getNotesFromOffSetWithLimit(
    limit: Long,
    offset: Long,
  ): Flow<List<NoteEntity>> =
    localDataSource.value.getNotesFromOffSetWithLimit(limit = limit, offset = offset)

  override fun getAllNotes(): Flow<List<NoteEntity>> = localDataSource.value.getAllNotes()

  override suspend fun getAllNonSyncedNotes(): List<NoteEntity> =
    localDataSource.value.getAllNonSyncedNotes()

  override suspend fun getAllMarkedAsDeletedNotes(): List<NoteEntity> =
    localDataSource.value.getAllMarkedAsDeletedNotes()

  override suspend fun getRemoteNotes(page: Int, size: Int): Result<NoteResponse> =
    remoteDataSource.value.getAllNotes(page = page, size = size)

  override suspend fun getRemoteNotesAndSaveInDB(page: Int, size: Int): Result<NoteResponse> {
    val remoteNotes = remoteDataSource.value.getAllNotes(page = page, size = size)
    remoteNotes.getOrNull()?.notes?.let { note ->
      val notesToBeSavedInDB = note.map { note -> note.toNoteEntity(synced = true) }
      return if (localDataSource.value.insertNotes(noteEntities = notesToBeSavedInDB)) {
        remoteNotes
      } else Result.failure(Exception("Failed to fetch notes from remote"))
    }
    return Result.failure(Exception("Failed to fetch notes from remote"))
  }

  override suspend fun getNoteById(noteId: Long) =
    localDataSource.value.getNoteById(noteId = noteId)

  override suspend fun getNoteByUUID(uuid: String) =
    localDataSource.value.getNoteByUUID(uuid = uuid)

  override suspend fun createNote(noteEntity: NoteEntity): NoteEntity? =
    localDataSource.value.createNote(noteEntity = noteEntity.copy(synced = false))

  override suspend fun updateLocalNote(
    title: String,
    content: String,
    lastEditedAt: String,
    noteEntity: NoteEntity,
  ): NoteEntity? =
    localDataSource.value.updateNote(
      title = title,
      content = content,
      lastEditedAt = lastEditedAt,
      uuid = noteEntity.uuid,
      synced = noteEntity.synced,
    )

  override suspend fun insertNotes(noteEntities: List<NoteEntity>) =
    localDataSource.value.insertNotes(noteEntities = noteEntities)

  override suspend fun createNewRemoteNote(noteEntity: NoteEntity): Boolean {
    val noteCreatedRemotely = remoteDataSource.value.createNote(noteEntity = noteEntity)
    return noteCreatedRemotely.getOrNull() != null
  }

  override suspend fun updateRemoteNote(
    title: String,
    content: String,
    lastEditedAt: String,
    noteEntity: NoteEntity,
  ): Boolean {
    val noteUpdatedRemotely =
      remoteDataSource.value.updateNote(
        title = title,
        content = content,
        lastEditedAt = lastEditedAt,
        noteEntity = noteEntity,
      )
    return noteUpdatedRemotely.getOrNull() != null
  }

  override suspend fun deleteRemoteNote(noteEntity: NoteEntity): Boolean {
    val noteDeletedRemotely = remoteDataSource.value.deleteNote(noteEntity = noteEntity)
    return noteDeletedRemotely.getOrNull() == Unit
  }

  override suspend fun deleteLocalNote(noteEntity: NoteEntity): Boolean {
    val noteDeletedLocally = localDataSource.value.deleteNote(noteEntity = noteEntity)
    return noteDeletedLocally
  }

  override suspend fun markAsDeleted(noteEntity: NoteEntity): Boolean =
    localDataSource.value.markAsDeleted(noteEntity = noteEntity)

  override suspend fun deleteAllLocalNotes() =
    try {
      localDataSource.value.deleteAllNotes()
    } catch (_: Exception) {
      currentCoroutineContext().ensureActive()
      false
    }

  override suspend fun logout(request: RefreshRequest): Result<Unit> =
    remoteDataSource.value.logout(request = request)
}
