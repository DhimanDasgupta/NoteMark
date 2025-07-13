package com.dhimandasgupta.notemark.features.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleStartEffect
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
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
        val Empty = LoginUiModel()
    }
}

class LoginPresenter(
    private val loginStateMachine: LoginStateMachine
) {
    private val events = MutableSharedFlow<LoginAction>(extraBufferCapacity = 10)

    @Composable
    fun uiModel(): LoginUiModel {
        val scope = rememberCoroutineScope()
        var loginUiModel by remember { mutableStateOf(LoginUiModel.Empty) }

        // Receives the State from the StateMachine
        LifecycleStartEffect(key1 = Unit) {
            scope.launch {
                loginStateMachine.state.collect { loginState ->
                    loginUiModel = LoginUiModel(
                        email = loginState.email,
                        password = loginState.password,
                        emailError = loginState.emailError,
                        passwordError = loginState.passwordError,
                        loginEnabled = loginState.loginEnabled,
                        loginSuccess = loginState.loginSuccess
                    )
                }
            }
            onStopOrDispose { scope.cancel() }
        }

        // Send the Events to the State Machine through Actions
        LifecycleStartEffect(key1 = Unit) {
            scope.launch {
                events.collect { loginAction ->
                    loginStateMachine.dispatch(loginAction)
                }
            }
            onStopOrDispose { scope.cancel() }
        }

        return loginUiModel
    }

    fun processEvent(event: LoginAction) {
        events.tryEmit(event)
    }
}