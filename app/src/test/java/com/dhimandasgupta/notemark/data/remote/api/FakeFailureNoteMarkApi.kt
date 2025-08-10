package com.dhimandasgupta.notemark.data.remote.api

import com.dhimandasgupta.notemark.data.remote.model.LoginRequest
import com.dhimandasgupta.notemark.data.remote.model.Note
import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.data.remote.model.RegisterRequest
import com.dhimandasgupta.notemark.database.NoteEntity

class FakeFailureNoteMarkApi : NoteMarkApi {
    override suspend fun register(request: RegisterRequest): Result<Unit> =
        Result.failure(exception = Exception("Something went wrong"))

    override suspend fun login(request: LoginRequest): Result<Unit> =
        Result.failure(exception = Exception("Something went wrong"))

    override suspend fun getNotes(page: Int, size: Int): Result<NoteResponse> =
        Result.failure(exception = Exception("Something went wrong"))

    override suspend fun logout(request: RefreshRequest): Result<Unit> =
        Result.failure(exception = Exception("Something went wrong"))

    override suspend fun createNote(noteEntity: NoteEntity): Result<Note> =
        Result.failure(exception = Exception("Something went wrong"))

    override suspend fun updateNote(
        title: String,
        content: String,
        lastEditedAt: String,
        noteEntity: NoteEntity
    ): Result<Note> = Result.failure(exception = Exception("Something went wrong"))

    override suspend fun deleteNote(noteEntity: NoteEntity): Result<Unit> =
        Result.failure(exception = Exception("Something went wrong"))
}