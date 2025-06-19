package com.dhimandasgupta.notemark.statemachine

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.extensions.isValidEmail
import com.dhimandasgupta.notemark.common.extensions.isValidPassword
import com.dhimandasgupta.notemark.data.remote.api.NoteMarkApi
import com.dhimandasgupta.notemark.data.remote.model.LoginRequest
import com.dhimandasgupta.notemark.statemachine.LoginAction.EmailEntered
import com.dhimandasgupta.notemark.statemachine.LoginAction.PasswordEntered
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine

@Immutable
data class LoginState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val loginEnabled: Boolean = false,
    val loginSuccess: Boolean? = null
)


sealed interface LoginAction {
    data class EmailEntered(val email: String) : LoginAction
    data class PasswordEntered(val password: String) : LoginAction
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
                    state.mutate { modifiedState.validateNonEmptyInputs() }
                }
                on<PasswordEntered> { action, state ->
                    val modifiedState = state.snapshot.copy(password = action.password)
                    state.mutate { modifiedState.validateNonEmptyInputs() }
                }
                on<LoginAction.HideLoginButton> { action, state ->
                    val modifiedState = state.snapshot.copy(loginEnabled = false)
                    state.mutate { modifiedState }
                }
                on<LoginAction.LoginClicked> { action, state ->
                    val modifiedState = state.snapshot.validateInputs()

                    if (modifiedState.emailError?.isNotEmpty() == true && modifiedState.passwordError?.isNotEmpty() == true) {
                        return@on state.mutate { modifiedState }
                    }

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

private fun LoginState.validateNonEmptyInputs(): LoginState = this.copy(
    loginEnabled = email.isNotEmpty() && password.isNotEmpty()
)

private fun LoginState.validateInputs(): LoginState {
    var updatedLoginState = this.copy(emailError = if (!email.isValidEmail()) "Invalid email provided" else null)
    updatedLoginState = updatedLoginState.copy(passwordError = if (!password.isValidPassword()) "Please enter password" else null)
    return updatedLoginState
}
