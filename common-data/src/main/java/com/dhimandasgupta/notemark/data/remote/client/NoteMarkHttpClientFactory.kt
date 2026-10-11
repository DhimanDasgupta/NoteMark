package com.dhimandasgupta.notemark.data.remote.client

import com.dhimandasgupta.notemark.core.di.AppBackgroundDispatcher
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.data.remote.model.RefreshResponse
import com.dhimandasgupta.notemark.data.repository.UserRepository
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.RefreshTokensParams
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/** Build-time values the client needs; provided by the app module from its BuildConfig. */
data class NoteMarkHttpConfig(
  val baseUrl: String,
  val userEmailHeader: String,
  val isDebug: Boolean,
)

@Inject
class NoteMarkHttpClientFactory(
  private val engine: HttpClientEngine,
  private val config: NoteMarkHttpConfig,
  @AppBackgroundDispatcher private val dispatcher: CoroutineDispatcher,
  userRepository: Lazy<UserRepository>,
) {
  private val userRepository by userRepository

  fun create(): HttpClient =
    HttpClient(engine = engine) {
      install(plugin = ContentNegotiation) {
        json(
          Json {
            isLenient = true
            ignoreUnknownKeys = true
          }
        )
      }
      install(plugin = Logging) {
        logger = Logger.ANDROID
        level = if (config.isDebug) LogLevel.ALL else LogLevel.NONE
        sanitizeHeader { header -> header == HttpHeaders.Authorization }
      }
      install(plugin = Auth) {
        bearer {
          // Read the tokens from the user store on every request so logout and login take effect
          // immediately, instead of Ktor serving a cached token of the previous session.
          cacheTokens = false
          // Cancelling the request that triggered a refresh must not drop the rotated tokens.
          nonCancellableRefresh = true
          loadTokens { storedTokens() }
          refreshTokens { withContext(context = dispatcher) { refresh() } }
        }
      }
      defaultRequest {
        url(urlString = config.baseUrl)
        header("X-User-Email", config.userEmailHeader)
        header("Debug", config.isDebug.toString())
      }
    }

  private suspend fun storedTokens(): BearerTokens? =
    withContext(context = dispatcher) {
      val user = userRepository.getUser().firstOrNull() ?: return@withContext null
      if (user.accessToken.isEmpty() || user.refreshToken.isEmpty()) return@withContext null
      BearerTokens(accessToken = user.accessToken, refreshToken = user.refreshToken)
    }

  private suspend fun RefreshTokensParams.refresh(): BearerTokens? {
    val current = storedTokens() ?: return null

    // Concurrent 401s refresh one after another; if an earlier one already rotated the tokens, the
    // failed request carried a stale token and the stored one can be retried as is.
    if (response.request.headers[HttpHeaders.Authorization] != "Bearer ${current.accessToken}") {
      return current
    }

    return try {
      val refreshResponse = client.post {
        url(urlString = "/api/auth/refresh")
        markAsRefreshTokenRequest()
        contentType(type = ContentType.Application.Json)
        setBody(RefreshRequest(refreshToken = current.refreshToken.orEmpty()))
      }

      when {
        refreshResponse.status.isSuccess() -> {
          val body = refreshResponse.body<RefreshResponse>()
          val newTokens =
            BearerTokens(accessToken = body.accessToken, refreshToken = body.refreshToken)
          userRepository.saveBearToken(token = newTokens)
          newTokens
        }
        // The server rejected the refresh token: the session is over.
        refreshResponse.status == HttpStatusCode.Unauthorized ||
          refreshResponse.status == HttpStatusCode.Forbidden -> {
          userRepository.deleteUser()
          null
        }
        // Server errors are transient; keep the user signed in.
        else -> null
      }
    } catch (_: Exception) {
      // Network or parsing failures are transient too; never log the user out for them.
      currentCoroutineContext().ensureActive()
      null
    }
  }
}
