package com.dhimandasgupta.notemark.statemachine

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.database.NoteEntity
import com.dhimandasgupta.notemark.statemachine.NoteListState.NoteListStateWithNoNotes
import com.dhimandasgupta.notemark.statemachine.NoteListState.NoteListStateWithNotes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine

@Immutable
sealed interface NoteListState {
    object NoteListStateWithNoNotes : NoteListState
    data class NoteListStateWithNotes(
        val notes: List<NoteEntity>,
        val clickedNoteUuid: String = "",
        val longClickedNoteUuid: String = ""
    ): NoteListState
}

sealed interface NoteListAction {
    data class NoteClicked(val uuid: String): NoteListAction
    data class NoteLongClicked(val uuid: String): NoteListAction
    data object NoteClickConsumed: NoteListAction
    data object NoteLongClickConsumed: NoteListAction
    data class NoteDeleted(val uuid: String): NoteListAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class NoteListStateMachine(
    val noteMarkRepository: NoteMarkRepository
) : StateMachine<NoteListState, NoteListAction>(defaultNoteListState) {
    init {
        spec {
            inState<NoteListStateWithNoNotes> {
                collectWhileInState(noteMarkRepository.getAllNotes()) { notes, state ->
                    if (notes.isEmpty()) {
                        state.noChange()
                    } else {
                        state.override { NoteListStateWithNotes(notes.sortedByDescending { it.lastEditedAt }) }
                    }
                }
            }

            inState<NoteListStateWithNotes> {
                on<NoteListAction.NoteClicked> { action, state ->
                    state.mutate {
                        copy(
                            clickedNoteUuid = action.uuid,
                            longClickedNoteUuid = ""
                        )
                    }
                }
                on<NoteListAction.NoteLongClicked> { action, state ->
                    state.mutate {
                        copy(
                            clickedNoteUuid = "",
                            longClickedNoteUuid = action.uuid
                        )
                    }
                }
                on<NoteListAction.NoteClickConsumed> { _, state ->
                    state.mutate {
                        copy(
                            clickedNoteUuid = "",
                            longClickedNoteUuid = ""
                        )
                    }
                }
                on<NoteListAction.NoteLongClickConsumed> { _, state ->
                    state.mutate {
                        copy(
                            clickedNoteUuid = "",
                            longClickedNoteUuid = ""
                        )
                    }
                }
                on<NoteListAction.NoteDeleted> { action, state ->
                    noteMarkRepository.getNoteByUUID(uuid = action.uuid)?.let { noteEntity ->
                        if  (noteMarkRepository.deleteNote(noteEntity)) {
                            return@on state.override { state.snapshot.copy(notes = state.snapshot.notes.filter { it.uuid != action.uuid }) }
                        }
                    }
                    state.noChange()
                }
                collectWhileInState(noteMarkRepository.getAllNotes()) { notes, state ->
                    if (notes.isNotEmpty()) {
                        state.mutate { NoteListStateWithNotes(state.snapshot.notes.sortedByDescending { it.lastEditedAt }) }
                    } else {
                        state.override { NoteListStateWithNoNotes } }
                    }
                }
            }
        }

    companion object {
        val defaultNoteListState = NoteListStateWithNoNotes
    }
}