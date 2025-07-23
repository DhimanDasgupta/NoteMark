package com.dhimandasgupta.notemark.features.addnote

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleStartEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

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
        val scope = rememberCoroutineScope()
        var addNoteUiModel by remember(
            key1 = addNoteStateMachine.state
        ) { mutableStateOf(value = AddNoteUiModel.Empty) }

        // Receives the State from the StateMachine
        LifecycleStartEffect(
            key1 = Unit
        ) {
            scope.launch {
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
            onStopOrDispose { scope.cancel() }
        }

        // Send the Events to the State Machine through Actions
        LifecycleStartEffect(key1 = Unit) {
            scope.launch {
                events.collect { editNoteAction ->
                    addNoteStateMachine.dispatch(editNoteAction)
                }
            }
            onStopOrDispose { scope.cancel() }
        }

        return addNoteUiModel
    }

    fun processEvent(event: AddNoteAction) {
        events.tryEmit(value = event)
    }
}