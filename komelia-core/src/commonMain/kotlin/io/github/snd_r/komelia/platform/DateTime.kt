package io.github.snd_r.komelia.platform

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char


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
}
