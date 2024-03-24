package io.github.snd_r.komga.sse

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import okio.withLock
import java.util.concurrent.locks.ReentrantLock

class OkHttpKomgaEventSource(
    private val client: OkHttpClient,
    private val json: Json,
    private val baseUrl: () -> String,
) : KomgaEventSource, EventSourceListener() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(Dispatchers.IO.limitedParallelism(1))

    private val _incoming = MutableSharedFlow<KomgaEvent>()
    override val incoming = _incoming.asSharedFlow()

    private var serverSentEventsSource: EventSource? = null

    private val connectionLock = ReentrantLock()
    private var isActive: Boolean = false

    override fun connect() {
        connectionLock.withLock {
            disconnect()
            serverSentEventsSource = createEventSource(baseUrl().toHttpUrl())
            isActive = true
        }
    }

    override fun disconnect() {
        connectionLock.withLock {
            isActive = false
            serverSentEventsSource?.cancel()
            scope.coroutineContext.cancelChildren()
        }
    }

    private fun createEventSource(url: HttpUrl): EventSource {
        val request = Request.Builder()
            .url(url.newBuilder().addPathSegments("sse/v1/events").build())
            .build()

        return EventSources.createFactory(client).newEventSource(request, this)
    }

    override fun onClosed(eventSource: EventSource) {
    }

    override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
        scope.launch { _incoming.emit(json.toKomgaEvent(type, data)) }
    }

    override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
        if (isActive) reconnect()
    }

    private fun reconnect() {
        scope.launch {
            if (isActive) {
                delay(10000)
                connect()
            }
        }
    }

    override fun close() {
        connectionLock.withLock {
            scope.cancel()
            disconnect()
        }
    }
}