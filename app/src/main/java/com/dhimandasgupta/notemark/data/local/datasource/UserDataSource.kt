package com.dhimandasgupta.notemark.data.local.datasource

import androidx.datastore.core.DataStore
import com.dhimandasgupta.notemark.proto.User
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface UserDataSource {
    fun getUser(): Flow<User?>
    suspend fun saveUser(user: User)
    suspend fun saveBearToken(token: BearerTokens)
    suspend fun deleteUser()
}

class UserDataSourceImpl(
    private val userDataStore: DataStore<User>
) : UserDataSource {

    override fun getUser(): Flow<User?> = userDataStore.data.map { user ->
        if (user.userName.isNotEmpty() && user.accessToken.isNotEmpty())
            user
        else
            null
    }

    override suspend fun saveUser(user: User) {
        userDataStore.updateData { transform ->
            transform.toBuilder()
                .setUserName(user.userName)
                .setAccessToken(user.accessToken)
                .setRefreshToken(user.refreshToken)
                .build()

        }
    }

    override suspend fun saveBearToken(token: BearerTokens) {
        userDataStore.updateData { transform ->
            transform.toBuilder()
                .setAccessToken(token.accessToken)
                .setRefreshToken(token.refreshToken)
                .build()
        }
    }

    override suspend fun deleteUser() {
        userDataStore.updateData { transform ->
            transform.toBuilder().clear().build()
        }
    }
}