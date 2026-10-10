package com.dhimandasgupta.notemark.common.state.impl

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.common.state.AppAction
import com.dhimandasgupta.notemark.common.state.AppState
import com.dhimandasgupta.notemark.proto.Sync
import com.dhimandasgupta.notemark.proto.User
import com.freeletics.flowredux2.FlowReduxStateMachine
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AppStateMachineTest {
  private val user =
    User.newBuilder()
      .setUserName("Some User name")
      .setAccessToken("Some access token")
      .setRefreshToken("Some refresh token")
      .build()

  private val connectivity = MutableStateFlow<ConnectionState>(value = ConnectionState.Available)
  private val userRepository = FakeUserRepository()
  private val syncRepository = FakeSyncRepository()
  private val syncScheduler = FakeSyncScheduler()
  private val appShortcuts = FakeAppShortcuts()

  @Test
  fun `requires an Application context`() {
    val context = mock<Context>()

    assertFailsWith<IllegalArgumentException> {
      createFactory(context = context)
    }
  }

  @Test
  fun `starts NotLoggedIn with the current connectivity when no user is stored`() = runTest {
    val stateMachine = launchStateMachine()

    assertEquals(NotLoggedIn(connectionState = ConnectionState.Available), stateMachine.state.value)
    assertEquals(1, appShortcuts.removeCount)
  }

  @Test
  fun `NotLoggedIn follows connectivity changes`() = runTest {
    val stateMachine = launchStateMachine()

    connectivity.value = ConnectionState.Unavailable
    runCurrent()

    assertEquals(
      NotLoggedIn(connectionState = ConnectionState.Unavailable),
      stateMachine.state.value,
    )
  }

  @Test
  fun `moves to LoggedIn when a user is stored`() = runTest {
    val stateMachine = launchStateMachine()

    userRepository.user.value = user
    runCurrent()

    assertEquals(
      LoggedIn(
        connectionState = ConnectionState.Available,
        appEvents = persistentListOf(),
        user = user,
        sync = Sync.getDefaultInstance(),
        isSyncing = false,
        appVersionName = APP_VERSION_NAME,
      ),
      stateMachine.state.value,
    )
  }

  @Test
  fun `entering LoggedIn adds the new note shortcut`() = runTest {
    userRepository.user.value = user
    launchStateMachine()

    assertEquals(1, appShortcuts.addCount)
  }

  @Test
  fun `entering LoggedIn syncs when never synced before`() = runTest {
    userRepository.user.value = user
    launchStateMachine()

    assertEquals(1, syncScheduler.oneTimeSyncCount)
  }

  @Test
  fun `entering LoggedIn syncs when the last upload is older than 5 minutes`() = runTest {
    syncRepository.sync.value = syncUploadedMinutesAgo(minutes = 10)
    userRepository.user.value = user
    launchStateMachine()

    assertEquals(1, syncScheduler.oneTimeSyncCount)
  }

  @Test
  fun `entering LoggedIn skips sync when the last upload is recent`() = runTest {
    syncRepository.sync.value = syncUploadedMinutesAgo(minutes = 1)
    userRepository.user.value = user
    launchStateMachine()

    assertEquals(0, syncScheduler.oneTimeSyncCount)
  }

  @Test
  fun `connectivity changes while LoggedIn add network events`() = runTest {
    userRepository.user.value = user
    val stateMachine = launchStateMachine()

    connectivity.value = ConnectionState.Unavailable
    runCurrent()

    val offline = assertIs<LoggedIn>(stateMachine.state.value)
    assertEquals(ConnectionState.Unavailable, offline.connectionState)
    assertIs<AppEvent.NetworkUnAvailable>(offline.appEvents.single())

    connectivity.value = ConnectionState.Available
    runCurrent()

    val online = assertIs<LoggedIn>(stateMachine.state.value)
    assertEquals(ConnectionState.Available, online.connectionState)
    assertEquals(2, online.appEvents.size)
    assertIs<AppEvent.NetworkAvailable>(online.appEvents.last())
  }

  @Test
  fun `AppEventConsumed removes the consumed event`() = runTest {
    userRepository.user.value = user
    val stateMachine = launchStateMachine()
    connectivity.value = ConnectionState.Unavailable
    runCurrent()
    val event = assertIs<LoggedIn>(stateMachine.state.value).appEvents.single()

    dispatchAndRun(stateMachine = stateMachine, action = AppEventConsumed(id = event.id))

    assertTrue(assertIs<LoggedIn>(stateMachine.state.value).appEvents.isEmpty())
  }

  @Test
  fun `LoggedIn follows stored sync settings and running syncs`() = runTest {
    userRepository.user.value = user
    val stateMachine = launchStateMachine()

    syncRepository.saveLastDownloadedTime(downLoadedTime = "2026-10-10T10:00:00Z")
    syncScheduler.isSyncing.value = true
    runCurrent()

    val state = assertIs<LoggedIn>(stateMachine.state.value)
    assertEquals("2026-10-10T10:00:00Z", state.sync?.lastDownloadedTime)
    assertTrue(state.isSyncing)
  }

  @Test
  fun `UpdateSync with a periodic duration schedules a periodic sync`() = runTest {
    userRepository.user.value = user
    val stateMachine = launchStateMachine()

    dispatchAndRun(
      stateMachine = stateMachine,
      action = UpdateSync(syncDuration = Sync.SyncDuration.SYNC_DURATION_THIRTY_MINUTES),
    )

    assertEquals(listOf(30.minutes), syncScheduler.periodicIntervals)
    assertEquals(0, syncScheduler.cancelPeriodicSyncCount)
    assertEquals(
      Sync.SyncDuration.SYNC_DURATION_THIRTY_MINUTES,
      syncRepository.sync.value.syncDuration,
    )
    assertEquals(
      Sync.SyncDuration.SYNC_DURATION_THIRTY_MINUTES,
      assertIs<LoggedIn>(stateMachine.state.value).sync?.syncDuration,
    )
  }

  @Test
  fun `UpdateSync to manual cancels the periodic sync`() = runTest {
    userRepository.user.value = user
    val stateMachine = launchStateMachine()

    dispatchAndRun(
      stateMachine = stateMachine,
      action = UpdateSync(syncDuration = Sync.SyncDuration.SYNC_DURATION_MANUAL),
    )

    assertEquals(1, syncScheduler.cancelPeriodicSyncCount)
    assertTrue(syncScheduler.periodicIntervals.isEmpty())
    assertEquals(Sync.SyncDuration.SYNC_DURATION_MANUAL, syncRepository.sync.value.syncDuration)
  }

  @Test
  fun `SyncNow triggers a one-time sync`() = runTest {
    syncRepository.sync.value = syncUploadedMinutesAgo(minutes = 1)
    userRepository.user.value = user
    val stateMachine = launchStateMachine()

    dispatchAndRun(stateMachine = stateMachine, action = SyncNow)

    assertEquals(1, syncScheduler.oneTimeSyncCount)
  }

  @Test
  fun `DeleteLocalNotesOnLogout saves the preference`() = runTest {
    userRepository.user.value = user
    val stateMachine = launchStateMachine()

    dispatchAndRun(
      stateMachine = stateMachine,
      action = DeleteLocalNotesOnLogout(deleteOnLogout = true),
    )

    assertTrue(syncRepository.sync.value.deleteLocalNotesOnLogout)
    assertTrue(assertIs<LoggedIn>(stateMachine.state.value).sync?.deleteLocalNotesOnLogout == true)
  }

  @Test
  fun `AppLogout while offline is ignored`() = runTest {
    val noteMarkRepository = FakeNoteMarkRepository(logoutResult = Result.success(Unit))
    connectivity.value = ConnectionState.Unavailable
    userRepository.user.value = user
    val stateMachine = launchStateMachine(noteMarkRepository = noteMarkRepository)

    dispatchAndRun(stateMachine = stateMachine, action = AppLogout)

    assertTrue(noteMarkRepository.logoutRequests.isEmpty())
    assertIs<LoggedIn>(stateMachine.state.value)
  }

  @Test
  fun `AppLogout success clears the session and moves to NotLoggedIn`() = runTest {
    val noteMarkRepository = FakeNoteMarkRepository(logoutResult = Result.success(Unit))
    userRepository.user.value = user
    val stateMachine = launchStateMachine(noteMarkRepository = noteMarkRepository)

    dispatchAndRun(stateMachine = stateMachine, action = AppLogout)

    assertEquals(user.refreshToken, noteMarkRepository.logoutRequests.single().refreshToken)
    assertEquals(1, syncScheduler.cancelSyncWorkCount)
    assertEquals(1, syncRepository.resetCount)
    assertEquals(1, userRepository.resetCount)
    assertEquals(0, noteMarkRepository.deleteAllLocalNotesCount)
    assertEquals(NotLoggedIn(connectionState = ConnectionState.Available), stateMachine.state.value)
  }

  @Test
  fun `AppLogout success deletes local notes when enabled`() = runTest {
    val noteMarkRepository = FakeNoteMarkRepository(logoutResult = Result.success(Unit))
    syncRepository.saveDeleteLocalNotesOnLogout(deleteLocalNotesOnLogout = true)
    userRepository.user.value = user
    val stateMachine = launchStateMachine(noteMarkRepository = noteMarkRepository)

    dispatchAndRun(stateMachine = stateMachine, action = AppLogout)

    assertEquals(1, noteMarkRepository.deleteAllLocalNotesCount)
  }

  @Test
  fun `AppLogout failure keeps the session`() = runTest {
    val noteMarkRepository =
      FakeNoteMarkRepository(logoutResult = Result.failure(IllegalStateException("Logout failed")))
    userRepository.user.value = user
    val stateMachine = launchStateMachine(noteMarkRepository = noteMarkRepository)

    dispatchAndRun(stateMachine = stateMachine, action = AppLogout)

    assertEquals(1, noteMarkRepository.logoutRequests.size)
    assertEquals(0, syncScheduler.cancelSyncWorkCount)
    assertEquals(0, userRepository.resetCount)
    assertIs<LoggedIn>(stateMachine.state.value)
  }

  @Test
  fun `removing the stored user moves to NotLoggedIn and keeps connectivity`() = runTest {
    connectivity.value = ConnectionState.Unavailable
    userRepository.user.value = user
    val stateMachine = launchStateMachine()

    userRepository.user.value = null
    runCurrent()

    assertEquals(
      NotLoggedIn(connectionState = ConnectionState.Unavailable),
      stateMachine.state.value,
    )
  }

  private fun createFactory(
    context: Context = applicationContext(),
    noteMarkRepository: FakeNoteMarkRepository =
      FakeNoteMarkRepository(logoutResult = Result.success(Unit)),
  ) =
    AppStateMachineFactory(
      applicationContext = context,
      userRepository = userRepository,
      syncRepository = syncRepository,
      noteMarkRepository = noteMarkRepository,
      syncScheduler = syncScheduler,
      appShortcuts = appShortcuts,
      connectivityObserver = { connectivity },
    )

  private fun TestScope.launchStateMachine(
    noteMarkRepository: FakeNoteMarkRepository =
      FakeNoteMarkRepository(logoutResult = Result.success(Unit))
  ): FlowReduxStateMachine<StateFlow<AppState>, AppAction> {
    val stateMachine =
      createFactory(noteMarkRepository = noteMarkRepository).launchIn(scope = backgroundScope)
    // The state is shared lazily, so the machine only runs while something collects it.
    backgroundScope.launch { stateMachine.state.collect() }
    runCurrent()
    return stateMachine
  }

  private suspend fun TestScope.dispatchAndRun(
    stateMachine: FlowReduxStateMachine<StateFlow<AppState>, AppAction>,
    action: AppAction,
  ) {
    stateMachine.dispatch(action = action)
    runCurrent()
  }

  /** An Application whose package manager reports [APP_VERSION_NAME]. */
  private fun applicationContext(): Application {
    val packageManager = mock<PackageManager>()
    whenever(packageManager.getPackageInfo(any<String>(), any<Int>()))
      .doReturn(PackageInfo().apply { versionName = APP_VERSION_NAME })

    val application = mock<Application>()
    whenever(application.applicationContext).doReturn(application)
    whenever(application.packageManager).doReturn(packageManager)
    whenever(application.packageName).doReturn("com.dhimandasgupta.notemark")
    return application
  }

  private fun syncUploadedMinutesAgo(minutes: Long): Sync =
    Sync.newBuilder()
      .setLastUploadedTime(Instant.now().minus(minutes, ChronoUnit.MINUTES).toString())
      .build()

  private companion object {
    const val APP_VERSION_NAME = "1.2.3"
  }
}
