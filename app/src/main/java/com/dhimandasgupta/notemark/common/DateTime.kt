package com.dhimandasgupta.notemark.common

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.time.ExperimentalTime

fun getCurrentIso8601Timestamp(): String {
    val currentDateTime = OffsetDateTime.now()
    return currentDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

@OptIn(ExperimentalTime::class)
fun convertIsoOffsetToReadableFormat(isoOffsetDateTimeString: String): String {
    return try {
        val offsetDateTime = OffsetDateTime.parse(isoOffsetDateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val targetFormatterCurrentYear = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
        val targetFormatterPreviousYear = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

        if (offsetDateTime.year == OffsetDateTime.now().year) {
            offsetDateTime.format(targetFormatterCurrentYear)
        } else {
            offsetDateTime.format(targetFormatterPreviousYear)
        }
    } catch (e: DateTimeParseException) {
        ""
    } catch (e: Exception) {
        ""
    }
}