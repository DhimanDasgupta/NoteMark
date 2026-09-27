package com.dhimandasgupta.notemark.data.remote.api

import com.dhimandasgupta.notemark.data.remote.model.LoginRequest
import com.dhimandasgupta.notemark.data.remote.model.Note
import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.data.remote.model.RegisterRequest
import com.dhimandasgupta.notemark.database.NoteEntity

class UserAlreadyExistsException(message: String = "User already exists") : Exception(message)

class ApiGenericException(
  message: String = "Something went wrong with the API request",
  cause: Throwable? = null,
) : Exception(message, cause)

class AuthenticationException(
  message: String = "Authentication failed",
  cause: Throwable? = null,
) : Exception(message, cause)

interface NoteMarkApi {
  /** Auth Purpose methods */
  suspend fun register(request: RegisterRequest): Result<Unit>

  suspend fun login(request: LoginRequest): Result<Unit>

  suspend fun logout(request: RefreshRequest): Result<Unit>

  /** CRUD purpose methods */
  suspend fun getNotes(page: Int = -1, size: Int = 20): Result<NoteResponse>

  suspend fun createNote(noteEntity: NoteEntity): Result<Note>

  suspend fun updateNote(
    title: String,
    content: String,
    lastEditedAt: String,
    noteEntity: NoteEntity,
  ): Result<Note>

  suspend fun deleteNote(noteEntity: NoteEntity): Result<Unit>
}
