package com.dhimandasgupta.notemark.network.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_tokens")

class TokenManager(private val context: Context) {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }

    /**
     * Saves the Bearer Token to the DataStore.
     * The `edit` function is a transactional suspend function.
     *
     * @param token The Bearer Token to be saved.
     */
    suspend fun saveToken(token: BearerTokens) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token.accessToken
            token.refreshToken?.let { preferences[REFRESH_TOKEN_KEY] = it }
        }
    }

    /**
     * Retrieves the Bearer Token from the DataStore as a Flow.
     * Using a Flow allows you to observe changes to the token in real-time.
     *
     * @return A Flow that emits the stored Bearer Token, or null if it doesn't exist.
     */
    fun getToken(): Flow<BearerTokens?> {
        return context.dataStore.data.map { preferences ->
            val accessToken = preferences[ACCESS_TOKEN_KEY]
            val refreshToken = preferences[REFRESH_TOKEN_KEY]

            if (accessToken != null && refreshToken != null) {
                BearerTokens(
                    accessToken = accessToken.toString(),
                    refreshToken = refreshToken.toString()
                )
            } else
                null
        }
    }

    /**
     * Clears the Bearer Token to from DataStore.
     * The `edit` function is a transactional suspend function.
     *
     */
    suspend fun clearToken() {
        context.dataStore.edit {  preferences ->
            preferences.clear()
        }
    }
}