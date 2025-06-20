package com.dhimandasgupta.notemark.common

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import kotlin.time.ExperimentalTime

fun getCurrentIso8601Timestamp(): String {
    val currentDateTime = OffsetDateTime.now()
    return currentDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

@OptIn(ExperimentalTime::class)
fun convertIso8601ToDate(isoString: String): Date? {
    try {
        val offsetDateTime =
            OffsetDateTime.parse(isoString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val instant: java.time.Instant? = offsetDateTime.toInstant()
        return Date.from(instant)
    } catch (_: DateTimeParseException) {
        return null
    }
}