package com.dhimandasgupta.notemark.data.remote.model.extension

import com.dhimandasgupta.notemark.data.remote.model.Note
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

class ExtensionsTest {
  private val note =
    Note(
      uuid = "uuid",
      title = "title",
      content = "content",
      createdAt = "createdAt",
      lastEditedAt = "lastEditedAt",
    )

  @Test
  fun `note maps to an unsaved, not deleted entity`() {
    val entity = note.toNoteEntity(synced = true)

    assertEquals(0L, entity.id)
    assertTrue(entity.synced)
    assertFalse(entity.markAsDeleted)
  }

  @Test
  fun `note round-trips through the entity`() {
    assertEquals(note, note.toNoteEntity(synced = false).toNote())
  }
}
