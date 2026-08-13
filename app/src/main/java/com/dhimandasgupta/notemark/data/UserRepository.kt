package com.dhimandasgupta.notemark.data

import com.dhimandasgupta.notemark.data.local.datasource.UserDataSource
import com.dhimandasgupta.notemark.proto.User
import dev.zacsweers.metro.Inject
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.Flow

interface UserRepository {
  fun getUser(): Flow<User?>

  suspend fun saveUser(user: User)

  suspend fun saveBearToken(token: BearerTokens)

  suspend fun deleteUser()

  suspend fun reset()
}

@Inject
class UserRepositoryImpl(private val userDataSource: Lazy<UserDataSource>) : UserRepository {
  override fun getUser(): Flow<User?> = userDataSource.value.getUser()

  override suspend fun saveUser(user: User) = userDataSource.value.saveUser(user = user)

  override suspend fun saveBearToken(token: BearerTokens) =
    userDataSource.value.saveBearToken(token = token)

  override suspend fun deleteUser() = userDataSource.value.deleteUser()

  override suspend fun reset() = userDataSource.value.reset()
}
