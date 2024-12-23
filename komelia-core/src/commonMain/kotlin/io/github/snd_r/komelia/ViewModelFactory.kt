package io.github.snd_r.komelia

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import io.github.snd_r.komelia.curves.CurvesViewModel
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.settings.SecretsRepository
import io.github.snd_r.komelia.ui.MainScreenViewModel
import io.github.snd_r.komelia.ui.book.BookViewModel
import io.github.snd_r.komelia.ui.collection.CollectionViewModel
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
import io.github.snd_r.komelia.ui.library.LibraryCollectionsViewModel
import io.github.snd_r.komelia.ui.library.LibraryReadListsViewModel
import io.github.snd_r.komelia.ui.library.LibraryViewModel
import io.github.snd_r.komelia.ui.login.LoginViewModel
import io.github.snd_r.komelia.ui.navigation.SearchBarState
import io.github.snd_r.komelia.ui.oneshot.OneshotViewModel
import io.github.snd_r.komelia.ui.reader.epub.EpubReaderViewModel
import io.github.snd_r.komelia.ui.reader.image.ReaderViewModel
import io.github.snd_r.komelia.ui.readlist.ReadListViewModel
import io.github.snd_r.komelia.ui.search.SearchViewModel
import io.github.snd_r.komelia.ui.series.SeriesViewModel
import io.github.snd_r.komelia.ui.series.list.SeriesListViewModel
import io.github.snd_r.komelia.ui.series.list.SeriesListViewModel.SeriesSort
import io.github.snd_r.komelia.ui.settings.account.AccountSettingsViewModel
import io.github.snd_r.komelia.ui.settings.analysis.MediaAnalysisViewModel
import io.github.snd_r.komelia.ui.settings.announcements.AnnouncementsViewModel
import io.github.snd_r.komelia.ui.settings.appearance.AppSettingsViewModel
import io.github.snd_r.komelia.ui.settings.authactivity.AuthenticationActivityViewModel
import io.github.snd_r.komelia.ui.settings.epub.EpubReaderSettingsViewModel
import io.github.snd_r.komelia.ui.settings.komf.KomfConfigState
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import snd.komf.api.MediaServer
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

    private val authenticatedUser = MutableStateFlow<KomgaUser?>(null)
    private val libraries = MutableStateFlow<List<KomgaLibrary>>(emptyList())
    private val releases = MutableStateFlow<List<AppRelease>>(emptyList())

    private val komfConfigState = KomfConfigState(
        dependencies.komfClientFactory.configClient(),
        dependencies.appNotifications,
    )

    private val komgaEventSource = ManagedKomgaEvents(
        authenticatedUser = authenticatedUser,
        eventSourceFactory = komgaClientFactory::sseSession,
        memoryCache = dependencies.imageLoader.memoryCache,
        diskCache = dependencies.imageLoader.diskCache,
        libraryClient = komgaClientFactory.libraryClient(),
        librariesFlow = libraries
    )

    private val startupUpdateChecker = dependencies.appUpdater?.let { updater ->
        StartupUpdateChecker(
            updater,
            settingsRepository,
            releases
        )
    }

    fun getLibraryViewModel(
        libraryId: KomgaLibraryId?,
    ): LibraryViewModel {
        return LibraryViewModel(
            libraryFlow = libraryId?.let { getLibraryFlow(it) },
            libraryClient = komgaClientFactory.libraryClient(),
            collectionClient = komgaClientFactory.collectionClient(),
            readListsClient = komgaClientFactory.readListClient(),
            appNotifications = dependencies.appNotifications,
            komgaEvents = komgaEventSource.events,
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
            searchBarState = SearchBarState(
                seriesClient = komgaClientFactory.seriesClient(),
                bookClient = komgaClientFactory.bookClient(),
                appNotifications = dependencies.appNotifications,
                libraries = libraries
            ),
            libraries = libraries,
        )
    }

    fun getSeriesViewModel(
        seriesId: KomgaSeriesId,
        series: KomgaSeries? = null,
        defaultTab: SeriesViewModel.SeriesTab,
    ) = SeriesViewModel(
        seriesId = seriesId,
        series = series,
        seriesClient = komgaClientFactory.seriesClient(),
        bookClient = komgaClientFactory.bookClient(),
        collectionClient = komgaClientFactory.collectionClient(),
        notifications = dependencies.appNotifications,
        events = komgaEventSource.events,
        settingsRepository = settingsRepository,
        referentialClient = komgaClientFactory.referentialClient(),
        defaultTab = defaultTab,
    )

    fun getBookViewModel(bookId: KomgaBookId, book: KomgaBook?): BookViewModel {
        return BookViewModel(
            book = book,
            bookId = bookId,
            bookClient = komgaClientFactory.bookClient(),
            notifications = dependencies.appNotifications,
            komgaEvents = komgaEventSource.events,
            libraries = libraries,
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
        libraries = libraries,
        settingsRepository = settingsRepository,
        readListClient = komgaClientFactory.readListClient(),
        collectionClient = komgaClientFactory.collectionClient(),
    )

    fun getSeriesBrowseViewModel(
        libraryId: KomgaLibraryId?,
        sort: SeriesSort = SeriesSort.TITLE_ASC,
    ): SeriesListViewModel {
        return SeriesListViewModel(
            seriesClient = komgaClientFactory.seriesClient(),
            referentialClient = komgaClientFactory.referentialClient(),
            notifications = dependencies.appNotifications,
            komgaEvents = komgaEventSource.events,
            settingsRepository = settingsRepository,
            libraryFlow = getLibraryFlow(libraryId),
            cardWidthFlow = getGridCardWidth(),
            defaultSort = sort
        )
    }

    fun getBookReaderViewModel(navigator: Navigator, markReadProgress: Boolean): ReaderViewModel {
        return ReaderViewModel(
            bookClient = komgaClientFactory.bookClient(),
            navigator = navigator,
            appNotifications = dependencies.appNotifications,
            settingsRepository = settingsRepository,
            readerSettingsRepository = readerSettingsRepository,
            imageLoader = dependencies.readerImageLoader,
            decoderDescriptor = dependencies.imageDecoderDescriptor,
            appStrings = dependencies.appStrings,
            markReadProgress = markReadProgress,
        )
    }

    fun getLoginViewModel(): LoginViewModel {
        return LoginViewModel(
            settingsRepository = settingsRepository,
            secretsRepository = secretsRepository,
            komgaUserClient = komgaClientFactory.userClient(),
            komgaLibraryClient = komgaClientFactory.libraryClient(),
            authenticatedUserFlow = authenticatedUser,
            availableLibrariesFlow = libraries,
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
        libraries = libraries,
    )


    fun getAccountViewModel(): AccountSettingsViewModel {
        val user = requireNotNull(authenticatedUser.value)
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
        val user = requireNotNull(authenticatedUser.value)
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
        val libraries = requireNotNull(libraries.value)
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
            libraries = libraries,
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
            authenticatedUser = authenticatedUser,
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

    fun getLibraryCollectionsViewModel(libraryId: KomgaLibraryId?): LibraryCollectionsViewModel {
        return LibraryCollectionsViewModel(
            collectionClient = komgaClientFactory.collectionClient(),
            appNotifications = dependencies.appNotifications,
            events = komgaEventSource.events,
            libraryFlow = libraryId?.let { getLibraryFlow(it) },
            cardWidthFlow = getGridCardWidth(),
        )
    }

    fun getLibraryReadListsViewModel(libraryId: KomgaLibraryId?): LibraryReadListsViewModel {
        return LibraryReadListsViewModel(
            readListClient = komgaClientFactory.readListClient(),
            appNotifications = dependencies.appNotifications,
            komgaEvents = komgaEventSource.events,
            libraryFlow = libraryId?.let { getLibraryFlow(it) },
            cardWidthFlow = getGridCardWidth(),
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

    fun getKomfSettingsViewModel(): KomfSettingsViewModel {
        return KomfSettingsViewModel(
            komfConfigClient = dependencies.komfClientFactory.configClient(),
            appNotifications = dependencies.appNotifications,
            settingsRepository = settingsRepository,
            komfConfig = komfConfigState,
            libraries = libraries,
        )
    }

    fun getKomfNotificationViewModel(): KomfNotificationSettingsViewModel {
        return KomfNotificationSettingsViewModel(
            komfConfigClient = dependencies.komfClientFactory.configClient(),
            komfNotificationClient = dependencies.komfClientFactory.notificationClient(),
            appNotifications = dependencies.appNotifications,
            komfConfig = komfConfigState
        )
    }

    fun getKomfProcessingViewModel(): KomfProcessingSettingsViewModel {
        return KomfProcessingSettingsViewModel(
            komfConfigClient = dependencies.komfClientFactory.configClient(),
            libraries = libraries,
            appNotifications = dependencies.appNotifications,
            komfConfig = komfConfigState
        )
    }

    fun getKomfProvidersViewModel(): KomfProvidersSettingsViewModel {
        return KomfProvidersSettingsViewModel(
            komfConfigClient = dependencies.komfClientFactory.configClient(),
            libraries = libraries,
            appNotifications = dependencies.appNotifications,
            komfConfig = komfConfigState
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
            komfConfig = komfConfigState,
            komfMetadataClient = dependencies.komfClientFactory.metadataClient(MediaServer.KOMGA),
            komfJobClient = dependencies.komfClientFactory.jobClient(),
            appNotifications = dependencies.appNotifications,
            onDismiss = onDismissRequest,
        )
    }

    fun getKomfResetMetadataDialogViewModel(
        onDismissRequest: () -> Unit
    ): KomfResetMetadataDialogViewModel {
        return KomfResetMetadataDialogViewModel(
            komfMetadataClient = dependencies.komfClientFactory.metadataClient(MediaServer.KOMGA),
            appNotifications = dependencies.appNotifications,
            onDismiss = onDismissRequest,
        )
    }

    fun getKomfLibraryIdentifyViewModel(
        library: KomgaLibrary
    ): KomfLibraryIdentifyViewmodel {
        return KomfLibraryIdentifyViewmodel(
            library = library,
            komfMetadataClient = dependencies.komfClientFactory.metadataClient(MediaServer.KOMGA),
            appNotifications = dependencies.appNotifications,
        )
    }

    fun getEpubReaderViewModel(
        bookId: KomgaBookId,
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
        )
    }

    fun getEpubReaderSettingsViewModel(): EpubReaderSettingsViewModel {
        return EpubReaderSettingsViewModel(dependencies.epubReaderSettingsRepository)
    }

    fun getCurvesViewModel(): CurvesViewModel {
        return CurvesViewModel()
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

    fun getKomgaEvents(): SharedFlow<KomgaEvent> = komgaEventSource.events

    fun getStartupUpdateChecker() = startupUpdateChecker

    private fun getLibraryFlow(id: KomgaLibraryId?): Flow<KomgaLibrary?> {
        if (id == null) return flowOf(null)
        return libraries.map { libraries -> libraries.firstOrNull { it.id == id } }
    }

    private fun getGridCardWidth(): Flow<Dp> {
        return settingsRepository.getCardWidth().map { it.dp }
    }
}
