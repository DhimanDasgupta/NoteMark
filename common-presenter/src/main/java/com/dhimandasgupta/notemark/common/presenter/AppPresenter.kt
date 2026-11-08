package com.dhimandasgupta.notemark.common.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dhimandasgupta.notemark.common.state.AppAction
import com.dhimandasgupta.notemark.common.state.AppState
import com.dhimandasgupta.notemark.common.state.impl.AppStateMachineFactory
import com.dhimandasgupta.notemark.common.state.impl.AppStateStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

class AppPresenter(private val appStateStore: AppStateStore) {
  private val actions = MutableSharedFlow<AppAction>(extraBufferCapacity = 10)

  @OptIn(ExperimentalCoroutinesApi::class)
  @Composable
  fun uiModel(): AppState {
    var applicationState: AppState by
      remember(key1 = Unit) { mutableStateOf(value = AppStateMachineFactory.defaultAppState) }

    // Receives the State from the StateMachine
    LaunchedEffect(key1 = Unit) {
      launch {
        appStateStore.state
          .mapLatest { appState ->
            applicationState = appState
          }
          .cancellable()
          .catch { throwable ->
            if (throwable is CancellationException) throw throwable
            // else can can be something like page level error etc.
          }
          .flowOn(context = Dispatchers.Default)
          .collect()
      }

      // Send the Events to the State Machine through Actions
      launch {
        actions.collect { event ->
          appStateStore.dispatch(action = event)
        }
      }
    }

    return applicationState
  }

  fun dispatchAction(action: AppAction) {
    actions.tryEmit(value = action)
  }
}
