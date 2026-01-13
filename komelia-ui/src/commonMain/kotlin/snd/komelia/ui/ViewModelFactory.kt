package snd.komelia.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.book.BookViewModel
import snd.komelia.ui.collection.CollectionViewModel
import snd.komelia.ui.color.ColorCorrectionViewModel
import snd.komelia.ui.common.menus.bulk.BookBulkActions
import snd.komelia.ui.common.menus.bulk.CollectionBulkActions
import snd.komelia.ui.common.menus.bulk.ReadListBulkActions
import snd.komelia.ui.common.menus.bulk.SeriesBulkActions
import snd.komelia.ui.dialogs.book.edit.BookEditDialogViewModel
import snd.komelia.ui.dialogs.book.editbulk.BookBulkEditDialogViewModel
import snd.komelia.ui.dialogs.collectionadd.AddToCollectionDialogViewModel
import snd.komelia.ui.dialogs.collectionedit.CollectionEditDialogViewModel
import snd.komelia.ui.dialogs.filebrowser.FileBrowserDialogViewModel
import snd.komelia.ui.dialogs.komf.identify.KomfIdentifyDialogViewModel
import snd.komelia.ui.dialogs.komf.identify.KomfLibraryIdentifyViewmodel
import snd.komelia.ui.dialogs.komf.reset.KomfResetMetadataDialogViewModel
import snd.komelia.ui.dialogs.libraryedit.LibraryEditDialogViewModel
import snd.komelia.ui.dialogs.oneshot.OneshotEditDialogViewModel
import snd.komelia.ui.dialogs.readlistadd.AddToReadListDialogViewModel
import snd.komelia.ui.dialogs.readlistedit.ReadListEditDialogViewModel
import snd.komelia.ui.dialogs.series.edit.SeriesEditDialogViewModel
import snd.komelia.ui.dialogs.series.editbulk.SeriesBulkEditDialogViewModel
import snd.komelia.ui.dialogs.user.PasswordChangeDialogViewModel
import snd.komelia.ui.dialogs.user.UserAddDialogViewModel
import snd.komelia.ui.dialogs.user.UserEditDialogViewModel
import snd.komelia.ui.home.HomeFilterData
import snd.komelia.ui.home.HomeViewModel
import snd.komelia.ui.home.edit.FilterEditViewModel
import snd.komelia.ui.library.LibraryViewModel
import snd.komelia.ui.login.LoginViewModel
import snd.komelia.ui.login.offline.OfflineLoginViewModel
import snd.komelia.ui.oneshot.OneshotViewModel
import snd.komelia.ui.platform.PlatformType
import snd.komelia.ui.reader.epub.EpubReaderViewModel
import snd.komelia.ui.reader.image.ReaderViewModel
import snd.komelia.ui.readlist.ReadListViewModel
import snd.komelia.ui.search.SearchViewModel
import snd.komelia.ui.series.SeriesViewModel
import snd.komelia.ui.series.SeriesViewModel.SeriesTab
import snd.komelia.ui.settings.account.AccountSettingsViewModel
import snd.komelia.ui.settings.analysis.MediaAnalysisViewModel
import snd.komelia.ui.settings.announcements.AnnouncementsViewModel
import snd.komelia.ui.settings.appearance.AppSettingsViewModel
import snd.komelia.ui.settings.authactivity.AuthenticationActivityViewModel
import snd.komelia.ui.settings.epub.EpubReaderSettingsViewModel
import snd.komelia.ui.settings.imagereader.ImageReaderSettingsViewModel
import snd.komelia.ui.settings.komf.KomfSharedState
import snd.komelia.ui.settings.komf.general.KomfSettingsViewModel
import snd.komelia.ui.settings.komf.jobs.KomfJobsViewModel
import snd.komelia.ui.settings.komf.notifications.KomfNotificationSettingsViewModel
import snd.komelia.ui.settings.komf.processing.KomfProcessingSettingsViewModel
import snd.komelia.ui.settings.komf.providers.KomfProvidersSettingsViewModel
import snd.komelia.ui.settings.navigation.SettingsNavigationViewModel
import snd.komelia.ui.settings.offline.OfflineSettingsViewModel
import snd.komelia.ui.settings.server.ServerSettingsViewModel
import snd.komelia.ui.settings.updates.AppUpdatesViewModel
import snd.komelia.ui.settings.users.UsersViewModel
import snd.komelia.ui.topbar.NotificationsState
import snd.komelia.ui.topbar.SearchBarState
import snd.komelia.updates.AppRelease
import snd.komelia.updates.StartupUpdateChecker
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId
import snd.komf.api.MediaServer
import snd.komf.api.MediaServer.KAVITA
import snd.komf.api.MediaServer.KOMGA
import snd.komga.client.book.KomgaBookId
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.user.KomgaUser

