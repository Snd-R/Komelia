package io.github.snd_r.komga.sse

import kotlinx.coroutines.flow.SharedFlow

interface KomgaEventSource {
    val incoming: SharedFlow<KomgaEvent>

    fun connect()
    fun disconnect()
    fun close()
}
