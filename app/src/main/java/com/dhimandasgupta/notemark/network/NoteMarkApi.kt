package com.dhimandasgupta.notemark.network

import com.dhimandasgupta.notemark.network.model.AuthResponse
import com.dhimandasgupta.notemark.network.model.LoginRequest
import com.dhimandasgupta.notemark.network.model.RefreshRequest
import com.dhimandasgupta.notemark.network.model.RegisterRequest
import com.dhimandasgupta.notemark.network.storage.TokenStorage
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
import kotlinx.serialization.json.Json

class NoteMarkApi(
    private val tokenStorage: TokenStorage
) {
    private val authClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json()
        }
        defaultRequest {
            url(BASE_URL)
            header("X-User-Email", YOUR_DEV_CAMPUS_EMAIL)
        }
    }

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
                    tokenStorage.getTokens()
                }

                refreshTokens {
                    val currentTokens = tokenStorage.getTokens()
                    if (currentTokens == null) {
                        return@refreshTokens null
                    }

                    val response: AuthResponse = authClient.post("/api/auth/refresh") {
                        contentType(ContentType.Application.Json)
                        setBody(RefreshRequest(refreshToken = currentTokens.refreshToken!!))
                        // Optional: uncomment for easier testing
                        // header("Debug", "true")
                    }.body()

                    val newTokens = BearerTokens(response.accessToken, response.refreshToken)
                    tokenStorage.saveTokens(newTokens)
                    tokenStorage.saveTokens(newTokens)
                    newTokens
                }
            }
        }

        // Default request configuration for all calls made with this client
        defaultRequest {
            url(BASE_URL)
            header("X-User-Email", YOUR_DEV_CAMPUS_EMAIL)
            // header("Debug", "true")
        }
    }

    suspend fun register(request: RegisterRequest) {
        // Registration is a non-authenticated call
        authClient.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun login(request: LoginRequest): Result<Unit> {
        return try {
            val response: AuthResponse = authClient.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            val tokens = BearerTokens(response.accessToken, response.refreshToken)
            tokenStorage.saveTokens(tokens)
            Result.success(Unit)
        } catch (e: ClientRequestException) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        tokenStorage.clearTokens()
    }

    companion object {
        private const val BASE_URL = "https://notemark.pl-coding.com"
        private const val YOUR_DEV_CAMPUS_EMAIL = "your-email@example.com"
    }
}