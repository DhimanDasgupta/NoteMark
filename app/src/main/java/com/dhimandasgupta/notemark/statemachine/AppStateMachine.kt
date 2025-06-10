package com.dhimandasgupta.notemark.statemachine

import androidx.compose.runtime.Immutable
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Immutable
data object AppState

sealed interface AppAction

@OptIn(ExperimentalCoroutinesApi::class)
class AppStateMachine : StateMachine<AppState, AppAction>(defaultAppState) {
    init {
        spec { /* More Details goes here... */ }
    }

    companion object {
        val defaultAppState = AppState
    }
}

