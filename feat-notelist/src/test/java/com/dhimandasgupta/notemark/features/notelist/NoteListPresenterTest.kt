package com.dhimandasgupta.notemark.features.notelist

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.dhimandasgupta.notemark.common.android.ConnectionState
import com.dhimandasgupta.notemark.common.state.impl.AppEvent
import com.dhimandasgupta.notemark.common.state.impl.AppEventConsumed
import com.dhimandasgupta.notemark.common.state.impl.AppStateMachineFactory
import com.dhimandasgupta.notemark.common.state.impl.FakeAppShortcuts
import com.dhimandasgupta.notemark.common.state.impl.FakeNoteMarkRepository
import com.dhimandasgupta.notemark.common.state.impl.FakeSyncRepository
import com.dhimandasgupta.notemark.common.state.impl.FakeSyncScheduler
import com.dhimandasgupta.notemark.common.state.impl.FakeUserRepository
import com.dhimandasgupta.notemark.common.state.impl.fakeApplication
import com.dhimandasgupta.notemark.database.NoteEntity
import com.dhimandasgupta.notemark.proto.User
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteListPresenterTest {
  private val user = User.newBuilder().setUserName("Some User name").build()
  private val notes = listOf(noteEntity(id = 1), noteEntity(id = 2))

  private val connectivity = MutableStateFlow<ConnectionState>(value = ConnectionState.Available)
  private val userRepository = FakeUserRepository(user = user)
  private val syncRepository = FakeSyncRepository()
  private val noteListRepository = FakeNoteListRepository(notes = notes)

  private val presenter =
    NoteListPresenter(
      appStateMachineFactory =
        AppStateMachineFactory(
          applicationContext = fakeApplication(versionName = "1.2.3"),
          userRepository = userRepository,
          syncRepository = syncRepository,
          noteMarkRepository = FakeNoteMarkRepository(logoutResult = Result.success(Unit)),
          syncScheduler = FakeSyncScheduler(),
          appShortcuts = FakeAppShortcuts(),
          connectivityObserver = { connectivity },
        ),
      noteListStateMachineFactory =
        NoteListStateMachineFactory(
          userRepository = userRepository,
          noteMarkRepository = noteListRepository,
          pagedNotes = PagedNotes(noteMarkRepository = noteListRepository),
        ),
    )

  private val uiModels = moleculeFlow(mode = RecompositionMode.Immediate) { presenter.uiModel() }

  private val loadedUiModel =
    NoteListUiModel(
      userName = "Some User name",
      loading = false,
      noteEntities = persistentListOf(notes[0].toUiModel(), notes[1].toUiModel()),
      isConnected = true,
    )

  @Before
  fun setUp() {
    // PagedNotes presents its pages on the main dispatcher; runTest shares this scheduler.
    Dispatchers.setMain(StandardTestDispatcher())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `starts with the default ui model`() = runTest {
    uiModels.test {
      assertEquals(NoteListUiModel.defaultOrEmpty, awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `shows the logged in user's notes`() = runTest {
    uiModels.test {
      awaitUiModel { model -> model == loadedUiModel }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `losing connectivity shows a network event`() = runTest {
    uiModels.test {
      awaitUiModel { model -> model == loadedUiModel }

      connectivity.value = ConnectionState.Unavailable

      val offline = awaitUiModel { model -> !model.isConnected }
      assertTrue(offline.appEvent is AppEvent.NetworkUnAvailable)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `dispatchAppAction consumes the network event`() = runTest {
    uiModels.test {
      awaitUiModel { model -> model == loadedUiModel }
      connectivity.value = ConnectionState.Unavailable
      val event = awaitUiModel { model -> model.appEvent != null }.appEvent!!

      presenter.dispatchAppAction(action = AppEventConsumed(id = event.id))

      assertNull(awaitUiModel { model -> model.appEvent == null }.appEvent)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `shows sync progress while syncing`() = runTest {
    uiModels.test {
      awaitUiModel { model -> model == loadedUiModel }

      syncRepository.saveSyncing(isSyncing = true)

      awaitUiModel { model -> model == loadedUiModel.copy(showSyncProgress = true) }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `dispatchAction deletes the note`() = runTest {
    uiModels.test {
      awaitUiModel { model -> model == loadedUiModel }

      presenter.dispatchAction(action = NoteListAction.NoteDelete(uuid = "uuid-1"))

      awaitUiModel { model ->
        model == loadedUiModel.copy(noteEntities = persistentListOf(notes[1].toUiModel()))
      }
      assertEquals(listOf(notes[0]), noteListRepository.markedAsDeleted)
      cancelAndIgnoreRemainingEvents()
    }
  }

  /**
   * The presenter combines both state machines on [Dispatchers.Default], so intermediate ui models
   * can be skipped or repeated. Waits for the first one matching [predicate].
   */
  private suspend fun ReceiveTurbine<NoteListUiModel>.awaitUiModel(
    predicate: (NoteListUiModel) -> Boolean
  ): NoteListUiModel {
    while (true) {
      val model = awaitItem()
      if (predicate(model)) return model
    }
  }

  private fun NoteEntity.toUiModel() =
    NoteEntityUiModel(
      id = id,
      title = title,
      content = content,
      createdAt = createdAt,
      lastEditedAt = lastEditedAt,
      uuid = uuid,
      synced = synced,
      markAsDeleted = markAsDeleted,
    )
}
