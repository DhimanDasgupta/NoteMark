package com.dhimandasgupta.notemark.statemachine

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.network.NoteMarkApi
import com.dhimandasgupta.notemark.network.storage.TokenStorage
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Immutable
data object AppState

sealed interface AppAction

@OptIn(ExperimentalCoroutinesApi::class)
class AppStateMachine(
    val noteMarkApi: NoteMarkApi,
    val tokenStorage: TokenStorage,
) : StateMachine<AppState, AppAction>(defaultAppState) {

    init {
        spec { /* More Details goes here... */ }
    }

    companion object {
        val defaultAppState = AppState
    }
}

