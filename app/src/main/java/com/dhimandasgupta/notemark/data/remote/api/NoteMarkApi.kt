package com.dhimandasgupta.notemark.data.remote.api

import LoggedInUser
import UserManager
import com.dhimandasgupta.notemark.BuildConfig
import com.dhimandasgupta.notemark.data.remote.model.AuthResponse
import com.dhimandasgupta.notemark.data.remote.model.LoginRequest
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.data.remote.model.RegisterRequest
import com.dhimandasgupta.notemark.database.NoteEntity
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlin.coroutines.coroutineContext

interface NoteMarkApi {
    val client: HttpClient // Optional: See note above

    /** Auth Purpose methods */
    suspend fun register(request: RegisterRequest): Result<Unit>
    suspend fun login(request: LoginRequest): Result<Unit>
    suspend fun logout()

    /** CRUD purpose methods */
    suspend fun getNotes(pageNumber: Int = -1, pageSize: Int = 20): List<NoteEntity>
    suspend fun createNote(noteEntity: NoteEntity): NoteEntity
    suspend fun updateNote(title: String, content: String, lastEditedAt: String, noteEntity: NoteEntity): NoteEntity
    suspend fun deleteNote(noteEntity: NoteEntity): HttpStatusCode
}

class NoteMarkApiImpl(
    private val userManager: UserManager
) : NoteMarkApi {
    override val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
        }

        install(Auth) {
            bearer {
                loadTokens {
                    userManager.getUser().first()?.bearerTokens
                }

                refreshTokens {
                    val currentTokens = userManager.getUser().first()?.bearerTokens
                    if (currentTokens == null) {
                        return@refreshTokens null
                    }

                    val response: AuthResponse = client.post("/api/auth/refresh") {
                        contentType(ContentType.Application.Json)
                        setBody(RefreshRequest(refreshToken = currentTokens.refreshToken ?: ""))
                        // Optional: uncomment for easier testing
                        // header("Debug", "true")
                    }.body()

                    val newTokens = BearerTokens(response.accessToken, response.refreshToken)
                    userManager.saveUser(
                        loggerInUser = LoggedInUser(
                            userName = response.userName,
                            bearerTokens = newTokens
                        )
                    )
                    newTokens
                }
            }
        }

        // Default request configuration for all calls made with this client
        defaultRequest {
            url(BASE_URL)
            header("X-User-Email", YOUR_DEV_CAMPUS_EMAIL)
            header("Debug", "true")
        }
    }

    override suspend fun register(request: RegisterRequest): Result<Unit> {
        return try {
            val response = client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    Result.success(Unit)
                }
                HttpStatusCode.Conflict -> {
                    Result.failure(Exception("User already exists"))
                }
                else -> {
                    Result.failure(Exception("Something went wrong"))
                }
            }
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Result.failure(e)
        }
    }

    override suspend fun login(request: LoginRequest): Result<Unit> {
        return try {
            val response: AuthResponse = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            val tokens = BearerTokens(response.accessToken, response.refreshToken)
            userManager.saveUser(
                loggerInUser = LoggedInUser(
                    userName = response.userName,
                    bearerTokens = tokens
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        userManager.clearUser()
    }

    override suspend fun getNotes(
        pageNumber: Int,
        pageSize: Int
    ): List<NoteEntity>  = client.get("/api/notes").body<List<NoteEntity>>()

    override suspend fun createNote(noteEntity: NoteEntity) = client.post("/api/notes") {
        contentType(ContentType.Application.Json)
        setBody(noteEntity)
    }.body<NoteEntity>()

    override suspend fun updateNote(
        title: String,
        content: String,
        lastEditedAt: String,
        noteEntity: NoteEntity
    ) = client.put("/api/notes") {
        contentType(ContentType.Application.Json)
        setBody(noteEntity.copy(title = title, content = content, lastEditedAt = lastEditedAt))
    }.body<NoteEntity>()

    override suspend fun deleteNote(noteEntity: NoteEntity) =  client.delete("/api/notes/${noteEntity.id}").status

    companion object {
        private const val BASE_URL = "https://notemark.pl-coding.com"
        private const val YOUR_DEV_CAMPUS_EMAIL = BuildConfig.HEADER_VALUE_FOR_NOTE_MARK_API
    }
}