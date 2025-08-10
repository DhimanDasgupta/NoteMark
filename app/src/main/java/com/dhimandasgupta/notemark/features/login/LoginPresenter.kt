package com.dhimandasgupta.notemark.features.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

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
        var loginUiModel by remember(key1 = Unit) { mutableStateOf(value = LoginUiModel.Empty) }

        // Receives the State from the StateMachine
        LaunchedEffect(key1 = Unit) {
            loginStateMachine.state
                .flowOn(Dispatchers.Default)
                .catch { /* TODO if needed */  }
                .collect { loginState ->
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

        // Send the Events to the State Machine through Actions
        LaunchedEffect(key1 = Unit) {
            events.collect { loginAction ->
                loginStateMachine.dispatch(loginAction)
            }
        }

        return loginUiModel
    }

    fun processEvent(event: LoginAction) {
        events.tryEmit(value = event)
    }
}