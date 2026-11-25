package com.dhimandasgupta.notemark.app.nav

import androidx.navigation3.runtime.NavKey
import com.dhimandasgupta.notemark.common.navigation.LauncherNavKey
import com.dhimandasgupta.notemark.common.navigation.LoginNavKey
import com.dhimandasgupta.notemark.common.navigation.NoteCreateNavKey
import com.dhimandasgupta.notemark.common.navigation.NoteListNavKey
import com.dhimandasgupta.notemark.common.navigation.RegistrationNavKey
import com.dhimandasgupta.notemark.data.repository.UserRepository
import com.dhimandasgupta.notemark.proto.User
import io.ktor.client.plugins.auth.providers.BearerTokens
import java.io.IOException
import kotlin.test.assertEquals
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeepLinkLoginGuardTest {
  private val newNote = persistentListOf<NavKey>(NoteListNavKey, NoteCreateNavKey)
  private val login = persistentListOf<NavKey>(LauncherNavKey, LoginNavKey)

  @Test
  fun `logged in user gets the deep link`() = runTest {
    assertEquals(newNote, guard(flowOf(User.getDefaultInstance())).guard(newNote))
  }

  @Test
  fun `logged out user is sent to login`() = runTest {
    assertEquals(login, guard(flowOf(null)).guard(newNote))
  }

  @Test
  fun `unreadable user is sent to login`() = runTest {
    assertEquals(login, guard(flow { throw IOException() }).guard(newNote))
  }

  @Test
  fun `deep link that needs no login is kept for a logged out user`() = runTest {
    val registration = persistentListOf<NavKey>(LauncherNavKey, RegistrationNavKey)

    assertEquals(registration, guard(flowOf(null)).guard(registration))
  }

  private fun guard(user: Flow<User?>) = DeepLinkLoginGuard(StubUserRepository(user))
}

private class StubUserRepository(private val user: Flow<User?>) : UserRepository {
  override fun getUser(): Flow<User?> = user

  override suspend fun saveUser(user: User) = Unit

  override suspend fun saveBearToken(token: BearerTokens) = Unit

  override suspend fun deleteUser() = Unit

  override suspend fun reset() = Unit
}
