package com.dhimandasgupta.notemark.common

import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


// Constants for time thresholds
private const val FIVE_MINUTES_IN_SECONDS = 5 * 60L
private const val SIXTY_MINUTES_IN_SECONDS = 60 * 60L

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

fun convertIsoToRelativeYearFormat(isoOffsetDateTimeString: String): String {
    return try {
        val offsetDateTime = OffsetDateTime.parse(isoOffsetDateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val targetFormatterCurrentYear = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
        val targetFormatterPreviousYear = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

        if (offsetDateTime.year == OffsetDateTime.now().year) {
            offsetDateTime.format(targetFormatterCurrentYear)
        } else {
            offsetDateTime.format(targetFormatterPreviousYear)
        }
    } catch (_: Exception) {
        "Unknown"
    }
}

fun convertIsoToRelativeTimeFormat(isoOffsetDateTimeString: String): String {
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
                convertNoteTimestampToReadableFormat(isoOffsetDateTimeString)
            }
            differenceInSeconds < FIVE_MINUTES_IN_SECONDS -> "Just now"
            differenceInSeconds <= SIXTY_MINUTES_IN_SECONDS -> "Last hour"
            else -> convertNoteTimestampToReadableFormat(isoOffsetDateTimeString)
        }
    } catch (_: Exception) {
        "Unknown"
    }
}

fun convertNoteTimestampToReadableFormat(isoOffsetDateTimeString: String): String {
    return try {
        val offsetDateTime = OffsetDateTime.parse(isoOffsetDateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val targetFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.getDefault())

        offsetDateTime.format(targetFormatter)
    } catch (_: Exception) {
        "Unknown"
    }
}