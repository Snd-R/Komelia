package io.github.snd_r.komga.sse

import kotlinx.coroutines.flow.Flow

interface KomgaSSESession {
    val incoming: Flow<KomgaEvent>

    fun cancel()

}
