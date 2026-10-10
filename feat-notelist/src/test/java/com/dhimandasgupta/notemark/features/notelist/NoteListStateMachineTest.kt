package com.dhimandasgupta.notemark.features.notelist

import com.dhimandasgupta.notemark.common.state.impl.FakeUserRepository
import com.dhimandasgupta.notemark.features.notelist.NoteListState.NoteListStateWithNoNotes
import com.dhimandasgupta.notemark.features.notelist.NoteListState.NoteListStateWithNotes
import com.dhimandasgupta.notemark.proto.User
import com.freeletics.flowredux2.FlowReduxStateMachine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteListStateMachineTest {
  private val user = User.newBuilder().setUserName("Some User name").build()
  private val userRepository = FakeUserRepository(user = user)
  private val noteMarkRepository = FakeNoteListRepository()

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
  fun `default state is loading without notes`() {
    assertEquals(
      NoteListStateWithNoNotes(userName = null, loading = true),
      NoteListStateMachineFactory.defaultNoteListState,
    )
  }

  @Test
  fun `stops loading with no notes when the repository is empty`() = runTest {
    val stateMachine = launchStateMachine()

    assertEquals(
      NoteListStateWithNoNotes(userName = "Some User name", loading = false),
      stateMachine.state.value,
    )
  }

  @Test
  fun `uses an empty user name when no user is stored`() = runTest {
    userRepository.user.value = null
    val stateMachine = launchStateMachine()

    assertEquals("", stateMachine.state.value.userName)
  }

  @Test
  fun `moves to the notes state once notes are paged in`() = runTest {
    val notes = listOf(noteEntity(id = 1), noteEntity(id = 2))
    noteMarkRepository.notes.value = notes
    val stateMachine = launchStateMachine()

    assertEquals(
      NoteListStateWithNotes(userName = "Some User name", loading = false, notes = notes),
      stateMachine.state.value,
    )
  }

  @Test
  fun `follows notes added after launch`() = runTest {
    val stateMachine = launchStateMachine()

    noteMarkRepository.notes.value = listOf(noteEntity(id = 1))
    runCurrent()

    assertEquals(
      listOf(noteEntity(id = 1)),
      (stateMachine.state.value as NoteListStateWithNotes).notes,
    )
  }

  @Test
  fun `returns to the no notes state when the last note goes away`() = runTest {
    noteMarkRepository.notes.value = listOf(noteEntity(id = 1))
    val stateMachine = launchStateMachine()

    noteMarkRepository.notes.value = emptyList()
    runCurrent()

    assertEquals(
      NoteListStateWithNoNotes(userName = "Some User name", loading = false),
      stateMachine.state.value,
    )
  }

  @Test
  fun `updates the user name while showing notes`() = runTest {
    noteMarkRepository.notes.value = listOf(noteEntity(id = 1))
    val stateMachine = launchStateMachine()

    userRepository.user.value = user.toBuilder().setUserName("Another name").build()
    runCurrent()

    assertEquals("Another name", stateMachine.state.value.userName)
  }

  @Test
  fun `NoteDelete marks the note as deleted`() = runTest {
    noteMarkRepository.notes.value = listOf(noteEntity(id = 1), noteEntity(id = 2))
    val stateMachine = launchStateMachine()

    dispatchAndRun(
      stateMachine = stateMachine,
      action = NoteListAction.NoteDelete(uuid = "uuid-1"),
    )

    assertEquals(listOf(noteEntity(id = 1)), noteMarkRepository.markedAsDeleted)
    assertEquals(
      listOf(noteEntity(id = 2)),
      (stateMachine.state.value as NoteListStateWithNotes).notes,
    )
  }

  @Test
  fun `NoteDelete with an unknown uuid deletes nothing`() = runTest {
    noteMarkRepository.notes.value = listOf(noteEntity(id = 1))
    val stateMachine = launchStateMachine()

    dispatchAndRun(
      stateMachine = stateMachine,
      action = NoteListAction.NoteDelete(uuid = "unknown"),
    )

    assertTrue(noteMarkRepository.markedAsDeleted.isEmpty())
    assertEquals(
      listOf(noteEntity(id = 1)),
      (stateMachine.state.value as NoteListStateWithNotes).notes,
    )
  }

  @Test
  fun `LoadNextNotes keeps the notes when everything is loaded`() = runTest {
    val notes = listOf(noteEntity(id = 1), noteEntity(id = 2))
    noteMarkRepository.notes.value = notes
    val stateMachine = launchStateMachine()

    dispatchAndRun(
      stateMachine = stateMachine,
      action = NoteListAction.LoadNextNotes,
    )

    assertEquals(
      NoteListStateWithNotes(userName = "Some User name", loading = false, notes = notes),
      stateMachine.state.value,
    )
  }

  private fun TestScope.launchStateMachine():
    FlowReduxStateMachine<StateFlow<NoteListState>, NoteListAction> {
    val stateMachine =
      NoteListStateMachineFactory(
          userRepository = userRepository,
          noteMarkRepository = noteMarkRepository,
          pagedNotes = PagedNotes(noteMarkRepository = noteMarkRepository),
        )
        .launchIn(scope = backgroundScope)
    // The state is shared lazily, so the machine only runs while something collects it.
    backgroundScope.launch { stateMachine.state.collect() }
    runCurrent()
    return stateMachine
  }

  private suspend fun TestScope.dispatchAndRun(
    stateMachine: FlowReduxStateMachine<StateFlow<NoteListState>, NoteListAction>,
    action: NoteListAction,
  ) {
    stateMachine.dispatch(action = action)
    runCurrent()
  }
}
