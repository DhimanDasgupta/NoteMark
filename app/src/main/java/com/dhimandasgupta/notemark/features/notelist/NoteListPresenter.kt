package com.dhimandasgupta.notemark.features.notelist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.features.launcher.AppAction
import com.dhimandasgupta.notemark.features.launcher.AppState
import com.dhimandasgupta.notemark.features.launcher.AppStateMachine
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

@Immutable
data class NoteListUiModel(
    val userName: String? = null,
    val noteEntities: ImmutableList<NoteEntityUiModel>,
    val noteLongClickedUuid: String = "",
    val showSyncProgress: Boolean = false,
    val isConnected: Boolean = false
) {
    companion object {
        val Empty = defaultNoteListUiModel
    }
}

@Immutable
data class NoteEntityUiModel(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: String,
    val lastEditedAt: String,
    val uuid: String,
    val synced: Boolean,
    val markAsDeleted: Boolean,
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
        var noteListUiModel by remember { mutableStateOf(value = defaultNoteListUiModel) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            val appSM = appStateMachine.launchIn(this)
            val noteListSM = noteListStateMachine.launchIn(this)

            launch {
                combine(
                    flow = appSM.state.filter { appState -> appState is AppState.LoggedIn },
                    flow2 = noteListSM.state
                ) {
                        appState, noteListState -> mapToNoteListUiModel(
                    loggedIn = appState as AppState.LoggedIn,
                    noteListState = noteListState
                )
                }
                    .flowOn(context = Dispatchers.Default)
                    /*.onStart { emit(value = NoteListUiModel.Empty) } // Do not emit the default state as data will come from State Machine */
                    .cancellable()
                    .catch {} // Do something with error if required
                    .collect { mappedNoteListUiModel ->
                        noteListUiModel = mappedNoteListUiModel
                    }
            }

            launch {
                appActionEvents.collect { appAction ->
                    appSM.dispatch(appAction)
                }
            }

            launch {
                events.collect { noteListAction ->
                    noteListSM.dispatch(noteListAction)
                }
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

    private fun mapToNoteListUiModel(loggedIn: AppState.LoggedIn, noteListState: NoteListState): NoteListUiModel {
        return NoteListUiModel(
            userName = loggedIn.user.userName,
            noteEntities = if (noteListState is NoteListState.NoteListStateWithNotes) {
                noteListState
                    .notes
                    .map { note ->
                        NoteEntityUiModel(
                            id = note.id,
                            title = note.title,
                            content = note.content,
                            createdAt = note.createdAt,
                            lastEditedAt = note.lastEditedAt,
                            uuid = note.uuid,
                            synced = note.synced,
                            markAsDeleted = note.markAsDeleted
                        )
                    }
                    .toPersistentList()
            } else {
                persistentListOf()
            },
            noteLongClickedUuid = if (noteListState is NoteListState.NoteListStateWithNotes) {
                noteListState.longClickedNoteUuid
            } else {
                ""
            },
            showSyncProgress = loggedIn.sync?.syncing ?: false,
            isConnected = loggedIn.connectionState == ConnectionState.Available
        )
    }
}