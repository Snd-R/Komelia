package io.github.snd_r.komelia.platform

import io.ktor.http.*

expect fun Url.resolve(childUrl: String): Url