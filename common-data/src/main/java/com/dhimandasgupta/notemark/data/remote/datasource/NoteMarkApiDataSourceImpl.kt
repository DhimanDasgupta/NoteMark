package com.dhimandasgupta.notemark.data.remote.datasource

import com.dhimandasgupta.notemark.data.remote.api.NoteMarkApi
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.database.NoteEntity
import dev.zacsweers.metro.Inject

@Inject
class NoteMarkApiDataSourceImpl(noteMarkApi: Lazy<NoteMarkApi>) : NoteMarkApiDataSource {
  private val noteMarkApi by noteMarkApi

  override suspend fun getAllNotes(page: Int, size: Int) =
    noteMarkApi.getNotes(page = page, size = size)

  override suspend fun createNote(noteEntity: NoteEntity) =
    noteMarkApi.createNote(noteEntity = noteEntity)

  override suspend fun updateNote(
    title: String,
    content: String,
    lastEditedAt: String,
    noteEntity: NoteEntity,
  ) =
    noteMarkApi.updateNote(
      title = title,
      content = content,
      lastEditedAt = lastEditedAt,
      noteEntity = noteEntity,
    )

  override suspend fun deleteNote(noteEntity: NoteEntity) =
    noteMarkApi.deleteNote(noteEntity = noteEntity)

  override suspend fun logout(request: RefreshRequest): Result<Unit> =
    noteMarkApi.logout(request = request)
}
