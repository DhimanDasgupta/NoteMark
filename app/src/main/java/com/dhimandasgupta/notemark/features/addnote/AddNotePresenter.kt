package com.dhimandasgupta.notemark.features.addnote

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@Immutable
data class AddNoteUiModel(
    val title: String,
    val content: String,
    val saved: Boolean? = null
) {
    companion object {
        val defaultOrEmpty = AddNoteUiModel(
            title = "",
            content = "",
            saved = null
        )
    }
}

@Stable
class AddNotePresenter(
    private val addNoteStateMachineFactory: AddNoteStateMachineFactory
) {
    private val events = MutableSharedFlow<AddNoteAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): AddNoteUiModel {
        var addNoteUiModel by remember(key1 = Unit) { mutableStateOf(value = AddNoteUiModel.defaultOrEmpty) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            val addNoteStateMachine = addNoteStateMachineFactory.launchIn(this)

            launch {
                addNoteStateMachine.state
                    .onStart { emit(value = AddNoteStateMachineFactory.defaultAddNoteState) }
                    .map { addNoteState ->
                        addNoteUiModel.copy(
                            title = addNoteState.title,
                            content = addNoteState.content,
                            saved = addNoteState.saved
                        )
                    }
                    .cancellable()
                    .catch { throwable ->
                        if (throwable is CancellationException) throw throwable
                        // else can can be something like page level error etc.
                    }
                    .flowOn(context = Dispatchers.Default)
                    .collectLatest { uiModel ->
                        addNoteUiModel = uiModel
                    }
            }

            // Send the Events to the State Machine through Actions
            launch {
                events.collect { editNoteAction ->
                    addNoteStateMachine.dispatch(editNoteAction)
                }
            }
        }

        return addNoteUiModel
    }

    fun dispatchAction(event: AddNoteAction) =
        events.tryEmit(value = event)
}