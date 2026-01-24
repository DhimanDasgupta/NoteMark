package com.dhimandasgupta.notemark.features.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.proto.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
    private val appStateMachineFactory: AppStateMachineFactory
) {
    private val events = MutableSharedFlow<AppAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): LauncherUiModel {
        var launcherUiModel by remember(key1 = Unit) { mutableStateOf(value = LauncherUiModel.Empty) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            val appStateMachine = appStateMachineFactory.launchIn(this)

            launch {
                appStateMachine.state
                    .onStart { emit(value = AppStateMachineFactory.defaultAppState) }
                    .map { appState ->
                        launcherUiModel = launcherUiModel.copy(
                            connectionState = appState.connectionState,
                            loggedInUser = when (appState) {
                                is AppState.NotLoggedIn -> null
                                is AppState.LoggedIn -> appState.user
                            }
                        )
                    }
                    .cancellable()
                    .catch { throwable ->
                        if (throwable is CancellationException) throw throwable
                        // else can can be something like page level error etc.
                    }
                    .flowOn(context = Dispatchers.Default)
                    .collectLatest {}
            }

            // Send the Events to the State Machine through Actions
            launch {
                events.collect { event ->
                    appStateMachine.dispatch(action = event)
                }
            }
        }

        return launcherUiModel
    }

    fun processEvent(event: AppAction) {
        events.tryEmit(value = event)
    }
}