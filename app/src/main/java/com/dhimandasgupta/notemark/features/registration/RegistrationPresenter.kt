package com.dhimandasgupta.notemark.features.registration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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
        val defaultOrEmpty = RegistrationUiModel()
    }
}

class RegistrationPresenter(
    private val registrationStateMachine: RegistrationStateMachineFactory
) {
    private val events = MutableSharedFlow<RegistrationAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): RegistrationUiModel {
        var registrationUiModel by remember(key1 = Unit) { mutableStateOf(value = RegistrationUiModel.defaultOrEmpty) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            val registrationStateMachine = this@RegistrationPresenter.registrationStateMachine.launchIn(this)

            launch {
                registrationStateMachine.state
                    .onStart { emit(value = RegistrationStateMachineFactory.defaultRegistrationState) }
                    .map { registrationState -> mapToRegistrationModel(registrationState) }
                    .cancellable()
                    .catch { throwable ->
                        if (throwable is CancellationException) throw throwable
                        // else can can be something like page level error etc.
                    }
                    .flowOn(context = Dispatchers.Default)
                    .collectLatest { mappedUiModel ->
                        registrationUiModel = mappedUiModel
                    }
            }

            // Send the Events to the State Machine through Actions
            launch {
                events.collect { loginAction ->
                    registrationStateMachine.dispatch(loginAction)
                }
            }
        }

        return registrationUiModel
    }

    fun dispatchAction(event: RegistrationAction) {
        events.tryEmit(value = event)
    }

    private fun mapToRegistrationModel(registrationState: RegistrationState): RegistrationUiModel {
        return RegistrationUiModel(
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