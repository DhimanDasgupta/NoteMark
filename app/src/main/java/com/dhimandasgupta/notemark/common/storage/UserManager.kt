import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Immutable
data class LoggedInUser(
    val userName: String,
    val bearerTokens: BearerTokens
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_tokens")

interface UserManager {
    suspend fun saveToken(bearerTokens: BearerTokens)
    suspend fun saveUser(loggerInUser: LoggedInUser)
    suspend fun saveSyncTime(syncTime: Long)
    fun getUser(): Flow<LoggedInUser?>
    fun getSyncTime(): Flow<Long>
    suspend fun clearUser()
}

/**
 * Default implementation of the [UserManager] interface.
 * This class uses AndroidX DataStore with [Preferences] to persist user authentication data,
 * specifically the username, access token, and refresh token.
 *
 * @property context The Android [Context] required to access the DataStore.
 */
class UserManagerImpl(
    private val context: Context
) : UserManager {
    private val dataStore: DataStore<Preferences> = context.dataStore

    /**
     * Companion object holding the keys used for storing user preferences in DataStore.
     */
    companion object {
        /** [Preferences.Key] for storing the user's name. */
        private val USER_NAME = stringPreferencesKey("user_name")

        /** [Preferences.Key] for storing the access token. */
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")

        /** [Preferences.Key] for storing the refresh token. */
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")

        /** [Preferences.Key] for storing the last sync time. */
        private val LAST_SYNC_TIME = longPreferencesKey(name = "last_sync_time")
    }

    /**
     * Saves the provided [BearerTokens]'s details to DataStore.
     * Stores the access token, refresh token (if available).
     *
     * @param bearerTokens The token to save.
     */
    override suspend fun saveToken(bearerTokens: BearerTokens) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                set(ACCESS_TOKEN_KEY, bearerTokens.accessToken)
                bearerTokens.refreshToken?.let { refreshToken ->
                    set(REFRESH_TOKEN_KEY, refreshToken)
                }
            }
        }
    }

    /**
     * Saves the provided [LoggedInUser]'s details to DataStore.
     * Stores the access token, refresh token (if available), and username.
     *
     * @param loggerInUser The user data to save.
     */
    override suspend fun saveUser(loggerInUser: LoggedInUser) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                clear()
                set(USER_NAME, loggerInUser.userName)
                set(ACCESS_TOKEN_KEY, loggerInUser.bearerTokens.accessToken)
                loggerInUser.bearerTokens.refreshToken?.let { refreshToken ->
                    set(REFRESH_TOKEN_KEY, refreshToken)
                }
            }
        }
    }

    /**
     * Saves the provided sync time to DataStore.
     * Stores the last sync time as a Long value.
     *
     * @param syncTime The last sync time to save.
     */
    override suspend fun saveSyncTime(syncTime: Long) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                set(LAST_SYNC_TIME, syncTime)
            }
        }
    }

    /**
     * Retrieves the [LoggedInUser] from DataStore by observing its [Flow] of [Preferences].
     * Maps the stored preferences into a [LoggedInUser] object if all necessary
     * data (username, access token, refresh token) is present.
     *
     * @return A [Flow] emitting the [LoggedInUser] or `null` if no user data is found or is incomplete.
     */
    override fun getUser(): Flow<LoggedInUser?> = dataStore.data.catch { exception ->
        null
    }.map {
        val userName = it[USER_NAME]
        val accessToken = it[ACCESS_TOKEN_KEY]
        val refreshToken = it[REFRESH_TOKEN_KEY]

        if (userName != null) {
            LoggedInUser(
                userName = userName,
                bearerTokens = BearerTokens(
                    accessToken = accessToken ?: "",
                    refreshToken = refreshToken
                )
            )
        } else {
            null
        }
    }

    /**
     * Retrieves the last synchronization time from DataStore.
     * Observes the [Flow] of [Preferences] and extracts the timestamp stored
     * under the `LAST_SYNC_TIME` key. If the key is not found or an error occurs
     * during data retrieval, it defaults to `0L`.
     *
     * @return A [Flow] emitting the last synchronization time as a [Long] value.
     */
    override fun getSyncTime(): Flow<Long> = dataStore.data.catch { exception ->
        0L
    }.map {
        it[LAST_SYNC_TIME] ?: 0L
    }

    /**
     * Clears all preferences stored in the "auth_tokens" DataStore.
     * Effectively removes all user authentication data.
     */
    override suspend fun clearUser() {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences().also {
                it.clear()
            }
        }
    }
}