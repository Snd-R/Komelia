package snd.komelia.offline

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineActions
import snd.komelia.offline.api.OfflineActuatorApi
import snd.komelia.offline.api.OfflineAnnouncementsApi
import snd.komelia.offline.api.OfflineBookApi
import snd.komelia.offline.api.OfflineCollectionsApi
import snd.komelia.offline.api.OfflineFileSystemApi
import snd.komelia.offline.api.OfflineKomgaApi
import snd.komelia.offline.api.OfflineLibraryApi
import snd.komelia.offline.api.OfflineReadListApi
import snd.komelia.offline.api.OfflineReferentialApi
import snd.komelia.offline.api.OfflineSeriesApi
import snd.komelia.offline.api.OfflineSettingsApi
import snd.komelia.offline.api.OfflineTaskApi
import snd.komelia.offline.api.OfflineUserApi
import snd.komelia.offline.api.repository.OfflineBookDtoRepository
import snd.komelia.offline.api.repository.OfflineReferentialRepository
import snd.komelia.offline.api.repository.OfflineSeriesDtoRepository
import snd.komelia.offline.book.actions.BookAnalyzeAction
import snd.komelia.offline.book.actions.BookDeleteAction
import snd.komelia.offline.book.actions.BookDeleteFilesAction
import snd.komelia.offline.book.actions.BookDeleteManyAction
import snd.komelia.offline.book.actions.BookKomgaImportAction
import snd.komelia.offline.book.actions.BookMarkRemoteDeletedAction
import snd.komelia.offline.book.actions.BookMetadataRefreshAction
import snd.komelia.offline.book.actions.BookMetadataUpdateAction
import snd.komelia.offline.book.actions.BookThumbnailDeleteAction
import snd.komelia.offline.book.actions.BookThumbnailSelectAction
import snd.komelia.offline.book.actions.BookThumbnailUploadAction
import snd.komelia.offline.book.repository.OfflineBookMetadataAggregationRepository
import snd.komelia.offline.book.repository.OfflineBookMetadataRepository
import snd.komelia.offline.book.repository.OfflineBookRepository
import snd.komelia.offline.book.repository.OfflineThumbnailBookRepository
import snd.komelia.offline.library.actions.LibraryAddAction
import snd.komelia.offline.library.actions.LibraryAnalyzeAction
import snd.komelia.offline.library.actions.LibraryDeleteAction
import snd.komelia.offline.library.actions.LibraryEmptyTrashAction
import snd.komelia.offline.library.actions.LibraryKomgaImportAction
import snd.komelia.offline.library.actions.LibraryPatchAction
import snd.komelia.offline.library.actions.LibraryRefreshMetadataAction
import snd.komelia.offline.library.actions.LibraryScanAction
import snd.komelia.offline.library.repository.OfflineLibraryRepository
import snd.komelia.offline.media.repository.OfflineMediaRepository
import snd.komelia.offline.mediacontainer.BookContentExtractors
import snd.komelia.offline.mediacontainer.DivinaExtractor
import snd.komelia.offline.mediacontainer.EpubExtractor
import snd.komelia.offline.readprogress.OfflineReadProgressRepository
import snd.komelia.offline.readprogress.actions.ProgressCompleteForBookAction
import snd.komelia.offline.readprogress.actions.ProgressCompleteForSeriesAction
import snd.komelia.offline.readprogress.actions.ProgressDeleteForBookAction
import snd.komelia.offline.readprogress.actions.ProgressMarkAction
import snd.komelia.offline.readprogress.actions.ProgressMarkProgressionAction
import snd.komelia.offline.series.actions.SeriesAddThumbnailAction
import snd.komelia.offline.series.actions.SeriesAggregateBookMetadataAction
import snd.komelia.offline.series.actions.SeriesAnalyzeAction
import snd.komelia.offline.series.actions.SeriesDeleteAction
import snd.komelia.offline.series.actions.SeriesDeleteManyAction
import snd.komelia.offline.series.actions.SeriesDeleteThumbnailAction
import snd.komelia.offline.series.actions.SeriesKomgaImportAction
import snd.komelia.offline.series.actions.SeriesRefreshMetadataAction
import snd.komelia.offline.series.actions.SeriesSelectThumbnailAction
import snd.komelia.offline.series.actions.SeriesUpdateMetadataAction
import snd.komelia.offline.series.repository.OfflineSeriesMetadataRepository
import snd.komelia.offline.series.repository.OfflineSeriesRepository
import snd.komelia.offline.series.repository.OfflineThumbnailSeriesRepository
import snd.komelia.offline.server.actions.MediaServerDeleteAction
import snd.komelia.offline.server.actions.MediaServerSaveAction
import snd.komelia.offline.server.repository.OfflineMediaServerRepository
import snd.komelia.offline.settings.OfflineSettingsRepository
import snd.komelia.offline.sync.BookDownloadService
import snd.komelia.offline.sync.PlatformDownloadManager
import snd.komelia.offline.sync.SyncManager
import snd.komelia.offline.sync.actions.SyncEntrySaveAction
import snd.komelia.offline.sync.actions.SyncReadProgressAction
import snd.komelia.offline.sync.model.DownloadEvent
import snd.komelia.offline.sync.repository.LogJournalRepository
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komelia.offline.tasks.TaskHandler
import snd.komelia.offline.tasks.TaskProcessor
import snd.komelia.offline.tasks.model.TaskAddedEvent
import snd.komelia.offline.tasks.repository.OfflineTasksRepository
import snd.komelia.offline.user.actions.UserDeleteAction
import snd.komelia.offline.user.actions.UserKomgaImportAction
import snd.komelia.offline.user.model.OfflineUser
import snd.komelia.offline.user.repository.OfflineUserRepository
import snd.komga.client.KomgaClientFactory
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.user.KomgaUser
import snd.komga.client.user.KomgaUserId

