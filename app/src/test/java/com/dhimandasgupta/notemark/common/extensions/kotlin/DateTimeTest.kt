package com.dhimandasgupta.notemark.common.extensions.kotlin

import java.util.Locale
import java.util.TimeZone
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class DateTimeTest {
  private lateinit var originalTimeZone: TimeZone

  @Before
  fun setUp() {
    originalTimeZone = TimeZone.getDefault()
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  }

  @After
  fun tearDown() {
    TimeZone.setDefault(originalTimeZone)
  }

  @Test
  fun `current timestamp round-trips through the parser`() {
    assertNotNull(parseIsoInstantOrNull(isoOffsetDateTimeString = getCurrentIso8601Timestamp()))
  }

  @Test
  fun `parses Z and numeric offsets and rejects other strings`() {
    assertNotNull(parseIsoInstantOrNull(isoOffsetDateTimeString = "2025-06-29T19:18:24.369Z"))
    assertNotNull(parseIsoInstantOrNull(isoOffsetDateTimeString = "2025-06-29T19:18:24+05:30"))
    assertNull(parseIsoInstantOrNull(isoOffsetDateTimeString = "0"))
    assertNull(parseIsoInstantOrNull(isoOffsetDateTimeString = ""))
  }

  @Test
  fun `compares instants across offsets`() {
    assertTrue(
      isIsoTimestampAfter(first = "2025-07-01T09:30:00-02:00", second = "2025-07-01T10:00:00Z")
    )
    assertFalse(
      isIsoTimestampAfter(first = "2025-07-01T10:00:00Z", second = "2025-07-01T12:00:00+02:00")
    )
    assertFalse(isIsoTimestampAfter(first = "0", second = "2025-07-01T10:00:00Z"))
  }

  @Test
  fun `formats a readable timestamp with localized month names`() {
    assertEquals(
      "01 Jul 2025, 14:05",
      convertNoteTimestampToReadableFormat(Locale.US, "2025-07-01T14:05:00Z"),
    )
    assertEquals(
      "01 juil. 2025, 14:05",
      convertNoteTimestampToReadableFormat(Locale.FRANCE, "2025-07-01T14:05:00Z"),
    )
  }

  @Test
  fun `relative year format shows the year only for earlier years`() {
    assertEquals("05 Jan 2001", convertIsoToRelativeYearFormat(Locale.US, "2001-01-05T08:00:00Z"))
    assertEquals(
      "Just now",
      convertIsoToRelativeYearFormat(Locale.US, getCurrentIso8601Timestamp()),
    )
  }

  @Test
  fun `relative time format`() {
    assertEquals(
      "Just now",
      convertIsoToRelativeTimeFormat(Locale.US, getCurrentIso8601Timestamp()),
    )
    assertEquals(
      "05 Jan 2001, 08:00",
      convertIsoToRelativeTimeFormat(Locale.US, "2001-01-05T08:00:00Z"),
    )
    assertEquals("Unknown", convertIsoToRelativeTimeFormat(Locale.US, "not a date"))
  }
}
