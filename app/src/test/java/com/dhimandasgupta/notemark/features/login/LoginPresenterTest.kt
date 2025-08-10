package com.dhimandasgupta.notemark.features.login

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.dhimandasgupta.notemark.data.FakeSuccessfulNoteRepository
import com.dhimandasgupta.notemark.data.remote.api.FakeSuccessfulNoteMarkApi
import com.dhimandasgupta.notemark.features.addnote.AddNotePresenter
import com.dhimandasgupta.notemark.features.addnote.AddNoteStateMachine
import com.dhimandasgupta.notemark.features.addnote.AddNoteUiModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class LoginPresenterTest {
    @Test
    fun `test presenter default state`() = runTest {
        turbineScope {
            // Setup Presenter
            val presenter = LoginPresenter(
                loginStateMachine = LoginStateMachine(
                    noteMarkApi = FakeSuccessfulNoteMarkApi()
                )
            )

            // Setup uiModel flow
            val flow = moleculeFlow(mode = RecompositionMode.Immediate) {
                presenter.uiModel()
            }

            // Start flow validation
            flow.test {
                val loginUiModel = awaitItem()
                assertEquals(LoginUiModel.Empty, loginUiModel)
            }
        }
    }
}