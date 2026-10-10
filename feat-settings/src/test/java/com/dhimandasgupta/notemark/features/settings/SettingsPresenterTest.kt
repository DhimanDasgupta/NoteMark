package com.dhimandasgupta.notemark.features.settings

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.common.state.impl.AppLogout
import com.dhimandasgupta.notemark.common.state.impl.AppStateMachineFactory
import com.dhimandasgupta.notemark.common.state.impl.DeleteLocalNotesOnLogout
import com.dhimandasgupta.notemark.common.state.impl.FakeAppShortcuts
import com.dhimandasgupta.notemark.common.state.impl.FakeNoteMarkRepository
import com.dhimandasgupta.notemark.common.state.impl.FakeSyncRepository
import com.dhimandasgupta.notemark.common.state.impl.FakeSyncScheduler
import com.dhimandasgupta.notemark.common.state.impl.FakeUserRepository
import com.dhimandasgupta.notemark.common.state.impl.UpdateSync
import com.dhimandasgupta.notemark.common.state.impl.fakeApplication
import com.dhimandasgupta.notemark.proto.Sync
import com.dhimandasgupta.notemark.proto.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsPresenterTest {
  private val user =
    User.newBuilder().setUserName("Some User name").setRefreshToken("Some refresh token").build()

  private val connectivity = MutableStateFlow<ConnectionState>(value = ConnectionState.Available)
  private val userRepository = FakeUserRepository(user = user)
  private val syncRepository =
    FakeSyncRepository(
      sync =
        Sync.newBuilder()
          .setLastUploadedTime(LAST_UPLOADED_TIME)
          .setSyncDuration(Sync.SyncDuration.SYNC_DURATION_THIRTY_MINUTES)
          .build()
    )
  private val syncScheduler = FakeSyncScheduler()

  private val presenter =
    SettingsPresenter(
      appStateMachineFactory =
        AppStateMachineFactory(
          applicationContext = fakeApplication(versionName = "1.2.3"),
          userRepository = userRepository,
          syncRepository = syncRepository,
          noteMarkRepository = FakeNoteMarkRepository(logoutResult = Result.success(Unit)),
          syncScheduler = syncScheduler,
          appShortcuts = FakeAppShortcuts(),
          connectivityObserver = { connectivity },
        )
    )

  private val uiModels = moleculeFlow(mode = RecompositionMode.Immediate) { presenter.uiModel() }

  private val loggedInUiModel =
    SettingsUiModel(
      selectedSyncInterval = "30 Minutes",
      lastSynced = LAST_UPLOADED_TIME,
      deleteLocalNotesOnLogout = false,
      logoutStatus = null,
      isSyncing = false,
      isConnected = true,
      appVersionName = "1.2.3",
    )

  @Test
  fun `starts with the default ui model`() = runTest {
    uiModels.test {
      assertEquals(SettingsUiModel.defaultOrEmpty, awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `does not report a logout while the logged in state loads`() = runTest {
    uiModels.test {
      val uiModelsUntilLoggedIn = mutableListOf<SettingsUiModel>()
      do {
        val uiModel = awaitItem()
        uiModelsUntilLoggedIn += uiModel
      } while (uiModel != loggedInUiModel)

      assertTrue(uiModelsUntilLoggedIn.none { uiModel -> uiModel.logoutStatus == true })
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `reports a logout when the stored user is removed`() = runTest {
    uiModels.test {
      awaitUiModel(expected = loggedInUiModel)

      userRepository.user.value = null

      awaitUiModel(expected = SettingsUiModel.defaultOrEmpty.copy(logoutStatus = true))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `shows the stored sync settings of the logged in user`() = runTest {
    uiModels.test {
      awaitUiModel(expected = loggedInUiModel)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `shows Manual when no sync interval is stored`() = runTest {
    syncRepository.sync.value = Sync.getDefaultInstance()

    uiModels.test {
      awaitUiModel(
        expected = loggedInUiModel.copy(selectedSyncInterval = "Manual", lastSynced = "")
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `UpdateSync changes the selected interval`() = runTest {
    uiModels.test {
      awaitUiModel(expected = loggedInUiModel)

      presenter.dispatchAction(
        action = UpdateSync(syncDuration = Sync.SyncDuration.SYNC_DURATION_FIFTEEN_MINUTES)
      )
      awaitUiModel(expected = loggedInUiModel.copy(selectedSyncInterval = "15 Minutes"))

      presenter.dispatchAction(
        action = UpdateSync(syncDuration = Sync.SyncDuration.SYNC_DURATION_ONE_HOUR)
      )
      awaitUiModel(expected = loggedInUiModel.copy(selectedSyncInterval = "1 Hour"))

      presenter.dispatchAction(
        action = UpdateSync(syncDuration = Sync.SyncDuration.SYNC_DURATION_MANUAL)
      )
      awaitUiModel(expected = loggedInUiModel.copy(selectedSyncInterval = "Manual"))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `DeleteLocalNotesOnLogout updates the toggle`() = runTest {
    uiModels.test {
      awaitUiModel(expected = loggedInUiModel)

      presenter.dispatchAction(action = DeleteLocalNotesOnLogout(deleteOnLogout = true))

      awaitUiModel(expected = loggedInUiModel.copy(deleteLocalNotesOnLogout = true))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `shows connectivity and running syncs`() = runTest {
    uiModels.test {
      awaitUiModel(expected = loggedInUiModel)

      connectivity.value = ConnectionState.Unavailable
      awaitUiModel(expected = loggedInUiModel.copy(isConnected = false))

      syncScheduler.isSyncing.value = true
      awaitUiModel(expected = loggedInUiModel.copy(isConnected = false, isSyncing = true))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `AppLogout reports a successful logout`() = runTest {
    uiModels.test {
      awaitUiModel(expected = loggedInUiModel)

      presenter.dispatchAction(action = AppLogout)

      awaitUiModel(expected = SettingsUiModel.defaultOrEmpty.copy(logoutStatus = true))
      cancelAndIgnoreRemainingEvents()
    }
  }

  /**
   * The presenter maps the state machine on [kotlinx.coroutines.Dispatchers.Default] and briefly
   * reports the default NotLoggedIn state as a logout, so intermediate ui models can be skipped,
   * repeated or transient. Waits until [expected] is emitted.
   */
  private suspend fun ReceiveTurbine<SettingsUiModel>.awaitUiModel(expected: SettingsUiModel) {
    while (awaitItem() != expected) Unit
  }

  private companion object {
    const val LAST_UPLOADED_TIME = "2026-10-10T10:00:00Z"
  }
}
