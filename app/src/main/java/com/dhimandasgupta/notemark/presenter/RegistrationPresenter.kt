package com.dhimandasgupta.notemark.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleStartEffect
import com.dhimandasgupta.notemark.statemachine.RegistrationAction
import com.dhimandasgupta.notemark.statemachine.RegistrationStateMachine
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

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
        val scope = rememberCoroutineScope()
        var registrationUiModel by remember { mutableStateOf(RegistrationUiModel.Empty) }

        // Receives the State from the StateMachine
        LifecycleStartEffect(key1 = Unit) {
            scope.launch {
                registrationStateMachine.state.collect { registrationState ->
                    registrationUiModel = RegistrationUiModel(
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
            }
            onStopOrDispose { scope.cancel() }
        }

        // Send the Events to the State Machine through Actions
        LifecycleStartEffect(key1 = Unit) {
            scope.launch {
                events.collect { loginAction ->
                    registrationStateMachine.dispatch(loginAction)
                }
            }
            onStopOrDispose { scope.cancel() }
        }

        return registrationUiModel
    }

    fun processEvent(event: RegistrationAction) {
        events.tryEmit(event)
    }
}