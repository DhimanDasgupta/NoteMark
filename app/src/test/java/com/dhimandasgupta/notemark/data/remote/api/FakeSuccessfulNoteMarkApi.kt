package com.dhimandasgupta.notemark.data.remote.api

import com.dhimandasgupta.notemark.data.remote.model.LoginRequest
import com.dhimandasgupta.notemark.data.remote.model.Note
import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.data.remote.model.RegisterRequest
import com.dhimandasgupta.notemark.database.NoteEntity

class FakeSuccessfulNoteMarkApi : NoteMarkApi {
    override suspend fun register(request: RegisterRequest): Result<Unit> =
        Result.success(value = Unit)

    override suspend fun login(request: LoginRequest): Result<Unit> =
        Result.success(value = Unit)

    override suspend fun getNotes(page: Int, size: Int): Result<NoteResponse> =
        Result.success(value = noteResponse)

    override suspend fun logout(request: RefreshRequest): Result<Unit> =
        Result.success(value = Unit)

    override suspend fun createNote(noteEntity: NoteEntity): Result<Note> =
        Result.success(value = note)

    override suspend fun updateNote(
        title: String,
        content: String,
        lastEditedAt: String,
        noteEntity: NoteEntity
    ): Result<Note> = Result.success(value = note)

    override suspend fun deleteNote(noteEntity: NoteEntity): Result<Unit> =
        Result.success(value = Unit)
}

private val noteResponse = NoteResponse(
    notes = listOf(
        Note(
            uuid = "1",
            title = "title",
            content = "content",
            createdAt = "createdAt",
            lastEditedAt = "lastEditedAt"
        )
    ),
    total = 1
)

private val note = Note(
    uuid = "1",
    title = "title",
    content = "content",
    createdAt = "createdAt",
    lastEditedAt = "lastEditedAt"
)