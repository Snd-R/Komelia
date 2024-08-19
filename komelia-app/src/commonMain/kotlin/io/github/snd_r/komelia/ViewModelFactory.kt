package io.github.snd_r.komelia

import cafe.adriel.lyricist.Lyricist
import cafe.adriel.voyager.navigator.Navigator
import coil3.ImageLoader
import coil3.PlatformContext
import io.github.snd_r.komelia.image.ReaderImageLoader
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.settings.ReaderSettingsRepository
import io.github.snd_r.komelia.settings.SecretsRepository
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.strings.EnStrings
import io.github.snd_r.komelia.strings.Locales
import io.github.snd_r.komelia.strings.Strings
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
import io.github.snd_r.komelia.ui.dialogs.komf.reset.KomfResetMetadataDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.libraryedit.LibraryEditDialogViewModel
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
import io.github.snd_r.komelia.ui.reader.ReaderViewModel
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
import io.github.snd_r.komelia.ui.settings.komf.KomfConfigState
import io.github.snd_r.komelia.ui.settings.komf.general.KomfSettingsViewModel
import io.github.snd_r.komelia.ui.settings.komf.jobs.KomfJobsViewModel
import io.github.snd_r.komelia.ui.settings.komf.notifications.KomfNotificationSettingsViewModel
import io.github.snd_r.komelia.ui.settings.komf.processing.KomfProcessingSettingsViewModel
import io.github.snd_r.komelia.ui.settings.komf.providers.KomfProvidersSettingsViewModel
import io.github.snd_r.komelia.ui.settings.navigation.SettingsNavigationViewModel
import io.github.snd_r.komelia.ui.settings.server.ServerSettingsViewModel
import io.github.snd_r.komelia.ui.settings.server.management.ServerManagementViewModel
import io.github.snd_r.komelia.ui.settings.updates.AppUpdatesViewModel
import io.github.snd_r.komelia.ui.settings.users.UsersViewModel
import io.github.snd_r.komelia.updates.AppRelease
import io.github.snd_r.komelia.updates.AppUpdater
import io.github.snd_r.komelia.updates.StartupUpdateChecker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import snd.komf.api.MediaServer
import snd.komf.client.KomfClientFactory
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

interface DependencyContainer {
    val settingsRepository: SettingsRepository
    val readerSettingsRepository: ReaderSettingsRepository
    val secretsRepository: SecretsRepository
    val appUpdater: AppUpdater
    val availableDecoders: Flow<List<PlatformDecoderDescriptor>>
    val komgaClientFactory: KomgaClientFactory
    val imageLoader: ImageLoader
    val imageLoaderContext: PlatformContext
    val appNotifications: AppNotifications
    val readerImageLoader: ReaderImageLoader
    val komfClientFactory: KomfClientFactory
    val lyricist: Lyricist<Strings>
        get() = Lyricist(Locales.EN, mapOf(Locales.EN to EnStrings))
}

class ViewModelFactory(private val dependencies: DependencyContainer) {
    private val komgaClientFactory: KomgaClientFactory
        get() = dependencies.komgaClientFactory
    private val appUpdater: AppUpdater
        get() = dependencies.appUpdater
    private val settingsRepository: SettingsRepository
        get() = dependencies.settingsRepository
    private val readerSettingsRepository: ReaderSettingsRepository
        get() = dependencies.readerSettingsRepository
    private val secretsRepository: SecretsRepository
        get() = dependencies.secretsRepository
    private val imageLoader: ImageLoader
        get() = dependencies.imageLoader
    private val availableDecoders: Flow<List<PlatformDecoderDescriptor>>
        get() = dependencies.availableDecoders
    val appNotifications
        get() = dependencies.appNotifications
    private val appStrings
        get() = dependencies.lyricist.state.map { it.strings }

    private val authenticatedUser = MutableStateFlow<KomgaUser?>(null)
    private val libraries = MutableStateFlow<List<KomgaLibrary>>(emptyList())
    private val releases = MutableStateFlow<List<AppRelease>>(emptyList())

