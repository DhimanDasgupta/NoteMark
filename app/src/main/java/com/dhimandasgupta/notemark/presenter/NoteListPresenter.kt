package com.dhimandasgupta.notemark.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleStartEffect
import com.dhimandasgupta.notemark.database.NoteEntity
import com.dhimandasgupta.notemark.statemachine.AppAction
import com.dhimandasgupta.notemark.statemachine.AppState
import com.dhimandasgupta.notemark.statemachine.AppStateMachine
import com.dhimandasgupta.notemark.statemachine.NoteListAction
import com.dhimandasgupta.notemark.statemachine.NoteListState
import com.dhimandasgupta.notemark.statemachine.NoteListStateMachine
import com.dhimandasgupta.notemark.statemachine.SyncState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@Immutable
data class NoteListUiModel(
    val userName: String? = null,
    val noteEntities: ImmutableList<NoteEntity>,
    val noteClickedUuid: String = "",
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
        val scope = rememberCoroutineScope()
        var noteListUiModel by remember(
            key1 = appStateMachine.state,
            key2 = noteListStateMachine.state
        ) { mutableStateOf(defaultNoteListUiModel) }

        // Receives the State from the StateMachine
        LifecycleStartEffect(
            key1 = Unit
        ) {
            scope.launch {
                appStateMachine.state.onStart { AppStateMachine.defaultAppState }.collect { appState ->
                    noteListUiModel = noteListUiModel.copy(
                        userName = when (appState) {
                            is AppState.LoggedIn -> appState.loggedInUser.userName
                            else -> ""
                        },
                        showSyncProgress = when (appState) {
                            is AppState.LoggedIn -> appState.syncState != SyncState.SyncFinished
                            else -> true
                        }
                    )
                }
            }
            onStopOrDispose { scope.cancel() }
        }

        LifecycleStartEffect(
            key1 = Unit
        ) {
            scope.launch {
                noteListStateMachine.state.onStart { NoteListStateMachine.defaultNoteListState }.collect { noteListState ->
                    noteListUiModel = when (noteListState) {
                        is NoteListState.NoteListStateWithNotes -> {
                            noteListUiModel.copy(
                                noteEntities = noteListState.notes.toPersistentList(),
                                noteClickedUuid = noteListState.clickedNoteUuid,
                                noteLongClickedUuid = noteListState.longClickedNoteUuid
                            )
                        }

                        else -> noteListUiModel.copy(
                            noteEntities = persistentListOf()
                        )
                    }
                }
            }
            onStopOrDispose { scope.cancel() }
        }

        LifecycleStartEffect(key1 = Unit) {
            scope.launch {
                appActionEvents.collect { appAction ->
                    appStateMachine.dispatch(appAction)
                }
            }
            onStopOrDispose { scope.cancel() }
        }

        // Send the Events to the State Machine through Actions
        LifecycleStartEffect(key1 = Unit) {
            scope.launch {
                events.collect { noteListAction ->
                    noteListStateMachine.dispatch(noteListAction)
                }
            }
            onStopOrDispose { scope.cancel() }
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