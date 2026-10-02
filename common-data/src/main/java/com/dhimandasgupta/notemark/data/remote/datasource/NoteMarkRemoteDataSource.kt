package com.dhimandasgupta.notemark.data.remote.datasource

import com.dhimandasgupta.notemark.data.remote.model.Note
import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.database.NoteEntity

interface NoteMarkApiDataSource {
  suspend fun getAllNotes(page: Int = -1, size: Int = 20): Result<NoteResponse>

  suspend fun createNote(noteEntity: NoteEntity): Result<Note>

  suspend fun updateNote(
    title: String,
    content: String,
    lastEditedAt: String,
    noteEntity: NoteEntity,
  ): Result<Note>

  suspend fun deleteNote(noteEntity: NoteEntity): Result<Unit>

  suspend fun logout(request: RefreshRequest): Result<Unit>
}
