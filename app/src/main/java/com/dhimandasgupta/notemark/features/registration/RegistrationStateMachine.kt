package com.dhimandasgupta.notemark.features.registration

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.extensions.isUsernameValid
import com.dhimandasgupta.notemark.common.extensions.isValidEmail
import com.dhimandasgupta.notemark.common.extensions.isValidPassword
import com.dhimandasgupta.notemark.data.remote.api.NoteMarkApi
import com.dhimandasgupta.notemark.data.remote.model.RegisterRequest
import com.dhimandasgupta.notemark.features.registration.RegistrationAction.EmailEntered
import com.dhimandasgupta.notemark.features.registration.RegistrationAction.PasswordEntered
import com.dhimandasgupta.notemark.features.registration.RegistrationAction.PasswordFiledInFocus
import com.dhimandasgupta.notemark.features.registration.RegistrationAction.RegisterClicked
import com.dhimandasgupta.notemark.features.registration.RegistrationAction.RepeatPasswordEntered
import com.dhimandasgupta.notemark.features.registration.RegistrationAction.UserNameEntered
import com.dhimandasgupta.notemark.features.registration.RegistrationAction.UserNameFiledInFocus
import com.dhimandasgupta.notemark.features.registration.RegistrationAction.UserNameFiledLostFocus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine

@Immutable
data class RegistrationState(
    val userName: String = "",
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val userNameExplanation: String? = null,
    val userNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val repeatPasswordError: String? = null,
    val passwordExplanation: String? = null,
    val registrationEnabled: Boolean = false,
    val registrationSuccess: Boolean? = null
)


sealed interface RegistrationAction {
    data class UserNameEntered(val userName: String) : RegistrationAction
    data class UserNameFiledInFocus(val userName: String) : RegistrationAction
    data class UserNameFiledLostFocus(val userName: String) : RegistrationAction
    data class EmailEntered(val email: String) : RegistrationAction
    data class PasswordEntered(val password: String) : RegistrationAction
    data class PasswordFiledInFocus(val password: String) : RegistrationAction
    data class RepeatPasswordEntered(val repeatPassword: String) : RegistrationAction
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
                on<UserNameFiledInFocus> { action, state ->
                    val modifiedState = if (action.userName.isEmpty()) {
                        state.snapshot.copy(
                            userNameExplanation = "Use between 3 and 20 characters for your username",
                            userNameError = null
                        )
                    } else {
                        state.snapshot.copy(
                            userNameExplanation = null,
                            userNameError = null
                        )
                    }
                    state.mutate { modifiedState }
                }
                on<UserNameFiledLostFocus> { action, state ->
                    val modifiedState = if (action.userName.isUsernameValid()) {
                        state.snapshot.copy(
                            userNameExplanation = null,
                            userNameError = null
                        )
                    } else {
                        state.snapshot.copy(
                            userNameExplanation = null,
                            userNameError = "Username must be at least 3 characters"
                        )
                    }
                    state.mutate { modifiedState }
                }
                on<UserNameEntered> { action, state ->
                    val modifiedState = state.snapshot.copy(
                        userName = action.userName,
                        userNameExplanation = if (action.userName.isEmpty())
                            "Use between 3 and 20 characters for your username"
                        else
                            null
                    )
                    state.mutate { modifiedState.validateNonEmptyInputs() }
                }
                on<EmailEntered> { action, state ->
                    val modifiedState = state.snapshot.copy(email = action.email)
                    state.mutate { modifiedState.validateNonEmptyInputs() }
                }
                on<PasswordFiledInFocus> { action, state ->
                    val modifiedState = state.snapshot.copy(
                        passwordExplanation = if (action.password.isEmpty())
                            "Use 8+ characters with a number or symbol for better security"
                        else
                            null,
                        passwordError = null
                    )
                    state.mutate { modifiedState }
                }
                on<PasswordEntered> { action, state ->
                    val modifiedState = state.snapshot.copy(
                        password = action.password,
                        passwordExplanation = if (action.password.isEmpty())
                            "Use 8+ characters with a number or symbol for better security"
                        else
                            null,
                        repeatPasswordError = if (action.password != state.snapshot.repeatPassword)
                            "Passwords do not match"
                        else
                            null
                    )
                    state.mutate { modifiedState.validateNonEmptyInputs() }
                }
                on<RepeatPasswordEntered> { action, state ->
                    var modifiedState = state.snapshot.copy(repeatPassword = action.repeatPassword)
                    modifiedState = modifiedState.copy(
                        repeatPasswordError = if (action.repeatPassword != state.snapshot.password)
                            "Passwords do not match"
                        else
                            null
                    )
                    state.mutate { modifiedState.validateNonEmptyInputs() }
                }
                on<RegisterClicked> { action, state ->
                    val modifiedState = state.snapshot.validateInputs()

                    if (modifiedState.userNameError != null) {
                        return@on state.mutate { modifiedState }
                    }

                    if (modifiedState.emailError?.isNotEmpty() == true
                        && modifiedState.passwordError?.isNotEmpty() == true
                        && modifiedState.repeatPasswordError?.isNotEmpty() == true) {
                        return@on state.mutate { modifiedState }
                    }

                    noteMarkApi.register(
                        RegisterRequest(
                            username = state.snapshot.userName,
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

private fun RegistrationState.validateNonEmptyInputs(): RegistrationState = this.copy(
    registrationEnabled = userName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && repeatPassword.isNotEmpty()
)

private fun RegistrationState.validateInputs(): RegistrationState {
    var updatedRegistrationState = this.copy(
        userNameError = if (!userName.isUsernameValid())
            "Username must be at least 3 characters"
        else
            null
    )
    updatedRegistrationState = updatedRegistrationState.copy(
        emailError = if (!email.isValidEmail())
            "Invalid email provided"
        else
            null
    )
    updatedRegistrationState = updatedRegistrationState.copy(
        passwordError = if (!password.isValidPassword())
            "Password must be at least 8 characters and include a number or symbol"
        else
            null
    )
    updatedRegistrationState = updatedRegistrationState.copy(
        repeatPasswordError = if (!repeatPassword.isValidPassword())
            "Password must be at least 8 characters and include a number or symbol"
        else
            null
    )
    return updatedRegistrationState
}
