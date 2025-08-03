package com.dhimandasgupta.notemark.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dhimandasgupta.notemark.common.convertNoteTimestampToReadableFormat
import com.dhimandasgupta.notemark.features.launcher.AppAction
import com.dhimandasgupta.notemark.features.launcher.AppState
import com.dhimandasgupta.notemark.features.launcher.AppStateMachine
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart

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
    val isSyncing: Boolean = false
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
        var logoutUiModel by remember(
            key1 = appStateMachine.state
        ) { mutableStateOf(value = SettingsUiModel.Empty) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            appStateMachine.state
                .flowOn(Dispatchers.Default)
                .catch { /* TODO if needed */ }
                .onStart { AppStateMachine.defaultAppState }
                .collect { appState ->
                    logoutUiModel = logoutUiModel.copy(
                        logoutStatus = when (appState) {
                            is AppState.NotLoggedIn -> true
                            else -> null
                        },
                        lastSynced = when (appState) {
                            is AppState.LoggedIn -> appState.sync?.lastUploadedTime?.let { time ->
                                convertNoteTimestampToReadableFormat(isoOffsetDateTimeString = time)
                            } ?: "--"

                            else -> "--"
                        },
                        selectedSyncInterval = when (appState) {
                            is AppState.LoggedIn -> appState.sync?.syncDuration?.let {
                                when (it.ordinal) {
                                    3 -> "15 Minutes"
                                    4 -> "30 Minutes"
                                    5 -> "1 Hour"
                                    else -> "Manual"
                                }
                            } ?: "Manual"

                            else -> "Manual"
                        },
                        deleteLocalNotesOnLogout = when (appState) {
                            is AppState.LoggedIn -> appState.sync?.deleteLocalNotesOnLogout
                                ?: false

                            else -> false
                        },
                        isSyncing = when (appState) {
                            is AppState.LoggedIn -> appState.sync?.syncing ?: false

                            else -> false
                        }
                    )
                }

        }

        // Send the Events to the State Machine through Actions
        LaunchedEffect(key1 = Unit) {
            events.collect { loginAction ->
                appStateMachine.dispatch(loginAction)
            }
        }

        return logoutUiModel
    }

    fun processEvent(event: AppAction) {
        events.tryEmit(value = event)
    }
}

