package com.dhimandasgupta.notemark.features.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableSharedFlow

class AppPresenter(
    private val appStateMachine: AppStateMachine
) {
    private val events = MutableSharedFlow<AppAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): AppState {
        var applicationState: AppState by remember { mutableStateOf(AppStateMachine.Companion.defaultAppState) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            appStateMachine.state.collect { appState ->
                applicationState = appState
            }
        }

        // Send the Events to the State Machine through Actions
        LaunchedEffect(key1 = Unit) {
            events.collect { event ->
                appStateMachine.dispatch(event)
            }
        }

        return applicationState
    }

    fun processEvent(event: AppAction) {
        events.tryEmit(event)
    }
}