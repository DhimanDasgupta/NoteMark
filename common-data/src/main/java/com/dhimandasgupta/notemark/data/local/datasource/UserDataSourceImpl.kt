package com.dhimandasgupta.notemark.data.local.datasource

import androidx.datastore.core.DataStore
import com.dhimandasgupta.notemark.common.extensions.coroutines.runCatchingCancelable
import com.dhimandasgupta.notemark.core.di.UserDataStore
import com.dhimandasgupta.notemark.data.storage.defaultUserValue
import com.dhimandasgupta.notemark.proto.User
import dev.zacsweers.metro.Inject
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
class UserDataSourceImpl(@UserDataStore userDataStore: Lazy<DataStore<User>>) : UserDataSource {
  private val userDataStore by userDataStore

  override fun getUser(): Flow<User?> =
    userDataStore.data.map { user ->
      if (user.userName.isNotEmpty() && user.accessToken.isNotEmpty()) user else null
    }

  override suspend fun saveUser(user: User) {
    runCatchingCancelable {
      userDataStore.updateData { transform ->
        transform
          .toBuilder()
          .setUserName(user.userName)
          .setAccessToken(user.accessToken)
          .setRefreshToken(user.refreshToken)
          .build()
      }
    }
  }

  override suspend fun saveBearToken(token: BearerTokens) {
    runCatchingCancelable {
      userDataStore.updateData { transform ->
        transform
          .toBuilder()
          .setAccessToken(token.accessToken)
          .setRefreshToken(token.refreshToken)
          .build()
      }
    }
  }

  override suspend fun deleteUser() {
    runCatchingCancelable {
      userDataStore.updateData { transform ->
        transform.toBuilder().clear().build()
      }
    }
  }

  override suspend fun reset() {
    runCatchingCancelable {
      userDataStore.updateData { defaultUserValue }
    }
  }
}
