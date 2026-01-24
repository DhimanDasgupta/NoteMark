package com.dhimandasgupta.notemark.features.login

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.extensions.isValidEmail
import com.dhimandasgupta.notemark.common.extensions.isValidPassword
import com.dhimandasgupta.notemark.data.remote.api.NoteMarkApi
import com.dhimandasgupta.notemark.data.remote.model.LoginRequest
import com.dhimandasgupta.notemark.features.login.LoginAction.EmailEntered
import com.dhimandasgupta.notemark.features.login.LoginAction.PasswordEntered
import com.freeletics.flowredux2.FlowReduxStateMachineFactory as StateMachineFactory
import com.freeletics.flowredux2.initializeWith
import kotlinx.coroutines.ExperimentalCoroutinesApi

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
class LoginStateMachineFactory(
    val noteMarkApi: NoteMarkApi
) : StateMachineFactory<LoginState, LoginAction>() {
    init {
        spec {
            initializeWith { defaultLoginState }

            inState<LoginState> {
                on<EmailEntered> { action ->
                    mutate { copy(email = action.email).validateNonEmptyInputs() }
                }
                on<PasswordEntered> { action ->
                    mutate { copy(password = action.password).validateNonEmptyInputs() }
                }
                on<LoginAction.HideLoginButton> { _ ->
                    mutate { copy(loginEnabled = false) }
                }
                on<LoginAction.LoginClicked> { _ ->
                    val modifiedState = snapshot.validateInputs()

                    if (modifiedState.emailError?.isNotEmpty() == true && modifiedState.passwordError?.isNotEmpty() == true) {
                        return@on mutate { modifiedState }
                    }

                    noteMarkApi.login(
                        request = LoginRequest(
                            email = snapshot.email,
                            password = snapshot.password
                        )
                    ).fold(onSuccess = {
                        mutate { copy(loginSuccess = true, loginEnabled = true) }
                    }, onFailure = {
                        mutate { copy(loginSuccess = false, loginEnabled = true) }
                    })
                }
                on<LoginAction.LoginChangeConsumed> { _ ->
                    mutate { copy(loginSuccess = null) }
                }
            }
        }
    }

    companion object Companion {
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
