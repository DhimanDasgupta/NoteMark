package com.dhimandasgupta.notemark.features.addnote

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.dhimandasgupta.notemark.data.FakeFailureNoteRepository
import com.dhimandasgupta.notemark.data.FakeSuccessfulNoteRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AddNotePresenterTest {
    @Test
    fun `test presenter default state`() = runTest {
        turbineScope {
            // Setup Presenter
            val presenter = AddNotePresenter(
                addNoteStateMachine = AddNoteStateMachine(
                    noteMarkRepository = FakeSuccessfulNoteRepository()
                )
            )

            // Setup uiModel flow
            val flow = moleculeFlow(mode = RecompositionMode.Immediate) {
                presenter.uiModel()
            }

            // Start flow validation
            flow.test {
                val addUiModel = awaitItem()
                assertEquals(AddNoteUiModel.Empty, addUiModel)
            }
        }
    }

    @Test
    fun `test presenter state when title is entered`() = runTest {
        turbineScope {
            // Setup Presenter
            val presenter = AddNotePresenter(AddNoteStateMachine(FakeSuccessfulNoteRepository()))

            // Setup uiModel flow
            val flow = moleculeFlow(mode = RecompositionMode.Immediate) { presenter.uiModel() }

            // Start flow validation
            flow.test {
                val addUiModel = awaitItem()
                assertEquals(AddNoteUiModel.Empty, addUiModel)
                presenter.processEvent(AddNoteAction.UpdateTitle("Some title"))
                assertEquals(addUiModel.copy(title = "Some title"), awaitItem())
            }
        }
    }

    @Test
    fun `test presenter state when title and content are entered`() = runTest {
        turbineScope {
            // Setup Presenter
            val presenter = AddNotePresenter(AddNoteStateMachine(FakeSuccessfulNoteRepository()))

            // Setup uiModel flow
            val flow = moleculeFlow(mode = RecompositionMode.Immediate) { presenter.uiModel() }

            // Start flow validation
            flow.test {
                val addUiModel = awaitItem()
                assertEquals(AddNoteUiModel.Empty, addUiModel)
                presenter.processEvent(AddNoteAction.UpdateTitle("Some title"))
                assertEquals(addUiModel.copy(title = "Some title"), awaitItem())
                presenter.processEvent(AddNoteAction.UpdateContent("Some content"))
                assertEquals(addUiModel.copy(title = "Some title", content = "Some content"), awaitItem())
            }
        }
    }

    @Test
    fun `test presenter state when title and content are entered and then saved successfully`() = runTest {
        turbineScope {
            // Setup Presenter
            val presenter = AddNotePresenter(AddNoteStateMachine(FakeSuccessfulNoteRepository()))

            // Setup uiModel flow
            val flow = moleculeFlow(mode = RecompositionMode.Immediate) { presenter.uiModel() }

            // Start flow validation
            flow.test {
                val addUiModel = awaitItem()
                assertEquals(AddNoteUiModel.Empty, addUiModel)
                presenter.processEvent(AddNoteAction.UpdateTitle("Some title"))
                assertEquals(addUiModel.copy(title = "Some title"), awaitItem())
                presenter.processEvent(AddNoteAction.UpdateContent("Some content"))
                assertEquals(addUiModel.copy(title = "Some title", content = "Some content"), awaitItem())
                presenter.processEvent(AddNoteAction.Save)
                assertEquals(addUiModel.copy(title = "Some title", content = "Some content", saved = true), awaitItem())
            }
        }
    }

    @Test
    fun `test presenter state when title and content are entered and then save failed`() = runTest {
        turbineScope {
            // Setup Presenter
            val presenter = AddNotePresenter(AddNoteStateMachine(FakeFailureNoteRepository()))

            // Setup uiModel flow
            val flow = moleculeFlow(mode = RecompositionMode.Immediate) { presenter.uiModel() }

            // Start flow validation
            flow.test {
                val addUiModel = awaitItem()
                assertEquals(AddNoteUiModel.Empty, addUiModel)
                presenter.processEvent(AddNoteAction.UpdateTitle("Some title"))
                assertEquals(addUiModel.copy(title = "Some title"), awaitItem())
                presenter.processEvent(AddNoteAction.UpdateContent("Some content"))
                assertEquals(addUiModel.copy(title = "Some title", content = "Some content"), awaitItem())
                presenter.processEvent(AddNoteAction.Save)
                expectNoEvents()
            }
        }
    }
}