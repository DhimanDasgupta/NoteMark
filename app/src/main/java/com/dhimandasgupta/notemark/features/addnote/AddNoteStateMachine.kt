package com.dhimandasgupta.notemark.features.addnote

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.getCurrentIso8601Timestamp
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.database.NoteEntity
import com.freeletics.flowredux2.FlowReduxStateMachineFactory
import com.freeletics.flowredux2.initializeWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.uuid.ExperimentalUuidApi
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
) : FlowReduxStateMachineFactory<AddNoteState, AddNoteAction>() {
    init {
        spec {
            initializeWith { defaultAddNoteState }

            inState<AddNoteState> {
                on<AddNoteAction.UpdateTitle> { action ->
                    mutate { copy(title = action.title) }
                }
                on<AddNoteAction.UpdateContent> { action ->
                    mutate { copy(content = action.content) }
                }
                on<AddNoteAction.Save> { _ ->
                    val inserted = noteMarkRepository.createNote(
                        NoteEntity(
                            id = System.currentTimeMillis(),
                            title = snapshot.title.trim(),
                            content = snapshot.content.trim(),
                            createdAt = getCurrentIso8601Timestamp(),
                            lastEditedAt = getCurrentIso8601Timestamp(),
                            uuid = Uuid.random().toHexDashString(),
                            synced = false,
                            markAsDeleted = false
                        )
                    )

                    inserted?.let {
                        return@on mutate { copy(saved = true) }
                    }

                    noChange()
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