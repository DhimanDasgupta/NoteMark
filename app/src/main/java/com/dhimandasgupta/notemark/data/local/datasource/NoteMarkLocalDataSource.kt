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

  suspend fun deleteAllNotes(): Boolean
}
