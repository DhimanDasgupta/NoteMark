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
            val stateMachineFactory = RegistrationStateMachineFactory(
                noteMarkApi = FakeSuccessfulNoteMarkApi()
            )

            // Setup state flow from state machine
            val stateMachine = stateMachineFactory.launchIn(backgroundScope)
            val flow = stateMachine.state

            // Start flow validation
            flow.test {
                val initialState = awaitItem()
                assertEquals(RegistrationStateMachineFactory.defaultRegistrationState, initialState)
            }
        }
    }
}