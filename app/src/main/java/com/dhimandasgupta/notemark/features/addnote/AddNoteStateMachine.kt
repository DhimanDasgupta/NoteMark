package com.dhimandasgupta.notemark.features.addnote

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.getCurrentIso8601Timestamp
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.uuid.ExperimentalUuidApi
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine
import kotlin.uuid.Uuid

@Immutable
data class AddNoteState(
    val title: String,
    val content: String,
    val saved: Boolean? = null
)

sealed interface AddNoteAction {
    data class UpdateTitle(val title: String) : AddNoteAction
    data class UpdateContent(val content: String) : AddNoteAction
    data object Save : AddNoteAction
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
class AddNoteStateMachine(
    val noteMarkRepository: NoteMarkRepository
) : StateMachine<AddNoteState, AddNoteAction>(defaultAddNoteState) {
    init {
        spec {
            inState<AddNoteState> {
                on<AddNoteAction.UpdateTitle> { action, state ->
                    state.mutate { copy(title = action.title) }
                }
                on<AddNoteAction.UpdateContent> { action, state ->
                    state.mutate { copy(content = action.content) }
                }
                on<AddNoteAction.Save> { _, state ->
                    val inserted = noteMarkRepository.createNote(
                        NoteEntity(
                            id = System.currentTimeMillis(),
                            title = state.snapshot.title.trim(),
                            content = state.snapshot.content.trim(),
                            createdAt = getCurrentIso8601Timestamp(),
                            lastEditedAt = getCurrentIso8601Timestamp(),
                            uuid = Uuid.random().toHexDashString()
                        )
                    )

                    inserted?.let {
                        return@on state.mutate { copy(saved = true) }
                    }

                    state.noChange()
                }
            }
        }
    }

    companion object {
        val defaultAddNoteState = AddNoteState(
            title = "",
            content = "",
            saved = null
        )
    }
}