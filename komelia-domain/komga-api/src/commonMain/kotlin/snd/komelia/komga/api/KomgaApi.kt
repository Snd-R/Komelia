package snd.komelia.komga.api

import snd.komga.client.sse.KomgaSSESession

interface KomgaApi {
    val actuatorApi: KomgaActuatorApi
    val announcementsApi: KomgaAnnouncementsApi
    val bookApi: KomgaBookApi
    val collectionsApi: KomgaCollectionsApi
    val fileSystemApi: KomgaFileSystemApi
    val libraryApi: KomgaLibraryApi
    val readListApi: KomgaReadListApi
    val referentialApi: KomgaReferentialApi
    val seriesApi: KomgaSeriesApi
    val settingsApi: KomgaSettingsApi
    val tasksApi: KomgaTaskApi
    val userApi: KomgaUserApi

    suspend fun createSSESession(): KomgaSSESession
}
