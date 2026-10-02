package com.dhimandasgupta.notemark.data.local.datasource

import androidx.paging.PagingSource
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlinx.coroutines.flow.Flow

interface NoteMarkLocalDataSource {
  /**
   * A [PagingSource] over the notes the list shows, newest edit first. Pages are read with SQL
   * `LIMIT`/`OFFSET`, and the source invalidates itself whenever a write touches `NoteEntity`, so
   * inserts, edits and deletes reach the list without the caller re-subscribing.
   */
  fun notesPagingSource(): PagingSource<Int, NoteEntity>

  fun getNotesFromOffSetWithLimitAsList(limit: Long = 10L, offset: Long): List<NoteEntity>

  fun getNotesFromOffSetWithLimit(limit: Long = 10L, offset: Long): Flow<List<NoteEntity>>

  fun getAllNotes(): Flow<List<NoteEntity>>

  suspend fun getAllNonSyncedNotes(): List<NoteEntity>

  suspend fun getAllMarkedAsDeletedNotes(): List<NoteEntity>

  /** Every row, including those marked as deleted. */
  suspend fun getAllNotesForSync(): List<NoteEntity>

  suspend fun getNoteById(noteId: Long): NoteEntity?

  suspend fun getNoteByUUID(uuid: String): NoteEntity?

  suspend fun createNote(noteEntity: NoteEntity): NoteEntity?

  suspend fun updateNote(
    title: String,
    content: String,
    lastEditedAt: String,
    uuid: String,
    synced: Boolean,
  ): NoteEntity?

  suspend fun insertNote(noteEntity: NoteEntity): Boolean

  suspend fun insertNotes(noteEntities: List<NoteEntity>): Boolean

  suspend fun markAsDeleted(noteEntity: NoteEntity): Boolean

  suspend fun deleteNote(noteEntity: NoteEntity): Boolean

  /** Inserts notes as synced, leaving any row that already has the same uuid untouched. */
  suspend fun insertRemoteNotesIfMissing(noteEntities: List<NoteEntity>): Boolean

  /**
   * The methods below only change a row whose `lastEditedAt` still equals the value the caller
   * read, so an edit made in the meantime is kept. Each returns whether a row was changed.
   */
  suspend fun replaceWithRemoteNote(remoteNote: NoteEntity, expectedLastEditedAt: String): Boolean

  suspend fun markSyncedIfUnchanged(uuid: String, lastEditedAt: String): Boolean

  suspend fun deleteSyncedNoteIfUnchanged(uuid: String, lastEditedAt: String): Boolean

  suspend fun deleteAllNotes(): Boolean
}
