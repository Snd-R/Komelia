package io.github.snd_r.komga

import io.github.snd_r.komga.sse.KomgaEvent
import io.github.snd_r.komga.sse.KomgaSSESession
import io.github.snd_r.komga.sse.toKomgaEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

internal actual suspend fun getSseSession(json: Json, baseUrl: String, authCookie: String): KomgaSSESession {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder().build()
        val session = OkHttpKomgaSseSession(client, json, baseUrl.toHttpUrl(), authCookie)
        session.connect()
        session
    }
}

class OkHttpKomgaSseSession(
    private val client: OkHttpClient,
    private val json: Json,
    private val baseUrl: HttpUrl,
    private val authCookie: String,
) : KomgaSSESession, EventSourceListener() {
    private val scope = CoroutineScope(Dispatchers.IO)
    override val incoming = MutableSharedFlow<KomgaEvent>()
    private var serverSentEventsSource: EventSource? = null
    private val connectionLock = ReentrantLock()
    private var isActive: Boolean = false

    fun connect() {
        connectionLock.withLock {
            isActive = true
            serverSentEventsSource = getSseConnection()
        }
    }

    override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
        scope.launch { incoming.emit(json.toKomgaEvent(type, data)) }
    }

    override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
        if (isActive) reconnect()
    }

    private fun reconnect() {
        scope.launch {
            if (isActive) {
                delay(10000)

                connectionLock.withLock {
                    if (isActive) {
                        serverSentEventsSource?.cancel()
                        serverSentEventsSource = getSseConnection()
                    }
                }

            }
        }
    }

    override fun cancel() {
        connectionLock.withLock {
            isActive = false
            scope.cancel()
            serverSentEventsSource?.cancel()
        }
    }

    private fun getSseConnection(): EventSource {
        val request = Request.Builder()
            .url(baseUrl.newBuilder().addPathSegments("sse/v1/events").build())
            .header("Cookie", authCookie)
            .build()

        return EventSources.createFactory(client).newEventSource(request, this)
    }
}
