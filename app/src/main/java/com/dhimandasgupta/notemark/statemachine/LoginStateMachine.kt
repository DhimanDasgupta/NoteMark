package com.dhimandasgupta.notemark.statemachine

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.extensions.isValidEmail
import com.dhimandasgupta.notemark.common.extensions.isValidPassword
import com.dhimandasgupta.notemark.statemachine.LoginAction.EmailEntered
import com.dhimandasgupta.notemark.statemachine.LoginAction.PasswordEntered
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Immutable
data class LoginState(
    val email: String = "",
    val password: String = "",
    val emailValid: Boolean? = false,
    val passwordValid: Boolean? = false,
    val loginEnabled: Boolean = false
)


sealed interface LoginAction {
    data class EmailEntered(val email: String) : LoginAction
    data class PasswordEntered(val password: String) : LoginAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class LoginStateMachine : StateMachine<LoginState, LoginAction>(defaultLoginState) {
    init {
        spec {
            inState<LoginState> {
                on<EmailEntered> { action, state ->
                    val modifiedState = state.snapshot.copy(email = action.email)
                    state.mutate { modifiedState.validateAndReturn() }
                }
                on<PasswordEntered> { action, state ->
                    val modifiedState = state.snapshot.copy(password = action.password)
                    state.mutate { modifiedState.validateAndReturn() }
                }
            }
        }
    }

    companion object {
        val defaultLoginState = LoginState()
    }
}

private fun LoginState.validateAndReturn(): LoginState = this.copy(
    emailValid = email.isValidEmail(),
    passwordValid = password.isValidPassword(),
    loginEnabled = emailValid == true && passwordValid == true
)
