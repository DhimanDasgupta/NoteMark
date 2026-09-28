package com.dhimandasgupta.notemark.data

import com.dhimandasgupta.notemark.data.local.datasource.UserDataSource
import com.dhimandasgupta.notemark.proto.User
import dev.zacsweers.metro.Inject
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.Flow

@Inject
class UserRepositoryImpl(userDataSource: Lazy<UserDataSource>) : UserRepository {
  private val userDataSource by userDataSource

  override fun getUser(): Flow<User?> = userDataSource.getUser()

  override suspend fun saveUser(user: User) = userDataSource.saveUser(user = user)

  override suspend fun saveBearToken(token: BearerTokens) =
    userDataSource.saveBearToken(token = token)

  override suspend fun deleteUser() = userDataSource.deleteUser()

  override suspend fun reset() = userDataSource.reset()
}
