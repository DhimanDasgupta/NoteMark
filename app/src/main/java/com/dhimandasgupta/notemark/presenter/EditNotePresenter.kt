package com.dhimandasgupta.notemark.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dhimandasgupta.notemark.statemachine.EditNoteAction
import com.dhimandasgupta.notemark.statemachine.EditNoteState
import com.dhimandasgupta.notemark.statemachine.EditNoteStateMachine
import com.dhimandasgupta.notemark.statemachine.EditNoteStateMachine.Companion.defaultEditNoteState
import kotlinx.coroutines.flow.MutableSharedFlow

class EditNotePresenter(
    private val editNoteStateMachine: EditNoteStateMachine
) {
    private val events = MutableSharedFlow<EditNoteAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): EditNoteState {
        var editNoteUiModel by remember { mutableStateOf(defaultEditNoteState) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            editNoteStateMachine.state.collect { editNoteState ->
                editNoteUiModel = editNoteState
            }
        }

        // Send the Events to the State Machine through Actions
        LaunchedEffect(key1 = Unit) {
            events.collect { editNoteAction ->
                editNoteStateMachine.dispatch(editNoteAction)
            }
        }

        return editNoteUiModel
    }

    fun processEvent(event: EditNoteAction) {
        events.tryEmit(event)
    }
}