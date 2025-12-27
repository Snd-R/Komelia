package snd.komelia.ui.settings.komf.general

import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import snd.komelia.ui.error.formatExceptionMessage
import snd.komelia.ui.settings.komf.KomfSharedState
import snd.komf.api.KomfErrorResponse
import snd.komf.api.PatchValue.Some
import snd.komf.api.config.EventListenerConfigUpdateRequest
import snd.komf.api.config.KavitaConfigUpdateRequest
import snd.komf.api.config.KomfConfig
import snd.komf.api.config.KomfConfigUpdateRequest
import snd.komf.api.mediaserver.KomfMediaServerLibrary
import snd.komf.api.mediaserver.KomfMediaServerLibraryId
import snd.komf.client.KomfMediaServerClient

class KavitaConnectionState(
    private val coroutineScope: CoroutineScope,
    private val komfMediaServerClient: KomfMediaServerClient,
    private val komfSharedState: KomfSharedState,
    private val onConfigUpdate: suspend (KomfConfigUpdateRequest) -> Unit,
) {
    val libraries = MutableStateFlow<List<KomfMediaServerLibrary>>(emptyList())

    val baseUrl = MutableStateFlow("localhost:5000")
    val enableEventListener = MutableStateFlow(false)
    val metadataLibraryFilters = MutableStateFlow(emptyList<KomfMediaServerLibraryId>())
    val notificationsLibraryFilters = MutableStateFlow(emptyList<KomfMediaServerLibraryId>())
    val connectionError = MutableStateFlow<String?>(null)

    fun initialize(config: KomfConfig) {
        enableEventListener.value = config.kavita.eventListener.enabled
        metadataLibraryFilters.value =
            config.kavita.eventListener.metadataLibraryFilter.map { KomfMediaServerLibraryId(it) }
        notificationsLibraryFilters.value =
            config.kavita.eventListener.notificationsLibraryFilter.map { KomfMediaServerLibraryId(it) }
        baseUrl.value = config.kavita.baseUri
        komfSharedState.getKavitaLibraries()
            .onEach { libraries.value = it }
            .launchIn(coroutineScope)
    }

    fun onBaseUrlChange(url: String) {
        this.baseUrl.value = url
        val kavitaUpdate = KavitaConfigUpdateRequest(baseUri = Some(url))
        coroutineScope.launch {
            onConfigUpdate(KomfConfigUpdateRequest(kavita = Some(kavitaUpdate)))
            checkConnection()
        }
    }

    fun onApiKeyUpdate(apiKey: String) {
        val kavitaUpdate = KavitaConfigUpdateRequest(apiKey = Some(apiKey))
        coroutineScope.launch {
            onConfigUpdate(KomfConfigUpdateRequest(kavita = Some(kavitaUpdate)))
            checkConnection()
        }
    }

    fun checkConnection() {
        coroutineScope.launch {
            try {
                val response = komfMediaServerClient.checkConnection()
                if (response.success) {
                    connectionError.value = null
                    komfSharedState.loadKavitaLibraries()
                } else {
                    connectionError.value = response.errorMessage
                        ?.let { "Connection Error: $it" }
                        ?: "Connection Error"
                }
            } catch (e: ResponseException) {
                val errorResponse = runCatching { e.response.body<KomfErrorResponse>().message }
                    .getOrElse { HttpStatusCode.fromValue(e.response.status.value).description }
                connectionError.value = errorResponse
            } catch (e: Exception) {
                connectionError.value = formatExceptionMessage(e)
            }
        }
    }

    fun onEventListenerEnable(enable: Boolean) {
        enableEventListener.value = enable
        val eventListenerUpdate = EventListenerConfigUpdateRequest(enabled = Some(enable))
        val kavitaUpdate = KavitaConfigUpdateRequest(eventListener = Some(eventListenerUpdate))

        coroutineScope.launch {
            onConfigUpdate(KomfConfigUpdateRequest(kavita = Some(kavitaUpdate)))
            checkConnection()
        }
    }

    fun onMetadataLibraryFilterSelect(libraryId: KomfMediaServerLibraryId) {
        metadataLibraryFilters.update { it.addOrRemove(libraryId) }

        val eventListenerUpdate = EventListenerConfigUpdateRequest(
            metadataLibraryFilter = Some(metadataLibraryFilters.value.map { it.value }),
        )
        val kavitaUpdate = KavitaConfigUpdateRequest(eventListener = Some(eventListenerUpdate))
        coroutineScope.launch {
            onConfigUpdate(KomfConfigUpdateRequest(kavita = Some(kavitaUpdate)))
        }
    }

    fun onNotificationsLibraryFilterSelect(libraryId: KomfMediaServerLibraryId) {
        notificationsLibraryFilters.update { it.addOrRemove(libraryId) }
        val eventListenerUpdate = EventListenerConfigUpdateRequest(
            notificationsLibraryFilter = Some(notificationsLibraryFilters.value.map { it.value })
        )
        val kavitaUpdate = KavitaConfigUpdateRequest(eventListener = Some(eventListenerUpdate))
        coroutineScope.launch {
            onConfigUpdate(KomfConfigUpdateRequest(kavita = Some(kavitaUpdate)))
        }
    }

    private fun <T> List<T>.addOrRemove(value: T): List<T> {
        val mutable = this.toMutableList()
        val existingIndex = mutable.indexOf(value)
        if (existingIndex != -1) mutable.removeAt(existingIndex)
        else mutable.add(value)

        return mutable
    }
}
