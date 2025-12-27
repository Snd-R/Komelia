package snd.komelia.offline.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import snd.komelia.komga.api.KomgaApi
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaSSESession

data class OfflineKomgaApi(
    override val actuatorApi: OfflineActuatorApi,
    override val announcementsApi: OfflineAnnouncementsApi,
    override val bookApi: OfflineBookApi,
    override val collectionsApi: OfflineCollectionsApi,
    override val fileSystemApi: OfflineFileSystemApi,
    override val libraryApi: OfflineLibraryApi,
    override val readListApi: OfflineReadListApi,
    override val referentialApi: OfflineReferentialApi,
    override val seriesApi: OfflineSeriesApi,
    override val settingsApi: OfflineSettingsApi,
    override val tasksApi: OfflineTaskApi,
    override val userApi: OfflineUserApi,
    private val komgaEvents: SharedFlow<KomgaEvent>
) : KomgaApi {
    override suspend fun createSSESession() = object : KomgaSSESession {
        override val incoming: Flow<KomgaEvent> = komgaEvents
        override fun cancel() = Unit
    }
}
