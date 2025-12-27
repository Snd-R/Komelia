package snd.komelia.offline.sync

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import snd.komelia.offline.book.actions.BookKomgaImportAction
import snd.komelia.offline.book.actions.BookMarkRemoteDeletedAction
import snd.komelia.offline.book.repository.OfflineBookRepository
import snd.komelia.offline.library.actions.LibraryKomgaImportAction
import snd.komelia.offline.library.repository.OfflineLibraryRepository
import snd.komelia.offline.series.actions.SeriesKomgaImportAction
import snd.komelia.offline.series.repository.OfflineSeriesRepository
import snd.komelia.offline.server.repository.OfflineMediaServerRepository
import snd.komelia.offline.settings.OfflineSettingsRepository
import snd.komelia.offline.sync.actions.SyncReadProgressAction
import snd.komelia.offline.sync.model.OfflineLogEntry.Companion.logError
import snd.komelia.offline.sync.repository.LogJournalRepository
import snd.komelia.offline.user.actions.UserKomgaImportAction
import snd.komelia.offline.user.model.OfflineUser
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryClient
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.user.KomgaUser
import snd.komga.client.user.KomgaUserId
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours


private val logger = KotlinLogging.logger { }

class SyncManager(
    onlineUser: StateFlow<KomgaUser?>,
    private val bookClient: KomgaBookClient,
    private val seriesClient: KomgaSeriesClient,
    private val libraryClient: KomgaLibraryClient,

    private val libraryRepository: OfflineLibraryRepository,
    private val seriesRepository: OfflineSeriesRepository,
    private val bookRepository: OfflineBookRepository,
    private val mediaServerRepository: OfflineMediaServerRepository,
    private val logJournalRepository: LogJournalRepository,
    private val settingsRepository: OfflineSettingsRepository,

    private val userSaveAction: UserKomgaImportAction,
    private val libraryImportAction: LibraryKomgaImportAction,
    private val seriesImportAction: SeriesKomgaImportAction,
    private val bookImportAction: BookKomgaImportAction,
    private val bookMarkDeletedAction: BookMarkRemoteDeletedAction,
    private val syncReadProgressAction: SyncReadProgressAction,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        onlineUser.filterNotNull().onEach { user -> doSync(user) }
            .launchIn(coroutineScope)
    }

    private suspend fun doSync(
        onlineUser: KomgaUser,
    ) {

        syncReadProgressAction.execute(onlineUser)
        syncDataToLocal(onlineUser)

    }

    private suspend fun syncDataToLocal(onlineUser: KomgaUser) {
        val newSyncDate = Clock.System.now()
        val lastSyncDate = settingsRepository.getDataSyncDate().first()

        // sync all data at most every 6 hours
        if (lastSyncDate != null && lastSyncDate.plus(6.hours) > newSyncDate) {
            return
        }
        val server = mediaServerRepository.findByUserId(onlineUser.id) ?: return

        if (onlineUser.id != OfflineUser.ROOT) {
            userSaveAction.execute(onlineUser, server.id)
        }

        val localLibraries = libraryRepository.findAllByMediaServer(server.id)
        for (library in localLibraries) {
            try {
                val remoteLibrary = libraryClient.getLibrary(library.id)
                libraryImportAction.execute(remoteLibrary, server.id)
                syncSeriesData(remoteLibrary, onlineUser.id)
            } catch (e: ClientRequestException) {
                logger.catching(e)
                if (e.response.status == HttpStatusCode.NotFound) {
                    bookRepository.findAllIdsByLibraryId(library.id).forEach { bookMarkDeletedAction.execute(it) }
                }
                logJournalRepository.logError(e) { "Library import error '${library.name}'" }
            }
        }

        settingsRepository.putDataSyncDate(newSyncDate)
    }

    private suspend fun syncSeriesData(library: KomgaLibrary, userId: KomgaUserId) {
        val series = seriesRepository.findAllByLibraryId(library.id)
        for (offlineSeries in series) {
            try {
                val remoteSeries = seriesClient.getOneSeries(offlineSeries.id)
                seriesImportAction.execute(remoteSeries)
                syncBookData(remoteSeries, userId)
            } catch (e: ClientRequestException) {
                logger.catching(e)
                if (e.response.status == HttpStatusCode.NotFound) {
                    bookRepository.findAllIdsBySeriesId(offlineSeries.id).forEach { bookMarkDeletedAction.execute(it) }
                }
                logJournalRepository.logError(e) {
                    "Series import error '${offlineSeries.name}'"
                }
            }
        }
    }

    private suspend fun syncBookData(series: KomgaSeries, userId: KomgaUserId) {
        val books = bookRepository.findAllNotDeleted(series.id)
        for (localBook in books) {
            try {
                val remoteBook = bookClient.getOne(localBook.id)
                bookImportAction.execute(
                    book = remoteBook,
                    offlinePath = localBook.fileDownloadPath,
                    userId = userId,
                    localFileModifiedDate = localBook.localFileLastModified
                )
            } catch (e: ClientRequestException) {
                logger.catching(e)
                logJournalRepository.logError(e) { "Book import error '${localBook.name}'" }
                if (e.response.status == HttpStatusCode.NotFound) {
                    bookMarkDeletedAction.execute(localBook.id)
                }
            }
        }
    }
}