package com.dhimandasgupta.notemark.features.launcher

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.common.state.impl.AppLogout
import com.dhimandasgupta.notemark.common.state.impl.AppStateMachineFactory
import com.dhimandasgupta.notemark.proto.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherPresenterTest {
  private val user =
    User.newBuilder()
      .setUserName("Some User name")
      .setAccessToken("Some access token")
      .setRefreshToken("Some refresh token")
      .build()

  private val uiUser =
    UiUser(
      userName = "Some User name",
      accessToken = "Some access token",
      refreshToken = "Some refresh token",
    )

  private val connectivity = MutableStateFlow<ConnectionState>(value = ConnectionState.Available)
  private val userRepository = FakeUserRepository()
  private val noteMarkRepository = FakeNoteMarkRepository(logoutResult = Result.success(Unit))

  private val presenter =
    LauncherPresenter(
      appStateMachineFactory =
        AppStateMachineFactory(
          applicationContext = fakeApplication(versionName = "1.2.3"),
          userRepository = userRepository,
          syncRepository = FakeSyncRepository(),
          noteMarkRepository = noteMarkRepository,
          syncScheduler = FakeSyncScheduler(),
          appShortcuts = FakeAppShortcuts(),
          connectivityObserver = { connectivity },
        )
    )

  private val uiModels = moleculeFlow(mode = RecompositionMode.Immediate) { presenter.uiModel() }

  @Test
  fun `starts with the default ui model`() = runTest {
    uiModels.test {
      assertEquals(LauncherUiModel.defaultOrEmpty, awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `connection state follows the app state`() = runTest {
    uiModels.test {
      awaitUiModel(LauncherUiModel(connectionState = ConnectionState.Available))

      connectivity.value = ConnectionState.Unavailable

      awaitUiModel(LauncherUiModel(connectionState = ConnectionState.Unavailable))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `maps the logged in user`() = runTest {
    uiModels.test {
      awaitUiModel(LauncherUiModel(connectionState = ConnectionState.Available))

      userRepository.user.value = user

      awaitUiModel(
        LauncherUiModel(connectionState = ConnectionState.Available, loggedInUser = uiUser)
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `clears the user when the stored user is removed`() = runTest {
    userRepository.user.value = user

    uiModels.test {
      awaitUiModel(
        LauncherUiModel(connectionState = ConnectionState.Available, loggedInUser = uiUser)
      )

      userRepository.user.value = null

      awaitUiModel(LauncherUiModel(connectionState = ConnectionState.Available))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `dispatchAction sends the action to the state machine`() = runTest {
    userRepository.user.value = user

    uiModels.test {
      awaitUiModel(
        LauncherUiModel(connectionState = ConnectionState.Available, loggedInUser = uiUser)
      )

      presenter.dispatchAction(action = AppLogout)

      awaitUiModel(LauncherUiModel(connectionState = ConnectionState.Available))
      assertEquals(user.refreshToken, noteMarkRepository.logoutRequests.single().refreshToken)
      cancelAndIgnoreRemainingEvents()
    }
  }

  /**
   * The presenter collects the state machine on [kotlinx.coroutines.Dispatchers.Default], so
   * intermediate ui models can be skipped or repeated. Waits until [expected] is emitted.
   */
  private suspend fun ReceiveTurbine<LauncherUiModel>.awaitUiModel(expected: LauncherUiModel) {
    while (awaitItem() != expected) Unit
  }
}
