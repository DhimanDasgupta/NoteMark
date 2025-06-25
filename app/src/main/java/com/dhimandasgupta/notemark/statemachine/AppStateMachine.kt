package com.dhimandasgupta.notemark.statemachine

import LoggedInUser
import UserManager
import android.content.Context
import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.common.android.observeConnectivityAsFlow
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine

@Immutable
data class AppState(
    val connectionState: ConnectionState? = ConnectionState.Unavailable,
    val loggedInUser: LoggedInUser? = null
)

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

    init {
        spec {
            inState<AppState> {
                // All Flows while in the app state should be collected here
                collectWhileInState(userManager.getUser()) { user, state ->
                    user?.let { nonNullUser ->
                        state.override { AppState(loggedInUser = nonNullUser, connectionState = ConnectionState.Unavailable) }
                    } ?: state.noChange()
                }
                collectWhileInState(applicationContext.observeConnectivityAsFlow()) { connected, state ->
                    state.mutate { state.snapshot.copy(connectionState = connected) }
                }

                onEnterEffect { state ->
                    syncRemoteNotes()
                }

                // All the actions valid for app state should be handled here
                on<AppAction.ConnectionStateConsumed> { _, state ->
                    state.mutate { state.snapshot.copy(connectionState = null) }
                }
                on<AppAction.AppLogout> { _, state ->
                    noteMarkRepository.deleteAllLocalNotes()
                    userManager.clearUser()
                    state.override { AppState(loggedInUser = null, connectionState = state.snapshot.connectionState) }
                }
            }
        }
    }

    companion object {
        val defaultAppState = AppState(
            connectionState = ConnectionState.Unavailable,
            loggedInUser = null
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

