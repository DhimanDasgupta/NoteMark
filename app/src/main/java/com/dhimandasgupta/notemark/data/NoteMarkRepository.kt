package com.dhimandasgupta.notemark.data

import androidx.paging.PagingData
import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlinx.coroutines.flow.Flow

/** Notes held in memory per page by [NoteMarkRepository.getPagedNotes]. */
const val NOTES_PAGE_SIZE: Int = 10

interface NoteMarkRepository {
  /**
   * Pages of notes read straight from the database, newest edit first. The stream re-emits whenever
   * a write touches the notes table, so a sync, an edit or a delete refreshes the pages already
   * loaded.
   */
  fun getPagedNotes(pageSize: Int = NOTES_PAGE_SIZE): Flow<PagingData<NoteEntity>>

  fun getNotesFromOffSetWithLimitAsList(limit: Long = 10L, offset: Long): List<NoteEntity>

  fun getNotesFromOffSetWithLimit(limit: Long = 10L, offset: Long): Flow<List<NoteEntity>>

  fun getAllNotes(): Flow<List<NoteEntity>>

  suspend fun getRemoteNotes(page: Int = -1, size: Int = 20): Result<NoteResponse>

  suspend fun getRemoteNotesAndSaveInDB(page: Int = -1, size: Int = 20): Result<NoteResponse>

  suspend fun getAllNonSyncedNotes(): List<NoteEntity>

  suspend fun getAllMarkedAsDeletedNotes(): List<NoteEntity>

  suspend fun getNoteById(noteId: Long): NoteEntity?

  suspend fun getNoteByUUID(uuid: String): NoteEntity?

  suspend fun createNote(noteEntity: NoteEntity): NoteEntity?

  suspend fun updateLocalNote(
    title: String,
    content: String,
    lastEditedAt: String,
    noteEntity: NoteEntity,
  ): NoteEntity?

  suspend fun createNewRemoteNote(noteEntity: NoteEntity): Boolean

  suspend fun updateRemoteNote(
    title: String,
    content: String,
    lastEditedAt: String,
    noteEntity: NoteEntity,
  ): Boolean

  suspend fun insertNotes(noteEntities: List<NoteEntity>): Boolean

  suspend fun markAsDeleted(noteEntity: NoteEntity): Boolean

  suspend fun deleteRemoteNote(noteEntity: NoteEntity): Boolean

  suspend fun deleteLocalNote(noteEntity: NoteEntity): Boolean

  suspend fun deleteAllLocalNotes(): Boolean

  suspend fun logout(request: RefreshRequest): Result<Unit>
}
