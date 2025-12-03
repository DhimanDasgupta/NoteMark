package com.dhimandasgupta.notemark.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.common.convertNoteTimestampToReadableFormat
import com.dhimandasgupta.notemark.features.launcher.AppAction
import com.dhimandasgupta.notemark.features.launcher.AppState
import com.dhimandasgupta.notemark.features.launcher.AppStateMachine
import com.dhimandasgupta.notemark.proto.Sync
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@Immutable
data class SettingsUiModel(
    val syncIntervals: ImmutableList<String> = persistentListOf(
        "Manual",
        "15 Minutes",
        "30 Minutes",
        "1 Hour"
    ),
    val selectedSyncInterval: String = "Manual",
    val lastSynced: String = "--",
    val deleteLocalNotesOnLogout: Boolean = false,
    val logoutStatus: Boolean? = null,
    val isSyncing: Boolean = false,
    val isConnected: Boolean = false
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
        var settingsUiModel by remember { mutableStateOf(value = SettingsUiModel.Empty) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            appStateMachine.state
                .filter { appState -> (appState is AppState.LoggedIn && appState.sync != null) || appState is AppState.NotLoggedIn }
                .map { appState -> mapToSettingsUiModel(appState = appState) }
                .flowOn(context = Dispatchers.Default)
                /*.onStart { emit(value = AppStateMachine.defaultAppState) } // Do not emit the default state as data will come from State Machine */
                .cancellable()
                .catch {} // Do something with error if required
                .collect { mappedUiModel ->
                    settingsUiModel = mappedUiModel
                }

        }

        // Send the Events to the State Machine through Actions
        LaunchedEffect(key1 = Unit) {
            events.collect { loginAction ->
                appStateMachine.dispatch(loginAction)
            }
        }

        return settingsUiModel
    }

    fun processEvent(event: AppAction) {
        events.tryEmit(value = event)
    }

    private fun mapToSettingsUiModel(appState: AppState): SettingsUiModel {
        when (appState) {
            is AppState.LoggedIn -> {
                return SettingsUiModel(
                    logoutStatus = null,
                    lastSynced = appState.sync?.lastUploadedTime?.let { time ->
                        convertNoteTimestampToReadableFormat(isoOffsetDateTimeString = time)
                    } ?: "--",
                    selectedSyncInterval = appState.sync?.syncDuration?.toReadableString() ?: "Manual",
                    deleteLocalNotesOnLogout = appState.sync?.deleteLocalNotesOnLogout ?: false,
                    isSyncing = appState.sync?.syncing ?: false,
                    isConnected = appState.connectionState == ConnectionState.Available
                )
            }
            else -> {
                return SettingsUiModel.Empty.copy(
                    logoutStatus = true,
                )
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
}

