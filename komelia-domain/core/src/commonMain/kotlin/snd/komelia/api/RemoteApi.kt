package snd.komelia.api

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import snd.komelia.komga.api.KomgaApi
import snd.komga.client.KomgaClientFactory
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaSSESession

data class RemoteApi(
    override val actuatorApi: RemoteActuatorApi,
    override val announcementsApi: RemoteAnnouncementsApi,
    override val bookApi: RemoteBookApi,
    override val collectionsApi: RemoteCollectionsApi,
    override val fileSystemApi: RemoteFileSystemApi,
    override val libraryApi: RemoteLibraryApi,
    override val readListApi: RemoteReadListApi,
    override val referentialApi: RemoteReferentialApi,
    override val seriesApi: RemoteSeriesApi,
    override val settingsApi: RemoteSettingsApi,
    override val tasksApi: RemoteTaskApi,
    override val userApi: RemoteUserApi,
    private val komgaClientFactory: KomgaClientFactory,
    private val offlineEvents: SharedFlow<KomgaEvent>
) : KomgaApi {
    override suspend fun createSSESession(): KomgaSSESession {
        return CombinedSSESession(komgaClientFactory, offlineEvents)
    }

    private class CombinedSSESession(
        private val komgaClientFactory: KomgaClientFactory,
        offlineEvents: SharedFlow<KomgaEvent>,
    ) : KomgaSSESession {
        override val incoming: MutableSharedFlow<KomgaEvent> = MutableSharedFlow()
        private val logger = KotlinLogging.logger { }
        private val coroutineScope = CoroutineScope(
            Dispatchers.Default + SupervisorJob() +
                    CoroutineExceptionHandler { _, exception -> logger.catching(exception) })


        init {
            // it might take a long time for the sse connection to be established
            // and for the server to respond with at least single event so that ktor could transform response body to sse session
            // launch the connection in separate coroutine to prevent blocking offline events
            coroutineScope.launch {
                var session: KomgaSSESession? = null

                while (session == null && isActive) {
                    try {
                        session = komgaClientFactory.sseSession()
                    } catch (e: ClientRequestException) {
                        logger.catching(e)
                        delay(10_000)
                    }
                }

                currentCoroutineContext().ensureActive()
                if (session == null) return@launch

                session.incoming.collect { incoming.emit(it) }
            }

            coroutineScope.launch {
                offlineEvents.collect { incoming.emit(it) }
            }
        }


        override fun cancel() {
            // sse session should have this scope as its coroutine context
            coroutineScope.cancel()
        }
    }
}