    private val komfConfigState = KomfConfigState(
        dependencies.komfClientFactory.configClient(),
        appNotifications
    )

    private val komgaEventSource = ManagedKomgaEvents(
        authenticatedUser = authenticatedUser,
        eventSourceFactory = komgaClientFactory::sseSession,
        memoryCache = imageLoader.memoryCache,
        diskCache = imageLoader.diskCache,
        libraryClient = komgaClientFactory.libraryClient(),
        librariesFlow = libraries
    )

    private val startupUpdateChecker = StartupUpdateChecker(appUpdater, settingsRepository, releases)

    fun getLibraryViewModel(
        libraryId: KomgaLibraryId?,
    ): LibraryViewModel {
        return LibraryViewModel(
            libraryFlow = libraryId?.let { getLibraryFlow(it) },
            libraryClient = komgaClientFactory.libraryClient(),
            collectionClient = komgaClientFactory.collectionClient(),
            readListsClient = komgaClientFactory.readListClient(),
            appNotifications = appNotifications,
            komgaEvents = komgaEventSource.events,
        )
    }

    fun getHomeViewModel(libraryId: KomgaLibraryId?): HomeViewModel {
        return HomeViewModel(
            seriesClient = komgaClientFactory.seriesClient(),
            bookClient = komgaClientFactory.bookClient(),
            appNotifications = appNotifications,
            komgaEvents = komgaEventSource.events,
            cardWidthFlow = settingsRepository.getCardWidth(),
        )
    }

