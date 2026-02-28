package com.dhimandasgupta.notemark.features.login

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
data class LoginUiModel(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val loginEnabled: Boolean = false,
    val loginSuccess: Boolean? = null
) {
    companion object {
        val defaultOrEmpty = LoginUiModel()
    }
}

@Stable
class LoginPresenter(
    private val loginStateMachineFactory: LoginStateMachineFactory
) {
    private val events = MutableSharedFlow<LoginAction>(extraBufferCapacity = 10)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Composable
    fun uiModel(): LoginUiModel {
        var loginUiModel by remember(key1 = Unit) { mutableStateOf(value = LoginUiModel.defaultOrEmpty) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            val loginStateMachine = loginStateMachineFactory.launchIn(this)

            launch {
                loginStateMachine.state
                    .onStart { emit(value = LoginStateMachineFactory.defaultLoginState) }
                    .mapLatest { loginState ->
                        loginUiModel = loginUiModel.mapToLoginUiModel(loginState)
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
                    loginStateMachine.dispatch(loginAction)
                }
            }
        }

        return loginUiModel
    }

    fun dispatchAction(event: LoginAction) =
        events.tryEmit(value = event)
}

private fun LoginUiModel.mapToLoginUiModel(
    loginState: LoginState
) = this.copy(
    email = loginState.email,
    password = loginState.password,
    emailError = loginState.emailError,
    passwordError = loginState.passwordError,
    loginEnabled = loginState.loginEnabled,
    loginSuccess = loginState.loginSuccess
)