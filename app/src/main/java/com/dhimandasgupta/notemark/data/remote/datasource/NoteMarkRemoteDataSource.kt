package com.dhimandasgupta.notemark.data.remote.datasource

import com.dhimandasgupta.notemark.data.remote.api.NoteMarkApi
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
        noteEntity: NoteEntity
    ): Result<Note>

    suspend fun deleteNote(noteEntity: NoteEntity): Result<Unit>
    suspend fun logout(request: RefreshRequest): Result<Unit>
}

class NoteMarkApiDataSourceImpl(
    private val noteMarkApi: NoteMarkApi
) : NoteMarkApiDataSource {
    override suspend fun getAllNotes(page: Int, size: Int) =
        noteMarkApi.getNotes(page = page, size = size)

    override suspend fun createNote(noteEntity: NoteEntity) =
        noteMarkApi.createNote(noteEntity = noteEntity)

    override suspend fun updateNote(
        title: String,
        content: String,
        lastEditedAt: String,
        noteEntity: NoteEntity
    ) = noteMarkApi.updateNote(
        title = title,
        content = content,
        lastEditedAt = lastEditedAt,
        noteEntity = noteEntity
    )

    override suspend fun deleteNote(noteEntity: NoteEntity) =
        noteMarkApi.deleteNote(noteEntity = noteEntity)

    override suspend fun logout(request: RefreshRequest): Result<Unit> =
        noteMarkApi.logout(request = request)
}