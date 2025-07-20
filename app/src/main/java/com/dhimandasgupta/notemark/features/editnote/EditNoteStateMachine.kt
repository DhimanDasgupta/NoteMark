package com.dhimandasgupta.notemark.features.editnote

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.getCurrentIso8601Timestamp
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.database.NoteEntity
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
    data class LoadNote(val uuid: String) : EditNoteAction
    data class UpdateNote(val noteEntity: NoteEntity) : EditNoteAction
    data class UpdateTitle(val title: String) : EditNoteAction
    data class UpdateContent(val content: String) : EditNoteAction
    data class ModeChange(val mode: Mode) : EditNoteAction
    data object Save : EditNoteAction
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
class EditNoteStateMachine(
    val noteMarkRepository: NoteMarkRepository
) : StateMachine<EditNoteState, EditNoteAction>(defaultEditNoteState) {
    init {
        spec {
            inState<EditNoteState> {
                on<EditNoteAction.LoadNote> { action, state ->
                    noteMarkRepository.getNoteByUUID(uuid = action.uuid)?.let { noteEntity ->
                        state.mutate {
                            copy(
                                title = noteEntity.title,
                                content = noteEntity.content,
                                noteEntity = noteEntity
                            )
                        }
                    } ?: state.noChange()
                }
                on<EditNoteAction.UpdateNote> { action, state ->
                    state.mutate { copy(noteEntity = action.noteEntity) }
                }
                on<EditNoteAction.UpdateTitle> { action, state ->
                    state.mutate { copy(title = action.title) }
                }
                on<EditNoteAction.UpdateContent> { action, state ->
                    state.mutate { copy(content = action.content) }
                }
                on<EditNoteAction.Save> { _, state ->
                    when (state.snapshot.noteEntity) {
                        null -> {
                            val inserted = noteMarkRepository.createNote(
                                NoteEntity(
                                    id = System.currentTimeMillis(),
                                    title = state.snapshot.title.trim(),
                                    content = state.snapshot.content.trim(),
                                    createdAt = getCurrentIso8601Timestamp(),
                                    lastEditedAt = getCurrentIso8601Timestamp(),
                                    uuid = Uuid.random().toHexDashString(),
                                    synced = false,
                                    markAsDeleted = false
                                )
                            )

                            inserted?.let {
                                return@on state.mutate { copy(saved = true) }
                            }
                        }
                        else -> {
                            state.snapshot.noteEntity?.let { noteEntity ->
                                val updatedNote = noteMarkRepository.updateLocalNote(
                                    title = state.snapshot.title.trim(),
                                    content = state.snapshot.content.trim(),
                                    lastEditedAt = getCurrentIso8601Timestamp(),
                                    noteEntity = noteEntity
                                )

                                updatedNote?.let {
                                    return@on state.mutate { copy(saved = true) }
                                }
                            }
                        }
                    }

                    state.noChange()
                }
                on<EditNoteAction.ModeChange> { action, state ->
                    state.mutate { copy(mode = action.mode) }
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