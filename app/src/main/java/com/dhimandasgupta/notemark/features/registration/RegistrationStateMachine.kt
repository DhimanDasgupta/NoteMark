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
import com.freeletics.flowredux2.FlowReduxStateMachineFactory
import com.freeletics.flowredux2.initializeWith
import kotlinx.coroutines.ExperimentalCoroutinesApi

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
    data object RegistrationChangeStatusConsumed : RegistrationAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationStateMachine(
    val noteMarkApi: NoteMarkApi
) : FlowReduxStateMachineFactory<RegistrationState, RegistrationAction>() {
    init {
        spec {
            initializeWith { defaultRegistrationState }
            inState<RegistrationState> {
                on<UserNameFiledInFocus> { action ->
                    val modifiedState = if (action.userName.isEmpty()) {
                        snapshot.copy(
                            userNameExplanation = "Use between 3 and 20 characters for your username",
                            userNameError = null
                        )
                    } else {
                        snapshot.copy(
                            userNameExplanation = null,
                            userNameError = null
                        )
                    }
                    mutate { modifiedState }
                }
                on<UserNameFiledLostFocus> { action ->
                    val modifiedState = if (action.userName.isUsernameValid()) {
                        snapshot.copy(
                            userNameExplanation = null,
                            userNameError = null
                        )
                    } else {
                        snapshot.copy(
                            userNameExplanation = null,
                            userNameError = "Username must be at least 3 characters"
                        )
                    }
                    mutate { modifiedState }
                }
                on<UserNameEntered> { action ->
                    val modifiedState = snapshot.copy(
                        userName = action.userName,
                        userNameExplanation = if (action.userName.isEmpty())
                            "Use between 3 and 20 characters for your username"
                        else
                            null
                    )
                    mutate { modifiedState.validateNonEmptyInputs() }
                }
                on<EmailEntered> { action ->
                    val modifiedState = snapshot.copy(email = action.email)
                    mutate { modifiedState.validateNonEmptyInputs() }
                }
                on<PasswordFiledInFocus> { action ->
                    val modifiedState = snapshot.copy(
                        passwordExplanation = if (action.password.isEmpty())
                            "Use 8+ characters with a number or symbol for better security"
                        else
                            null,
                        passwordError = null
                    )
                    mutate { modifiedState }
                }
                on<PasswordEntered> { action ->
                    val modifiedState = snapshot.copy(
                        password = action.password,
                        passwordExplanation = if (action.password.isEmpty())
                            "Use 8+ characters with a number or symbol for better security"
                        else
                            null,
                        repeatPasswordError = if (action.password != snapshot.repeatPassword)
                            "Passwords do not match"
                        else
                            null
                    )
                    mutate { modifiedState.validateNonEmptyInputs() }
                }
                on<RepeatPasswordEntered> { action ->
                    var modifiedState = snapshot.copy(repeatPassword = action.repeatPassword)
                    modifiedState = modifiedState.copy(
                        repeatPasswordError = if (action.repeatPassword != snapshot.password)
                            "Passwords do not match"
                        else
                            null
                    )
                    mutate { modifiedState.validateNonEmptyInputs() }
                }
                on<RegisterClicked> { _ ->
                    val modifiedState = snapshot.validateInputs()

                    if (modifiedState.userNameError != null) {
                        return@on mutate { modifiedState }
                    }

                    if (modifiedState.emailError?.isNotEmpty() == true
                        && modifiedState.passwordError?.isNotEmpty() == true
                        && modifiedState.repeatPasswordError?.isNotEmpty() == true
                    ) {
                        return@on mutate { modifiedState }
                    }

                    noteMarkApi.register(
                        request = RegisterRequest(
                            username = snapshot.userName,
                            email = snapshot.email,
                            password = snapshot.password
                        )
                    ).fold(onSuccess = {
                        mutate { copy(registrationSuccess = true) }
                    }, onFailure = {
                        mutate { copy(registrationSuccess = false) }
                    })
                }
                on<RegistrationAction.RegistrationChangeStatusConsumed> { _ ->
                    mutate { copy(registrationSuccess = null) }
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
