package com.dhimandasgupta.notemark.data.local.datasource

import androidx.datastore.core.DataStore
import com.dhimandasgupta.notemark.app.di.UserDataStore
import com.dhimandasgupta.notemark.proto.User
import dev.zacsweers.metro.Inject
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface UserDataSource {
  fun getUser(): Flow<User?>

  suspend fun saveUser(user: User)

  suspend fun saveBearToken(token: BearerTokens)

  suspend fun deleteUser()

  suspend fun reset()
}

@Inject
class UserDataSourceImpl(@UserDataStore private val userDataStore: Lazy<DataStore<User>>) :
  UserDataSource {

  override fun getUser(): Flow<User?> =
    userDataStore.value.data.map { user ->
      if (user.userName.isNotEmpty() && user.accessToken.isNotEmpty()) user else null
    }

  override suspend fun saveUser(user: User) {
    userDataStore.value.updateData { transform ->
      transform
        .toBuilder()
        .setUserName(user.userName)
        .setAccessToken(user.accessToken)
        .setRefreshToken(user.refreshToken)
        .build()
    }
  }

  override suspend fun saveBearToken(token: BearerTokens) {
    userDataStore.value.updateData { transform ->
      transform
        .toBuilder()
        .setAccessToken(token.accessToken)
        .setRefreshToken(token.refreshToken)
        .build()
    }
  }

  override suspend fun deleteUser() {
    userDataStore.value.updateData { transform ->
      transform.toBuilder().clear().build()
    }
  }

  override suspend fun reset() {
    userDataStore.value.updateData { transform ->
      transform.toBuilder().clear().build()
    }
  }
}
