package com.dhimandasgupta.notemark.features.launcher

import LoggedInUser
import UserManager
import android.content.Context
import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.common.android.observeConnectivityAsFlow
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine

@Immutable
sealed interface AppState {
    val connectionState: ConnectionState?

    data class NotLoggedIn(
        override val connectionState: ConnectionState? = ConnectionState.Unavailable
    ) : AppState

    data class LoggedIn(
        override val connectionState: ConnectionState? = ConnectionState.Unavailable,
        val loggedInUser: LoggedInUser,
        val syncState: SyncState = SyncState.SyncNotStarted
    ) : AppState
}

@Immutable
sealed interface SyncState {
    data object SyncNotStarted : SyncState
    data object SyncStarted : SyncState
    data object SyncFinished : SyncState
}

sealed interface AppAction {
    object ConnectionStateConsumed: AppAction
    object AppLogout : AppAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class AppStateMachine(
    private val applicationContext: Context,
    private val userManager: UserManager,
    private val noteMarkRepository: NoteMarkRepository
) : StateMachine<AppState, AppAction>(defaultAppState) {
    private var cachedUser: LoggedInUser? = null

    init {
        spec {
            inState<AppState.NotLoggedIn> {
                condition({ cachedUser != null }) {
                    onEnter { state ->
                        state.override { AppState.LoggedIn(loggedInUser = cachedUser!!) }
                    }
                }
                // All Flows while in the app state should be collected here
                collectWhileInState(userManager.getUser()) { user, state ->
                    user?.let { cachedUser = it }
                    cachedUser?.let { user ->
                        state.override {
                            AppState.LoggedIn(
                                connectionState = state.snapshot.connectionState,
                                loggedInUser = user
                            )
                        }
                    } ?: state.noChange()
                }
                collectWhileInState(applicationContext.observeConnectivityAsFlow()) { connected, state ->
                    state.mutate { state.snapshot.copy(connectionState = connected) }
                }
            }

            inState<AppState.LoggedIn> {
                onEnter { state ->
                    state.mutate { state.snapshot.copy(syncState = SyncState.SyncStarted) }
                    syncRemoteNotes()
                    state.mutate { state.snapshot.copy(syncState = SyncState.SyncFinished) }
                }

                // All the actions valid for app state should be handled here
                on<AppAction.ConnectionStateConsumed> { _, state ->
                    state.mutate { state.snapshot.copy(connectionState = null) }
                }
                on<AppAction.AppLogout> { _, state ->
                    noteMarkRepository.logout(
                        request = RefreshRequest(
                            refreshToken = userManager.getUser().first()?.bearerTokens?.refreshToken ?: ""
                        )
                    ).getOrNull()?.let {
                        cachedUser = null
                        state.override {
                            AppState.NotLoggedIn(
                                connectionState = state.snapshot.connectionState
                            )
                        }
                    } ?: state.noChange()
                }
            }
        }
    }

    companion object {
        val defaultAppState = AppState.NotLoggedIn()

    }

    private suspend fun syncRemoteNotes() {
        // Don't Sync if the last sync was less than a minute ago
        if (
            (System.currentTimeMillis() - userManager.getSyncTime()
                .first()) < DURATION_BETWEEN_SUCCESSFUL_SYNC
        ) return

        var total = 0
        var numberOfNotesLoaded = 0
        var pageNumber = 0

        do {
            delay(1000) // Intentionally adding delay.
            noteMarkRepository.getRemoteNotes(pageNumber, PAGE_SIZE).fold(
                onSuccess = { noteResponse ->
                    pageNumber++
                    total = noteResponse.total
                    numberOfNotesLoaded += noteResponse.notes.size

                    // Saving the last successful sync time.
                    userManager.saveSyncTime(System.currentTimeMillis())
                },
                onFailure = { throwable ->
                    // Handle error
                }
            )
        } while (numberOfNotesLoaded < total)
    }
}

private const val PAGE_SIZE: Int = 1
const val DURATION_BETWEEN_SUCCESSFUL_SYNC = 5 * 60 * 1000

