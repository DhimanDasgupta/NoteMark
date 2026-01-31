package com.dhimandasgupta.notemark.features.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

class AppPresenter(
    private val appStateMachineFactory: AppStateMachineFactory
) {
    private val events = MutableSharedFlow<AppAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): AppState {
        var applicationState: AppState by remember(key1 = Unit) { mutableStateOf(value = AppStateMachineFactory.defaultAppState) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            val appStateMachine = appStateMachineFactory.launchIn(this)

            launch {
                appStateMachine.state
                    .onStart { emit(value = AppStateMachineFactory.defaultAppState) }
                    .map { appState -> appState }
                    .cancellable()
                    .catch { throwable ->
                        if (throwable is CancellationException) throw throwable
                        // else can can be something like page level error etc.
                    }
                    .flowOn(context = Dispatchers.Default)
                    .collectLatest { uiModel ->
                        applicationState = uiModel
                    }
            }

            // Send the Events to the State Machine through Actions
            launch {
                events.collect { event ->
                    appStateMachine.dispatch(event)
                }
            }
        }

        return applicationState
    }

    fun dispatchAction(event: AppAction) {
        events.tryEmit(value = event)
    }
}