data class OfflineRepositories(
    val mediaServerRepository: OfflineMediaServerRepository,
    val mediaRepository: OfflineMediaRepository,
    val bookRepository: OfflineBookRepository,
    val bookMetadataRepository: OfflineBookMetadataRepository,
    val bookMetadataAggregationRepository: OfflineBookMetadataAggregationRepository,
    val libraryRepository: OfflineLibraryRepository,
    val readProgressRepository: OfflineReadProgressRepository,
    val seriesMetadataRepository: OfflineSeriesMetadataRepository,
    val seriesRepository: OfflineSeriesRepository,
    val thumbnailBookRepository: OfflineThumbnailBookRepository,
    val thumbnailSeriesRepository: OfflineThumbnailSeriesRepository,
    val userRepository: OfflineUserRepository,
    val bookDtoRepository: OfflineBookDtoRepository,
    val referentialRepository: OfflineReferentialRepository,
    val seriesDtoRepository: OfflineSeriesDtoRepository,
    val logJournalRepository: LogJournalRepository,
    val transactionTemplate: TransactionTemplate,

    val tasksRepository: OfflineTasksRepository,
    val offlineSettingsRepository: OfflineSettingsRepository,
)

abstract class OfflineModule(
    val repositories: OfflineRepositories,
    val authenticatedUser: StateFlow<KomgaUser?>,
    val onlineServerUrl: StateFlow<String>,
    val isOffline: StateFlow<Boolean>,
    val komgaClientFactory: KomgaClientFactory,
) {
    private val moduleScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun initDependencies(): OfflineDependencies {

        val komgaEvents = MutableSharedFlow<KomgaEvent>(
            replay = 0,
            extraBufferCapacity = Int.MAX_VALUE,
            onBufferOverflow = BufferOverflow.SUSPEND
        )

        val offlineUserId: StateFlow<KomgaUserId> = repositories.offlineSettingsRepository.getUserId()
            .stateIn(moduleScope, SharingStarted.Eagerly, OfflineUser.ROOT)


        val taskAddedEventFlow = MutableSharedFlow<TaskAddedEvent>(0, Int.MAX_VALUE, BufferOverflow.SUSPEND)
        val taskEmitter = OfflineTaskEmitter(
            tasksRepository = repositories.tasksRepository,
            tasksFlow = taskAddedEventFlow
        )
        val actions = createActions(
            isOffline = isOffline,
            komgaEvents = komgaEvents,
            taskEmitter = taskEmitter,
        )
        val downloadService = BookDownloadService(
            libraryDownloadPath = repositories.offlineSettingsRepository.getDownloadDirectory(),
            bookClient = komgaClientFactory.bookClient(),
            seriesClient = komgaClientFactory.seriesClient(),
            libraryClient = komgaClientFactory.libraryClient(),
            userClient = komgaClientFactory.userClient(),

            saveUserAction = actions.get(),
            saveServerAction = actions.get(),
            libraryImportAction = actions.get(),
            seriesImportAction = actions.get(),
            bookImportAction = actions.get(),
            onlineServerUrl = onlineServerUrl
        )

        val bookDownloadEvents: MutableSharedFlow<DownloadEvent> = MutableSharedFlow(
            replay = 0,
            extraBufferCapacity = 10_000,
            onBufferOverflow = BufferOverflow.SUSPEND
        )
        val downloadManager: PlatformDownloadManager = createPlatformDownloadManager(
            downloadService = downloadService,
            logJournalRepository = repositories.logJournalRepository,
            events = bookDownloadEvents
        )
        val fileService = BookContentExtractors(createDivinaExtractors(), createEpubExtractor())

        val offlineServerFlow = offlineUserId
            .map { repositories.mediaServerRepository.findByUserId(it) }
            .filterNotNull()
            .stateIn(moduleScope, SharingStarted.Eagerly, null)


        val komgaApi = OfflineKomgaApi(
            actuatorApi = OfflineActuatorApi(),
            announcementsApi = OfflineAnnouncementsApi(),
            bookApi = OfflineBookApi(
                mediaRepository = repositories.mediaRepository,
                komeliaBookRepository = repositories.bookDtoRepository,
                bookRepository = repositories.bookRepository,
                thumbnailBookRepository = repositories.thumbnailBookRepository,
                readProgressRepository = repositories.readProgressRepository,
                actions = actions,
                fileContentExtractors = fileService,
                offlineUserId = offlineUserId,
            ),
            collectionsApi = OfflineCollectionsApi(),
            fileSystemApi = OfflineFileSystemApi(),
            libraryApi = OfflineLibraryApi(
                libraryRepository = repositories.libraryRepository,
                mediaServer = offlineServerFlow,
                offlineUserId = offlineUserId,
                actions = actions
            ),
            readListApi = OfflineReadListApi(),
            referentialApi = OfflineReferentialApi(
                referentialRepository = repositories.referentialRepository
            ),
            seriesApi = OfflineSeriesApi(
                actions = actions,
                seriesDtoRepository = repositories.seriesDtoRepository,
                seriesThumbnailRepository = repositories.thumbnailSeriesRepository,
                seriesRepository = repositories.seriesRepository,
                libraryRepository = repositories.libraryRepository,
                bookRepository = repositories.bookRepository,
                thumbnailBookRepository = repositories.thumbnailBookRepository,
                offlineUserId = offlineUserId,
            ),
            settingsApi = OfflineSettingsApi(),
            tasksApi = OfflineTaskApi(),
            userApi = OfflineUserApi(
                offlineUserId = offlineUserId,
                userRepository = repositories.userRepository
            ),
            komgaEvents = komgaEvents,
        )

        val taskHandler = TaskHandler(
            actions = actions,
            bookRepository = repositories.bookRepository,
            taskEmitter = taskEmitter,
            downloadManager = downloadManager,
            komgaBookClient = komgaClientFactory.bookClient(),
        )
        val taskProcessor = TaskProcessor(
            tasksRepository = repositories.tasksRepository,
            taskHandler = taskHandler,
            taskAddedEvents = taskAddedEventFlow,
            logJournalRepository = repositories.logJournalRepository,
        )

        val syncManager = SyncManager(
            onlineUser = authenticatedUser,
            bookClient = komgaClientFactory.bookClient(),
            seriesClient = komgaClientFactory.seriesClient(),
            libraryClient = komgaClientFactory.libraryClient(),
            libraryRepository = repositories.libraryRepository,
            seriesRepository = repositories.seriesRepository,
            bookRepository = repositories.bookRepository,
            mediaServerRepository = repositories.mediaServerRepository,
            logJournalRepository = repositories.logJournalRepository,
            settingsRepository = repositories.offlineSettingsRepository,
            userSaveAction = actions.get(),
            libraryImportAction = actions.get(),
            seriesImportAction = actions.get(),
            bookImportAction = actions.get(),
            bookMarkDeletedAction = actions.get(),
            syncReadProgressAction = actions.get(),
        )
        taskProcessor.initialize()

        return OfflineDependencies(
            actions = actions,
            taskEmitter = taskEmitter,
            komgaEvents = komgaEvents,
            bookDownloadEvents = bookDownloadEvents,
            downloadService = downloadService,
            repositories = repositories,
            fileService = fileService,
            komgaApi = komgaApi
        )
    }

    private fun createActions(
        isOffline: StateFlow<Boolean>,
        komgaEvents: MutableSharedFlow<KomgaEvent>,
        taskEmitter: OfflineTaskEmitter
    ): OfflineActions {

        val bookDeleteManyAction = BookDeleteManyAction(
            bookRepository = repositories.bookRepository,
            bookMetadataRepository = repositories.bookMetadataRepository,
            thumbnailBookRepository = repositories.thumbnailBookRepository,
            mediaRepository = repositories.mediaRepository,
            readProgressRepository = repositories.readProgressRepository,
            transactionTemplate = repositories.transactionTemplate,
            komgaEvents = komgaEvents,
            taskEmitter = taskEmitter
        )
        val seriesDeleteManyAction = SeriesDeleteManyAction(
            seriesRepository = repositories.seriesRepository,
            seriesMetadataRepository = repositories.seriesMetadataRepository,
            seriesThumbnailSeriesRepository = repositories.thumbnailSeriesRepository,
            bookRepository = repositories.bookRepository,
            bookMetadataAggregationRepository = repositories.bookMetadataAggregationRepository,
            readProgressRepository = repositories.readProgressRepository,
            bookDeleteManyAction = bookDeleteManyAction,
            komgaEvents = komgaEvents,
            transactionTemplate = repositories.transactionTemplate
        )
        val userDeleteAction = UserDeleteAction(
            userRepository = repositories.userRepository,
            readProgressRepository = repositories.readProgressRepository,
            settingsRepository = repositories.offlineSettingsRepository,
            transactionTemplate = repositories.transactionTemplate,
            komgaEvents = komgaEvents
        )
        val libraryDeleteAction = LibraryDeleteAction(
            libraryRepository = repositories.libraryRepository,
            seriesRepository = repositories.seriesRepository,
            seriesDeleteManyAction = seriesDeleteManyAction,
            transactionTemplate = repositories.transactionTemplate,
            komgaEvents = komgaEvents,
        )
        val mediaServerDeleteAction = MediaServerDeleteAction(
            mediaServerRepository = repositories.mediaServerRepository,
            libraryRepository = repositories.libraryRepository,
            userRepository = repositories.userRepository,
            transactionTemplate = repositories.transactionTemplate,
            libraryDeleteAction = libraryDeleteAction,
            userDeleteAction = userDeleteAction,
        )

        return OfflineActions(
            listOf(
                bookDeleteManyAction,
                seriesDeleteManyAction,
                userDeleteAction,
                libraryDeleteAction,
                mediaServerDeleteAction,

                BookAnalyzeAction(),
                BookDeleteAction(
                    bookRepository = repositories.bookRepository,
                    bookMetadataRepository = repositories.bookMetadataRepository,
                    thumbnailBookRepository = repositories.thumbnailBookRepository,
                    readProgressRepository = repositories.readProgressRepository,
                    transactionTemplate = repositories.transactionTemplate,
                    mediaRepository = repositories.mediaRepository,
                    komgaEvents = komgaEvents,
                    taskEmitter = taskEmitter,
                    isOffline = isOffline
                ),
                BookDeleteFilesAction(),
                BookMarkRemoteDeletedAction(
                    bookRepository = repositories.bookRepository,
                    transactionTemplate = repositories.transactionTemplate
                ),
                BookMetadataRefreshAction(),
                BookMetadataUpdateAction(),
                BookThumbnailDeleteAction(),
                BookThumbnailSelectAction(),
                BookThumbnailUploadAction(),
                BookKomgaImportAction(
                    bookRepository = repositories.bookRepository,
                    bookMetadataRepository = repositories.bookMetadataRepository,
                    thumbnailBookRepository = repositories.thumbnailBookRepository,
                    readProgressRepository = repositories.readProgressRepository,
                    mediaRepository = repositories.mediaRepository,
                    logJournalRepository = repositories.logJournalRepository,
                    bookClient = komgaClientFactory.bookClient(),
                    taskEmitter = taskEmitter,
                    transactionTemplate = repositories.transactionTemplate,
                    komgaEvents = komgaEvents
                ),

                LibraryAddAction(
                    libraryRepository = repositories.libraryRepository,
                    events = komgaEvents
                ),
                LibraryAnalyzeAction(),
                LibraryEmptyTrashAction(),
                LibraryKomgaImportAction(
                    libraryRepository = repositories.libraryRepository,
                    mediaServerRepository = repositories.mediaServerRepository,
                    logJournalRepository = repositories.logJournalRepository,
                    transactionTemplate = repositories.transactionTemplate
                ),
                LibraryPatchAction(),
                LibraryRefreshMetadataAction(),
                LibraryScanAction(),

                ProgressCompleteForBookAction(
                    mediaRepository = repositories.mediaRepository,
                    readProgressRepository = repositories.readProgressRepository,
                    transactionTemplate = repositories.transactionTemplate,
                    komgaEvents = komgaEvents
                ),
                ProgressCompleteForSeriesAction(
                    readProgressRepository = repositories.readProgressRepository,
                    bookRepository = repositories.bookRepository,
                    mediaRepository = repositories.mediaRepository,
                    userRepository = repositories.userRepository,
                    transactionTemplate = repositories.transactionTemplate,
                    komgaEvents = komgaEvents
                ),
                ProgressDeleteForBookAction(
                    readProgressRepository = repositories.readProgressRepository,
                    transactionTemplate = repositories.transactionTemplate,
                    komgaEvents = komgaEvents
                ),
                ProgressMarkAction(
                    mediaRepository = repositories.mediaRepository,
                    readProgressRepository = repositories.readProgressRepository,
                    transactionTemplate = repositories.transactionTemplate,
                    komgaEvents = komgaEvents
                ),
                ProgressMarkProgressionAction(
                    mediaRepository = repositories.mediaRepository,
                    readProgressRepository = repositories.readProgressRepository,
                    transactionTemplate = repositories.transactionTemplate
                ),

                SeriesAddThumbnailAction(),
                SeriesAggregateBookMetadataAction(
                    bookRepository = repositories.bookRepository,
                    bookMetadataRepository = repositories.bookMetadataRepository,
                    bookMetadataAggregationRepository = repositories.bookMetadataAggregationRepository,
                    transactionTemplate = repositories.transactionTemplate
                ),
                SeriesAnalyzeAction(),
                SeriesDeleteAction(
                    seriesRepository = repositories.seriesRepository,
                    seriesMetadataRepository = repositories.seriesMetadataRepository,
                    seriesThumbnailSeriesRepository = repositories.thumbnailSeriesRepository,
                    bookRepository = repositories.bookRepository,
                    bookDeleteManyAction = bookDeleteManyAction,
                    transactionTemplate = repositories.transactionTemplate,
                    komgaEvents = komgaEvents,
                ),
                SeriesDeleteThumbnailAction(),
                SeriesKomgaImportAction(
                    seriesRepository = repositories.seriesRepository,
                    seriesMetadataRepository = repositories.seriesMetadataRepository,
                    thumbnailSeriesRepository = repositories.thumbnailSeriesRepository,
                    bookMetadataAggregationRepository = repositories.bookMetadataAggregationRepository,
                    logJournalRepository = repositories.logJournalRepository,
                    seriesClient = komgaClientFactory.seriesClient(),
                    transactionTemplate = repositories.transactionTemplate
                ),
                SeriesRefreshMetadataAction(),
                SeriesSelectThumbnailAction(),
                SeriesUpdateMetadataAction(),

                SyncEntrySaveAction(repositories.logJournalRepository),
                SyncReadProgressAction(
                    settingsRepository = repositories.offlineSettingsRepository,
                    bookClient = komgaClientFactory.bookClient(),
                    bookMetadataRepository = repositories.bookMetadataRepository,
                    readProgressRepository = repositories.readProgressRepository,
                    mediaServerRepository = repositories.mediaServerRepository,
                    userRepository = repositories.userRepository,
                    logJournalRepository = repositories.logJournalRepository,
                    transactionTemplate = repositories.transactionTemplate
                ),
                MediaServerSaveAction(
                    mediaServerRepository = repositories.mediaServerRepository,
                    transactionTemplate = repositories.transactionTemplate
                ),
                UserKomgaImportAction(
                    userRepository = repositories.userRepository,
                    transactionTemplate = repositories.transactionTemplate
                ),
            )
        )
    }

    protected abstract fun createDivinaExtractors(): List<DivinaExtractor>
    protected abstract fun createEpubExtractor(): EpubExtractor
    protected abstract fun createPlatformDownloadManager(
        downloadService: BookDownloadService,
        logJournalRepository: LogJournalRepository,
        events: MutableSharedFlow<DownloadEvent>,
    ): PlatformDownloadManager
}