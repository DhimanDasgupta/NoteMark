package com.dhimandasgupta.notemark.statemachine

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.extensions.isValidEmail
import com.dhimandasgupta.notemark.common.extensions.isValidPassword
import com.dhimandasgupta.notemark.network.NoteMarkApi
import com.dhimandasgupta.notemark.network.model.LoginRequest
import com.dhimandasgupta.notemark.statemachine.LoginAction.EmailEntered
import com.dhimandasgupta.notemark.statemachine.LoginAction.EmailFocusChanged
import com.dhimandasgupta.notemark.statemachine.LoginAction.PasswordEntered
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine

@Immutable
data class LoginState(
    val email: String = "",
    val password: String = "",
    val emailValid: Boolean? = false,
    val emailError: String? = null,
    val passwordValid: Boolean? = false,
    val passwordError: String? = null,
    val loginEnabled: Boolean = false,
    val loginSuccess: Boolean? = null
)


sealed interface LoginAction {
    data class EmailEntered(val email: String) : LoginAction
    data class PasswordEntered(val password: String) : LoginAction
    data object EmailFocusChanged : LoginAction
    data object PasswordFocusChanged : LoginAction
    data object LoginClicked : LoginAction
    data object HideLoginButton : LoginAction
    data object LoginChangeConsumed: LoginAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class LoginStateMachine(
    val noteMarkApi: NoteMarkApi
) : StateMachine<LoginState, LoginAction>(defaultLoginState) {
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
                on<EmailFocusChanged> { action, state ->
                    val newStateWithValidationApplied = state.snapshot.validateAndReturn()
                    val modifiedState = newStateWithValidationApplied.copy(
                        emailError = if (newStateWithValidationApplied.emailValid == false) "Please enter valid email" else null
                    )
                    state.mutate { modifiedState }
                }
                on<LoginAction.PasswordFocusChanged> { action, state ->
                    val newStateWithValidationApplied = state.snapshot.validateAndReturn()
                    val modifiedState = newStateWithValidationApplied.copy(
                        passwordError = if (newStateWithValidationApplied.passwordValid == false) "Please enter password" else null
                    )
                    state.mutate { modifiedState }
                }
                on<LoginAction.HideLoginButton> { action, state ->
                    val modifiedState = state.snapshot.copy(loginEnabled = false)
                    state.mutate { modifiedState }
                }
                on<LoginAction.LoginClicked> { action, state ->
                    noteMarkApi.login(
                        LoginRequest(
                            email = state.snapshot.email,
                            password = state.snapshot.password
                        )
                    ).fold(onSuccess = {
                        state.mutate { state.snapshot.copy(loginSuccess = true, loginEnabled = true) }
                    }, onFailure = {
                        state.mutate { state.snapshot.copy(loginSuccess = false, loginEnabled = true) }
                    })
                }
                on<LoginAction.LoginChangeConsumed> { action, state ->
                    state.mutate { state.snapshot.copy(loginSuccess = null) }
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
