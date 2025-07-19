package com.dhimandasgupta.notemark.features.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.proto.User
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@Immutable
data class LauncherUiModel(
    val connectionState: ConnectionState? = ConnectionState.Unavailable,
    val loggedInUser: User? = null
) {
    companion object {
        val Empty = LauncherUiModel()
    }
}

class LauncherPresenter(
    private val appStateMachine: AppStateMachine
) {
    private val events = MutableSharedFlow<AppAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): LauncherUiModel {
        val scope = rememberCoroutineScope()
        var launcherUiModel by remember(
            key1 = appStateMachine.state
        ) { mutableStateOf(LauncherUiModel.Empty) }

        // Receives the State from the StateMachine
        LifecycleStartEffect(
            key1 = Unit
        ) {
            scope.launch {
                appStateMachine.state.onStart { emit(AppStateMachine.defaultAppState) }.collect { appState ->
                    launcherUiModel = launcherUiModel.copy(
                        connectionState = appState.connectionState,
                        loggedInUser = when (appState) {
                            is AppState.NotLoggedIn -> null
                            is AppState.LoggedIn -> appState.user
                        }
                    )
                }
            }
            onStopOrDispose { scope.cancel() }
        }

        // Send the Events to the State Machine through Actions
        LifecycleResumeEffect(key1 = Unit) {
            scope.launch {
                events.collect { event ->
                    appStateMachine.dispatch(event)
                }
            }
            onPauseOrDispose { scope.cancel() }
        }

        return launcherUiModel
    }

    fun processEvent(event: AppAction) {
        events.tryEmit(event)
    }
}