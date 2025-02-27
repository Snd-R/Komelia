package io.github.snd_r.komelia

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.settings.SecretsRepository
import io.github.snd_r.komelia.ui.BookSiblingsContext
import io.github.snd_r.komelia.ui.KomgaSharedState
import io.github.snd_r.komelia.ui.MainScreenViewModel
import io.github.snd_r.komelia.ui.book.BookViewModel
import io.github.snd_r.komelia.ui.collection.CollectionViewModel
import io.github.snd_r.komelia.ui.color.ColorCorrectionViewModel
import io.github.snd_r.komelia.ui.common.menus.bulk.BookBulkActions
import io.github.snd_r.komelia.ui.common.menus.bulk.CollectionBulkActions
import io.github.snd_r.komelia.ui.common.menus.bulk.ReadListBulkActions
import io.github.snd_r.komelia.ui.common.menus.bulk.SeriesBulkActions
import io.github.snd_r.komelia.ui.dialogs.book.edit.BookEditDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.book.editbulk.BookBulkEditDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.collectionadd.AddToCollectionDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.collectionedit.CollectionEditDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.filebrowser.FileBrowserDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.komf.identify.KomfIdentifyDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.komf.identify.KomfLibraryIdentifyViewmodel
import io.github.snd_r.komelia.ui.dialogs.komf.reset.KomfResetMetadataDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.libraryedit.LibraryEditDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.oneshot.OneshotEditDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.readlistadd.AddToReadListDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.readlistedit.ReadListEditDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.series.edit.SeriesEditDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.series.editbulk.SeriesBulkEditDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.user.PasswordChangeDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.user.UserAddDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.user.UserEditDialogViewModel
import io.github.snd_r.komelia.ui.home.HomeViewModel
import io.github.snd_r.komelia.ui.library.LibraryViewModel
import io.github.snd_r.komelia.ui.login.LoginViewModel
import io.github.snd_r.komelia.ui.navigation.SearchBarState
import io.github.snd_r.komelia.ui.oneshot.OneshotViewModel
import io.github.snd_r.komelia.ui.reader.epub.EpubReaderViewModel
import io.github.snd_r.komelia.ui.reader.image.ReaderViewModel
import io.github.snd_r.komelia.ui.readlist.ReadListViewModel
import io.github.snd_r.komelia.ui.search.SearchViewModel
import io.github.snd_r.komelia.ui.series.SeriesViewModel
import io.github.snd_r.komelia.ui.series.SeriesViewModel.SeriesTab
import io.github.snd_r.komelia.ui.settings.account.AccountSettingsViewModel
import io.github.snd_r.komelia.ui.settings.analysis.MediaAnalysisViewModel
import io.github.snd_r.komelia.ui.settings.announcements.AnnouncementsViewModel
import io.github.snd_r.komelia.ui.settings.appearance.AppSettingsViewModel
import io.github.snd_r.komelia.ui.settings.authactivity.AuthenticationActivityViewModel
import io.github.snd_r.komelia.ui.settings.epub.EpubReaderSettingsViewModel
import io.github.snd_r.komelia.ui.settings.imagereader.ImageReaderSettingsViewModel
import io.github.snd_r.komelia.ui.settings.komf.KomfSharedState
import io.github.snd_r.komelia.ui.settings.komf.general.KomfSettingsViewModel
import io.github.snd_r.komelia.ui.settings.komf.jobs.KomfJobsViewModel
import io.github.snd_r.komelia.ui.settings.komf.notifications.KomfNotificationSettingsViewModel
import io.github.snd_r.komelia.ui.settings.komf.processing.KomfProcessingSettingsViewModel
import io.github.snd_r.komelia.ui.settings.komf.providers.KomfProvidersSettingsViewModel
import io.github.snd_r.komelia.ui.settings.navigation.SettingsNavigationViewModel
import io.github.snd_r.komelia.ui.settings.server.ServerSettingsViewModel
import io.github.snd_r.komelia.ui.settings.updates.AppUpdatesViewModel
import io.github.snd_r.komelia.ui.settings.users.UsersViewModel
import io.github.snd_r.komelia.updates.AppRelease
import io.github.snd_r.komelia.updates.StartupUpdateChecker
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import snd.komf.api.MediaServer
import snd.komf.api.MediaServer.KAVITA
import snd.komf.api.MediaServer.KOMGA
import snd.komga.client.KomgaClientFactory
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookId
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.user.KomgaUser

