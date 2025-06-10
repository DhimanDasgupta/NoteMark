package com.dhimandasgupta.notemark.statemachine

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.extensions.isValidEmail
import com.dhimandasgupta.notemark.common.extensions.isValidPassword
import com.dhimandasgupta.notemark.statemachine.RegistrationAction.EmailEntered
import com.dhimandasgupta.notemark.statemachine.RegistrationAction.PasswordEntered
import com.dhimandasgupta.notemark.statemachine.RegistrationAction.RepeatPasswordEntered
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Immutable
data class RegistrationState(
    val userName: String = "",
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val emailValid: Boolean? = false,
    val passwordValid: Boolean? = false,
    val registrationEnabled: Boolean = false
)


sealed interface RegistrationAction {
    data class UserNameEntered(val userName: String) : RegistrationAction
    data class EmailEntered(val email: String) : RegistrationAction
    data class PasswordEntered(val password: String) : RegistrationAction
    data class RepeatPasswordEntered(val repeatPassword: String) : RegistrationAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationStateMachine : StateMachine<RegistrationState, RegistrationAction>(defaultRegistrationState) {
    init {
        spec {
            inState<RegistrationState> {
                on<RegistrationAction.UserNameEntered> { action, state ->
                    val modifiedState = state.snapshot.copy(userName = action.userName)
                    state.mutate { modifiedState.validateAndReturn() }
                }
                on<EmailEntered> { action, state ->
                    val modifiedState = state.snapshot.copy(email = action.email)
                    state.mutate { modifiedState.validateAndReturn() }
                }
                on<PasswordEntered> { action, state ->
                    val modifiedState = state.snapshot.copy(password = action.password)
                    state.mutate { modifiedState.validateAndReturn() }
                }
                on<RepeatPasswordEntered> { action, state ->
                    val modifiedState = state.snapshot.copy(repeatPassword = action.repeatPassword)
                    state.mutate { modifiedState.validateAndReturn() }
                }
            }
        }
    }

    companion object {
        val defaultRegistrationState = RegistrationState()
    }
}

private fun RegistrationState.validateAndReturn(): RegistrationState = this.copy(
    emailValid = email.isValidEmail(),
    passwordValid = password.isValidPassword(),
    registrationEnabled = emailValid == true && passwordValid == true && repeatPassword == password
)
