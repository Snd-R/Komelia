package snd.komelia

import io.ktor.http.*

expect fun codepointsCount(string: String): Long
expect fun Float.formatDecimal(numberOfDecimals: Int): String
expect fun Double.formatDecimal(numberOfDecimals: Int): String
expect fun Url.resolve(childUrl: String): Url
