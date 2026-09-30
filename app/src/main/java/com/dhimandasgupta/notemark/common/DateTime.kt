package com.dhimandasgupta.notemark.common

import java.text.DateFormatSymbols
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime

// Constants for time thresholds
private val FIVE_MINUTES = 5.minutes
private val SIXTY_MINUTES = 60.minutes

/** Current time as ISO 8601 with the device's UTC offset, e.g. `2025-07-01T12:00:00.123+02:00`. */
fun getCurrentIso8601Timestamp(): String {
  val now = Clock.System.now()
  return now.format(
    format = DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET,
    offset = TimeZone.currentSystemDefault().offsetAt(instant = now),
  )
}

fun getDifferenceFromTimestampInMinutes(isoOffsetDateTimeString: String): Long {
  val instant =
    parseIsoInstantOrNull(isoOffsetDateTimeString = isoOffsetDateTimeString) ?: return 0L
  return (Clock.System.now() - instant).inWholeMinutes
}

/** Parses an ISO 8601 timestamp with a UTC offset (`Z` or `±hh:mm`), or null if it isn't one. */
fun parseIsoInstantOrNull(isoOffsetDateTimeString: String): Instant? =
  try {
    Instant.parse(input = isoOffsetDateTimeString)
  } catch (_: IllegalArgumentException) {
    null
  }

/**
 * Whether [first] is a later instant than [second]. Compares the parsed instants, so timestamps
 * written with different UTC offsets compare correctly. Returns false if either can't be parsed.
 */
fun isIsoTimestampAfter(first: String, second: String): Boolean {
  val firstInstant = parseIsoInstantOrNull(isoOffsetDateTimeString = first) ?: return false
  val secondInstant = parseIsoInstantOrNull(isoOffsetDateTimeString = second) ?: return false
  return firstInstant > secondInstant
}

fun convertIsoToRelativeYearFormat(
  locale: Locale,
  isoOffsetDateTimeString: String,
): String {
  val instant =
    parseIsoInstantOrNull(isoOffsetDateTimeString = isoOffsetDateTimeString) ?: return "Unknown"
  val timeZone = TimeZone.currentSystemDefault()
  val dateTime = instant.toLocalDateTime(timeZone = timeZone)
  val now = Clock.System.now().toLocalDateTime(timeZone = timeZone)
  val elapsed = Clock.System.now() - instant

  // Only the format that is actually used gets built.
  return when {
    elapsed < FIVE_MINUTES -> "Just now"
    elapsed <= SIXTY_MINUTES -> "Last hour"
    dateTime.date == now.date -> "Today"
    dateTime.year == now.year -> dateTime.format(format = currentYearFormat(locale = locale))
    else -> dateTime.format(format = previousYearFormat(locale = locale))
  }
}

fun convertIsoToRelativeTimeFormat(
  locale: Locale,
  isoOffsetDateTimeString: String,
): String {
  val instant =
    parseIsoInstantOrNull(isoOffsetDateTimeString = isoOffsetDateTimeString) ?: return "Unknown"
  val elapsed = Clock.System.now() - instant

  return when {
    // A time in the future falls back to showing the date.
    elapsed.isNegative() -> convertNoteTimestampToReadableFormat(locale, isoOffsetDateTimeString)
    elapsed < FIVE_MINUTES -> "Just now"
    elapsed <= SIXTY_MINUTES -> "Last hour"
    else -> convertNoteTimestampToReadableFormat(locale, isoOffsetDateTimeString)
  }
}

fun convertNoteTimestampToReadableFormat(
  locale: Locale,
  isoOffsetDateTimeString: String,
): String {
  val instant =
    parseIsoInstantOrNull(isoOffsetDateTimeString = isoOffsetDateTimeString) ?: return "Unknown"
  return instant
    .toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
    .format(format = yearMonthDayTimeMinuteFormat(locale = locale))
}

/**
 * kotlinx-datetime only ships English month names, so the localized short names come from
 * [DateFormatSymbols]. Its array has a 13th, empty entry for lunar calendars, which is dropped.
 */
private fun shortMonthNames(locale: Locale): MonthNames =
  MonthNames(names = DateFormatSymbols.getInstance(locale).shortMonths.take(n = 12))

/** `dd MMM`, e.g. `01 Jul`. */
private fun currentYearFormat(locale: Locale): DateTimeFormat<LocalDateTime> =
  LocalDateTime.Format {
    day()
    char(' ')
    monthName(names = shortMonthNames(locale = locale))
  }

/** `dd MMM yyyy`, e.g. `01 Jul 2025`. */
private fun previousYearFormat(locale: Locale): DateTimeFormat<LocalDateTime> =
  LocalDateTime.Format {
    day()
    char(' ')
    monthName(names = shortMonthNames(locale = locale))
    char(' ')
    year()
  }

/** `dd MMM yyyy, HH:mm`, e.g. `01 Jul 2025, 14:05`. */
private fun yearMonthDayTimeMinuteFormat(locale: Locale): DateTimeFormat<LocalDateTime> =
  LocalDateTime.Format {
    day()
    char(' ')
    monthName(names = shortMonthNames(locale = locale))
    char(' ')
    year()
    chars(", ")
    hour()
    char(':')
    minute()
  }
