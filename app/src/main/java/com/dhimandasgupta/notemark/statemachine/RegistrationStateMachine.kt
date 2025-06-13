package com.dhimandasgupta.notemark.statemachine

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.extensions.isUsernameValid
import com.dhimandasgupta.notemark.common.extensions.isValidEmail
import com.dhimandasgupta.notemark.common.extensions.isValidPassword
import com.dhimandasgupta.notemark.network.NoteMarkApi
import com.dhimandasgupta.notemark.network.model.RegisterRequest
import com.dhimandasgupta.notemark.network.storage.TokenStorage
import com.dhimandasgupta.notemark.statemachine.RegistrationAction.EmailEntered
import com.dhimandasgupta.notemark.statemachine.RegistrationAction.EmailFocusChanged
import com.dhimandasgupta.notemark.statemachine.RegistrationAction.PasswordEntered
import com.dhimandasgupta.notemark.statemachine.RegistrationAction.PasswordFocusChanged
import com.dhimandasgupta.notemark.statemachine.RegistrationAction.RepeatPasswordEntered
import com.dhimandasgupta.notemark.statemachine.RegistrationAction.UserNameFocusChanged
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Immutable
data class RegistrationState(
    val userName: String = "",
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val userNameValid: Boolean? = false,
    val userNameError: String? = null,
    val emailValid: Boolean? = false,
    val emailError: String? = null,
    val passwordValid: Boolean? = false,
    val passwordError: String? = null,
    val repeatPasswordValid: Boolean? = false,
    val repeatPasswordError: String? = null,
    val registrationEnabled: Boolean = false,
    val registrationSuccess: Boolean? = null
)


sealed interface RegistrationAction {
    data class UserNameEntered(val userName: String) : RegistrationAction
    data class EmailEntered(val email: String) : RegistrationAction
    data class PasswordEntered(val password: String) : RegistrationAction
    data class RepeatPasswordEntered(val repeatPassword: String) : RegistrationAction
    data object UserNameFocusChanged : RegistrationAction
    data object EmailFocusChanged : RegistrationAction
    data object PasswordFocusChanged : RegistrationAction
    data object RepeatPasswordFocusChanged : RegistrationAction
    data object RegisterClicked : RegistrationAction
    data object RegistrationChangeStatusConsumed: RegistrationAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationStateMachine(
    val noteMarkApi: NoteMarkApi
) : StateMachine<RegistrationState, RegistrationAction>(defaultRegistrationState) {
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
                on<UserNameFocusChanged> { action, state ->
                    val newStateWithValidationApplied = state.snapshot.validateAndReturn()
                    val modifiedState = newStateWithValidationApplied.copy(
                        userNameError = if (newStateWithValidationApplied.userNameValid == false) "Please enter valid username" else null
                    )
                    state.mutate { modifiedState }
                }
                on<EmailFocusChanged> { action, state ->
                    val newStateWithValidationApplied = state.snapshot.validateAndReturn()
                    val modifiedState = newStateWithValidationApplied.copy(
                        emailError = if (newStateWithValidationApplied.emailValid == false) "Please enter valid email" else null
                    )
                    state.mutate { modifiedState }
                }
                on<PasswordFocusChanged> { action, state ->
                    val newStateWithValidationApplied = state.snapshot.validateAndReturn()
                    val modifiedState = newStateWithValidationApplied.copy(
                        passwordError = if (newStateWithValidationApplied.passwordValid == false) "Please enter password" else null
                    )
                    state.mutate { modifiedState }
                }
                on<RegistrationAction.RepeatPasswordFocusChanged> { action, state ->
                    val newStateWithValidationApplied = state.snapshot.validateAndReturn()
                    val modifiedState = newStateWithValidationApplied.copy(
                        repeatPasswordError = if (newStateWithValidationApplied.repeatPasswordValid == false) "Please enter same password here" else null
                    )
                    state.mutate { modifiedState }
                }
                on<RegistrationAction.RegisterClicked> { action, state ->
                    noteMarkApi.register(
                        RegisterRequest(
                            userName = state.snapshot.userName,
                            email = state.snapshot.email,
                            password = state.snapshot.password
                        )
                    ).fold(onSuccess = {
                        state.mutate { state.snapshot.copy(registrationSuccess = true) }
                    }, onFailure = {
                        state.mutate { state.snapshot.copy(registrationSuccess = false) }
                    })
                }
                on<RegistrationAction.RegistrationChangeStatusConsumed> { action, state ->
                    state.mutate { state.snapshot.copy(registrationSuccess = null) }
                }
            }
        }
    }

    companion object {
        val defaultRegistrationState = RegistrationState()
    }
}

private fun RegistrationState.validateAndReturn(): RegistrationState = this.copy(
    userNameValid = userName.isUsernameValid(),
    emailValid = email.isValidEmail(),
    passwordValid = password.isValidPassword(),
    repeatPasswordValid = repeatPassword.isValidPassword(),
    registrationEnabled = emailValid == true && passwordValid == true && repeatPassword == password
)
