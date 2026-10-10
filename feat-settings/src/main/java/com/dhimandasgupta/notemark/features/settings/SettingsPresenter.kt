package com.dhimandasgupta.notemark.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.common.state.AppAction
import com.dhimandasgupta.notemark.common.state.AppState
import com.dhimandasgupta.notemark.common.state.impl.AppStateMachineFactory
import com.dhimandasgupta.notemark.common.state.impl.LoggedIn
import com.dhimandasgupta.notemark.common.state.impl.NotLoggedIn
import com.dhimandasgupta.notemark.proto.Sync
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class SettingsUiModel(
  val selectedSyncInterval: String = "Manual",
  val lastSynced: String = "--",
  val deleteLocalNotesOnLogout: Boolean = false,
  val logoutStatus: Boolean? = null,
  val isSyncing: Boolean = false,
  val isConnected: Boolean = false,
  val appVersionName: String? = null,
) {
  companion object {
    val defaultOrEmpty = SettingsUiModel()
  }

  fun syncIntervals(): ImmutableList<String> =
    persistentListOf(
      "Manual",
      "15 Minutes",
      "30 Minutes",
      "1 Hour",
    )
}

@Inject
@Stable
class SettingsPresenter(private val appStateMachineFactory: AppStateMachineFactory) {
  private val actions = MutableSharedFlow<AppAction>(extraBufferCapacity = 10)

  @OptIn(ExperimentalCoroutinesApi::class)
  @Composable
  fun uiModel(): SettingsUiModel {
    var settingsUiModel by remember { mutableStateOf(value = SettingsUiModel.defaultOrEmpty) }

    // Receives the State from the StateMachine
    LaunchedEffect(key1 = Unit) {
      val appStateMachine = appStateMachineFactory.launchIn(this)

      launch {
        appStateMachine.state
          // The machine starts in NotLoggedIn before it has read the stored user, so NotLoggedIn
          // only means a logout once LoggedIn has been seen.
          .dropWhile { appState -> appState !is LoggedIn }
          .filter { appState ->
            (appState is LoggedIn && appState.sync != null) || appState is NotLoggedIn
          }
          .mapLatest { appState ->
            settingsUiModel = settingsUiModel.mapToSettingsUiModel(appState = appState)
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
        actions.collect { loginAction ->
          appStateMachine.dispatch(loginAction)
        }
      }
    }

    return settingsUiModel
  }

  fun dispatchAction(action: AppAction) {
    actions.tryEmit(value = action)
  }
}

private fun SettingsUiModel.mapToSettingsUiModel(appState: AppState): SettingsUiModel {
  when (appState) {
    is LoggedIn -> {
      return copy(
        logoutStatus = null,
        lastSynced = appState.sync?.lastUploadedTime ?: "--",
        selectedSyncInterval = appState.sync?.syncDuration?.toReadableString() ?: "Manual",
        deleteLocalNotesOnLogout = appState.sync?.deleteLocalNotesOnLogout ?: false,
        isSyncing = appState.isSyncing,
        isConnected = appState.connectionState == ConnectionState.Available,
        appVersionName = appState.appVersionName,
      )
    }

    else -> {
      return SettingsUiModel.defaultOrEmpty.copy(logoutStatus = true)
    }
  }
}

private fun Sync.SyncDuration.toReadableString(): String {
  return when (this) {
    Sync.SyncDuration.SYNC_DURATION_FIFTEEN_MINUTES -> "15 Minutes"
    Sync.SyncDuration.SYNC_DURATION_THIRTY_MINUTES -> "30 Minutes"
    Sync.SyncDuration.SYNC_DURATION_ONE_HOUR -> "1 Hour"
    else -> "Manual"
  }
}
