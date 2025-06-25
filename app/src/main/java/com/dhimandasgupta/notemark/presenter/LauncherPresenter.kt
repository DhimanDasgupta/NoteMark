package com.dhimandasgupta.notemark.presenter

import LoggedInUser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleStartEffect
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.statemachine.AppAction
import com.dhimandasgupta.notemark.statemachine.AppStateMachine
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Immutable
data class LauncherUiModel(
    val connectionState: ConnectionState? = ConnectionState.Unavailable,
    val loggedInUser: LoggedInUser? = null
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
        var launcherUiModel by remember { mutableStateOf(LauncherUiModel.Empty) }

        // Receives the State from the StateMachine
        LifecycleStartEffect(Unit) {
            scope.launch {
                appStateMachine.state.collect { appState ->
                    launcherUiModel = LauncherUiModel(
                        connectionState = appState.connectionState,
                        loggedInUser = appState.loggedInUser
                    )
                }
            }
            onStopOrDispose {
                scope.cancel()
            }
        }

        // Send the Events to the State Machine through Actions
        LifecycleStartEffect(key1 = Unit) {
            scope.launch {
                events.collect { event ->
                    appStateMachine.dispatch(event)
                }
            }
            onStopOrDispose {
                scope.cancel()
            }
        }

        return launcherUiModel
    }

    fun processEvent(event: AppAction) {
        events.tryEmit(event)
    }
}