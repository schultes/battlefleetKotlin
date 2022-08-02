package de.thm.mow2.fleetbattlegameandroid.controller.helper

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class DateTimeHelper {

    companion object {
        fun getCurrentDateAsString(): String {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"))
            val instant: OffsetDateTime = Instant.now().atOffset(ZoneOffset.ofHours(1))
            return instant.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
        }

        fun getFormattedDateAsString(dateString: String): String {
            val backToDate = getDateFromString(dateString)
            return backToDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + " Uhr"
        }

        fun getDateFromString(dateString: String): OffsetDateTime {
            return OffsetDateTime.parse(dateString)
        }
    }
}
