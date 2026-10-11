package com.dhimandasgupta.notemark.common.presenter

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.common.state.AppState
import com.dhimandasgupta.notemark.common.state.impl.AppLogout
import com.dhimandasgupta.notemark.common.state.impl.AppStateMachineFactory
import com.dhimandasgupta.notemark.common.state.impl.DeleteLocalNotesOnLogout
import com.dhimandasgupta.notemark.common.state.impl.LoggedIn
import com.dhimandasgupta.notemark.common.state.impl.NotLoggedIn
import com.dhimandasgupta.notemark.proto.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppPresenterTest {
  private val user =
    User.newBuilder()
      .setUserName("Some User name")
      .setAccessToken("Some access token")
      .setRefreshToken("Some refresh token")
      .build()

  private val connectivity = MutableStateFlow<ConnectionState>(value = ConnectionState.Available)
  private val userRepository = FakeUserRepository()
  private val syncRepository = FakeSyncRepository()
  private val noteMarkRepository = FakeNoteMarkRepository(logoutResult = Result.success(Unit))

  private val presenter =
    AppPresenter(
      appStateMachineFactory =
        AppStateMachineFactory(
          applicationContext = fakeApplication(versionName = "1.2.3"),
          userRepository = userRepository,
          syncRepository = syncRepository,
          noteMarkRepository = noteMarkRepository,
          syncScheduler = FakeSyncScheduler(),
          appShortcuts = FakeAppShortcuts(),
          connectivityObserver = { connectivity },
        )
    )

  private val uiModels = moleculeFlow(mode = RecompositionMode.Immediate) { presenter.uiModel() }

  @Test
  fun `starts with the default app state`() = runTest {
    uiModels.test {
      assertEquals(AppStateMachineFactory.defaultAppState, awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `emits the state machine state`() = runTest {
    uiModels.test {
      assertEquals(
        NotLoggedIn(connectionState = ConnectionState.Available),
        awaitState { state -> state == NotLoggedIn(connectionState = ConnectionState.Available) },
      )

      connectivity.value = ConnectionState.Unavailable

      awaitState { state -> state == NotLoggedIn(connectionState = ConnectionState.Unavailable) }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `emits LoggedIn once a user is stored`() = runTest {
    uiModels.test {
      awaitState { state -> state is NotLoggedIn }

      userRepository.user.value = user

      val loggedIn = awaitState { state -> state is LoggedIn } as LoggedIn
      assertEquals(user, loggedIn.user)
      assertEquals("1.2.3", loggedIn.appVersionName)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `dispatchAction sends the action to the state machine`() = runTest {
    userRepository.user.value = user

    uiModels.test {
      awaitState { state -> state is LoggedIn }

      presenter.dispatchAction(action = DeleteLocalNotesOnLogout(deleteOnLogout = true))

      awaitState { state -> (state as? LoggedIn)?.sync?.deleteLocalNotesOnLogout == true }
      assertTrue(syncRepository.sync.value.deleteLocalNotesOnLogout)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `dispatching AppLogout emits NotLoggedIn`() = runTest {
    userRepository.user.value = user

    uiModels.test {
      awaitState { state -> state is LoggedIn }

      presenter.dispatchAction(action = AppLogout)

      assertEquals(
        NotLoggedIn(connectionState = ConnectionState.Available),
        awaitState { state -> state is NotLoggedIn },
      )
      assertEquals(user.refreshToken, noteMarkRepository.logoutRequests.single().refreshToken)
      cancelAndIgnoreRemainingEvents()
    }
  }

  /**
   * The presenter collects the state machine on [kotlinx.coroutines.Dispatchers.Default], so
   * intermediate states can be skipped or repeated. Waits for the first state matching [predicate].
   */
  private suspend fun ReceiveTurbine<AppState>.awaitState(
    predicate: (AppState) -> Boolean
  ): AppState {
    while (true) {
      val state = awaitItem()
      if (predicate(state)) return state
    }
  }
}
