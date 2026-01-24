package com.dhimandasgupta.notemark.features.editnote

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.getCurrentIso8601Timestamp
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.database.NoteEntity
import com.freeletics.flowredux2.FlowReduxStateMachineFactory
import com.freeletics.flowredux2.initializeWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.uuid.ExperimentalUuidApi

@Immutable
data class EditNoteState(
    val title: String,
    val content: String,
    val noteEntity: NoteEntity? = null,
    val saved: Boolean? = null,
    val mode: Mode = Mode.ViewMode
)

@Immutable
sealed interface Mode {
    data object ViewMode : Mode
    data object EditMode : Mode
    data object ReaderMode : Mode
}

sealed interface EditNoteAction {
    data class UpdateNote(val noteEntity: NoteEntity) : EditNoteAction
    data class UpdateTitle(val title: String) : EditNoteAction
    data class UpdateContent(val content: String) : EditNoteAction
    data class ModeChange(val mode: Mode) : EditNoteAction
    data object Save : EditNoteAction
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
class EditNoteStateMachine(
    val noteMarkRepository: NoteMarkRepository,
    val noteId: String
) : FlowReduxStateMachineFactory<EditNoteState, EditNoteAction>() {
    init {
        spec {
            initializeWith { defaultEditNoteState }

            inState<EditNoteState> {
                onEnter {
                    noteMarkRepository.getNoteByUUID(uuid = noteId)?.let { noteEntity ->
                        mutate {
                            copy(
                                title = noteEntity.title,
                                content = noteEntity.content,
                                noteEntity = noteEntity
                            )
                        }
                    } ?: noChange()
                }
                on<EditNoteAction.UpdateNote> { action ->
                    mutate { copy(noteEntity = action.noteEntity) }
                }
                on<EditNoteAction.UpdateTitle> { action ->
                    mutate { copy(title = action.title) }
                }
                on<EditNoteAction.UpdateContent> { action ->
                    mutate { copy(content = action.content) }
                }
                on<EditNoteAction.Save> { _ ->
                    snapshot.noteEntity?.let { noteEntity ->
                        val updatedNote = noteMarkRepository.updateLocalNote(
                            title = snapshot.title.trim(),
                            content = snapshot.content.trim(),
                            lastEditedAt = getCurrentIso8601Timestamp(),
                            noteEntity = noteEntity
                        )

                        updatedNote?.let {
                            return@on mutate { copy(saved = true) }
                        }
                    }

                    noChange()
                }
                on<EditNoteAction.ModeChange> { action ->
                    mutate { copy(mode = action.mode) }
                }
            }
        }
    }

    companion object {
        val defaultEditNoteState = EditNoteState(
            title = "",
            content = "",
            noteEntity = null,
            saved = null,
            mode = Mode.ViewMode
        )
    }
}