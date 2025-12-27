package snd.komelia

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

object DefaultDateTimeFormats {
    val dateTimeFormat = DateTimeComponents.Format {
        year()
        char('-')
        monthNumber()
        char('-')
        day()
        char(' ')
        hour()
        char(':')
        minute()
    }
    val localDateTimeFormat = LocalDateTime.Format {
        year()
        char('-')
        monthNumber()
        char('-')
        day()
        char(' ')
        hour()
        char(':')
        minute()
    }
    val localDateFormat = LocalDateTime.Format {
        year()
        char('-')
        monthNumber()
        char('-')
        day()
    }
    val localTimeFormat = LocalDateTime.Format {
        hour()
        char(':')
        minute()
    }

    fun Instant.toSystemTimeString(): String {
        val localDateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
        return localDateTime.format(localDateTimeFormat)
    }
}
