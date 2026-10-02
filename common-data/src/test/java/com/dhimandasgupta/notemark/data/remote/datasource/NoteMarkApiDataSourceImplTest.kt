package com.dhimandasgupta.notemark.data.remote.datasource

import com.dhimandasgupta.notemark.data.remote.api.FakeFailureNoteMarkApi
import com.dhimandasgupta.notemark.data.remote.api.FakeSuccessfulNoteMarkApi
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NoteMarkApiDataSourceImplTest {
  private val noteEntity =
    NoteEntity(
      id = 1L,
      title = "title",
      content = "content",
      createdAt = "createdAt",
      lastEditedAt = "lastEditedAt",
      uuid = "1",
      synced = false,
      markAsDeleted = false,
    )

  @Test
  fun `forwards successful api results`() = runTest {
    val dataSource = NoteMarkApiDataSourceImpl(noteMarkApi = lazy { FakeSuccessfulNoteMarkApi() })

    assertEquals(1, dataSource.getAllNotes().getOrThrow().total)
    assertEquals("1", dataSource.createNote(noteEntity = noteEntity).getOrThrow().uuid)
    assertEquals(
      "title",
      dataSource
        .updateNote(
          title = "title",
          content = "content",
          lastEditedAt = "lastEditedAt",
          noteEntity = noteEntity,
        )
        .getOrThrow()
        .title,
    )
    assertTrue(dataSource.deleteNote(noteEntity = noteEntity).isSuccess)
    assertTrue(dataSource.logout(request = RefreshRequest(refreshToken = "token")).isSuccess)
  }

  @Test
  fun `forwards failed api results`() = runTest {
    val dataSource = NoteMarkApiDataSourceImpl(noteMarkApi = lazy { FakeFailureNoteMarkApi() })

    assertTrue(dataSource.getAllNotes().isFailure)
    assertTrue(dataSource.createNote(noteEntity = noteEntity).isFailure)
    assertTrue(
      dataSource
        .updateNote(
          title = "title",
          content = "content",
          lastEditedAt = "lastEditedAt",
          noteEntity = noteEntity,
        )
        .isFailure
    )
    assertTrue(dataSource.deleteNote(noteEntity = noteEntity).isFailure)
    assertTrue(dataSource.logout(request = RefreshRequest(refreshToken = "token")).isFailure)
  }
}
