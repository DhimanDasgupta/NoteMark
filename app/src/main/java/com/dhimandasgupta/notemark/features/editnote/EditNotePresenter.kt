package com.dhimandasgupta.notemark.features.editnote

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.get

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
    private val noteId: String,
    private val editNoteStateMachineFactory: EditNoteStateMachineFactory = get(clazz = EditNoteStateMachineFactory::class.java) { parametersOf(noteId) }
) {
    private val events = MutableSharedFlow<EditNoteAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): EditNoteUiModel {
        var editNoteUiModel by remember(key1 = Unit) { mutableStateOf(value = EditNoteUiModel.Empty) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            val editNoteStateMachine = editNoteStateMachineFactory.launchIn(this)

            launch {
                editNoteStateMachine.state
                    .onStart { emit(value = EditNoteStateMachineFactory.defaultEditNoteState) }
                    .filter { editNoteState -> editNoteState.noteEntity != null && editNoteState.title.isNotEmpty() && editNoteState.content.isNotEmpty() }
                    .map { editNoteState -> mapToEditNoteUiModel(editNoteState) }
                    .cancellable()
                    .distinctUntilChanged()
                    .catch { throwable ->
                        if (throwable is CancellationException) throw throwable
                        // else can can be something like page level error etc.
                    }
                    .flowOn(context = Dispatchers.Default)
                    .collectLatest { uiMoel ->
                        editNoteUiModel = uiMoel
                    }
            }

            // Send the Events to the State Machine through Actions
            launch {
                events.collect { editNoteAction ->
                    editNoteStateMachine.dispatch(editNoteAction)
                }
            }
        }

        return editNoteUiModel
    }

    private fun mapToEditNoteUiModel(
        editNoteState: EditNoteState
    ): EditNoteUiModel {
        return EditNoteUiModel(
            title = editNoteState.title,
            content = editNoteState.content,
            noteEntity = editNoteState.noteEntity?.copy(
                createdAt = editNoteState.noteEntity.createdAt,
                lastEditedAt = editNoteState.noteEntity.lastEditedAt
            ),
            saved = editNoteState.saved,
            editEnable = editNoteState.mode == Mode.EditMode,
            isReaderMode = editNoteState.mode == Mode.ReaderMode
        )
    }


    fun processEvent(event: EditNoteAction) {
        events.tryEmit(value = event)
    }
}