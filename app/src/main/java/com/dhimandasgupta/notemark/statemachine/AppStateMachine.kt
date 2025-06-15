package com.dhimandasgupta.notemark.statemachine

import android.content.Context
import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.common.android.observeConnectivityAsFlow
import com.dhimandasgupta.notemark.network.storage.TokenManager
import io.ktor.client.plugins.auth.providers.BearerTokens
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Immutable
data class AppState(
    val bearerTokens: BearerTokens? = null,
    val connectionState: ConnectionState? = null
)

sealed interface AppAction {
    object ConnectionStateConsumed: AppAction
    object AppLogout : AppAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class AppStateMachine(
    val applicationContext: Context,
    val tokenManager: TokenManager
) : StateMachine<AppState, AppAction>(defaultAppState) {

    init {
        spec {
            inState<AppState> {
                // All Flows while in the app state should be collected here
                collectWhileInState(tokenManager.getToken()) { bearerTokens, state ->
                    state.mutate { state.snapshot.copy(bearerTokens = bearerTokens) }
                }
                collectWhileInState(applicationContext.observeConnectivityAsFlow()) { connected, state ->
                    state.mutate { state.snapshot.copy(connectionState = connected) }
                }

                // All the actions valid for app state should be handled here
                on<AppAction.ConnectionStateConsumed> { _, state ->
                    state.mutate { state.snapshot.copy(connectionState = null) }
                }
                on<AppAction.AppLogout> { _, state ->
                    tokenManager.clearToken()
                    //state.mutate { state.snapshot.copy(bearerTokens = null) }
                    state.noChange()
                }
            }
        }
    }

    companion object {
        val defaultAppState = AppState()
    }
}

