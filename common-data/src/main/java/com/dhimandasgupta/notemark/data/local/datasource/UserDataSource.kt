package com.dhimandasgupta.notemark.data.local.datasource

import com.dhimandasgupta.notemark.proto.User
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.Flow

interface UserDataSource {
  fun getUser(): Flow<User?>

  suspend fun saveUser(user: User)

  suspend fun saveBearToken(token: BearerTokens)

  suspend fun deleteUser()

  suspend fun reset()
}
