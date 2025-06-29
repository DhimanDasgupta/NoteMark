package com.dhimandasgupta.notemark.statemachine

import UserManager
import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.database.NoteEntity
import com.dhimandasgupta.notemark.statemachine.NoteListState.NoteListStateWithNoNotes
import com.dhimandasgupta.notemark.statemachine.NoteListState.NoteListStateWithNotes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine

@Immutable
sealed interface NoteListState {
    val userName: String?
    data class NoteListStateWithNoNotes(
        override val userName: String? = null
    ) : NoteListState

    data class NoteListStateWithNotes(
        override val userName: String?,
        val notes: List<NoteEntity>,
        val longClickedNoteUuid: String = "",
        val clickedNoteUuid: String = ""
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
    private val userManager: UserManager,
    private val noteMarkRepository: NoteMarkRepository
) : StateMachine<NoteListState, NoteListAction>(defaultNoteListState) {
    init {
        spec {
            inState<NoteListStateWithNoNotes> {
                collectWhileInState(noteMarkRepository.getAllNotes()) { notes, state ->
                    if (notes.isEmpty()) {
                        state.noChange()
                    } else {
                        state.override {
                            NoteListStateWithNotes(
                                userName = state.snapshot.userName,
                                notes = notes.sortedByDescending { it.lastEditedAt }
                            )
                        }
                    }
                }
                collectWhileInState(userManager.getUser()) { user, state ->
                    state.mutate {
                        copy(
                            userName = user?.userName ?: ""
                        )
                    }
                }
            }

            inState<NoteListStateWithNotes> {
                collectWhileInState(noteMarkRepository.getAllNotes()) { notes, state ->
                    if (notes.isNotEmpty()) {
                        state.mutate {
                            NoteListStateWithNotes(
                                userName = state.snapshot.userName,
                                notes = notes.sortedByDescending { it.lastEditedAt }
                            )
                        }
                    } else {
                        state.override {
                            NoteListStateWithNoNotes(
                                userName = state.snapshot.userName
                            )
                        }
                    }
                }
                collectWhileInState(userManager.getUser()) { user, state ->
                    state.mutate {
                        copy(
                            userName = user?.userName ?: ""
                        )
                    }
                }
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
                        if (noteMarkRepository.deleteNote(noteEntity)) {
                            return@on state.override { state.snapshot.copy(notes = state.snapshot.notes.filter { it.uuid != action.uuid }) }
                        }
                    }
                    state.noChange()
                }
            }
        }
    }

    companion object {
        val defaultNoteListState = NoteListStateWithNoNotes(
            userName = null
        )
    }
}