    fun getNavigationViewModel(navigator: Navigator): MainScreenViewModel {
        return MainScreenViewModel(
            libraryClient = komgaClientFactory.libraryClient(),
            appNotifications = appNotifications,
            navigator = navigator,
            komgaEvents = komgaEventSource.events,
            searchBarState = SearchBarState(
                seriesClient = komgaClientFactory.seriesClient(),
                bookClient = komgaClientFactory.bookClient(),
                appNotifications = appNotifications,
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
        notifications = appNotifications,
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
            notifications = appNotifications,
            komgaEvents = komgaEventSource.events,
            libraries = libraries,
            settingsRepository = settingsRepository,
            readListClient = komgaClientFactory.readListClient(),
        )
    }

    fun getSeriesBrowseViewModel(
        libraryId: KomgaLibraryId?,
        sort: SeriesSort = SeriesSort.TITLE_ASC,
    ): SeriesListViewModel {
        return SeriesListViewModel(
            seriesClient = komgaClientFactory.seriesClient(),
            referentialClient = komgaClientFactory.referentialClient(),
            notifications = appNotifications,
            komgaEvents = komgaEventSource.events,
            settingsRepository = settingsRepository,
            libraryFlow = getLibraryFlow(libraryId),
            cardWidthFlow = settingsRepository.getCardWidth(),
            defaultSort = sort
        )
    }

    fun getBookReaderViewModel(navigator: Navigator, markReadProgress: Boolean): ReaderViewModel {
        return ReaderViewModel(
            bookClient = komgaClientFactory.bookClient(),
            navigator = navigator,
            appNotifications = appNotifications,
            settingsRepository = settingsRepository,
            readerSettingsRepository = readerSettingsRepository,
            imageLoader = dependencies.readerImageLoader,
            availableDecoders = availableDecoders,
            appStrings = appStrings,
            markReadProgress = markReadProgress,
        )
    }

    fun getLoginViewModel(): LoginViewModel {
        return LoginViewModel(
            settingsRepository = settingsRepository,
            komgaUserClient = komgaClientFactory.userClient(),
            komgaLibraryClient = komgaClientFactory.libraryClient(),
            authenticatedUserFlow = authenticatedUser,
            availableLibrariesFlow = libraries,
            secretsRepository = secretsRepository,
            notifications = appNotifications,
        )
    }

    fun getLibraryEditDialogViewModel(library: KomgaLibrary?, onDismissRequest: () -> Unit) =
        LibraryEditDialogViewModel(
            library = library,
            onDialogDismiss = onDismissRequest,
            libraryClient = komgaClientFactory.libraryClient(),
            appNotifications = appNotifications
        )

    fun getSeriesEditDialogViewModel(series: KomgaSeries, onDismissRequest: () -> Unit) =
        SeriesEditDialogViewModel(
            series = series,
            onDialogDismiss = onDismissRequest,
            seriesClient = komgaClientFactory.seriesClient(),
            notifications = appNotifications,
            cardWidth = settingsRepository.getCardWidth(),
        )

    fun getSeriesBulkEditDialogViewModel(series: List<KomgaSeries>, onDismissRequest: () -> Unit) =
        SeriesBulkEditDialogViewModel(
            series = series,
            onDialogDismiss = onDismissRequest,
            seriesClient = komgaClientFactory.seriesClient(),
            notifications = appNotifications,
        )

    fun getBookEditDialogViewModel(book: KomgaBook, onDismissRequest: () -> Unit) =
        BookEditDialogViewModel(
            book = book,
            onDialogDismiss = onDismissRequest,
            bookClient = komgaClientFactory.bookClient(),
            notifications = appNotifications,
            cardWidth = settingsRepository.getCardWidth(),
        )

    fun getBookBulkEditDialogViewModel(books: List<KomgaBook>, onDismissRequest: () -> Unit) =
        BookBulkEditDialogViewModel(
            books = books,
            onDialogDismiss = onDismissRequest,
            bookClient = komgaClientFactory.bookClient(),
            notifications = appNotifications,
        )

    fun getCollectionEditDialogViewModel(collection: KomgaCollection, onDismissRequest: () -> Unit) =
        CollectionEditDialogViewModel(
            collection = collection,
            onDialogDismiss = onDismissRequest,
            collectionClient = komgaClientFactory.collectionClient(),
            notifications = appNotifications,
            cardWidth = settingsRepository.getCardWidth(),
        )

    fun getReadListEditDialogViewModel(readList: KomgaReadList, onDismissRequest: () -> Unit) =
        ReadListEditDialogViewModel(
            readList = readList,
            onDialogDismiss = onDismissRequest,
            readListClient = komgaClientFactory.readListClient(),
            notifications = appNotifications,
            cardWidth = settingsRepository.getCardWidth(),
        )

    fun getAddToCollectionDialogViewModel(series: List<KomgaSeries>, onDismissRequest: () -> Unit) =
        AddToCollectionDialogViewModel(
            series = series,
            onDismissRequest = onDismissRequest,
            collectionClient = komgaClientFactory.collectionClient(),
            appNotifications = appNotifications
        )

    fun getAddToReadListDialogViewModel(books: List<KomgaBook>, onDismissRequest: () -> Unit) =
        AddToReadListDialogViewModel(
            books = books,
            onDismissRequest = onDismissRequest,
            readListClient = komgaClientFactory.readListClient(),
            appNotifications = appNotifications
        )

    fun getFileBrowserDialogViewModel() =
        FileBrowserDialogViewModel(komgaClientFactory.fileSystemClient(), appNotifications)


    fun getSearchViewModel(initialQuery: String?) = SearchViewModel(
        seriesClient = komgaClientFactory.seriesClient(),
        bookClient = komgaClientFactory.bookClient(),
        appNotifications = appNotifications,
        libraries = libraries,
        initialQuery = initialQuery
    )


    fun getAccountViewModel(): AccountSettingsViewModel {
        val user = requireNotNull(authenticatedUser.value)
        return AccountSettingsViewModel(user)
    }

    fun getAuthenticationActivityViewModel(forMe: Boolean): AuthenticationActivityViewModel {
        return AuthenticationActivityViewModel(forMe, komgaClientFactory.userClient(), appNotifications)
    }

    fun getUsersViewModel(): UsersViewModel {
        val user = requireNotNull(authenticatedUser.value)
        return UsersViewModel(appNotifications, komgaClientFactory.userClient(), user)
    }

    fun getPasswordChangeDialogViewModel(user: KomgaUser?) = PasswordChangeDialogViewModel(
        appNotifications,
        komgaClientFactory.userClient(),
        user
    )

    fun getUserAddDialogViewModel(): UserAddDialogViewModel {
        return UserAddDialogViewModel(
            appNotifications = appNotifications,
            userClient = komgaClientFactory.userClient()
        )
    }

    fun getUserEditDialogViewModel(user: KomgaUser): UserEditDialogViewModel {
        val libraries = requireNotNull(libraries.value)
        return UserEditDialogViewModel(appNotifications, user, libraries, komgaClientFactory.userClient())
    }

    fun getServerSettingsViewModel(): ServerSettingsViewModel {
        return ServerSettingsViewModel(
            appNotifications = appNotifications,
            settingsClient = komgaClientFactory.settingsClient(),
            bookClient = komgaClientFactory.bookClient(),
            libraryClient = komgaClientFactory.libraryClient(),
            libraries = libraries,
            taskClient = komgaClientFactory.taskClient(),
            actuatorClient = komgaClientFactory.actuatorClient()
        )
    }

    fun getServerManagementViewModel(): ServerManagementViewModel {
        return ServerManagementViewModel(
            appNotifications = appNotifications,
            libraryClient = komgaClientFactory.libraryClient(),
            libraries = libraries,
            taskClient = komgaClientFactory.taskClient(),
            actuatorClient = komgaClientFactory.actuatorClient(),
        )
    }

    fun getAnnouncementsViewModel(): AnnouncementsViewModel {
        return AnnouncementsViewModel(appNotifications, komgaClientFactory.announcementClient())
    }

    fun getSettingsNavigationViewModel(rootNavigator: Navigator): SettingsNavigationViewModel {
        return SettingsNavigationViewModel(
            rootNavigator = rootNavigator,
            appNotifications = appNotifications,
            userClient = komgaClientFactory.userClient(),
            authenticatedUser = authenticatedUser,
            secretsRepository = secretsRepository,
            currentServerUrl = settingsRepository.getServerUrl(),
            bookClient = komgaClientFactory.bookClient(),
            latestVersion = settingsRepository.getLastCheckedReleaseVersion()
        )
    }

    fun getAppearanceViewModel(): AppSettingsViewModel {
        return AppSettingsViewModel(settingsRepository)
    }

    fun getSettingsUpdatesViewModel(): AppUpdatesViewModel {
        return AppUpdatesViewModel(
            releases = releases,
            updater = appUpdater,
            settings = settingsRepository,
            notifications = appNotifications,
        )
    }

    fun getLibraryCollectionsViewModel(libraryId: KomgaLibraryId?): LibraryCollectionsViewModel {
        return LibraryCollectionsViewModel(
            collectionClient = komgaClientFactory.collectionClient(),
            appNotifications = appNotifications,
            events = komgaEventSource.events,
            libraryFlow = libraryId?.let { getLibraryFlow(it) },
            cardWidthFlow = settingsRepository.getCardWidth(),
        )
    }

    fun getLibraryReadListsViewModel(libraryId: KomgaLibraryId?): LibraryReadListsViewModel {
        return LibraryReadListsViewModel(
            readListClient = komgaClientFactory.readListClient(),
            appNotifications = appNotifications,
            komgaEvents = komgaEventSource.events,
            libraryFlow = libraryId?.let { getLibraryFlow(it) },
            cardWidthFlow = settingsRepository.getCardWidth(),
        )

    }

    fun getCollectionViewModel(collectionId: KomgaCollectionId): CollectionViewModel {
        return CollectionViewModel(
            collectionId = collectionId,
            collectionClient = komgaClientFactory.collectionClient(),
            notifications = appNotifications,
            seriesClient = komgaClientFactory.seriesClient(),
            komgaEvents = komgaEventSource.events,
            cardWidthFlow = settingsRepository.getCardWidth()
        )
    }

    fun getReadListViewModel(readListId: KomgaReadListId): ReadListViewModel {
        return ReadListViewModel(
            readListId = readListId,
            readListClient = komgaClientFactory.readListClient(),
            bookClient = komgaClientFactory.bookClient(),
            notifications = appNotifications,
            komgaEvents = komgaEventSource.events,
            cardWidthFlow = settingsRepository.getCardWidth()
        )
    }

    fun getMediaAnalysisViewModel(): MediaAnalysisViewModel {
        return MediaAnalysisViewModel(
            bookClient = komgaClientFactory.bookClient(),
            appNotifications = appNotifications
        )
    }

    fun getKomfSettingsViewModel(): KomfSettingsViewModel {
        return KomfSettingsViewModel(
            komfConfigClient = dependencies.komfClientFactory.configClient(),
            appNotifications = appNotifications,
            settingsRepository = settingsRepository,
            komfConfig = komfConfigState,
            libraries = libraries,
        )
    }

    fun getKomfNotificationViewModel(): KomfNotificationSettingsViewModel {
        return KomfNotificationSettingsViewModel(
            komfConfigClient = dependencies.komfClientFactory.configClient(),
            komfNotificationClient = dependencies.komfClientFactory.notificationClient(),
            libraries = libraries,
            appNotifications = appNotifications,
            komfConfig = komfConfigState
        )
    }

    fun getKomfProcessingViewModel(): KomfProcessingSettingsViewModel {
        return KomfProcessingSettingsViewModel(
            komfConfigClient = dependencies.komfClientFactory.configClient(),
            libraries = libraries,
            appNotifications = appNotifications,
            komfConfig = komfConfigState
        )
    }

    fun getKomfProvidersViewModel(): KomfProvidersSettingsViewModel {
        return KomfProvidersSettingsViewModel(
            komfConfigClient = dependencies.komfClientFactory.configClient(),
            libraries = libraries,
            appNotifications = appNotifications,
            komfConfig = komfConfigState
        )
    }

    fun getKomfJobsViewModel(): KomfJobsViewModel {
        return KomfJobsViewModel(
            jobClient = dependencies.komfClientFactory.jobClient(),
            seriesClient = dependencies.komgaClientFactory.seriesClient(),
            appNotifications = appNotifications
        )
    }

    fun getKomfIdentifyDialogViewModel(series: KomgaSeries, onDismissRequest: () -> Unit): KomfIdentifyDialogViewModel {
        return KomfIdentifyDialogViewModel(
            series = series,
            komfConfig = komfConfigState,
            komfMetadataClient = dependencies.komfClientFactory.metadataClient(MediaServer.KOMGA),
            komfJobClient = dependencies.komfClientFactory.jobClient(),
            appNotifications = appNotifications,
            onDismiss = onDismissRequest,
        )
    }

    fun getKomfResetMetadataDialogViewModel(
        series: KomgaSeries,
        onDismissRequest: () -> Unit
    ): KomfResetMetadataDialogViewModel {
        return KomfResetMetadataDialogViewModel(
            series = series,
            komfMetadataClient = dependencies.komfClientFactory.metadataClient(MediaServer.KOMGA),
            appNotifications = appNotifications,
            onDismiss = onDismissRequest,
        )
    }

    fun getSeriesBulkActions() = SeriesBulkActions(komgaClientFactory.seriesClient(), appNotifications)
    fun getCollectionBulkActions() = CollectionBulkActions(komgaClientFactory.collectionClient(), appNotifications)
    fun getBookBulkActions() = BookBulkActions(komgaClientFactory.bookClient(), appNotifications)
    fun getReadListBulkActions() = ReadListBulkActions(komgaClientFactory.readListClient(), appNotifications)

    fun getKomgaEvents(): SharedFlow<KomgaEvent> = komgaEventSource.events

    fun getStartupUpdateChecker() = startupUpdateChecker

    private fun getLibraryFlow(id: KomgaLibraryId?): Flow<KomgaLibrary?> {
        if (id == null) return flowOf(null)
        return libraries.map { libraries -> libraries.firstOrNull { it.id == id } }
    }
}
