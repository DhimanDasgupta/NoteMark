package com.dhimandasgupta.notemark.data.remote.api

import com.dhimandasgupta.notemark.data.local.datasource.UserDataSource
import com.dhimandasgupta.notemark.data.remote.model.AuthResponse
import com.dhimandasgupta.notemark.data.remote.model.LoginRequest
import com.dhimandasgupta.notemark.data.remote.model.Note
import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.data.remote.model.RegisterRequest
import com.dhimandasgupta.notemark.data.toNote
import com.dhimandasgupta.notemark.database.NoteEntity
import com.dhimandasgupta.notemark.proto.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import kotlin.uuid.ExperimentalUuidApi

class UserAlreadyExistsException(
    message: String = "User already exists"
) : Exception(message)

class ApiGenericException(
    message: String = "Something went wrong with the API request",
    cause: Throwable? = null
) : Exception(message, cause)

class AuthenticationException(
    message: String = "Authentication failed",
    cause: Throwable? = null
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
        noteEntity: NoteEntity
    ): Result<Note>

    suspend fun deleteNote(noteEntity: NoteEntity): Result<Unit>
}

class NoteMarkApiImpl(
    val client: HttpClient,
    private val userDataSource: UserDataSource
) : NoteMarkApi {
    override suspend fun register(request: RegisterRequest): Result<Unit> {
        return try {
            val response = client.post {
                url { "/api/auth/register" }
                contentType(type = ContentType.Application.Json)
                setBody(request)
            }

            // HttpResponseValidator should ideally handle non-2xx responses by throwing.
            // If it doesn't, or you want more specific handling here:
            when (response.status) {
                HttpStatusCode.OK -> Result.success(value = Unit)
                HttpStatusCode.Conflict -> Result.failure(UserAlreadyExistsException())
                // Consider handling other specific statuses like BadRequest, Unauthorized, etc.
                else -> Result.failure(exception = ApiGenericException(message = "Registration failed with status: ${response.status.value}"))
            }
        } catch (e: AuthenticationException) {
            Result.failure(
                exception = AuthenticationException(
                    message = "Authentication failed: ${e.message}",
                    e
                )
            )
        } catch (e: ClientRequestException) { // Ktor exception for 4xx/5xx
            coroutineContext.ensureActive()
            when (e.response.status) {
                HttpStatusCode.Conflict -> Result.failure(exception = UserAlreadyExistsException())
                HttpStatusCode.Unauthorized -> Result.failure(
                    exception = AuthenticationException(
                        message = "Authentication failed: ${e.message}",
                        cause = e
                    )
                )

                else -> Result.failure(
                    exception = ApiGenericException(
                        message = "Registration failed: ${e.message}",
                        cause = e
                    )
                )
            }
        } catch (e: Exception) { // Catch other potential exceptions (network, serialization)
            coroutineContext.ensureActive()
            Result.failure(
                exception = ApiGenericException(
                    message = "An unexpected error occurred during registration",
                    cause = e
                )
            )
        }
    }

    override suspend fun login(request: LoginRequest): Result<Unit> {
        return try {
            val response = client.post {
                url(urlString = "/api/auth/login")
                contentType(type = ContentType.Application.Json)
                setBody(request)
            } // Ktor will throw for non-2xx if not handled by HttpResponseValidator

            // HttpResponseValidator should ideally handle non-2xx responses by throwing.
            // If it doesn't, or you want more specific handling here:
            when (response.status) {
                HttpStatusCode.OK -> {
                    val authResponse = response.body<AuthResponse>()
                    // Save user only after successful response parsing
                    userDataSource.saveUser(
                        user = User.newBuilder().apply {
                            userName = authResponse.username
                            accessToken = authResponse.accessToken
                            refreshToken = authResponse.refreshToken
                        }.build()
                    )
                    Result.success(value = Unit)
                }

                else -> Result.failure(exception = ApiGenericException(message = "Registration failed with status: ${response.status.value}"))
            }
        } catch (e: AuthenticationException) {
            Result.failure(
                exception = AuthenticationException(
                    message = "Authentication failed: ${e.message}",
                    cause = e
                )
            )
        } catch (e: ClientRequestException) { // Ktor exception for 4xx/5xx
            coroutineContext.ensureActive()
            when (e.response.status) {
                HttpStatusCode.Unauthorized -> Result.failure(
                    exception = AuthenticationException(
                        message = "Authentication failed: ${e.message}",
                        cause = e
                    )
                )

                else -> Result.failure(
                    exception = ApiGenericException(
                        message = "Login failed: ${e.message}",
                        cause = e
                    )
                )
            }
        } catch (e: Exception) { // Catch other potential exceptions (network, serialization)
            coroutineContext.ensureActive()
            Result.failure(
                exception = ApiGenericException(
                    message = "An unexpected error occurred during registration",
                    cause = e
                )
            )
        }
    }

    override suspend fun getNotes(page: Int, size: Int): Result<NoteResponse> {
        return try {
            val response = client.get {
                url(urlString = "/api/notes")
                contentType(type = ContentType.Application.Json)
                parameter("page", page)
                parameter("size", size)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val noteResponse = response.body<NoteResponse>()
                    Result.success(value = noteResponse)
                }

                else -> Result.failure(exception = ApiGenericException(message = "Registration failed with status: ${response.status.value}"))
            }
        } catch (e: AuthenticationException) {
            Result.failure(
                exception = AuthenticationException(
                    message = "Authentication failed: ${e.message}",
                    cause = e
                )
            )
        } catch (e: ClientRequestException) {
            coroutineContext.ensureActive()
            when (e.response.status) {
                HttpStatusCode.Unauthorized -> Result.failure(
                    exception = AuthenticationException(
                        message = "Authentication failed: ${e.message}",
                        cause = e
                    )
                )

                else -> Result.failure(
                    exception = ApiGenericException(
                        message = "Fetch Notes failed: ${e.message}",
                        cause = e
                    )
                )
            }
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Result.failure(
                exception = ApiGenericException(
                    message = "An unexpected error occurred during registration",
                    cause = e
                )
            )
        }
    }

    override suspend fun logout(request: RefreshRequest): Result<Unit> {
        return try {
            val response = client.post {
                url(urlString = "/api/auth/logout")
                contentType(type = ContentType.Application.Json)
                setBody(request)
            }

            // HttpResponseValidator should ideally handle non-2xx responses by throwing.
            // If it doesn't, or you want more specific handling here:
            when (response.status) {
                HttpStatusCode.OK -> {
                    userDataSource.deleteUser()
                    Result.success(value = Unit)
                }
                // Consider handling other specific statuses like BadRequest, Unauthorized, etc.
                else -> Result.failure(exception = ApiGenericException(message = "Logout failed with status: ${response.status.value}"))
            }
        } catch (e: AuthenticationException) {
            Result.failure(
                exception = AuthenticationException(
                    message = "Authentication failed: ${e.message}",
                    cause = e
                )
            )
        } catch (e: ClientRequestException) { // Ktor exception for 4xx/5xx
            coroutineContext.ensureActive()
            when (e.response.status) {
                HttpStatusCode.Unauthorized -> Result.failure(
                    exception = AuthenticationException(
                        message = "Authentication failed: ${e.message}",
                        cause = e
                    )
                )

                else -> Result.failure(
                    exception = ApiGenericException(
                        message = "Logout failed: ${e.message}",
                        cause = e
                    )
                )
            }
        } catch (e: Exception) { // Catch other potential exceptions (network, serialization)
            coroutineContext.ensureActive()
            Result.failure(
                exception = ApiGenericException(
                    message = "An unexpected error occurred during registration",
                    cause = e
                )
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createNote(noteEntity: NoteEntity): Result<Note> {
        return try {
            val response = client.post {
                url(urlString = "/api/notes")
                contentType(type = ContentType.Application.Json)
                setBody(noteEntity.toNote())
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val note = response.body<Note>()
                    Result.success(value = note)
                }

                else -> Result.failure(exception = ApiGenericException(message = "Create Note failed with status: ${response.status.value}"))
            }
        } catch (e: AuthenticationException) {
            Result.failure(
                exception = AuthenticationException(
                    message = "Authentication failed: ${e.message}",
                    cause = e
                )
            )
        } catch (e: ClientRequestException) {
            coroutineContext.ensureActive()
            when (e.response.status) {
                HttpStatusCode.Unauthorized -> Result.failure(
                    exception = AuthenticationException(
                        message = "Authentication failed: ${e.message}",
                        cause = e
                    )
                )

                else -> Result.failure(
                    exception = ApiGenericException(
                        message = "Create Note failed: ${e.message}",
                        cause = e
                    )
                )
            }
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Result.failure(
                exception = ApiGenericException(
                    message = "An unexpected error occurred during registration",
                    cause = e
                )
            )
        }
    }

    override suspend fun updateNote(
        title: String,
        content: String,
        lastEditedAt: String,
        noteEntity: NoteEntity
    ): Result<Note> {
        return try {
            val response = client.put {
                url(urlString = "/api/notes")
                contentType(type = ContentType.Application.Json)
                setBody(
                    noteEntity.copy(
                        title = title,
                        content = content,
                        lastEditedAt = lastEditedAt
                    ).toNote()
                )
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val note = response.body<Note>()
                    Result.success(value = note)
                }

                else -> Result.failure(exception = ApiGenericException(message = "Registration failed with status: ${response.status.value}"))
            }
        } catch (e: AuthenticationException) {
            Result.failure(
                exception = AuthenticationException(
                    message = "Authentication failed: ${e.message}",
                    cause = e
                )
            )
        } catch (e: ClientRequestException) {
            coroutineContext.ensureActive()
            when (e.response.status) {
                HttpStatusCode.Unauthorized -> Result.failure(
                    exception = AuthenticationException(
                        message = "Authentication failed: ${e.message}",
                        cause = e
                    )
                )

                else -> Result.failure(
                    exception = ApiGenericException(
                        message = "Update failed: ${e.message}",
                        cause = e
                    )
                )
            }
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Result.failure(
                exception = ApiGenericException(
                    message = "An unexpected error occurred during registration",
                    cause = e
                )
            )
        }
    }

    override suspend fun deleteNote(noteEntity: NoteEntity): Result<Unit> {
        return try {
            val response = client.delete {
                url(urlString = "/api/notes/${noteEntity.uuid}")
                contentType(type = ContentType.Application.Json)
            }

            when (response.status) {
                HttpStatusCode.OK -> Result.success(value = Unit)
                else -> Result.failure(exception = ApiGenericException(message = "Registration failed with status: ${response.status.value}"))
            }
        } catch (e: AuthenticationException) {
            Result.failure(
                exception = AuthenticationException(
                    message = "Authentication failed: ${e.message}",
                    cause = e
                )
            )
        } catch (e: ClientRequestException) {
            coroutineContext.ensureActive()
            when (e.response.status) {
                HttpStatusCode.Unauthorized -> Result.failure(
                    exception = AuthenticationException(
                        message = "Authentication failed: ${e.message}",
                        cause = e
                    )
                )

                else -> Result.failure(
                    exception = ApiGenericException(
                        message = "Delete Note failed: ${e.message}",
                        cause = e
                    )
                )
            }
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Result.failure(
                exception = ApiGenericException(
                    message = "An unexpected error occurred during registration",
                    cause = e
                )
            )
        }
    }
}