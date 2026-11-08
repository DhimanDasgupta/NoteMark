package com.dhimandasgupta.notemark.common.state.impl

import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.proto.User
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppStateStoreTest {
  private val user = User.newBuilder().setUserName("Some User name").build()

  private val connectivity = MutableStateFlow<ConnectionState>(value = ConnectionState.Available)
  private val userRepository = FakeUserRepository()
  private val syncScheduler = FakeSyncScheduler()
  private val appShortcuts = FakeAppShortcuts()

  @Test
  fun `every reader shares one state machine`() = runTest {
    userRepository.user.value = user
    val store = createStore()

    // Two screens reading the state at once, like the note list and settings side by side
    backgroundScope.launch { store.state.collect() }
    backgroundScope.launch { store.state.collect() }
    runCurrent()

    assertIs<LoggedIn>(store.state.value)
    assertEquals(1, appShortcuts.addCount)
    assertEquals(1, syncScheduler.oneTimeSyncCount)
  }

  @Test
  fun `an event consumed through the store is gone for every reader`() = runTest {
    userRepository.user.value = user
    val store = createStore()
    backgroundScope.launch { store.state.collect() }
    runCurrent()
    connectivity.value = ConnectionState.Unavailable
    runCurrent()
    val event = assertIs<LoggedIn>(store.state.value).appEvents.single()

    store.dispatch(action = AppEventConsumed(appEvent = event))
    runCurrent()

    assertTrue(assertIs<LoggedIn>(store.state.value).appEvents.isEmpty())
  }

  private fun TestScope.createStore() =
    AppStateStore(
      appStateMachineFactory =
        AppStateMachineFactory(
          applicationContext = fakeApplication(versionName = "1.2.3"),
          userRepository = userRepository,
          syncRepository = FakeSyncRepository(),
          noteMarkRepository = FakeNoteMarkRepository(logoutResult = Result.success(Unit)),
          syncScheduler = syncScheduler,
          appShortcuts = appShortcuts,
          connectivityObserver = { connectivity },
        ),
      scope = backgroundScope,
    )
}
