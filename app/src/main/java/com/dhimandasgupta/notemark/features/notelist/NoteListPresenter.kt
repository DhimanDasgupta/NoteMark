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
import com.dhimandasgupta.notemark.features.launcher.AppStateMachineFactory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
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
    private val appStateMachineFactory: AppStateMachineFactory,
    private val noteListStateMachineFactory: NoteListStateMachineFactory
) {
    private val appActionEvents = MutableSharedFlow<AppAction>(extraBufferCapacity = 10)
    private val events = MutableSharedFlow<NoteListAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): NoteListUiModel {
        var noteListUiModel by remember { mutableStateOf(value = defaultNoteListUiModel) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            val appStateMachine = appStateMachineFactory.shareIn(this)
            val noteListStateMachine = noteListStateMachineFactory.launchIn(this)

            launch {
                combine(
                    flow = appStateMachine.state.filter { appState -> appState is AppState.LoggedIn },
                    flow2 = noteListStateMachine.state
                ) { appState, noteListState ->
                    mapToNoteListUiModel(
                        loggedIn = appState as AppState.LoggedIn,
                        noteListState = noteListState
                    )
                }
                    .onStart { emit(value = NoteListUiModel.Empty) }
                    .cancellable()
                    .catch {} // Do something with error if required
                    .flowOn(context = Dispatchers.Default)
                    .collectLatest { mappedNoteListUiModel ->
                        noteListUiModel = mappedNoteListUiModel
                    }
            }

            launch {
                appActionEvents.collect { appAction ->
                    appStateMachine.dispatch(appAction)
                }
            }

            launch {
                events.collect { noteListAction ->
                    noteListStateMachine.dispatch(noteListAction)
                }
            }
        }

        return noteListUiModel
    }

    fun dispatchAction(event: NoteListAction) {
        events.tryEmit(event)
    }

    fun dispatchAppAction(event: AppAction) {
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