class ViewModelFactory(
    private val dependencies: DependencyContainer,
    private val platformType: PlatformType,
) {
    private val komgaClientFactory: KomgaClientFactory
        get() = dependencies.komgaClientFactory
    private val settingsRepository: CommonSettingsRepository
        get() = dependencies.settingsRepository
    private val readerSettingsRepository: ImageReaderSettingsRepository
        get() = dependencies.imageReaderSettingsRepository
    private val secretsRepository: SecretsRepository
        get() = dependencies.secretsRepository

    private val releases = MutableStateFlow<List<AppRelease>>(emptyList())
    private val imageReaderCurrentBook = MutableStateFlow<KomgaBookId?>(null)
        .also { dependencies.colorCorrectionStep.setBookFlow(it) }

    private val komfSharedState = KomfSharedState(
        komfConfigClient = dependencies.komfClientFactory.configClient(),
        komgaServerClient = dependencies.komfClientFactory.mediaServerClient(KOMGA),
        kavitaServerClient = dependencies.komfClientFactory.mediaServerClient(KAVITA),
        notifications = dependencies.appNotifications,
    )
    val komgaSharedState = KomgaSharedState(
        userClient = komgaClientFactory.userClient(),
        libraryClient = komgaClientFactory.libraryClient(),
    )

    private val komgaEventSource = ManagedKomgaEvents(
        eventSourceFactory = komgaClientFactory::sseSession,
        memoryCache = dependencies.coilImageLoader.memoryCache,
        diskCache = dependencies.coilImageLoader.diskCache,
        libraryClient = komgaClientFactory.libraryClient(),
        komgaSharedState = komgaSharedState
    )

    private val startupUpdateChecker = dependencies.appUpdater?.let { updater ->
        StartupUpdateChecker(
            updater,
            settingsRepository,
            releases
        )
    }
    val screenReloadEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = DROP_OLDEST)

    fun getLibraryViewModel(
        libraryId: KomgaLibraryId?,
    ): LibraryViewModel {
        return LibraryViewModel(
            libraryClient = komgaClientFactory.libraryClient(),
            collectionClient = komgaClientFactory.collectionClient(),
            readListsClient = komgaClientFactory.readListClient(),
            seriesClient = komgaClientFactory.seriesClient(),
            referentialClient = komgaClientFactory.referentialClient(),

            appNotifications = dependencies.appNotifications,
            komgaEvents = komgaEventSource.events,
            libraryFlow = getLibraryFlow(libraryId),
            settingsRepository = dependencies.settingsRepository,
        )
    }

    fun getHomeViewModel(): HomeViewModel {
        return HomeViewModel(
            seriesClient = komgaClientFactory.seriesClient(),
            bookClient = komgaClientFactory.bookClient(),
            appNotifications = dependencies.appNotifications,
            komgaEvents = komgaEventSource.events,
            cardWidthFlow = getGridCardWidth(),
        )
    }

    fun getNavigationViewModel(navigator: Navigator): MainScreenViewModel {
        return MainScreenViewModel(
            libraryClient = komgaClientFactory.libraryClient(),
            appNotifications = dependencies.appNotifications,
            navigator = navigator,
            komgaEvents = komgaEventSource.events,
            screenReloadFlow = screenReloadEvents,
            searchBarState = SearchBarState(
                seriesClient = komgaClientFactory.seriesClient(),
                bookClient = komgaClientFactory.bookClient(),
                appNotifications = dependencies.appNotifications,
                libraries = komgaSharedState.libraries
            ),
            libraries = komgaSharedState.libraries,
        )
    }

    fun getSeriesViewModel(
        seriesId: KomgaSeriesId,
        series: KomgaSeries? = null,
        defaultTab: SeriesTab? = null,
    ) = SeriesViewModel(
        seriesId = seriesId,
        series = series,
        libraries = komgaSharedState.libraries,
        seriesClient = komgaClientFactory.seriesClient(),
        bookClient = komgaClientFactory.bookClient(),
        collectionClient = komgaClientFactory.collectionClient(),
        notifications = dependencies.appNotifications,
        events = komgaEventSource.events,
        settingsRepository = settingsRepository,
        referentialClient = komgaClientFactory.referentialClient(),
        defaultTab = defaultTab ?: SeriesTab.BOOKS,
    )

    fun getBookViewModel(bookId: KomgaBookId, book: KomgaBook?): BookViewModel {
        return BookViewModel(
            book = book,
            bookId = bookId,
            bookClient = komgaClientFactory.bookClient(),
            notifications = dependencies.appNotifications,
            komgaEvents = komgaEventSource.events,
            libraries = komgaSharedState.libraries,
            settingsRepository = settingsRepository,
            readListClient = komgaClientFactory.readListClient(),
        )
    }

    fun getOneshotViewModel(
        seriesId: KomgaSeriesId,
        series: KomgaSeries? = null,
        book: KomgaBook? = null,
    ) = OneshotViewModel(
        series = series,
        book = book,
        seriesId = seriesId,
        seriesClient = komgaClientFactory.seriesClient(),
        bookClient = komgaClientFactory.bookClient(),
        events = komgaEventSource.events,
        notifications = dependencies.appNotifications,
        libraries = komgaSharedState.libraries,
        settingsRepository = settingsRepository,
        readListClient = komgaClientFactory.readListClient(),
        collectionClient = komgaClientFactory.collectionClient(),
    )

    fun getBookReaderViewModel(
        navigator: Navigator,
        markReadProgress: Boolean,
        bookSiblingsContext: BookSiblingsContext
    ): ReaderViewModel {
        return ReaderViewModel(
            bookClient = komgaClientFactory.bookClient(),
            seriesClient = komgaClientFactory.seriesClient(),
            readListClient = komgaClientFactory.readListClient(),
            navigator = navigator,
            appNotifications = dependencies.appNotifications,
            readerSettingsRepository = readerSettingsRepository,
            imageLoader = dependencies.bookImageLoader,
            appStrings = dependencies.appStrings,
            readerImageFactory = dependencies.readerImageFactory,
            currentBookId = imageReaderCurrentBook,
            onnxRuntime = dependencies.onnxRuntime,
            colorCorrectionIsActive = dependencies.colorCorrectionStep.isActive,
            bookSiblingsContext = bookSiblingsContext,
            markReadProgress = markReadProgress,
        )
    }

    fun getLoginViewModel(): LoginViewModel {
        return LoginViewModel(
            settingsRepository = settingsRepository,
            secretsRepository = secretsRepository,
            komgaUserClient = komgaClientFactory.userClient(),
            komgaLibraryClient = komgaClientFactory.libraryClient(),
            komgaSharedState = komgaSharedState,
            notifications = dependencies.appNotifications,
            platform = platformType
        )
    }

    fun getLibraryEditDialogViewModel(library: KomgaLibrary?, onDismissRequest: () -> Unit) =
        LibraryEditDialogViewModel(
            library = library,
            onDialogDismiss = onDismissRequest,
            libraryClient = komgaClientFactory.libraryClient(),
            appNotifications = dependencies.appNotifications,
        )

    fun getSeriesEditDialogViewModel(series: KomgaSeries, onDismissRequest: () -> Unit) =
        SeriesEditDialogViewModel(
            series = series,
            onDialogDismiss = onDismissRequest,
            seriesClient = komgaClientFactory.seriesClient(),
            referentialClient = komgaClientFactory.referentialClient(),
            notifications = dependencies.appNotifications,
            cardWidth = getGridCardWidth(),
        )

    fun getSeriesBulkEditDialogViewModel(series: List<KomgaSeries>, onDismissRequest: () -> Unit) =
        SeriesBulkEditDialogViewModel(
            series = series,
            onDialogDismiss = onDismissRequest,
            seriesClient = komgaClientFactory.seriesClient(),
            referentialClient = komgaClientFactory.referentialClient(),
            notifications = dependencies.appNotifications,
        )

    fun getBookEditDialogViewModel(book: KomgaBook, onDismissRequest: () -> Unit) =
        BookEditDialogViewModel(
            book = book,
            onDialogDismiss = onDismissRequest,
            bookClient = komgaClientFactory.bookClient(),
            referentialClient = komgaClientFactory.referentialClient(),
            notifications = dependencies.appNotifications,
            cardWidth = getGridCardWidth(),
        )

    fun getOneshotEditDialogViewModel(
        seriesId: KomgaSeriesId,
        series: KomgaSeries?,
        book: KomgaBook?,
        onDismissRequest: () -> Unit
    ) = OneshotEditDialogViewModel(
        seriesId = seriesId,
        series = series,
        book = book,
        onDialogDismiss = onDismissRequest,
        bookClient = komgaClientFactory.bookClient(),
        seriesClient = komgaClientFactory.seriesClient(),
        referentialClient = komgaClientFactory.referentialClient(),
        notifications = dependencies.appNotifications,
        cardWidth = getGridCardWidth(),
    )

    fun getBookBulkEditDialogViewModel(books: List<KomgaBook>, onDismissRequest: () -> Unit) =
        BookBulkEditDialogViewModel(
            books = books,
            onDialogDismiss = onDismissRequest,
            bookClient = komgaClientFactory.bookClient(),
            referentialClient = komgaClientFactory.referentialClient(),
            notifications = dependencies.appNotifications,
        )

    fun getCollectionEditDialogViewModel(
        collection: KomgaCollection,
        onDismissRequest: () -> Unit
    ) = CollectionEditDialogViewModel(
        collection = collection,
        onDialogDismiss = onDismissRequest,
        collectionClient = komgaClientFactory.collectionClient(),
        notifications = dependencies.appNotifications,
        cardWidth = getGridCardWidth(),
    )

    fun getReadListEditDialogViewModel(readList: KomgaReadList, onDismissRequest: () -> Unit) =
        ReadListEditDialogViewModel(
            readList = readList,
            onDialogDismiss = onDismissRequest,
            readListClient = komgaClientFactory.readListClient(),
            notifications = dependencies.appNotifications,
            cardWidth = getGridCardWidth(),
        )

    fun getAddToCollectionDialogViewModel(series: List<KomgaSeries>, onDismissRequest: () -> Unit) =
        AddToCollectionDialogViewModel(
            series = series,
            onDismissRequest = onDismissRequest,
            collectionClient = komgaClientFactory.collectionClient(),
            appNotifications = dependencies.appNotifications,
        )

    fun getAddToReadListDialogViewModel(books: List<KomgaBook>, onDismissRequest: () -> Unit) =
        AddToReadListDialogViewModel(
            books = books,
            onDismissRequest = onDismissRequest,
            readListClient = komgaClientFactory.readListClient(),
            appNotifications = dependencies.appNotifications,
        )

    fun getFileBrowserDialogViewModel() =
        FileBrowserDialogViewModel(komgaClientFactory.fileSystemClient(), dependencies.appNotifications)


    fun getSearchViewModel() = SearchViewModel(
        seriesClient = komgaClientFactory.seriesClient(),
        bookClient = komgaClientFactory.bookClient(),
        appNotifications = dependencies.appNotifications,
        libraries = komgaSharedState.libraries,
    )


    fun getAccountViewModel(): AccountSettingsViewModel {
        val user = requireNotNull(komgaSharedState.authenticatedUser.value)
        return AccountSettingsViewModel(user)
    }

    fun getAuthenticationActivityViewModel(forMe: Boolean): AuthenticationActivityViewModel {
        return AuthenticationActivityViewModel(
            forMe,
            komgaClientFactory.userClient(),
            dependencies.appNotifications
        )
    }

    fun getUsersViewModel(): UsersViewModel {
        val user = requireNotNull(komgaSharedState.authenticatedUser.value)
        return UsersViewModel(dependencies.appNotifications, komgaClientFactory.userClient(), user)
    }

    fun getPasswordChangeDialogViewModel(user: KomgaUser?) = PasswordChangeDialogViewModel(
        dependencies.appNotifications,
        komgaClientFactory.userClient(),
        user
    )

    fun getUserAddDialogViewModel(): UserAddDialogViewModel {
        return UserAddDialogViewModel(
            appNotifications = dependencies.appNotifications,
            userClient = komgaClientFactory.userClient()
        )
    }

    fun getUserEditDialogViewModel(user: KomgaUser): UserEditDialogViewModel {
        val libraries = requireNotNull(komgaSharedState.libraries.value)
        return UserEditDialogViewModel(
            dependencies.appNotifications,
            user,
            libraries,
            komgaClientFactory.userClient()
        )
    }

    fun getServerSettingsViewModel(): ServerSettingsViewModel {
        return ServerSettingsViewModel(
            appNotifications = dependencies.appNotifications,
            settingsClient = komgaClientFactory.settingsClient(),
            bookClient = komgaClientFactory.bookClient(),
            libraryClient = komgaClientFactory.libraryClient(),
            libraries = komgaSharedState.libraries,
            taskClient = komgaClientFactory.taskClient(),
            actuatorClient = komgaClientFactory.actuatorClient()
        )
    }

    fun getAnnouncementsViewModel(): AnnouncementsViewModel {
        return AnnouncementsViewModel(dependencies.appNotifications, komgaClientFactory.announcementClient())
    }

    fun getSettingsNavigationViewModel(rootNavigator: Navigator): SettingsNavigationViewModel {
        return SettingsNavigationViewModel(
            rootNavigator = rootNavigator,
            appNotifications = dependencies.appNotifications,
            userClient = komgaClientFactory.userClient(),
            komgaSharedState = komgaSharedState,
            secretsRepository = secretsRepository,
            currentServerUrl = settingsRepository.getServerUrl(),
            bookClient = komgaClientFactory.bookClient(),
            latestVersion = settingsRepository.getLastCheckedReleaseVersion(),
            komfEnabled = settingsRepository.getKomfEnabled(),
            platformType = platformType,
            updatesEnabled = dependencies.appUpdater != null
        )
    }

    fun getAppearanceViewModel(): AppSettingsViewModel {
        return AppSettingsViewModel(settingsRepository)
    }

    fun getSettingsUpdatesViewModel(): AppUpdatesViewModel {
        return AppUpdatesViewModel(
            releases = releases,
            updater = dependencies.appUpdater,
            settings = settingsRepository,
            notifications = dependencies.appNotifications,
        )
    }

    fun getCollectionViewModel(collectionId: KomgaCollectionId): CollectionViewModel {
        return CollectionViewModel(
            collectionId = collectionId,
            collectionClient = komgaClientFactory.collectionClient(),
            notifications = dependencies.appNotifications,
            seriesClient = komgaClientFactory.seriesClient(),
            komgaEvents = komgaEventSource.events,
            cardWidthFlow = getGridCardWidth()
        )
    }

    fun getReadListViewModel(readListId: KomgaReadListId): ReadListViewModel {
        return ReadListViewModel(
            readListId = readListId,
            readListClient = komgaClientFactory.readListClient(),
            bookClient = komgaClientFactory.bookClient(),
            notifications = dependencies.appNotifications,
            komgaEvents = komgaEventSource.events,
            cardWidthFlow = getGridCardWidth()
        )
    }

    fun getMediaAnalysisViewModel(): MediaAnalysisViewModel {
        return MediaAnalysisViewModel(
            bookClient = komgaClientFactory.bookClient(),
            appNotifications = dependencies.appNotifications,
        )
    }

    fun getKomfSettingsViewModel(
        enableKavita: Boolean,
        integrationToggleEnabled: Boolean,
    ): KomfSettingsViewModel {
        return KomfSettingsViewModel(
            komfConfigClient = dependencies.komfClientFactory.configClient(),
            komgaMediaServerClient = dependencies.komfClientFactory.mediaServerClient(KOMGA),
            kavitaMediaServerClient = if (enableKavita) dependencies.komfClientFactory.mediaServerClient(KAVITA) else null,
            appNotifications = dependencies.appNotifications,
            settingsRepository = settingsRepository,
            integrationToggleEnabled = integrationToggleEnabled,
            komfSharedState = komfSharedState,
        )
    }

    fun getKomfNotificationViewModel(): KomfNotificationSettingsViewModel {
        return KomfNotificationSettingsViewModel(
            komfConfigClient = dependencies.komfClientFactory.configClient(),
            komfNotificationClient = dependencies.komfClientFactory.notificationClient(),
            appNotifications = dependencies.appNotifications,
            komfConfig = komfSharedState
        )
    }

    fun getKomfProcessingViewModel(serverType: MediaServer): KomfProcessingSettingsViewModel {
        return KomfProcessingSettingsViewModel(
            komfConfigClient = dependencies.komfClientFactory.configClient(),
            appNotifications = dependencies.appNotifications,
            serverType = serverType,
            komfSharedState = komfSharedState
        )
    }

    fun getKomfProvidersViewModel(): KomfProvidersSettingsViewModel {
        return KomfProvidersSettingsViewModel(
            komfConfigClient = dependencies.komfClientFactory.configClient(),
            appNotifications = dependencies.appNotifications,
            komfSharedState = komfSharedState
        )
    }

    fun getKomfJobsViewModel(): KomfJobsViewModel {
        return KomfJobsViewModel(
            jobClient = dependencies.komfClientFactory.jobClient(),
            seriesClient = dependencies.komgaClientFactory.seriesClient(),
            appNotifications = dependencies.appNotifications
        )
    }

    fun getKomfIdentifyDialogViewModel(
        series: KomgaSeries,
        onDismissRequest: () -> Unit
    ): KomfIdentifyDialogViewModel {
        return KomfIdentifyDialogViewModel(
            series = series,
            komfConfig = komfSharedState,
            komfMetadataClient = dependencies.komfClientFactory.metadataClient(KOMGA),
            komfJobClient = dependencies.komfClientFactory.jobClient(),
            appNotifications = dependencies.appNotifications,
            onDismiss = onDismissRequest,
        )
    }

    fun getKomfResetMetadataDialogViewModel(
        onDismissRequest: () -> Unit
    ): KomfResetMetadataDialogViewModel {
        return KomfResetMetadataDialogViewModel(
            komfMetadataClient = dependencies.komfClientFactory.metadataClient(KOMGA),
            appNotifications = dependencies.appNotifications,
            onDismiss = onDismissRequest,
        )
    }

    fun getKomfLibraryIdentifyViewModel(
        library: KomgaLibrary
    ): KomfLibraryIdentifyViewmodel {
        return KomfLibraryIdentifyViewmodel(
            library = library,
            komfMetadataClient = dependencies.komfClientFactory.metadataClient(KOMGA),
            appNotifications = dependencies.appNotifications,
        )
    }

    fun getEpubReaderViewModel(
        bookId: KomgaBookId,
        bookSiblingsContext: BookSiblingsContext,
        book: KomgaBook? = null,
        markReadProgress: Boolean = true
    ): EpubReaderViewModel {
        return EpubReaderViewModel(
            bookId = bookId,
            book = book,
            markReadProgress = markReadProgress,
            bookClient = komgaClientFactory.bookClient(),
            seriesClient = komgaClientFactory.seriesClient(),
            readListClient = komgaClientFactory.readListClient(),
            ktor = komgaClientFactory.ktor(),
            settingsRepository = dependencies.settingsRepository,
            epubSettingsRepository = dependencies.epubReaderSettingsRepository,
            fontsRepository = dependencies.fontsRepository,
            notifications = dependencies.appNotifications,
            windowState = dependencies.windowState,
            platformType = platformType,
            bookSiblingsContext = bookSiblingsContext,
        )
    }

    fun getEpubReaderSettingsViewModel(): EpubReaderSettingsViewModel {
        return EpubReaderSettingsViewModel(dependencies.epubReaderSettingsRepository)
    }

    fun getCurvesViewModel(
        bookId: KomgaBookId,
        pageNumber: Int,
    ): ColorCorrectionViewModel {
        return ColorCorrectionViewModel(
            bookColorCorrectionRepository = dependencies.bookColorCorrectionRepository,
            curvePresetRepository = dependencies.colorCurvesPresetsRepository,
            levelsPresetRepository = dependencies.colorLevelsPresetRepository,
            imageLoader = dependencies.bookImageLoader,
            appNotifications = dependencies.appNotifications,
            bookId = bookId,
            pageNumber = pageNumber,
        )
    }

    fun getSeriesBulkActions() = SeriesBulkActions(
        komgaClientFactory.seriesClient(),
        dependencies.appNotifications,
    )

    fun getCollectionBulkActions() = CollectionBulkActions(
        komgaClientFactory.collectionClient(),
        dependencies.appNotifications,
    )

    fun getBookBulkActions() = BookBulkActions(komgaClientFactory.bookClient(), dependencies.appNotifications)
    fun getReadListBulkActions() = ReadListBulkActions(
        komgaClientFactory.readListClient(),
        dependencies.appNotifications,
    )

    fun getImageReaderSettingsViewModel(): ImageReaderSettingsViewModel {
        return ImageReaderSettingsViewModel(
            settingsRepository = dependencies.imageReaderSettingsRepository,
            appNotifications = dependencies.appNotifications,
            onnxRuntimeInstaller = dependencies.onnxRuntimeInstaller,
            onnxRuntime = dependencies.onnxRuntime,
            mangaJaNaiDownloader = dependencies.mangaJaNaiDownloader,
            coilMemoryCache = dependencies.coilImageLoader.memoryCache,
            coilDiskCache = dependencies.coilImageLoader.diskCache,
            readerDiskCache = dependencies.bookImageLoader.diskCache,
        )
    }

    fun getKomgaEvents(): SharedFlow<KomgaEvent> = komgaEventSource.events

    fun getStartupUpdateChecker() = startupUpdateChecker

    fun getLibraries(): StateFlow<List<KomgaLibrary>> = komgaSharedState.libraries

    private fun getLibraryFlow(id: KomgaLibraryId?): Flow<KomgaLibrary?> {
        if (id == null) return flowOf(null)
        return komgaSharedState.libraries.map { libraries -> libraries.firstOrNull { it.id == id } }
    }

    private fun getGridCardWidth(): Flow<Dp> {
        return settingsRepository.getCardWidth().map { it.dp }
    }
}
