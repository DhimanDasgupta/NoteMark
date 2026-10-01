# This is an experiment with [FlowRedux](https://github.com/freeletics/FlowRedux) and [Molecule](https://github.com/cashapp/molecule)

## Building the Project

A comprehensive build script `build.sh` is provided in the project root:

```bash
# Display help and available options
./build.sh --help

# Run full pipeline: clean, stability check, lint, tests, and assemble
./build.sh full

# Build debug APK
./build.sh debug

# Build release APK and App Bundle (AAB)
./build.sh release

# Run all quality checks (stability, lint, unit tests)
./build.sh check

# Run unit tests only
./build.sh test
```

# App State Machine
```
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
```
