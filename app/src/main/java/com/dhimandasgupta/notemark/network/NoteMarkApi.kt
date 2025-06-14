package com.dhimandasgupta.notemark.network

import com.dhimandasgupta.notemark.BuildConfig
import com.dhimandasgupta.notemark.network.model.AuthResponse
import com.dhimandasgupta.notemark.network.model.LoginRequest
import com.dhimandasgupta.notemark.network.model.RefreshRequest
import com.dhimandasgupta.notemark.network.model.RegisterRequest
import com.dhimandasgupta.notemark.network.storage.TokenManager
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

class NoteMarkApi(
    private val tokenManager: TokenManager
) {
    val client = HttpClient(Android) {
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
                    tokenManager.getToken().first()
                }

                refreshTokens {
                    val currentTokens = tokenManager.getToken().first()
                    if (currentTokens == null) {
                        return@refreshTokens null
                    }

                    val response: AuthResponse = client.post("/api/auth/refresh") {
                        contentType(ContentType.Application.Json)
                        setBody(RefreshRequest(refreshToken = currentTokens.refreshToken!!))
                        // Optional: uncomment for easier testing
                        // header("Debug", "true")
                    }.body()

                    val newTokens = BearerTokens(response.accessToken, response.refreshToken)
                    tokenManager.saveToken(newTokens)
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

    suspend fun register(request: RegisterRequest): Result<Unit> {
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

    suspend fun login(request: LoginRequest): Result<Unit> {
        return try {
            val response: AuthResponse = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            val tokens = BearerTokens(response.accessToken, response.refreshToken)
            tokenManager.saveToken(tokens)
            Result.success(Unit)
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Result.failure(e)
        }
    }

    suspend fun logout() {
        tokenManager.clearToken()
    }

    companion object {
        private const val BASE_URL = "https://notemark.pl-coding.com"
        private const val YOUR_DEV_CAMPUS_EMAIL = BuildConfig.HEADER_VALUE_FOR_NOTE_MARK_API
    }
}