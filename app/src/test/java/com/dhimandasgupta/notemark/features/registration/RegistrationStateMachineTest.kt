package com.dhimandasgupta.notemark.features.registration

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.dhimandasgupta.notemark.data.remote.api.FakeSuccessfulNoteMarkApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RegistrationStateMachineTest {
    @Test
    fun `test RegistrationStateMachine with default state`() = runTest {
        turbineScope {
            // Setup state machine
            val stateMachine = RegistrationStateMachine(
                noteMarkApi = FakeSuccessfulNoteMarkApi()
            )

            // Setup state flow from state machine
            val flow = stateMachine.state

            // Start flow validation
            flow.test {
                val initialState = awaitItem()
                assertEquals(RegistrationStateMachine.defaultRegistrationState, initialState)
            }
        }
    }
}