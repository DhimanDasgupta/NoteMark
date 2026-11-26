package com.dhimandasgupta.notemark.data.repository

import com.dhimandasgupta.notemark.data.local.datasource.UserDataSource
import com.dhimandasgupta.notemark.proto.User
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.Flow

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class UserRepositoryImpl internal constructor(userDataSource: Lazy<UserDataSource>) :
  UserRepository {
  private val userDataSource by userDataSource

  override fun getUser(): Flow<User?> = userDataSource.getUser()

  override suspend fun saveUser(user: User) = userDataSource.saveUser(user = user)

  override suspend fun saveBearToken(token: BearerTokens) =
    userDataSource.saveBearToken(token = token)

  override suspend fun deleteUser() = userDataSource.deleteUser()

  override suspend fun reset() = userDataSource.reset()
}
