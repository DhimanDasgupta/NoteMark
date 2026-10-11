package com.dhimandasgupta.notemark.data.remote.client

import com.dhimandasgupta.notemark.data.repository.UserRepository
import com.dhimandasgupta.notemark.proto.User
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.io.IOException
import java.util.Collections
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

private const val BACKGROUND_THREAD = "app-bg-test"

class NoteMarkHttpClientFactoryTest {
  private val dispatcher: ExecutorCoroutineDispatcher =
    Executors.newSingleThreadExecutor { runnable -> Thread(runnable, BACKGROUND_THREAD) }
      .asCoroutineDispatcher()

  private val userRepository = RecordingUserRepository()
  private val refreshCalls = AtomicInteger()
  private val protectedAuthHeaders: MutableList<String?> =
    Collections.synchronizedList(mutableListOf())

  @After
  fun tearDown() {
    dispatcher.close()
  }

  @Test
  fun `sends no authorization header without a stored user`() = runTest {
    val client = client { respondOk() }

    client.get(urlString = "/api/notes")

    assertEquals(listOf<String?>(null), protectedAuthHeaders.toList())
  }

  @Test
  fun `sends the stored access token`() = runTest {
    userRepository.signIn(accessToken = "access", refreshToken = "refresh")
    val client = client { respondOk() }

    client.get(urlString = "/api/notes")

    assertEquals(listOf<String?>("Bearer access"), protectedAuthHeaders.toList())
  }

  @Test
  fun `refreshes on 401, saves the new tokens and retries`() = runTest {
    userRepository.signIn(accessToken = "old", refreshToken = "refresh")
    val client = client { request ->
      when (request.authHeader()) {
        "Bearer new" -> respondOk()
        else -> respondUnauthorized()
      }
    }

    val response = client.get(urlString = "/api/notes")

    assertEquals(HttpStatusCode.OK, response.status)
    assertEquals(listOf<String?>("Bearer old", "Bearer new"), protectedAuthHeaders.toList())
    assertEquals("new", userRepository.user.value?.accessToken)
    assertEquals("new-refresh", userRepository.user.value?.refreshToken)
    assertEquals(0, userRepository.deleteCount.get())
  }

  @Test
  fun `deletes the user when the refresh token is rejected`() = runTest {
    userRepository.signIn(accessToken = "old", refreshToken = "refresh")
    val client = client(refresh = { respondUnauthorized() }) { respondUnauthorized() }

    val response = client.get(urlString = "/api/notes")

    assertEquals(HttpStatusCode.Unauthorized, response.status)
    assertEquals(1, userRepository.deleteCount.get())
    assertNull(userRepository.user.value)
  }

  @Test
  fun `keeps the user signed in when refresh fails on the network`() = runTest {
    userRepository.signIn(accessToken = "old", refreshToken = "refresh")
    val client = client(refresh = { throw IOException("offline") }) { respondUnauthorized() }

    val response = client.get(urlString = "/api/notes")

    assertEquals(HttpStatusCode.Unauthorized, response.status)
    assertEquals(0, userRepository.deleteCount.get())
    assertEquals("old", userRepository.user.value?.accessToken)
  }

  @Test
  fun `keeps the user signed in when refresh fails on the server`() = runTest {
    userRepository.signIn(accessToken = "old", refreshToken = "refresh")
    val client =
      client(refresh = { respondError(status = HttpStatusCode.InternalServerError) }) {
        respondUnauthorized()
      }

    client.get(urlString = "/api/notes")

    assertEquals(0, userRepository.deleteCount.get())
    assertEquals("old", userRepository.user.value?.accessToken)
  }

  @Test
  fun `stops sending the token after logout`() = runTest {
    userRepository.signIn(accessToken = "access", refreshToken = "refresh")
    val client = client { respondOk() }

    client.get(urlString = "/api/notes")
    userRepository.user.value = null
    client.get(urlString = "/api/notes")

    assertEquals(listOf("Bearer access", null), protectedAuthHeaders.toList())
  }

  @Test
  fun `concurrent 401s trigger a single refresh`() = runTest {
    userRepository.signIn(accessToken = "old", refreshToken = "refresh")
    val bothRejected = CompletableDeferred<Unit>()
    val rejected = AtomicInteger()
    val client = client { request ->
      when (request.authHeader()) {
        "Bearer new" -> respondOk()
        else -> {
          // Hold the first 401 until the second request has also been rejected.
          if (rejected.incrementAndGet() == 2) bothRejected.complete(Unit)
          bothRejected.await()
          respondUnauthorized()
        }
      }
    }

    val responses = List(2) { async { client.get(urlString = "/api/notes") } }.awaitAll()

    assertTrue(responses.all { response -> response.status == HttpStatusCode.OK })
    assertEquals(1, refreshCalls.get())
  }

