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
import com.dhimandasgupta.notemark.statemachine.EditNoteAction
import com.dhimandasgupta.notemark.statemachine.EditNoteStateMachine
import com.dhimandasgupta.notemark.statemachine.Mode
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@Immutable
data class EditNoteUiModel(
    val title: String,
    val content: String,
    val noteEntity: NoteEntity? = null,
    val saved: Boolean? = null,
    val editEnable: Boolean = false,
    val isReaderMode: Boolean = false
) {
    companion object {
        val Empty = EditNoteUiModel(
            title = "",
            content = "",
            noteEntity = null,
            editEnable = false,
            isReaderMode = false
        )
    }
}

class EditNotePresenter(
    private val editNoteStateMachine: EditNoteStateMachine
) {
    private val events = MutableSharedFlow<EditNoteAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): EditNoteUiModel {
        val scope = rememberCoroutineScope()
        var editNoteUiModel by remember(
            key1 = editNoteStateMachine.state
        ) { mutableStateOf(EditNoteUiModel.Empty) }

        // Receives the State from the StateMachine
        LifecycleStartEffect(
            key1 = Unit
        ) {
            scope.launch {
                editNoteStateMachine.state.onStart { EditNoteStateMachine.defaultEditNoteState }.collect { editNoteState ->
                    editNoteUiModel = editNoteUiModel.copy(
                        title = editNoteState.title,
                        content = editNoteState.content,
                        noteEntity = editNoteState.noteEntity,
                        saved = editNoteState.saved,
                        editEnable = editNoteState.mode == Mode.EditMode,
                        isReaderMode = editNoteState.mode == Mode.ReaderMode
                    )
                }
            }
            onStopOrDispose { scope.cancel() }
        }

        // Send the Events to the State Machine through Actions
        LifecycleStartEffect(key1 = Unit) {
            scope.launch {
                events.collect { editNoteAction ->
                    editNoteStateMachine.dispatch(editNoteAction)
                }
            }
            onStopOrDispose { scope.cancel() }
        }

        return editNoteUiModel
    }

    fun processEvent(event: EditNoteAction) {
        events.tryEmit(event)
    }
}