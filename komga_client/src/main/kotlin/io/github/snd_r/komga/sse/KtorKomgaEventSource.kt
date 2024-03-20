package io.github.snd_r.komga.sse

import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class KtorKomgaEventSource internal constructor(
    private val ktor: HttpClient,
    private val json: Json,
) : KomgaEventSource {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(Dispatchers.IO.limitedParallelism(1) + SupervisorJob())

    private val _incoming = MutableSharedFlow<KomgaEvent>()
    override val incoming = _incoming.asSharedFlow()

    override fun connect() {
        disconnect()
        scope.launch {
            ktor.sse("sse/v1/events") {
                this.incoming.collect {
                    _incoming.emit(json.toKomgaEvent(it.event, it.data))
                }
            }
        }
    }

    override fun disconnect() {
        scope.coroutineContext.cancelChildren()
    }

    override fun close() {
        scope.cancel()
    }
}