  @Test
  fun `401 for a token already rotated elsewhere retries without refreshing again`() = runTest {
    userRepository.signIn(accessToken = "old", refreshToken = "refresh")
    val client = client { request ->
      when (request.authHeader()) {
        "Bearer new" -> respondOk()
        else -> {
          // Another request finishes its refresh while this one is still in flight.
          userRepository.signIn(accessToken = "new", refreshToken = "new-refresh")
          respondUnauthorized()
        }
      }
    }

    val response = client.get(urlString = "/api/notes")

    assertEquals(HttpStatusCode.OK, response.status)
    assertEquals(0, refreshCalls.get())
    assertEquals(listOf<String?>("Bearer old", "Bearer new"), protectedAuthHeaders.toList())
  }

  @Test
  fun `token load and refresh run on the app background dispatcher`() = runTest {
    userRepository.signIn(accessToken = "old", refreshToken = "refresh")
    val client = client(refresh = { respondUnauthorized() }) { respondUnauthorized() }

    client.get(urlString = "/api/notes")

    val threads = userRepository.threads.toList()
    // getUser for load, refresh and the retry check, plus deleteUser.
    assertTrue(threads.isNotEmpty())
    assertEquals(1, userRepository.deleteCount.get())
    assertEquals(setOf(BACKGROUND_THREAD), threads.toSet())
  }

  @Test
  fun `token save after refresh runs on the app background dispatcher`() = runTest {
    userRepository.signIn(accessToken = "old", refreshToken = "refresh")
    val client = client { request ->
      if (request.authHeader() == "Bearer new") respondOk() else respondUnauthorized()
    }

    client.get(urlString = "/api/notes")

    assertEquals(1, userRepository.saveCount.get())
    assertEquals(setOf(BACKGROUND_THREAD), userRepository.threads.toSet())
  }

  private fun client(
    refresh: suspend MockRequestHandleScope.() -> HttpResponseData = {
      respond(
        content = """{"accessToken":"new","refreshToken":"new-refresh"}""",
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
      )
    },
    protected: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
  ): HttpClient {
    val engine = MockEngine { request ->
      if (request.url.encodedPath == "/api/auth/refresh") {
        refreshCalls.incrementAndGet()
        refresh()
      } else {
        protectedAuthHeaders += request.authHeader()
        protected(request)
      }
    }
    return NoteMarkHttpClientFactory(
        engine = engine,
        config =
          NoteMarkHttpConfig(
            baseUrl = "https://example.com",
            userEmailHeader = "",
            isDebug = false,
          ),
        dispatcher = dispatcher,
        userRepository = lazy { userRepository },
      )
      .create()
  }
}

private fun HttpRequestData.authHeader(): String? = headers[HttpHeaders.Authorization]

private fun MockRequestHandleScope.respondOk() = respond(content = "", status = HttpStatusCode.OK)

private fun MockRequestHandleScope.respondUnauthorized() =
  respond(content = "", status = HttpStatusCode.Unauthorized)

private class RecordingUserRepository : UserRepository {
  val user = MutableStateFlow<User?>(value = null)
  val threads = ConcurrentLinkedQueue<String>()
  val saveCount = AtomicInteger()
  val deleteCount = AtomicInteger()

  fun signIn(accessToken: String, refreshToken: String) {
    user.value =
      User.newBuilder()
        .setUserName("user")
        .setAccessToken(accessToken)
        .setRefreshToken(refreshToken)
        .build()
  }

  override fun getUser(): Flow<User?> = flow {
    recordThread()
    emit(user.value)
  }

  override suspend fun saveUser(user: User) {
    this.user.value = user
  }

  override suspend fun saveBearToken(token: BearerTokens) {
    recordThread()
    saveCount.incrementAndGet()
    user.value =
      user.value
        ?.toBuilder()
        ?.setAccessToken(token.accessToken)
        ?.setRefreshToken(token.refreshToken.orEmpty())
        ?.build()
  }

  override suspend fun deleteUser() {
    recordThread()
    deleteCount.incrementAndGet()
    user.value = null
  }

  override suspend fun reset() {
    user.value = null
  }

  private fun recordThread() {
    threads += Thread.currentThread().name
  }
}
