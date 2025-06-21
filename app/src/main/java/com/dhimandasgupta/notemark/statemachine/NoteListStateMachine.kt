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
    data class NoteListStateWithNotes(val notes: List<NoteEntity>): NoteListState
}

sealed interface NoteListAction

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
                        state.override { NoteListStateWithNotes(notes) }
                    }
                }
            }

            inState<NoteListStateWithNotes> {
                collectWhileInState(noteMarkRepository.getAllNotes()) { notes, state ->
                    if (notes.isNotEmpty()) {
                        state.mutate { NoteListStateWithNotes(state.snapshot.notes) }
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