package com.dhimandasgupta.notemark.features.editnote

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class EditNotePresenterTest {
  @Test
  fun `test presenter default state`() = runTest {
    turbineScope {
      // Setup Presenter
      val presenter =
        EditNotePresenter(
          noteId = "",
          editNoteStateMachineFactoryBuilder =
            object : EditNoteStateMachineFactoryBuilder {
              override fun create(noteId: String): EditNoteStateMachineFactory {
                return EditNoteStateMachineFactory(
                  noteMarkRepository = FakeSuccessfulNoteRepository(),
                  noteId = noteId,
                )
              }
            },
        )

      // Setup uiModel flow
      val flow =
        moleculeFlow(mode = RecompositionMode.Immediate) {
          presenter.uiModel()
        }

      // Start flow validation
      flow.test {
        val editNoteUiModel = awaitItem()
        assertEquals(EditNoteUiModel.defaultOrEmpty, editNoteUiModel)
        // The note loads on Dispatchers.Default and may or may not land before the block ends.
        cancelAndIgnoreRemainingEvents()
      }
    }
  }
}
