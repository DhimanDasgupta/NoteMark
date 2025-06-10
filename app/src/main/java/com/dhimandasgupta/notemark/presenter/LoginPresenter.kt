package com.dhimandasgupta.notemark.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.dhimandasgupta.notemark.statemachine.LoginAction
import com.dhimandasgupta.notemark.statemachine.LoginState
import com.dhimandasgupta.notemark.statemachine.LoginStateMachine
import com.dhimandasgupta.notemark.statemachine.LoginStateMachine.Companion.defaultLoginState
import kotlinx.coroutines.flow.MutableSharedFlow

class LoginPresenter(
    private val loginStateMachine: LoginStateMachine
) {
    private val events = MutableSharedFlow<LoginAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): LoginState {
        var loginScreenState by remember { mutableStateOf(defaultLoginState) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            loginStateMachine.state.collect { loginState ->
                loginScreenState = loginState
            }
        }

        // Send the Events to the State Machine through Actions
        LaunchedEffect(key1 = Unit) {
            events.collect { loginAction ->
                loginStateMachine.dispatch(loginAction)
            }
        }

        return loginScreenState
    }

    fun processEvent(event: LoginAction) {
        events.tryEmit(event)
    }
}