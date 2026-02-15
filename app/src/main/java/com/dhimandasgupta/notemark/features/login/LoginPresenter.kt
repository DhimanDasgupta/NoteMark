package com.dhimandasgupta.notemark.features.login

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

class LoginPresenter(
    private val loginStateMachineFactory: LoginStateMachineFactory
) {
    private val events = MutableSharedFlow<LoginAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): LoginUiModel {
        var loginUiModel by remember(key1 = Unit) { mutableStateOf(value = LoginUiModel.defaultOrEmpty) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            val loginStateMachine = loginStateMachineFactory.launchIn(this)

            launch {
                loginStateMachine.state
                    .onStart { emit(value = LoginStateMachineFactory.defaultLoginState) }
                    .map { loginState ->
                        LoginUiModel(
                            email = loginState.email,
                            password = loginState.password,
                            emailError = loginState.emailError,
                            passwordError = loginState.passwordError,
                            loginEnabled = loginState.loginEnabled,
                            loginSuccess = loginState.loginSuccess
                        )
                    }
                    .cancellable()
                    .catch { throwable ->
                        if (throwable is CancellationException) throw throwable
                        // else can can be something like page level error etc.
                    }
                    .flowOn(context = Dispatchers.Default)
                    .collectLatest { uiModel ->
                        loginUiModel = uiModel
                    }
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