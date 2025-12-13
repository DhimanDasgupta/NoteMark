package com.dhimandasgupta.notemark.common

import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Constants for time thresholds
private const val FIVE_MINUTES_IN_SECONDS = 5 * 60L
private const val SIXTY_MINUTES_IN_SECONDS = 60 * 60L
private const val CURRENT_YEAR_PATTERN = "dd MMM"
private const val PREVIOUS_YEAR_PATTERN = "dd MMM yyyy"
private const val YEAR_MONTH_DAY_TIME_MINUTE_PATTERN = "dd MMM yyyy, HH:mm"

fun getCurrentIso8601Timestamp(): String {
    val currentDateTime = OffsetDateTime.now()
    return currentDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

fun getDifferenceFromTimestampInMinutes(isoOffsetDateTimeString: String): Long {
    return try {
        val parsedDateTime =
            OffsetDateTime.parse(isoOffsetDateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val currentDateTime =
            OffsetDateTime.now(parsedDateTime.offset)

        val durationBetween = Duration.between(parsedDateTime, currentDateTime)
        durationBetween.toMinutes()
    } catch (_: Exception) {
        0L
    }
}

fun convertIsoToRelativeYearFormat(
    locale: Locale,
    isoOffsetDateTimeString: String
): String {
    return try {
        val offsetDateTime = OffsetDateTime.parse(isoOffsetDateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val targetFormatterCurrentYear = DateTimeFormatter.ofPattern(CURRENT_YEAR_PATTERN, locale)
        val targetFormatterPreviousYear = DateTimeFormatter.ofPattern(PREVIOUS_YEAR_PATTERN, locale)

        if (offsetDateTime.dayOfYear == OffsetDateTime.now().dayOfYear)
            "Today"
        else if (offsetDateTime.year == OffsetDateTime.now().year) {
            offsetDateTime.format(targetFormatterCurrentYear)
        } else {
            offsetDateTime.format(targetFormatterPreviousYear)
        }
    } catch (_: Exception) {
        "Unknown"
    }
}

fun convertIsoToRelativeTimeFormat(
    locale: Locale,
    isoOffsetDateTimeString: String
): String {
    return try {
        val parsedDateTime = OffsetDateTime.parse(isoOffsetDateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val currentDateTime = OffsetDateTime.now(parsedDateTime.offset) // Use the same offset for accurate comparison

        val durationBetween = Duration.between(parsedDateTime, currentDateTime)
        val differenceInSeconds = durationBetween.seconds

        when {
            differenceInSeconds < 0 -> {
                // Time is in the future, handle as an edge case or error,
                // or show the date if that's preferred.
                // For now, let's fall back to showing the date as if it were in the past.
                // Or, you could return "In the future" or ""
                convertNoteTimestampToReadableFormat(locale, isoOffsetDateTimeString)
            }
            differenceInSeconds < FIVE_MINUTES_IN_SECONDS -> "Just now"
            differenceInSeconds <= SIXTY_MINUTES_IN_SECONDS -> "Last hour"
            else -> convertNoteTimestampToReadableFormat(locale, isoOffsetDateTimeString)
        }
    } catch (_: Exception) {
        "Unknown"
    }
}

fun convertNoteTimestampToReadableFormat(
    locale: Locale,
    isoOffsetDateTimeString: String
): String {
    return try {
        val offsetDateTime = OffsetDateTime.parse(isoOffsetDateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val targetFormatter = DateTimeFormatter.ofPattern(YEAR_MONTH_DAY_TIME_MINUTE_PATTERN, locale)

        offsetDateTime.format(targetFormatter)
    } catch (_: Exception) {
        "Unknown"
    }
}