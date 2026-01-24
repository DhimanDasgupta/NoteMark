package com.dhimandasgupta.notemark.features.editnote

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.dhimandasgupta.notemark.data.FakeSuccessfulNoteRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class EditNotePresenterTest {
    @Test
    fun `test presenter default state`() = runTest {
        turbineScope {
            // Setup Presenter
            val presenter = EditNotePresenter(
                noteId = "",
                editNoteStateMachineFactory = EditNoteStateMachineFactory(
                    noteMarkRepository = FakeSuccessfulNoteRepository(),
                    noteId = ""
                )
            )

            // Setup uiModel flow
            val flow = moleculeFlow(mode = RecompositionMode.Immediate) {
                presenter.uiModel()
            }

            // Start flow validation
            flow.test {
                val editNoteUiModel = awaitItem()
                assertEquals(EditNoteUiModel.Empty, editNoteUiModel)
            }
        }
    }
}