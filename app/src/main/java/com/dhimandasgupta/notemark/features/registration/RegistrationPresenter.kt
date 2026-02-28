package com.dhimandasgupta.notemark.features.registration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
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

@Stable
class RegistrationPresenter(
    private val registrationStateMachine: RegistrationStateMachineFactory
) {
    private val events = MutableSharedFlow<RegistrationAction>(extraBufferCapacity = 10)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Composable
    fun uiModel(): RegistrationUiModel {
        var registrationUiModel by remember(key1 = Unit) { mutableStateOf(value = RegistrationUiModel.defaultOrEmpty) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            val registrationStateMachine = registrationStateMachine.launchIn(this)

            launch {
                registrationStateMachine.state
                    .onStart { emit(value = RegistrationStateMachineFactory.defaultRegistrationState) }
                    .mapLatest { registrationState ->
                        registrationUiModel = registrationUiModel.mapToRegistrationModel(registrationState)
                    }
                    .cancellable()
                    .catch { throwable ->
                        if (throwable is CancellationException) throw throwable
                        // else can can be something like page level error etc.
                    }
                    .flowOn(context = Dispatchers.Default)
                    .collect()
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

    fun dispatchAction(event: RegistrationAction) =
        events.tryEmit(value = event)
}

private fun RegistrationUiModel.mapToRegistrationModel(
    registrationState: RegistrationState
) = this.copy(
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