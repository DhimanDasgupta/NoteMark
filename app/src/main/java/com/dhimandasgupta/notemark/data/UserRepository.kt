package com.dhimandasgupta.notemark.data

import com.dhimandasgupta.notemark.data.local.datasource.UserDataSource
import com.dhimandasgupta.notemark.proto.User
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(): Flow<User?>
    suspend fun saveUser(user: User)
    suspend fun saveBearToken(token: BearerTokens)
    suspend fun deleteUser()
    suspend fun reset()
}

class UserRepositoryImpl(
    private val userDataSource: UserDataSource,
) : UserRepository {
    override fun getUser(): Flow<User?> = userDataSource.getUser()

    override suspend fun saveUser(user: User) = userDataSource.saveUser(user = user)

    override suspend fun saveBearToken(token: BearerTokens) =
        userDataSource.saveBearToken(token = token)

    override suspend fun deleteUser() = userDataSource.deleteUser()

    override suspend fun reset() = userDataSource.reset()
}