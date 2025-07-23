package com.dhimandasgupta.notemark.features.notelist

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.UserRepository
import com.dhimandasgupta.notemark.database.NoteEntity
import com.dhimandasgupta.notemark.features.notelist.NoteListState.NoteListStateWithNoNotes
import com.dhimandasgupta.notemark.features.notelist.NoteListState.NoteListStateWithNotes
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
        val longClickedNoteUuid: String = ""
    ) : NoteListState
}

sealed interface NoteListAction {
    data class NoteDelete(val uuid: String) : NoteListAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class NoteListStateMachine(
    private val userRepository: UserRepository,
    private val noteMarkRepository: NoteMarkRepository
) : StateMachine<NoteListState, NoteListAction>(initialState = defaultNoteListState) {
    init {
        spec {
            inState<NoteListStateWithNoNotes> {
                collectWhileInState(flow = noteMarkRepository.getAllNotes()) { notes, state ->
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
                collectWhileInState(flow = userRepository.getUser()) { user, state ->
                    state.mutate {
                        copy(
                            userName = user?.userName ?: ""
                        )
                    }
                }
            }

            inState<NoteListStateWithNotes> {
                collectWhileInState(flow = noteMarkRepository.getAllNotes()) { notes, state ->
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
                collectWhileInState(flow = userRepository.getUser()) { user, state ->
                    state.mutate {
                        copy(
                            userName = user?.userName ?: ""
                        )
                    }
                }
                on<NoteListAction.NoteDelete> { action, state ->
                    noteMarkRepository.getNoteByUUID(uuid = action.uuid)?.let { noteEntity ->
                        if (noteMarkRepository.markAsDeleted(noteEntity)) {
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