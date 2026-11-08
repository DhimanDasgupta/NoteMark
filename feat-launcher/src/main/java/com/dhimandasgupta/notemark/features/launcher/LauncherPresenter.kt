package com.dhimandasgupta.notemark.features.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.common.state.impl.AppStateStore
import com.dhimandasgupta.notemark.common.state.impl.LoggedIn
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class LauncherUiModel(
  val connectionState: ConnectionState? = ConnectionState.Unavailable,
  val loggedInUser: UiUser? = null,
) {
  companion object {
    val defaultOrEmpty = LauncherUiModel()
  }
}

@Serializable
@Immutable
data class UiUser(
  val userName: String = "",
  val accessToken: String = "",
  val refreshToken: String = "",
)

@Stable
@Inject
class LauncherPresenter(private val appStateStore: AppStateStore) {
  @OptIn(ExperimentalCoroutinesApi::class)
  @Composable
  fun uiModel(): LauncherUiModel {
    var launcherUiModel by
      remember(key1 = Unit) { mutableStateOf(value = LauncherUiModel.defaultOrEmpty) }

    // Receives the State from the app-wide StateMachine
    LaunchedEffect(key1 = Unit) {
      appStateStore.state
        .mapLatest { appState ->
          launcherUiModel =
            launcherUiModel.copy(
              connectionState = appState.connectionState,
              loggedInUser =
                when (appState) {
                  is LoggedIn ->
                    UiUser(
                      userName = appState.user.userName,
                      accessToken = appState.user.accessToken,
                      refreshToken = appState.user.refreshToken,
                    )
                  else -> null
                },
            )
        }
        .cancellable()
        .catch { throwable ->
          if (throwable is CancellationException) throw throwable
          // else can can be something like page level error etc.
        }
        .flowOn(context = Dispatchers.Default)
        .collect()
    }

    return launcherUiModel
  }
}
