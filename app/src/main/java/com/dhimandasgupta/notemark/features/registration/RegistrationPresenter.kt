package com.dhimandasgupta.notemark.features.registration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

@Immutable
data class RegistrationUiModel(
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
) {
    companion object {
        val Empty = RegistrationUiModel()
    }
}


class RegistrationPresenter(
    private val registrationStateMachine: RegistrationStateMachine
) {
    private val events = MutableSharedFlow<RegistrationAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): RegistrationUiModel {
        var registrationUiModel by remember(key1 = Unit) { mutableStateOf(value = RegistrationUiModel.Empty) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            registrationStateMachine.state
                .onEach { registrationState ->
                    registrationUiModel = registrationUiModel.copy(
                        userName = registrationState.userName,
                        email = registrationState.email,
                        password = registrationState.password,
                        repeatPassword = registrationState.repeatPassword,
                        userNameExplanation = registrationState.userNameExplanation,
                        userNameError = registrationState.userNameError,
                        emailError = registrationState.emailError,
                        passwordError = registrationState.passwordError,
                        repeatPasswordError = registrationState.repeatPasswordError,
                        passwordExplanation = registrationState.passwordExplanation,
                        registrationEnabled = registrationState.registrationEnabled,
                        registrationSuccess = registrationState.registrationSuccess
                    )
                }
                .flowOn(Dispatchers.Default)
                .onStart { RegistrationStateMachine.defaultRegistrationState }
                .cancellable()
                .catch { /* TODO if needed */ }
                .collect()
        }

        // Send the Events to the State Machine through Actions
        LaunchedEffect(key1 = Unit) {
            events.collect { loginAction ->
                registrationStateMachine.dispatch(loginAction)
            }
        }

        return registrationUiModel
    }

    fun processEvent(event: RegistrationAction) {
        events.tryEmit(value = event)
    }
}