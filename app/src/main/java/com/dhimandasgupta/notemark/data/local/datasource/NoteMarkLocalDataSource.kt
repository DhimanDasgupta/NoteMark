package com.dhimandasgupta.notemark.data.local.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.paging3.QueryPagingSource
import com.dhimandasgupta.notemark.app.di.AppBackgroundDispatcher
import com.dhimandasgupta.notemark.database.NoteEntity
import com.dhimandasgupta.notemark.database.NoteMarkDatabase
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

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

@Inject
class NoteMarkLocalDataSourceImpl(
  database: Lazy<NoteMarkDatabase>,
  @AppBackgroundDispatcher private val applicationDispatcher: CoroutineDispatcher,
) : NoteMarkLocalDataSource {
  private val queries = database.value.noteMarkDatabaseQueries

  override fun notesPagingSource(): PagingSource<Int, NoteEntity> =
    TopAnchoredPagingSource(
      delegate =
        QueryPagingSource(
          countQuery = queries.getVisibleNoteCount(),
          transacter = queries,
          context = applicationDispatcher,
          queryProvider = { limit, offset ->
            queries.getNotesFromOffSetWithLimit(limit = limit, offset = offset)
          },
        )
    )

  override fun getNotesFromOffSetWithLimitAsList(
    limit: Long,
    offset: Long,
  ): List<NoteEntity> =
    queries.getNotesFromOffSetWithLimit(limit = limit, offset = offset).executeAsList()

  override fun getNotesFromOffSetWithLimit(
    limit: Long,
    offset: Long,
  ): Flow<List<NoteEntity>> =
    queries
      .getNotesFromOffSetWithLimit(limit = limit, offset = offset)
      .asFlow()
      .mapToList(context = applicationDispatcher)

  override fun getAllNotes(): Flow<List<NoteEntity>> =
    queries.getAllNotes().asFlow().mapToList(context = applicationDispatcher)

  override suspend fun getAllNonSyncedNotes(): List<NoteEntity> =
    withContext(context = applicationDispatcher) {
      return@withContext queries.getAllNonSyncedNotes().executeAsList()
    }

  override suspend fun getAllMarkedAsDeletedNotes(): List<NoteEntity> =
    withContext(context = applicationDispatcher) {
      return@withContext queries.getAllDeletedNotes().executeAsList()
    }

  override suspend fun getNoteById(noteId: Long): NoteEntity? =
    withContext(context = applicationDispatcher) {
      return@withContext queries.getNoteById(noteId).executeAsOneOrNull()
    }

  override suspend fun getNoteByUUID(uuid: String): NoteEntity? =
    withContext(context = applicationDispatcher) {
      return@withContext queries.getNoteByUUID(uuid).executeAsOneOrNull()
    }

  override suspend fun updateNote(
    title: String,
    content: String,
    lastEditedAt: String,
    uuid: String,
    synced: Boolean,
  ): NoteEntity =
    withContext(context = applicationDispatcher + NonCancellable) {
      val result =
        queries.updateNote(
          title = title,
          content = content,
          lastEditedAt = lastEditedAt,
          synced = synced,
          uuid = uuid,
        )

      return@withContext (if (result == 1L) {
        queries.getNoteByUUID(uuid).executeAsOne()
      } else {
        null
      })
        as NoteEntity
    }

  override suspend fun createNote(noteEntity: NoteEntity): NoteEntity =
    withContext(context = applicationDispatcher + NonCancellable) {
      val result =
        queries.insertNote(
          title = noteEntity.title,
          content = noteEntity.content,
          createdAt = noteEntity.createdAt,
          lastEditedAt = noteEntity.lastEditedAt,
          uuid = noteEntity.uuid,
          synced = false,
        )

      return@withContext (if (result == 1L) {
        noteEntity
      } else {
        null
      })
        as NoteEntity
    }

  override suspend fun insertNote(noteEntity: NoteEntity): Boolean =
    withContext(context = applicationDispatcher + NonCancellable) {
      val result = queries.transactionWithResult {
        queries.insertNote(
          title = noteEntity.title,
          content = noteEntity.content,
          createdAt = noteEntity.createdAt,
          lastEditedAt = noteEntity.lastEditedAt,
          uuid = noteEntity.uuid,
          synced = false,
        )
        return@transactionWithResult true
      }
      return@withContext result
    }

  override suspend fun insertNotes(noteEntities: List<NoteEntity>) =
    withContext(context = applicationDispatcher + NonCancellable) {
      val result = queries.transactionWithResult {
        noteEntities.forEach { noteEntity ->
          queries.insertNote(
            title = noteEntity.title,
            content = noteEntity.content,
            createdAt = noteEntity.createdAt,
            lastEditedAt = noteEntity.lastEditedAt,
            uuid = noteEntity.uuid,
            synced = false,
          )
        }
        return@transactionWithResult true
      }
      return@withContext result
    }

  override suspend fun markAsDeleted(noteEntity: NoteEntity): Boolean =
    withContext(context = applicationDispatcher + NonCancellable) {
      val result = queries.markNoteAsDeletedByUUID(uuid = noteEntity.uuid)
      return@withContext result == 1L
    }

  override suspend fun deleteNote(noteEntity: NoteEntity) =
    withContext(context = applicationDispatcher + NonCancellable) {
      val result = queries.deleteNoteByUUID(uuid = noteEntity.uuid)
      return@withContext result == 1L
    }

  override suspend fun deleteAllNotes() =
    withContext(context = applicationDispatcher + NonCancellable) {
      val result = queries.transactionWithResult {
        queries.deleteAll()
      }
      return@withContext result == 1L
    }
}

/**
 * Restarts the loaded window at the top of the table after an invalidation, instead of around the
 * row that was read last.
 *
 * `OffsetQueryPagingSource.getRefreshKey` anchors the next generation on `anchorPosition`, which is
 * wherever the last access hint pointed. The note list grows only through explicit `loadMore()`
 * calls, and those read the *last* loaded row, so once everything is paged in the anchor sits at
 * the bottom of the table. A write then invalidates the source and the refresh reloads a window
 * near that anchor — dropping every note above it, with no placeholders to mark the gap and no
 * scrolling list to prepend them back.
 *
 * Returning a null refresh key pins each new generation to offset 0; the presenter re-appends from
 * there up to however many notes had already been paged in.
 */
private class TopAnchoredPagingSource<Value : Any>(private val delegate: PagingSource<Int, Value>) :
  PagingSource<Int, Value>() {
  init {
    // The delegate is the one listening to the notes table, so its invalidation has to become ours;
    // ours has to reach the delegate so it drops that listener rather than leaking it.
    delegate.registerInvalidatedCallback { invalidate() }
    registerInvalidatedCallback { delegate.invalidate() }
  }

  override val jumpingSupported: Boolean
    get() = delegate.jumpingSupported

  override val keyReuseSupported: Boolean
    get() = delegate.keyReuseSupported

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Value> = delegate.load(params)

  override fun getRefreshKey(state: PagingState<Int, Value>): Int? = null
}
