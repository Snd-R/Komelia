@file:OptIn(InternalAPI::class)

package snd.komelia

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AppNotifications
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.date.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.w3c.dom.MutationObserverInit
import snd.komelia.db.SettingsStateActor
import snd.komelia.db.repository.ActorKomfSettingsRepository
import snd.komelia.db.settings.LocalStorageSettingsRepository
import snd.komf.client.KomfClientFactory
import kotlin.coroutines.CoroutineContext


val logger = KotlinLogging.logger("Komf")

fun main() {
    val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    coroutineScope.launch {
        val app = initApplication(coroutineScope)
        app.launch()
    }
}

private suspend fun initApplication(coroutineScope: CoroutineScope): AppState {
    val localStorageRepository = LocalStorageSettingsRepository()
    val komfSettingsRepository = ActorKomfSettingsRepository(
        SettingsStateActor(
            localStorageRepository.getKomfSettings(),
            localStorageRepository::saveKomfSettings
        )
    )
    val komfUrl = komfSettingsRepository.getKomfUrl().stateIn(coroutineScope)
    val komfClientFactory = KomfClientFactory.Builder()
        .baseUrl { komfUrl.value }
        .ktor(createKtorClient())
        .build()
    val vmFactory = KomfViewModelFactory(
        komfClientFactory = komfClientFactory,
        appNotifications = AppNotifications(),
        settingsRepository = komfSettingsRepository
    )

    return AppState(vmFactory)

}

internal val sseRequestAttr = AttributeKey<Boolean>("SSERequestFlag")
private val CustomResponse: AttributeKey<Any> = AttributeKey("CustomResponse")

// firefox returns empty list when calling Array.from(headers.keys()) on fetch response
// this results in missing content-type header and breaks json decode
// https://github.com/ktorio/ktor/blob/2673d3915e6d78ff8894ca2f3f2e34b03a73c9f2/ktor-client/ktor-client-core/wasmJs/src/io/ktor/client/engine/js/WasmJsClientEngine.kt#L161
// use this hack inject our own content-type header into every response
private fun createKtorClient(): HttpClient {
    val sessionAdapter = SSEClientResponseAdapter()
    return HttpClient(Js) {
        install("ContentTypeHack") {
            receivePipeline.intercept(HttpReceivePipeline.Before) { response ->
                if (!response.headers.isEmpty()) {
                    proceed()
                    return@intercept
                }

                val request = response.request
                val isSse = request.attributes.getOrNull(sseRequestAttr) == true

                val newResponse = object : HttpResponse() {
                    override val call: HttpClientCall = response.call
                    override val status: HttpStatusCode = response.status
                    override val version: HttpProtocolVersion = response.version
                    override val requestTime: GMTDate = response.requestTime
                    override val responseTime: GMTDate = response.responseTime
                    override val rawContent: ByteReadChannel = if (isSse) ByteReadChannel.Empty else response.rawContent

                    override val coroutineContext: CoroutineContext = response.coroutineContext
                    override val headers: Headers = HeadersBuilder().apply {
                        appendAll(response.headers)

                        if (isSse) {
                            this.append(HttpHeaders.ContentType, "text/event-stream")
                            this.append(HttpHeaders.TransferEncoding, "chunked")
                        } else {
                            this.append(HttpHeaders.ContentType, "application/json")
                        }
                    }.build()
                }
                if (isSse) {
                    request.attributes.remove(CustomResponse)
                    request.attributes.put(
                        CustomResponse,
                        DefaultClientSSESession(request.content as SSEClientContent, response.rawContent)
                    )
                }

                this.proceedWith(newResponse)
            }
        }
    }
}

fun mutationObserverConfig(): MutationObserverInit {
    js("return { childList: true, subtree: true };")
}