class ViewModelFactory(
    private val dependencies: DependencyContainer,
    private val platformType: PlatformType,
) {
    private val appRepositories = dependencies.appRepositories
    private val komgaApi
        get() = dependencies.komgaApi.value

    private val releases = MutableStateFlow<List<AppRelease>>(emptyList())
    private val imageReaderCurrentBook = MutableStateFlow<KomgaBookId?>(null)
        .also { dependencies.colorCorrectionStep.setBookFlow(it) }

    private val komfSharedState = KomfSharedState(
        komfConfigClient = dependencies.komfClientFactory.configClient(),
        komgaServerClient = dependencies.komfClientFactory.mediaServerClient(KOMGA),
        kavitaServerClient = dependencies.komfClientFactory.mediaServerClient(KAVITA),
        notifications = dependencies.appNotifications,
    )

    private val startupUpdateChecker = dependencies.appUpdater?.let { updater ->
        StartupUpdateChecker(
            updater,
            appRepositories.settingsRepository,
            releases
        )
    }
    val screenReloadEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = DROP_OLDEST)

    fun getLibraryViewModel(
        libraryId: KomgaLibraryId?,
    ): LibraryViewModel {
        return LibraryViewModel(
            libraryApi = komgaApi.libraryApi,
            collectionApi = komgaApi.collectionsApi,
            readListsApi = komgaApi.readListApi,
            seriesApi = komgaApi.seriesApi,
            referentialApi = komgaApi.referentialApi,

            appNotifications = dependencies.appNotifications,
            komgaEvents = dependencies.komgaEvents.events,
            libraryFlow = getLibraryFlow(libraryId),
            settingsRepository = appRepositories.settingsRepository,
            taskEmitter = dependencies.offlineDependencies.taskEmitter,
        )
    }

    fun getHomeViewModel(): HomeViewModel {
        return HomeViewModel(
            seriesApi = komgaApi.seriesApi,
            bookApi = komgaApi.bookApi,
            appNotifications = dependencies.appNotifications,
            komgaEvents = dependencies.komgaEvents.events,
            filterRepository = appRepositories.homeScreenFilterRepository,
            taskEmitter = dependencies.offlineDependencies.taskEmitter,
            cardWidthFlow = getGridCardWidth(),
        )
    }

    fun getFilterEditViewModel(homeFilters: List<HomeFilterData>?): FilterEditViewModel {
        return FilterEditViewModel(
            homeFilters = homeFilters,
            appNotifications = dependencies.appNotifications,
            seriesApi = komgaApi.seriesApi,
            bookApi = komgaApi.bookApi,
            readListApi = komgaApi.readListApi,
            collectionApi = komgaApi.collectionsApi,
            referentialApi = komgaApi.referentialApi,
            filterRepository = appRepositories.homeScreenFilterRepository,
            libraries = getLibraries(),
            cardWidthFlow = getGridCardWidth(),
        )
    }

    fun getNavigationViewModel(): MainScreenViewModel {
        return MainScreenViewModel(
            libraryApi = komgaApi.libraryApi,
            appNotifications = dependencies.appNotifications,
            komgaEvents = dependencies.komgaEvents.events,
            screenReloadFlow = screenReloadEvents,
            searchBarState = SearchBarState(
                seriesApi = komgaApi.seriesApi,
                bookApi = komgaApi.bookApi,
                appNotifications = dependencies.appNotifications,
                libraries = dependencies.komgaSharedState.libraries
            ),
            notificationsState = NotificationsState(
                komgaEvents = dependencies.komgaEvents.events,
                bookDownloadEvents = dependencies.offlineDependencies.bookDownloadEvents
            ),
            libraries = dependencies.komgaSharedState.libraries,
            offlineSettingsRepository = dependencies.offlineDependencies.repositories.offlineSettingsRepository,
            taskEmitter = dependencies.offlineDependencies.taskEmitter,
        )
    }

    fun getSeriesViewModel(
        seriesId: KomgaSeriesId,
        series: KomgaSeries? = null,
        defaultTab: SeriesTab? = null,
    ) = SeriesViewModel(
        seriesId = seriesId,
        series = series,
        libraries = dependencies.komgaSharedState.libraries,
        seriesApi = komgaApi.seriesApi,
        taskEmitter = dependencies.offlineDependencies.taskEmitter,
        bookApi = komgaApi.bookApi,
        collectionApi = komgaApi.collectionsApi,
        notifications = dependencies.appNotifications,
        events = dependencies.komgaEvents.events,
        settingsRepository = appRepositories.settingsRepository,
        referentialApi = komgaApi.referentialApi,
        defaultTab = defaultTab ?: SeriesTab.BOOKS,
    )

    fun getBookViewModel(bookId: KomgaBookId, book: KomeliaBook?): BookViewModel {
        return BookViewModel(
            book = book,
            bookId = bookId,
            bookApi = komgaApi.bookApi,
            notifications = dependencies.appNotifications,
            komgaEvents = dependencies.komgaEvents.events,
            libraries = dependencies.komgaSharedState.libraries,
            settingsRepository = appRepositories.settingsRepository,
            readListApi = komgaApi.readListApi,
            taskEmitter = dependencies.offlineDependencies.taskEmitter,
        )
    }

    fun getOneshotViewModel(
        seriesId: KomgaSeriesId,
        series: KomgaSeries? = null,
        book: KomeliaBook? = null,
    ) = OneshotViewModel(
        series = series,
        book = book,
        seriesId = seriesId,
        seriesApi = komgaApi.seriesApi,
        bookApi = komgaApi.bookApi,
        events = dependencies.komgaEvents.events,
        notifications = dependencies.appNotifications,
        libraries = dependencies.komgaSharedState.libraries,
        taskEmitter = dependencies.offlineDependencies.taskEmitter,
        settingsRepository = appRepositories.settingsRepository,
        readListApi = komgaApi.readListApi,
        collectionApi = komgaApi.collectionsApi,
    )

    fun getBookReaderViewModel(
        navigator: Navigator,
        markReadProgress: Boolean,
        bookSiblingsContext: BookSiblingsContext
    ): ReaderViewModel {
        return ReaderViewModel(
            bookApi = komgaApi.bookApi,
            seriesApi = komgaApi.seriesApi,
            readListApi = komgaApi.readListApi,
            navigator = navigator,
            appNotifications = dependencies.appNotifications,
            readerSettingsRepository = appRepositories.imageReaderSettingsRepository,
            imageLoader = dependencies.bookImageLoader,
            appStrings = dependencies.appStrings,
            readerImageFactory = dependencies.readerImageFactory,
            currentBookId = imageReaderCurrentBook,
            colorCorrectionRepository = appRepositories.bookColorCorrectionRepository,
            colorCorrectionIsActive = dependencies.colorCorrectionStep.isActive,
            onnxRuntime = dependencies.onnxRuntime,
            panelDetector = dependencies.panelDetector,
            upscaler = dependencies.upscaler,
            bookSiblingsContext = bookSiblingsContext,
            markReadProgress = markReadProgress,
        )
    }

    fun getLoginViewModel(): LoginViewModel {
        return LoginViewModel(
            isOffline = dependencies.isOffline,
            settingsRepository = appRepositories.settingsRepository,
            secretsRepository = appRepositories.secretsRepository,
            komgaUserApi = komgaApi.userApi,
            komgaLibraryApi = komgaApi.libraryApi,
            komgaAuthState = dependencies.komgaSharedState,
            notifications = dependencies.appNotifications,
            offlineUserRepository = dependencies.offlineDependencies.repositories.userRepository,
            platform = platformType,
        )
    }

    fun getLibraryEditDialogViewModel(library: KomgaLibrary?, onDismissRequest: () -> Unit) =
        LibraryEditDialogViewModel(
            library = library,
            onDialogDismiss = onDismissRequest,
            libraryApi = komgaApi.libraryApi,
            appNotifications = dependencies.appNotifications,
        )

    fun getSeriesEditDialogViewModel(series: KomgaSeries, onDismissRequest: () -> Unit) =
        SeriesEditDialogViewModel(
            series = series,
            onDialogDismiss = onDismissRequest,
            seriesApi = komgaApi.seriesApi,
            referentialApi = komgaApi.referentialApi,
            notifications = dependencies.appNotifications,
            cardWidth = getGridCardWidth(),
        )

    fun getSeriesBulkEditDialogViewModel(series: List<KomgaSeries>, onDismissRequest: () -> Unit) =
        SeriesBulkEditDialogViewModel(
            series = series,
            onDialogDismiss = onDismissRequest,
            seriesApi = komgaApi.seriesApi,
            referentialApi = komgaApi.referentialApi,
            notifications = dependencies.appNotifications,
        )

    fun getBookEditDialogViewModel(book: KomeliaBook, onDismissRequest: () -> Unit) =
        BookEditDialogViewModel(
            book = book,
            onDialogDismiss = onDismissRequest,
            bookApi = komgaApi.bookApi,
            referentialApi = komgaApi.referentialApi,
            notifications = dependencies.appNotifications,
            cardWidth = getGridCardWidth(),
        )

    fun getOneshotEditDialogViewModel(
        seriesId: KomgaSeriesId,
        series: KomgaSeries?,
        book: KomeliaBook?,
        onDismissRequest: () -> Unit
    ) = OneshotEditDialogViewModel(
        seriesId = seriesId,
        series = series,
        book = book,
        onDialogDismiss = onDismissRequest,
        bookApi = komgaApi.bookApi,
        seriesApi = komgaApi.seriesApi,
        referentialApi = komgaApi.referentialApi,
        notifications = dependencies.appNotifications,
        cardWidth = getGridCardWidth(),
    )

    fun getBookBulkEditDialogViewModel(books: List<KomeliaBook>, onDismissRequest: () -> Unit) =
        BookBulkEditDialogViewModel(
            books = books,
            onDialogDismiss = onDismissRequest,
            bookApi = komgaApi.bookApi,
            referentialApi = komgaApi.referentialApi,
            notifications = dependencies.appNotifications,
        )

    fun getCollectionEditDialogViewModel(
        collection: KomgaCollection,
        onDismissRequest: () -> Unit
    ) = CollectionEditDialogViewModel(
        collection = collection,
        onDialogDismiss = onDismissRequest,
        collectionApi = komgaApi.collectionsApi,
        notifications = dependencies.appNotifications,
        cardWidth = getGridCardWidth(),
    )

    fun getReadListEditDialogViewModel(readList: KomgaReadList, onDismissRequest: () -> Unit) =
        ReadListEditDialogViewModel(
            readList = readList,
            onDialogDismiss = onDismissRequest,
            readListApi = komgaApi.readListApi,
            notifications = dependencies.appNotifications,
            cardWidth = getGridCardWidth(),
        )

    fun getAddToCollectionDialogViewModel(series: List<KomgaSeries>, onDismissRequest: () -> Unit) =
        AddToCollectionDialogViewModel(
            series = series,
            onDismissRequest = onDismissRequest,
            collectionApi = komgaApi.collectionsApi,
            appNotifications = dependencies.appNotifications,
        )

    fun getAddToReadListDialogViewModel(books: List<KomeliaBook>, onDismissRequest: () -> Unit) =
        AddToReadListDialogViewModel(
            books = books,
            onDismissRequest = onDismissRequest,
            readListApi = komgaApi.readListApi,
            appNotifications = dependencies.appNotifications,
        )

    fun getFileBrowserDialogViewModel() =
        FileBrowserDialogViewModel(komgaApi.fileSystemApi, dependencies.appNotifications)


    fun getSearchViewModel() = SearchViewModel(
        seriesApi = komgaApi.seriesApi,
        bookApi = komgaApi.bookApi,
        appNotifications = dependencies.appNotifications,
        libraries = dependencies.komgaSharedState.libraries,
    )


    fun getAccountViewModel(): AccountSettingsViewModel {
        val user = requireNotNull(dependencies.komgaSharedState.authenticatedUser.value)
        return AccountSettingsViewModel(user)
    }

    fun getAuthenticationActivityViewModel(forMe: Boolean): AuthenticationActivityViewModel {
        return AuthenticationActivityViewModel(
            forMe,
            komgaApi.userApi,
            dependencies.appNotifications
        )
    }

    fun getUsersViewModel(): UsersViewModel {
        val user = requireNotNull(dependencies.komgaSharedState.authenticatedUser.value)
        return UsersViewModel(dependencies.appNotifications, komgaApi.userApi, user)
    }

    fun getPasswordChangeDialogViewModel(user: KomgaUser?) = PasswordChangeDialogViewModel(
        dependencies.appNotifications,
        komgaApi.userApi,
        user
    )

    fun getUserAddDialogViewModel(): UserAddDialogViewModel {
        return UserAddDialogViewModel(
            appNotifications = dependencies.appNotifications,
            userApi = komgaApi.userApi
        )
    }

    fun getUserEditDialogViewModel(user: KomgaUser): UserEditDialogViewModel {
        val libraries = requireNotNull(dependencies.komgaSharedState.libraries.value)
        return UserEditDialogViewModel(
            dependencies.appNotifications,
            user,
            libraries,
            komgaApi.userApi
        )
    }

    fun getServerSettingsViewModel(): ServerSettingsViewModel {
        return ServerSettingsViewModel(
            appNotifications = dependencies.appNotifications,
            settingsApi = komgaApi.settingsApi,
            bookApi = komgaApi.bookApi,
            libraryApi = komgaApi.libraryApi,
            libraries = dependencies.komgaSharedState.libraries,
            taskApi = komgaApi.tasksApi,
            actuatorApi = komgaApi.actuatorApi
        )
    }

    fun getAnnouncementsViewModel(): AnnouncementsViewModel {
        return AnnouncementsViewModel(dependencies.appNotifications, komgaApi.announcementsApi)
    }

    fun getSettingsNavigationViewModel(rootNavigator: Navigator): SettingsNavigationViewModel {
        return SettingsNavigationViewModel(
            rootNavigator = rootNavigator,
            appNotifications = dependencies.appNotifications,
            userApi = komgaApi.userApi,
            komgaSharedState = dependencies.komgaSharedState,
            secretsRepository = appRepositories.secretsRepository,
            offlineSettingsRepository = dependencies.offlineDependencies.repositories.offlineSettingsRepository,
            isOffline = dependencies.isOffline,
            currentServerUrl = appRepositories.settingsRepository.getServerUrl(),
            bookApi = komgaApi.bookApi,
            latestVersion = appRepositories.settingsRepository.getLastCheckedReleaseVersion(),
            komfEnabled = appRepositories.komfSettingsRepository.getKomfEnabled(),
            platformType = platformType,
            updatesEnabled = dependencies.appUpdater != null,
            user = dependencies.komgaSharedState.authenticatedUser,
        )
    }

    fun getAppearanceViewModel(): AppSettingsViewModel {
        return AppSettingsViewModel(appRepositories.settingsRepository)
    }

    fun getSettingsUpdatesViewModel(): AppUpdatesViewModel {
        return AppUpdatesViewModel(
            releases = releases,
            updater = dependencies.appUpdater,
            settings = appRepositories.settingsRepository,
            notifications = dependencies.appNotifications,
        )
    }

    fun getCollectionViewModel(collectionId: KomgaCollectionId): CollectionViewModel {
        return CollectionViewModel(
            collectionId = collectionId,
            collectionApi = komgaApi.collectionsApi,
            notifications = dependencies.appNotifications,
            seriesApi = komgaApi.seriesApi,
            komgaEvents = dependencies.komgaEvents.events,
            cardWidthFlow = getGridCardWidth(),
            taskEmitter = dependencies.offlineDependencies.taskEmitter
        )
    }

    fun getReadListViewModel(readListId: KomgaReadListId): ReadListViewModel {
        return ReadListViewModel(
            readListId = readListId,
            readListApi = komgaApi.readListApi,
            bookApi = komgaApi.bookApi,
            taskEmitter = dependencies.offlineDependencies.taskEmitter,
            notifications = dependencies.appNotifications,
            komgaEvents = dependencies.komgaEvents.events,
            cardWidthFlow = getGridCardWidth()
        )
    }

    fun getMediaAnalysisViewModel(): MediaAnalysisViewModel {
        return MediaAnalysisViewModel(
            bookApi = komgaApi.bookApi,
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
            settingsRepository = appRepositories.komfSettingsRepository,
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
            seriesApi = komgaApi.seriesApi,
            appNotifications = dependencies.appNotifications
        )
    }

    fun getKomfIdentifyDialogViewModel(
        series: KomgaSeries,
        onDismissRequest: () -> Unit
    ): KomfIdentifyDialogViewModel {
        return KomfIdentifyDialogViewModel(
            seriesId = KomfServerSeriesId(series.id.value),
            libraryId = KomfServerLibraryId(series.libraryId.value),
            seriesName = series.metadata.title,
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
            libraryId = KomfServerLibraryId(library.id.value),
            komfMetadataClient = dependencies.komfClientFactory.metadataClient(KOMGA),
            appNotifications = dependencies.appNotifications,
        )
    }

    fun getEpubReaderViewModel(
        bookId: KomgaBookId,
        bookSiblingsContext: BookSiblingsContext,
        book: KomeliaBook? = null,
        markReadProgress: Boolean = true
    ): EpubReaderViewModel {
        return EpubReaderViewModel(
            bookId = bookId,
            book = book,
            markReadProgress = markReadProgress,
            bookApi = komgaApi.bookApi,
            seriesApi = komgaApi.seriesApi,
            readListApi = komgaApi.readListApi,
//            ktor = TODO(),
            settingsRepository = appRepositories.settingsRepository,
            epubSettingsRepository = appRepositories.epubReaderSettingsRepository,
            fontsRepository = appRepositories.fontsRepository,
            notifications = dependencies.appNotifications,
            windowState = dependencies.windowState,
            platformType = platformType,
            bookSiblingsContext = bookSiblingsContext,
        )
    }

    fun getEpubReaderSettingsViewModel(): EpubReaderSettingsViewModel {
        return EpubReaderSettingsViewModel(appRepositories.epubReaderSettingsRepository)
    }

    fun getCurvesViewModel(
        bookId: KomgaBookId,
        pageNumber: Int,
    ): ColorCorrectionViewModel {
        return ColorCorrectionViewModel(
            bookColorCorrectionRepository = appRepositories.bookColorCorrectionRepository,
            curvePresetRepository = appRepositories.colorCurvesPresetsRepository,
            levelsPresetRepository = appRepositories.colorLevelsPresetRepository,
            imageLoader = dependencies.bookImageLoader,
            appNotifications = dependencies.appNotifications,
            bookId = bookId,
            pageNumber = pageNumber,
        )
    }

    fun getSeriesBulkActions() = SeriesBulkActions(
        komgaApi.seriesApi,
        dependencies.offlineDependencies.taskEmitter,
        dependencies.appNotifications,
    )

    fun getCollectionBulkActions() = CollectionBulkActions(
        komgaApi.collectionsApi,
        dependencies.appNotifications,
    )

    fun getBookBulkActions() = BookBulkActions(
        bookApi = komgaApi.bookApi,
        taskEmitter = dependencies.offlineDependencies.taskEmitter,
        notifications = dependencies.appNotifications
    )

    fun getReadListBulkActions() = ReadListBulkActions(
        komgaApi.readListApi,
        dependencies.appNotifications,
    )

    fun getImageReaderSettingsViewModel(): ImageReaderSettingsViewModel {
        return ImageReaderSettingsViewModel(
            settingsRepository = appRepositories.imageReaderSettingsRepository,
            appNotifications = dependencies.appNotifications,

            onnxRuntime = dependencies.onnxRuntime,
            upscaler = dependencies.upscaler,
            panelDetector = dependencies.panelDetector,
            onnxRuntimeInstaller = dependencies.onnxRuntimeInstaller,
            onnxModelDownloader = dependencies.onnxModelDownloader,

            coilMemoryCache = dependencies.coilImageLoader.memoryCache,
            coilDiskCache = dependencies.coilImageLoader.diskCache,
            readerDiskCache = dependencies.bookImageLoader.diskCache,
        )
    }

    fun getOfflineModeSettingsViewModel(): OfflineSettingsViewModel {
        return OfflineSettingsViewModel(
            authState = dependencies.komgaSharedState,
            appNotifications = dependencies.appNotifications,
            offlineSettingsRepository = dependencies.offlineDependencies.repositories.offlineSettingsRepository,
            userRepository = dependencies.offlineDependencies.repositories.userRepository,
            serverRepository = dependencies.offlineDependencies.repositories.mediaServerRepository,
            logJournalRepository = dependencies.offlineDependencies.repositories.logJournalRepository,
            serverDeleteAction = dependencies.offlineDependencies.actions.get(),
            userDeleteAction = dependencies.offlineDependencies.actions.get(),
            platformContext = dependencies.coilContext,

            taskEmitter = dependencies.offlineDependencies.taskEmitter,
            downloadEvents = dependencies.offlineDependencies.bookDownloadEvents
        )
    }

    fun getOfflineLoginViewModel(): OfflineLoginViewModel {
        return OfflineLoginViewModel(
            appNotifications = dependencies.appNotifications,
            offlineSettingsRepository = dependencies.offlineDependencies.repositories.offlineSettingsRepository,
            userRepository = dependencies.offlineDependencies.repositories.userRepository,
            serverRepository = dependencies.offlineDependencies.repositories.mediaServerRepository,
            komgaAuthState = dependencies.komgaSharedState,
            offlineLibraryApi = dependencies.offlineDependencies.komgaApi.libraryApi,
            serverDeleteAction = dependencies.offlineDependencies.actions.get(),
            userDeleteAction = dependencies.offlineDependencies.actions.get(),
        )
    }

    fun getStartupUpdateChecker() = startupUpdateChecker

    fun getLibraries(): StateFlow<List<KomgaLibrary>> = dependencies.komgaSharedState.libraries

    private fun getLibraryFlow(id: KomgaLibraryId?): Flow<KomgaLibrary?> {
        if (id == null) return flowOf(null)
        return dependencies.komgaSharedState.libraries.map { libraries -> libraries.firstOrNull { it.id == id } }
    }

    private fun getGridCardWidth(): Flow<Dp> {
        return appRepositories.settingsRepository.getCardWidth().map { it.dp }
    }
}
