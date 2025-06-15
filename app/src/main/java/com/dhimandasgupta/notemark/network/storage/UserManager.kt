import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Data class representing a logged-in user's essential information.
 * This includes the username and the Ktor [BearerTokens] for authentication.
 * Marked as [Immutable] for potential Compose UI optimizations.
 *
 * @property userName The display name or identifier of the logged-in user.
 * @property bearerTokens The [BearerTokens] containing the access and refresh tokens for the user.
 */
@Immutable
data class LoggedInUser(
    val userName: String,
    val bearerTokens: BearerTokens
)

/**
 * Extension property to provide a [DataStore] instance for storing [Preferences].
 * This DataStore is specifically used for persisting authentication tokens.
 * The DataStore instance is named "auth_tokens".
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_tokens")

/**
 * Interface defining the contract for managing user authentication state and related data.
 * This includes saving user information upon login, retrieving the current user's data,
 * and clearing user data upon logout.
 */
interface UserManager {
    /**
     * Saves the details of a logged-in user.
     * This typically involves persisting the [LoggedInUser] information, including tokens.
     *
     * @param loggerInUser The [LoggedInUser] object containing the user's details and tokens.
     */
    suspend fun saveUser(loggerInUser: LoggedInUser)

    /**
     * Retrieves the current [LoggedInUser] as a [Flow].
     * Using a [Flow] allows observers to react to changes in the user's login state
     * (e.g., login, logout, token refresh).
     *
     * @return A [Flow] that emits the current [LoggedInUser] if one exists, or `null` otherwise.
     */
    fun getUser(): Flow<LoggedInUser?>

    /**
     * Clears all stored information related to the current user.
     * This is typically called during a logout process to remove authentication tokens
     * and user details from persistent storage.
     */
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
    }

    /**
     * Saves the provided [LoggedInUser]'s details to DataStore.
     * Stores the access token, refresh token (if available), and username.
     *
     * @param loggerInUser The user data to save.
     */
    override suspend fun saveUser(loggerInUser: LoggedInUser) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = loggerInUser.bearerTokens.accessToken
            loggerInUser.bearerTokens.refreshToken?.let { preferences[REFRESH_TOKEN_KEY] = it }
            preferences[USER_NAME] = loggerInUser.userName
        }
    }

    /**
     * Retrieves the [LoggedInUser] from DataStore by observing its [Flow] of [Preferences].
     * Maps the stored preferences into a [LoggedInUser] object if all necessary
     * data (username, access token, refresh token) is present.
     *
     * @return A [Flow] emitting the [LoggedInUser] or `null` if no user data is found or is incomplete.
     */
    override fun getUser(): Flow<LoggedInUser?> {
        return context.dataStore.data.map { preferences ->
            val userName = preferences[USER_NAME]
            val accessToken = preferences[ACCESS_TOKEN_KEY]
            val refreshToken = preferences[REFRESH_TOKEN_KEY]

            if (userName != null && accessToken != null && refreshToken != null) {
                LoggedInUser(
                    userName = userName,
                    bearerTokens = BearerTokens(
                        accessToken = accessToken, // Removed .toString() as it's already String
                        refreshToken = refreshToken  // Removed .toString() as it's already String
                    )
                )
            } else {
                null
            }
        }
    }

    /**
     * Clears all preferences stored in the "auth_tokens" DataStore.
     * Effectively removes all user authentication data.
     */
    override suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}