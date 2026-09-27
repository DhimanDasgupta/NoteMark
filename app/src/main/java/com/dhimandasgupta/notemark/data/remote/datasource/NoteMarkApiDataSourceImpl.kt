package com.dhimandasgupta.notemark.data.remote.datasource

import com.dhimandasgupta.notemark.data.remote.api.NoteMarkApi
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.database.NoteEntity
import dev.zacsweers.metro.Inject

@Inject
class NoteMarkApiDataSourceImpl(private val noteMarkApi: Lazy<NoteMarkApi>) :
  NoteMarkApiDataSource {
  override suspend fun getAllNotes(page: Int, size: Int) =
    noteMarkApi.value.getNotes(page = page, size = size)

  override suspend fun createNote(noteEntity: NoteEntity) =
    noteMarkApi.value.createNote(noteEntity = noteEntity)

  override suspend fun updateNote(
    title: String,
    content: String,
    lastEditedAt: String,
    noteEntity: NoteEntity,
  ) =
    noteMarkApi.value.updateNote(
      title = title,
      content = content,
      lastEditedAt = lastEditedAt,
      noteEntity = noteEntity,
    )

  override suspend fun deleteNote(noteEntity: NoteEntity) =
    noteMarkApi.value.deleteNote(noteEntity = noteEntity)

  override suspend fun logout(request: RefreshRequest): Result<Unit> =
    noteMarkApi.value.logout(request = request)
}
