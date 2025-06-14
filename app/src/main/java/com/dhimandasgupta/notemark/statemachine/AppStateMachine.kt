package com.dhimandasgupta.notemark.statemachine

import androidx.compose.runtime.Immutable
import com.dhimandasgupta.notemark.network.storage.TokenManager
import io.ktor.client.plugins.auth.providers.BearerTokens
import com.freeletics.flowredux.dsl.FlowReduxStateMachine as StateMachine
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Immutable
data class AppState(
    val bearerTokens: BearerTokens? = null
)

sealed interface AppAction

@OptIn(ExperimentalCoroutinesApi::class)
class AppStateMachine(
    val tokenManager: TokenManager
) : StateMachine<AppState, AppAction>(defaultAppState) {

    init {
        spec {
            inState<AppState> {
                collectWhileInState(tokenManager.getToken()) { valueEmittedFromFlow, state ->
                    state.mutate { state.snapshot.copy(bearerTokens = valueEmittedFromFlow) }
                }
            }
        }
    }

    companion object {
        val defaultAppState = AppState()
    }
}

