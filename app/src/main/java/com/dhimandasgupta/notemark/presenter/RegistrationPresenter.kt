package com.dhimandasgupta.notemark.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dhimandasgupta.notemark.statemachine.RegistrationAction
import com.dhimandasgupta.notemark.statemachine.RegistrationState
import com.dhimandasgupta.notemark.statemachine.RegistrationStateMachine
import com.dhimandasgupta.notemark.statemachine.RegistrationStateMachine.Companion.defaultRegistrationState
import kotlinx.coroutines.flow.MutableSharedFlow

class RegistrationPresenter(
    private val registrationStateMachine: RegistrationStateMachine
) {
    private val events = MutableSharedFlow<RegistrationAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): RegistrationState {
        var registrationScreenState by remember { mutableStateOf(defaultRegistrationState) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            registrationStateMachine.state.collect { registrationState ->
                registrationScreenState = registrationState
            }
        }

        // Send the Events to the State Machine through Actions
        LaunchedEffect(key1 = Unit) {
            events.collect { loginAction ->
                registrationStateMachine.dispatch(loginAction)
            }
        }

        return registrationScreenState
    }

    fun processEvent(event: RegistrationAction) {
        events.tryEmit(event)
    }
}