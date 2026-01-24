package com.dhimandasgupta.notemark.features.addnote

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.dhimandasgupta.notemark.data.FakeFailureNoteRepository
import com.dhimandasgupta.notemark.data.FakeSuccessfulNoteRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AddNoteStateMachineTest {
    @Test
    fun `test AddNoteStateMachine with default state`() = runTest {
        turbineScope {
            // Setup state machine
            val stateMachineFactory = AddNoteStateMachineFactory(
                noteMarkRepository = FakeSuccessfulNoteRepository()
            )

            // Setup state flow from state machine
            val stateMachine = stateMachineFactory.launchIn(backgroundScope)
            val flow = stateMachine.state

            // Start flow validation
            flow.test {
                val initialState = awaitItem()
                assertEquals(AddNoteStateMachineFactory.defaultAddNoteState, initialState)
            }
        }
    }

    @Test
    fun `test AddNoteStateMachine with title and content`() = runTest {
        turbineScope {
            // Setup state machine
            val stateMachineFactory = AddNoteStateMachineFactory(
                noteMarkRepository = FakeSuccessfulNoteRepository()
            )

            // Setup state flow from state machine
            val stateMachine = stateMachineFactory.launchIn(backgroundScope)
            val flow = stateMachine.state

            // Start flow validation
            flow.test {
                val currentState = awaitItem()
                stateMachine.dispatch(AddNoteAction.UpdateTitle("Title"))
                assertEquals(currentState.copy(title = "Title"), awaitItem())
                stateMachine.dispatch(AddNoteAction.UpdateContent("Content"))
                assertEquals(currentState.copy(title = "Title", content = "Content"), awaitItem())
            }
        }
    }

    @Test
    fun `test AddNoteStateMachine when create note is successful`() = runTest {
        turbineScope {
            // Setup state machine
            val stateMachineFactory = AddNoteStateMachineFactory(
                noteMarkRepository = FakeSuccessfulNoteRepository()
            )

            // Setup state flow from state machine
            val stateMachine = stateMachineFactory.launchIn(backgroundScope)
            val flow = stateMachine.state

            // Start flow validation
            flow.test {
                val currentState = awaitItem()
                stateMachine.dispatch(AddNoteAction.UpdateTitle("Title"))
                assertEquals(currentState.copy(title = "Title"), awaitItem())
                stateMachine.dispatch(AddNoteAction.UpdateContent("Content"))
                assertEquals(currentState.copy(title = "Title", content = "Content"), awaitItem())
                stateMachine.dispatch(AddNoteAction.Save)
                assertEquals(currentState.copy(title = "Title", content = "Content", saved = true), awaitItem())
            }
        }
    }

    @Test
    fun `test AddNoteStateMachine when create note is failed`() = runTest {
        turbineScope {
            // Setup state machine
            val stateMachineFactory = AddNoteStateMachineFactory(
                noteMarkRepository = FakeFailureNoteRepository()
            )

            // Setup state flow from state machine
            val stateMachine = stateMachineFactory.launchIn(backgroundScope)
            val flow = stateMachine.state

            // Start flow validation
            flow.test {
                val currentState = awaitItem()
                stateMachine.dispatch(AddNoteAction.UpdateTitle("Title"))
                assertEquals(currentState.copy(title = "Title"), awaitItem())
                stateMachine.dispatch(AddNoteAction.UpdateContent("Content"))
                assertEquals(currentState.copy(title = "Title", content = "Content"), awaitItem())
                stateMachine.dispatch(AddNoteAction.Save)
                // Since on Failed Save, the state doesn't change hence no new items are emitted in the flow.
                expectNoEvents()
            }
        }
    }
}