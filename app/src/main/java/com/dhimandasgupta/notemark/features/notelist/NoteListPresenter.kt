package com.dhimandasgupta.notemark.features.notelist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dhimandasgupta.notemark.common.convertIsoToRelativeYearFormat
import com.dhimandasgupta.notemark.database.NoteEntity
import com.dhimandasgupta.notemark.features.launcher.AppAction
import com.dhimandasgupta.notemark.features.launcher.AppState
import com.dhimandasgupta.notemark.features.launcher.AppStateMachine
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart

@Immutable
data class NoteListUiModel(
    val userName: String? = null,
    val noteEntities: ImmutableList<NoteEntity>,
    val noteLongClickedUuid: String = "",
    val showSyncProgress: Boolean = false
)

private val defaultNoteListUiModel = NoteListUiModel(noteEntities = persistentListOf())

class NoteListPresenter(
    private val appStateMachine: AppStateMachine,
    private val noteListStateMachine: NoteListStateMachine
) {
    private val appActionEvents = MutableSharedFlow<AppAction>(extraBufferCapacity = 10)
    private val events = MutableSharedFlow<NoteListAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): NoteListUiModel {
        var noteListUiModel by remember(key1 = Unit) { mutableStateOf(defaultNoteListUiModel) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            appStateMachine.state.onStart { AppStateMachine.defaultAppState }
                .collect { appState ->
                    noteListUiModel = noteListUiModel.copy(
                        userName = when (appState) {
                            is AppState.LoggedIn -> appState.user.userName
                            else -> ""
                        },
                        showSyncProgress = when (appState) {
                            is AppState.LoggedIn -> appState.sync?.syncing ?: false
                            else -> true
                        }
                    )
                }
        }

        LaunchedEffect(key1 = noteListStateMachine.state) {
            noteListStateMachine.state
                .flowOn(Dispatchers.Default)
                .catch { /* TODO if needed */  }
                .onStart { NoteListStateMachine.defaultNoteListState }
                .collect { noteListState ->
                    noteListUiModel = when (noteListState) {
                        is NoteListState.NoteListStateWithNotes -> {
                            noteListUiModel.copy(
                                noteEntities = noteListState.notes.map { noteEntity ->
                                    noteEntity.copy(
                                        lastEditedAt = convertIsoToRelativeYearFormat(
                                            isoOffsetDateTimeString = noteEntity.lastEditedAt
                                        )
                                    )
                                }.toPersistentList(),
                                noteLongClickedUuid = noteListState.longClickedNoteUuid
                            )
                        }

                        else -> noteListUiModel.copy(
                            noteEntities = persistentListOf()
                        )
                    }
                }
        }

        LaunchedEffect(key1 = Unit) {
            appActionEvents.collect { appAction ->
                appStateMachine.dispatch(appAction)
            }
        }

        // Send the Events to the State Machine through Actions
        LaunchedEffect(key1 = Unit) {
            events.collect { noteListAction ->
                noteListStateMachine.dispatch(noteListAction)
            }
        }

        return noteListUiModel
    }

    fun processEvent(event: NoteListAction) {
        events.tryEmit(event)
    }

    fun processAppActionEvent(event: AppAction) {
        appActionEvents.tryEmit(event)
    }
}