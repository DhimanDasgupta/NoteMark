package com.dhimandasgupta.notemark.features.notelist

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.UserRepository
import com.dhimandasgupta.notemark.database.NoteEntity
import com.dhimandasgupta.notemark.features.notelist.NoteListState.NoteListStateWithNoNotes
import com.dhimandasgupta.notemark.features.notelist.NoteListState.NoteListStateWithNotes
import com.freeletics.flowredux2.FlowReduxStateMachineFactory as StateMachineFactory
import com.freeletics.flowredux2.initializeWith
import kotlinx.coroutines.ExperimentalCoroutinesApi

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
class NoteListStateMachineFactory(
    private val userRepository: UserRepository,
    private val noteMarkRepository: NoteMarkRepository
) : StateMachineFactory<NoteListState, NoteListAction>() {
    init {
        spec {
            initializeWith { defaultNoteListState }

            inState<NoteListStateWithNoNotes> {
                collectWhileInState(flow = noteMarkRepository.getAllNotes()) { notes ->
                    if (notes.isEmpty()) {
                        noChange()
                    } else {
                        override {
                            NoteListStateWithNotes(
                                userName = userName,
                                notes = notes.sortedByDescending { it.lastEditedAt }
                            )
                        }
                    }
                }
                collectWhileInState(flow = userRepository.getUser()) { user ->
                    mutate {
                        copy(
                            userName = user?.userName ?: ""
                        )
                    }
                }
            }

            inState<NoteListStateWithNotes> {
                collectWhileInState(flow = noteMarkRepository.getAllNotes()) { notes ->
                    if (notes.isNotEmpty()) {
                        mutate {
                            NoteListStateWithNotes(
                                userName = userName,
                                notes = notes.sortedByDescending { it.lastEditedAt }
                            )
                        }
                    } else {
                        override {
                            NoteListStateWithNoNotes(
                                userName = userName
                            )
                        }
                    }
                }
                collectWhileInState(flow = userRepository.getUser()) { user ->
                    mutate {
                        copy(
                            userName = user?.userName ?: ""
                        )
                    }
                }
                on<NoteListAction.NoteDelete> { action ->
                    noteMarkRepository.getNoteByUUID(uuid = action.uuid)?.let { noteEntity ->
                        if (noteMarkRepository.markAsDeleted(noteEntity)) {
                            return@on override { copy(notes = notes.filter { it.uuid != action.uuid }) }
                        }
                    }
                    noChange()
                }
            }
        }
    }

    companion object Companion {
        val defaultNoteListState = NoteListStateWithNoNotes(
            userName = null
        )
    }
}