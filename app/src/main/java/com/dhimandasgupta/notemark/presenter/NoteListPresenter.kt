package com.dhimandasgupta.notemark.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dhimandasgupta.notemark.database.NoteEntity
import com.dhimandasgupta.notemark.statemachine.NoteListAction
import com.dhimandasgupta.notemark.statemachine.NoteListState
import com.dhimandasgupta.notemark.statemachine.NoteListStateMachine
import kotlinx.coroutines.flow.MutableSharedFlow

@Immutable
data class NoteListUiModel(
    val noteEntities: List<NoteEntity>
)

private val defaultNoteListUiModel = NoteListUiModel(noteEntities = emptyList())

class NoteListPresenter(
    private val noteListStateMachine: NoteListStateMachine
) {
    private val events = MutableSharedFlow<NoteListAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): NoteListUiModel {
        var noteListUiModel by remember { mutableStateOf(defaultNoteListUiModel) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            noteListStateMachine.state.collect { noteListState ->
                noteListUiModel = when (noteListState) {
                    is NoteListState.NoteListStateWithNotes -> {
                        noteListUiModel.copy(noteEntities = noteListState.notes)
                    }

                    else -> noteListUiModel.copy(noteEntities = emptyList())
                }
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
}