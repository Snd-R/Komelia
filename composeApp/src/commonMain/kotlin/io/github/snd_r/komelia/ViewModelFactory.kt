package io.github.snd_r.komelia

import cafe.adriel.voyager.navigator.Navigator
import coil3.ImageLoader
import coil3.PlatformContext
import io.github.snd_r.komelia.settings.ReaderSettingsRepository
import io.github.snd_r.komelia.settings.SecretsRepository
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.ui.MainScreenViewModel
import io.github.snd_r.komelia.ui.book.BookViewModel
import io.github.snd_r.komelia.ui.collection.CollectionViewModel
import io.github.snd_r.komelia.ui.dialogs.bookedit.BookEditDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.filebrowser.FileBrowserDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.libraryedit.LibraryEditDialogViewModel
import io.github.snd_r.komelia.ui.dialogs.seriesedit.SeriesEditDialogViewModel
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
import io.github.snd_r.komelia.ui.series.SeriesListViewModel
import io.github.snd_r.komelia.ui.series.SeriesListViewModel.SeriesSort
import io.github.snd_r.komelia.ui.series.SeriesViewModel
import io.github.snd_r.komelia.ui.settings.account.AccountSettingsViewModel
import io.github.snd_r.komelia.ui.settings.analysis.MediaAnalysisViewModel
import io.github.snd_r.komelia.ui.settings.announcements.AnnouncementsViewModel
import io.github.snd_r.komelia.ui.settings.app.AppSettingsViewModel
import io.github.snd_r.komelia.ui.settings.authactivity.AuthenticationActivityViewModel
import io.github.snd_r.komelia.ui.settings.navigation.SettingsNavigationViewModel
import io.github.snd_r.komelia.ui.settings.server.ServerSettingsViewModel
import io.github.snd_r.komelia.ui.settings.server.management.ServerManagementViewModel
import io.github.snd_r.komelia.ui.settings.users.UsersViewModel
import io.github.snd_r.komga.KomgaClientFactory
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.collection.KomgaCollectionId
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.library.KomgaLibraryId
import io.github.snd_r.komga.readlist.KomgaReadListId
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesId
import io.github.snd_r.komga.sse.KomgaEvent
import io.github.snd_r.komga.user.KomgaUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map


class ViewModelFactory(
    private val komgaClientFactory: KomgaClientFactory,
    private val settingsRepository: SettingsRepository,
    private val readerSettingsRepository: ReaderSettingsRepository,
    private val secretsRepository: SecretsRepository,
    private val imageLoader: ImageLoader,
    private val imageLoaderContext: PlatformContext,
) {
    private val authenticatedUser = MutableStateFlow<KomgaUser?>(null)
    private val libraries = MutableStateFlow<List<KomgaLibrary>>(emptyList())

    private val komgaEventSource = ManagedKomgaEvents(
        authenticatedUser = authenticatedUser,
        eventSourceFactory = komgaClientFactory::sseSession,
        memoryCache = imageLoader.memoryCache,
        diskCache = imageLoader.diskCache,
        libraryClient = komgaClientFactory.libraryClient(),
        librariesFlow = libraries
    )
    private val appNotifications = AppNotifications()

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

    fun getSeriesViewModel(seriesId: KomgaSeriesId) = SeriesViewModel(
        seriesId = seriesId,
        seriesClient = komgaClientFactory.seriesClient(),
        bookClient = komgaClientFactory.bookClient(),
        notifications = appNotifications,
        events = komgaEventSource.events,
        settingsRepository = settingsRepository,
    )

    fun getBookViewModel(bookId: KomgaBookId): BookViewModel {
        return BookViewModel(
            libraryClient = komgaClientFactory.libraryClient(),
            bookClient = komgaClientFactory.bookClient(),
            bookId = bookId,
            notifications = appNotifications,
            komgaEvents = komgaEventSource.events
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
            imageLoader = imageLoader,
            imageLoaderContext = imageLoaderContext,
            navigator = navigator,
            appNotifications = appNotifications,
            settingsRepository = settingsRepository,
            readerSettingsRepository = readerSettingsRepository,
            markReadProgress = markReadProgress,
        )
    }

    fun getLoginViewModel(): LoginViewModel {
        return LoginViewModel(
            settingsRepository,
            komgaClientFactory.userClient(),
            komgaClientFactory.libraryClient(),
            authenticatedUser,
            libraries
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

    fun getBookEditDialogViewModel(book: KomgaBook, onDismissRequest: () -> Unit) =
        BookEditDialogViewModel(
            book = book,
            onDialogDismiss = onDismissRequest,
            bookClient = komgaClientFactory.bookClient(),
            notifications = appNotifications,
            cardWidth = settingsRepository.getCardWidth(),
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
            bookClient = komgaClientFactory.bookClient()
        )
    }

    fun getAppearanceViewModel(): AppSettingsViewModel {
        return AppSettingsViewModel(settingsRepository, imageLoader)
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

    fun getAppNotifications(): AppNotifications = appNotifications

    fun getKomgaEvents(): SharedFlow<KomgaEvent> = komgaEventSource.events

    private fun getLibraryFlow(id: KomgaLibraryId?): Flow<KomgaLibrary?> {
        if (id == null) return flowOf(null)
        return libraries.map { libraries -> libraries.firstOrNull { it.id == id } }
    }
}

expect suspend fun createViewModelFactory(context: PlatformContext): ViewModelFactory
