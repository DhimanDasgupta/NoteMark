package com.dhimandasgupta.notemark.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleStartEffect
import com.dhimandasgupta.notemark.features.launcher.AppAction
import com.dhimandasgupta.notemark.features.launcher.AppState
import com.dhimandasgupta.notemark.features.launcher.AppStateMachine
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@Immutable
data class SettingsUiModel(
    val logoutStatus: Boolean? = null
) {
    companion object {
        val Empty = SettingsUiModel()
    }
}

class SettingsPresenter(
    private val appStateMachine: AppStateMachine
) {
    private val events = MutableSharedFlow<AppAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): SettingsUiModel {
        val scope = rememberCoroutineScope()
        var logoutUiModel by remember { mutableStateOf(SettingsUiModel.Empty) }

        // Receives the State from the StateMachine
        LifecycleStartEffect(key1 = Unit) {
            scope.launch {
                appStateMachine.state.onStart { AppStateMachine.defaultAppState }.collect { appState ->
                    logoutUiModel = logoutUiModel.copy(
                        logoutStatus = when (appState) {
                            is AppState.NotLoggedIn -> true
                            else -> null
                        }
                    )
                }
            }
            onStopOrDispose { scope.cancel() }
        }

        // Send the Events to the State Machine through Actions
        LifecycleStartEffect(key1 = Unit) {
            scope.launch {
                events.collect { loginAction ->
                    appStateMachine.dispatch(loginAction)
                }
            }
            onStopOrDispose { scope.cancel() }
        }

        return logoutUiModel
    }

    fun processEvent(event: AppAction) {
        events.tryEmit(event)
    }
}

