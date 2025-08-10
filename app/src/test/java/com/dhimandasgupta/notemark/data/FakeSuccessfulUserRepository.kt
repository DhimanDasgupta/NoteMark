package com.dhimandasgupta.notemark.data

import com.dhimandasgupta.notemark.proto.User
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSuccessfulUserRepository : UserRepository {
    override fun getUser(): Flow<User?> = flowOf(
        value = User.newBuilder().apply {
            userName = "Some User name"
            accessToken = "Some access token"
            refreshToken = "Some refresh token"
        }.build()
    )

    override suspend fun saveUser(user: User) = Unit

    override suspend fun saveBearToken(token: BearerTokens) = Unit

    override suspend fun deleteUser() = Unit

    override suspend fun reset() = Unit
}