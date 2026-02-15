package com.dhimandasgupta.notemark.features.registration

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.dhimandasgupta.notemark.data.remote.api.FakeSuccessfulNoteMarkApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RegistrationPresenterTest {
    @Test
    fun `test presenter default state`() = runTest {
        turbineScope {
            // Setup Presenter
            val presenter = RegistrationPresenter(
                registrationStateMachine = RegistrationStateMachineFactory(
                    noteMarkApi = FakeSuccessfulNoteMarkApi()
                )
            )

            // Setup uiModel flow
            val flow = moleculeFlow(mode = RecompositionMode.Immediate) {
                presenter.uiModel()
            }

            // Start flow validation
            flow.test {
                val registrationUiModel = awaitItem()
                assertEquals(RegistrationUiModel.defaultOrEmpty, registrationUiModel)
            }
        }
    }
}