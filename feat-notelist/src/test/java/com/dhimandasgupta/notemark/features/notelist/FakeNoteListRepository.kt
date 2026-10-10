package com.dhimandasgupta.notemark.features.notelist

import androidx.paging.PagingData
import com.dhimandasgupta.notemark.data.repository.NoteMarkRepository
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.mockito.kotlin.mock

/**
 * Only the calls the note list makes are implemented. Like the database query, notes marked as
 * deleted drop out of the paged notes.
 */
class FakeNoteListRepository(notes: List<NoteEntity> = emptyList()) :
  NoteMarkRepository by mock<NoteMarkRepository>() {
  val notes = MutableStateFlow(value = notes)
  val markedAsDeleted = mutableListOf<NoteEntity>()

  override fun getPagedNotes(pageSize: Int): Flow<PagingData<NoteEntity>> = notes.map { current ->
    PagingData.from(data = current.filterNot { it.markAsDeleted })
  }

  override suspend fun getNoteByUUID(uuid: String): NoteEntity? =
    notes.value.firstOrNull { note -> note.uuid == uuid }

  override suspend fun markAsDeleted(noteEntity: NoteEntity): Boolean {
    markedAsDeleted += noteEntity
    notes.update { current ->
      current.map { note ->
        if (note.uuid == noteEntity.uuid) note.copy(markAsDeleted = true) else note
      }
    }
    return true
  }
}

fun noteEntity(id: Long, title: String = "Title $id") =
  NoteEntity(
    id = id,
    title = title,
    content = "Content $id",
    createdAt = "2026-10-10T10:00:00Z",
    lastEditedAt = "2026-10-10T10:00:00Z",
    uuid = "uuid-$id",
    synced = true,
    markAsDeleted = false,
  )
