package com.dhimandasgupta.notemark.statemachine

import LoggedInUser
import UserManager
import android.content.Context
import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.common.android.observeConnectivityAsFlow
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Immutable
sealed interface AppState {
    val connectionState: ConnectionState?
}

@Immutable
data class NonLoggedInState(
    override val connectionState: ConnectionState?
) : AppState

@Immutable
data class LoggedInState(
    val loggedInUser: LoggedInUser,
    override val connectionState: ConnectionState? = null
) : AppState

sealed interface AppAction {
    object ConnectionStateConsumed: AppAction
    object AppLogout : AppAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class AppStateMachine(
    val applicationContext: Context,
    val userManager: UserManager,
    val noteMarkRepository: NoteMarkRepository
) : StateMachine<AppState, AppAction>(defaultAppState) {

    init {
        spec {
            inState<NonLoggedInState> {
                // All Flows while in the app state should be collected here
                collectWhileInState(userManager.getUser()) { user, state ->
                    user?.let { nonNullUser ->
                        state.override { LoggedInState(loggedInUser = nonNullUser, connectionState = null) }
                    } ?: state.noChange()
                }
                collectWhileInState(applicationContext.observeConnectivityAsFlow()) { connected, state ->
                    state.mutate { state.snapshot.copy(connectionState = connected) }
                }

                // All the actions valid for app state should be handled here
                on<AppAction.ConnectionStateConsumed> { _, state ->
                    state.mutate { state.snapshot.copy(connectionState = null) }
                }
            }
            inState<LoggedInState> {
                onEnterEffect { state ->
                    syncRemoteNotes()
                }
                collectWhileInState(applicationContext.observeConnectivityAsFlow()) { connected, state ->
                    state.mutate { state.snapshot.copy(connectionState = connected) }
                }
                // All the actions valid for app state should be handled here
                on<AppAction.ConnectionStateConsumed> { _, state ->
                    state.mutate { state.snapshot.copy(connectionState = null) }
                }
                on<AppAction.AppLogout> { _, state ->
                    noteMarkRepository.deleteAllLocalNotes()
                    userManager.clearUser()
                    state.override { NonLoggedInState(connectionState = state.snapshot.connectionState) }
                }
            }
        }
    }

    companion object {
        val defaultAppState = NonLoggedInState(
            connectionState = null
        )
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

