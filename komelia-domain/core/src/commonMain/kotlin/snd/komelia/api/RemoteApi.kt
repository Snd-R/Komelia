package snd.komelia.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.merge
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
        return CombinedSSESession(komgaClientFactory.sseSession(), offlineEvents)
    }

    private class CombinedSSESession(
        private val onlineSession: KomgaSSESession,
        offlineEvents: SharedFlow<KomgaEvent>,
    ) : KomgaSSESession {
        override val incoming: Flow<KomgaEvent> = merge(onlineSession.incoming, offlineEvents)

        override fun cancel() {
            onlineSession.cancel()
        }

    }
}
