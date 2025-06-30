package com.dhimandasgupta.notemark.statemachine

import LoggedInUser
import UserManager
import android.content.Context
import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.common.android.observeConnectivityAsFlow
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        val loggedInUser: LoggedInUser
    ) : AppState
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
                onEnterEffect { state ->
                    syncRemoteNotes()
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
        var total = 0
        var numberOfNotesLoaded = 0
        var pageNumber = 0

        do {
            noteMarkRepository.getRemoteNotes(pageNumber, PAGE_SIZE).fold(
                onSuccess = { noteResponse ->
                    pageNumber++
                    total = noteResponse.total
                    numberOfNotesLoaded += noteResponse.notes.size
                },
                onFailure = { throwable ->
                    // Handle error
                }
            )
        } while (numberOfNotesLoaded < total)
    }
}

private const val PAGE_SIZE: Int = 10

