package com.dhimandasgupta.notemark.app.nav

import android.content.Intent
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dhimandasgupta.notemark.common.navigation.NoteCreateNavKey
import com.dhimandasgupta.notemark.common.navigation.NoteListNavKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeepLinkParserTest {
  @Test
  fun newNoteLinkOpensNoteCreateOnTopOfNoteList() {
    assertEquals(
      listOf(NoteListNavKey, NoteCreateNavKey),
      parse("notemark://notes/new"),
    )
  }

  @Test
  fun newNoteLinkIgnoresExtraQueryParameters() {
    assertEquals(
      listOf(NoteListNavKey, NoteCreateNavKey),
      parse("notemark://notes/new?source=shortcut"),
    )
  }

  @Test
  fun unsupportedLinksAreIgnored() {
    listOf(
        "notemark://notes",
        "notemark://notes/unknown",
        "notemark://notes/new/extra",
        "notemark://settings/new",
        "https://notes/new",
        "https://example.com/notes/new",
        // Opaque and relative URIs have no authority.
        "mailto:someone@example.com",
        "notes/new",
      )
      .forEach { uri -> assertNull(uri, parse(uri)) }
  }

  @Test
  fun intentsWithoutDataAreIgnored() {
    assertNull(DeepLinkParser.parse(null))
    assertNull(DeepLinkParser.parse(Intent()))
    assertNull(DeepLinkParser.parse(Intent(Intent.ACTION_MAIN)))
  }

  private fun parse(uri: String) = DeepLinkParser.parse(Intent(Intent.ACTION_VIEW, uri.toUri()))
}
