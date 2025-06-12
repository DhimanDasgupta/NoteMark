package com.dhimandasgupta.notemark.network.storage

import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// This interface is an abstraction for your local storage (DataStore, SharedPreferences, etc.)
interface TokenStorage {
    suspend fun saveTokens(tokens: BearerTokens)
    suspend fun getTokens(): BearerTokens?
    suspend fun clearTokens()
}

// A basic in-memory implementation for demonstration.
// In a real app, replace this with DataStore or encrypted SharedPreferences.
class InMemoryTokenStorage : TokenStorage {
    private var tokens: BearerTokens? = null

    override suspend fun saveTokens(tokens: BearerTokens) {
        this.tokens = tokens
    }

    override suspend fun getTokens(): BearerTokens? {
        return tokens
    }

    override suspend fun clearTokens() {
        tokens = null
    }
}