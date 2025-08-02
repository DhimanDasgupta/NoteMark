package com.dhimandasgupta.notemark.features.addnote

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart

@Immutable
data class AddNoteUiModel(
    val title: String,
    val content: String,
    val saved: Boolean? = null
) {
    companion object {
        val Empty = AddNoteUiModel(
            title = "",
            content = "",
            saved = null
        )
    }
}

class AddNotePresenter(
    private val addNoteStateMachine: AddNoteStateMachine
) {
    private val events = MutableSharedFlow<AddNoteAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): AddNoteUiModel {
        var addNoteUiModel by remember(
            key1 = addNoteStateMachine.state
        ) { mutableStateOf(value = AddNoteUiModel.Empty) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            addNoteStateMachine.state
                .flowOn(Dispatchers.Default)
                .catch { /* TODO if needed */  }
                .onStart { AddNoteStateMachine.defaultAddNoteState }
                .collect { addNoteState ->
                    addNoteUiModel = addNoteUiModel.copy(
                        title = addNoteState.title,
                        content = addNoteState.content,
                        saved = addNoteState.saved
                    )
                }
        }

        // Send the Events to the State Machine through Actions
        LaunchedEffect(key1 = Unit) {
            events.collect { editNoteAction ->
                addNoteStateMachine.dispatch(editNoteAction)
            }
        }

        return addNoteUiModel
    }

    fun processEvent(event: AddNoteAction) {
        events.tryEmit(value = event)
    }
}