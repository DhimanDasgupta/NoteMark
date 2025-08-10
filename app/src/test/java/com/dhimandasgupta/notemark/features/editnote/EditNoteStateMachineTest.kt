package com.dhimandasgupta.notemark.features.editnote

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.dhimandasgupta.notemark.data.FakeFailureNoteRepository
import com.dhimandasgupta.notemark.data.FakeSuccessfulNoteRepository
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class EditNoteStateMachineTest {
    @Test
    fun `test EditNoteStateMachine with default state`() = runTest {
        turbineScope {
            // Setup state machine
            val stateMachine = EditNoteStateMachine(
                noteMarkRepository = FakeSuccessfulNoteRepository()
            )

            // Setup state flow from state machine
            val flow = stateMachine.state

            // Start flow validation
            flow.test {
                val initialState = awaitItem()
                assertEquals(EditNoteStateMachine.defaultEditNoteState, initialState)
            }
        }
    }

    @Test
    fun `test EditNoteStateMachine with LoadNote action is successful`() = runTest {
        turbineScope {
            // Setup state machine
            val stateMachine = EditNoteStateMachine(
                noteMarkRepository = FakeSuccessfulNoteRepository()
            )

            // Setup state flow from state machine
            val flow = stateMachine.state

            // Start flow validation
            flow.test {
                val currentState = awaitItem()
                assertEquals(EditNoteStateMachine.defaultEditNoteState, currentState)
                stateMachine.dispatch(EditNoteAction.LoadNote("1"))
                assertEquals(
                    currentState.copy(
                        title = "title",
                        content = "content",
                        noteEntity = NoteEntity(
                            id = 1,
                            uuid = "some-uuid",
                            title = "title",
                            content = "content",
                            createdAt = "2025-06-29T19:18:24.369Z",
                            lastEditedAt = "2025-06-29T19:18:24.369Z",
                            synced = true,
                            markAsDeleted = false,
                        ),
                        saved = null,
                        mode = Mode.ViewMode
                    ),
                    awaitItem()
                )
            }
        }
    }

    @Test
    fun `test EditNoteStateMachine with UpdateNote action`() = runTest {
        turbineScope {
            // Setup state machine
            val stateMachine = EditNoteStateMachine(
                noteMarkRepository = FakeSuccessfulNoteRepository()
            )

            // Setup state flow from state machine
            val flow = stateMachine.state

            // Start flow validation
            flow.test {
                val currentState = awaitItem()
                assertEquals(EditNoteStateMachine.defaultEditNoteState, currentState)
                stateMachine.dispatch(
                    EditNoteAction.UpdateNote(
                        noteEntity = NoteEntity(
                            id = 1,
                            uuid = "some-uuid",
                            title = "title",
                            content = "content",
                            createdAt = "2025-06-29T19:18:24.369Z",
                            lastEditedAt = "2025-06-29T19:18:24.369Z",
                            synced = true,
                            markAsDeleted = false,
                        )
                    )
                )
                assertEquals(
                    currentState.copy(
                        title = "",
                        content = "",
                        noteEntity = NoteEntity(
                            id = 1,
                            uuid = "some-uuid",
                            title = "title",
                            content = "content",
                            createdAt = "2025-06-29T19:18:24.369Z",
                            lastEditedAt = "2025-06-29T19:18:24.369Z",
                            synced = true,
                            markAsDeleted = false,
                        ),
                        saved = null,
                        mode = Mode.ViewMode
                    ),
                    awaitItem()
                )
            }
        }
    }

    @Test
    fun `test EditNoteStateMachine with UpdateTitle action`() = runTest {
        turbineScope {
            // Setup state machine
            val stateMachine = EditNoteStateMachine(
                noteMarkRepository = FakeSuccessfulNoteRepository()
            )

            // Setup state flow from state machine
            val flow = stateMachine.state

            // Start flow validation
            flow.test {
                val currentState = awaitItem()
                assertEquals(EditNoteStateMachine.defaultEditNoteState, currentState)
                stateMachine.dispatch(EditNoteAction.UpdateTitle("some title"))
                assertEquals(
                    currentState.copy(
                        title = "some title",
                        content = "",
                        noteEntity = null,
                        saved = null,
                        mode = Mode.ViewMode
                    ),
                    awaitItem()
                )
            }
        }
    }

    @Test
    fun `test EditNoteStateMachine with UpdateContent action`() = runTest {
        turbineScope {
            // Setup state machine
            val stateMachine = EditNoteStateMachine(
                noteMarkRepository = FakeSuccessfulNoteRepository()
            )

            // Setup state flow from state machine
            val flow = stateMachine.state

            // Start flow validation
            flow.test {
                val currentState = awaitItem()
                assertEquals(EditNoteStateMachine.defaultEditNoteState, currentState)
                stateMachine.dispatch(EditNoteAction.UpdateContent("some content"))
                assertEquals(
                    currentState.copy(
                        title = "",
                        content = "some content",
                        noteEntity = null,
                        saved = null,
                        mode = Mode.ViewMode
                    ),
                    awaitItem()
                )
            }
        }
    }

    @Test
    fun `test EditNoteStateMachine with LoadNote action is failure`() = runTest {
        turbineScope {
            // Setup state machine
            val stateMachine = EditNoteStateMachine(
                noteMarkRepository = FakeFailureNoteRepository()
            )

            // Setup state flow from state machine
            val flow = stateMachine.state

            // Start flow validation
            flow.test {
                val currentState = awaitItem()
                assertEquals(EditNoteStateMachine.defaultEditNoteState, currentState)
                stateMachine.dispatch(EditNoteAction.LoadNote("1"))
                expectNoEvents()
            }
        }
    }
}