package snd.komelia.ui

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaLibraryApi
import snd.komelia.offline.settings.OfflineSettingsRepository
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komelia.ui.book.BookScreen
import snd.komelia.ui.collection.CollectionScreen
import snd.komelia.ui.common.menus.LibraryMenuActions
import snd.komelia.ui.home.HomeScreen
import snd.komelia.ui.library.LibraryScreen
import snd.komelia.ui.login.LoginScreen
import snd.komelia.ui.oneshot.OneshotScreen
import snd.komelia.ui.readlist.ReadListScreen
import snd.komelia.ui.series.SeriesScreen
import snd.komelia.ui.topbar.NotificationsState
import snd.komelia.ui.topbar.SearchBarState
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.BookDeleted
import snd.komga.client.sse.KomgaEvent.CollectionDeleted
import snd.komga.client.sse.KomgaEvent.LibraryDeleted
import snd.komga.client.sse.KomgaEvent.ReadListDeleted
import snd.komga.client.sse.KomgaEvent.SeriesDeleted
import snd.komga.client.sse.KomgaEvent.TaskQueueStatus

class MainScreenViewModel(
    private val libraryApi: KomgaLibraryApi,
    private val appNotifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    private val screenReloadFlow: MutableSharedFlow<Unit>,
    private val offlineSettingsRepository: OfflineSettingsRepository,
    private val taskEmitter: OfflineTaskEmitter,
    val searchBarState: SearchBarState,
    val notificationsState: NotificationsState,
    val libraries: StateFlow<List<KomgaLibrary>>,
) : ScreenModel {

    val isOffline = offlineSettingsRepository.getOfflineMode().stateIn(screenModelScope, SharingStarted.Eagerly, false)
    private val navigatorFlow = MutableStateFlow<Navigator?>(null)
    private val navigator
        get() = navigatorFlow.value ?: error("main screen navigator is not initialized")

    fun initialize(navigator: Navigator) {
        this.navigatorFlow.value = navigator
    }

    init {
        screenModelScope.launch { startEventListener() }
    }

    val komgaTaskQueueStatus = MutableStateFlow<TaskQueueStatus?>(null)

    val navBarState = DrawerState(DrawerValue.Closed)

    suspend fun toggleNavBar() {
        if (navBarState.currentValue == DrawerValue.Closed) navBarState.open()
        else navBarState.close()
    }

    fun getLibraryActions(): LibraryMenuActions {
        return LibraryMenuActions(libraryApi, appNotifications, taskEmitter, screenModelScope)
    }

    fun onScreenReload() {
        screenReloadFlow.tryEmit(Unit)
    }

    fun goOnline() {
        screenModelScope.launch {
            offlineSettingsRepository.putOfflineMode(false)

            val rootNavigator = navigator.parent ?: return@launch
            rootNavigator.replaceAll(LoginScreen())
        }
    }

    //Assuming that delete events come from bottom (book->series->library)
    //this should switch screens in correct order in case of a library or series delete
    private suspend fun startEventListener() {
        komgaEvents.collect { event ->
            when (event) {
                is TaskQueueStatus -> komgaTaskQueueStatus.value = event
                is BookDeleted -> onBookDeletedEvent(event)
                is SeriesDeleted -> onSeriesDeleted(event)
                is LibraryDeleted -> onLibraryDeleted(event)
                is CollectionDeleted -> onCollectionDeleted(event)
                is ReadListDeleted -> onReadListDeleted(event)
                else -> {}
            }
        }
    }

    private fun onBookDeletedEvent(event: BookDeleted) {
        val lastScreen = navigator.lastItem
        if (lastScreen is BookScreen && lastScreen.bookId == event.bookId)
            navigator.replaceAll(SeriesScreen(event.seriesId))
    }

    private fun onSeriesDeleted(event: SeriesDeleted) {
        val lastScreen = navigator.lastItem
        when {
            lastScreen is SeriesScreen || lastScreen is OneshotScreen && lastScreen.seriesId == event.seriesId ->
                navigator.replaceAll(LibraryScreen(event.libraryId))
        }
    }

    private fun onLibraryDeleted(event: LibraryDeleted) {
        val lastScreen = navigator.lastItem
        if (lastScreen is LibraryScreen && lastScreen.libraryId == event.libraryId)
            navigator.replaceAll(HomeScreen())
    }

    private fun onCollectionDeleted(event: CollectionDeleted) {
        val lastScreen = navigator.lastItem
        if (lastScreen is CollectionScreen && lastScreen.collectionId == event.collectionId) {
            val success = navigator.popUntil { it is LibraryScreen }
            if (!success && navigator.lastItem !is LibraryScreen) navigator.replaceAll(HomeScreen())
        }
    }

    private fun onReadListDeleted(event: ReadListDeleted) {
        val lastScreen = navigator.lastItem
        if (lastScreen is ReadListScreen && lastScreen.readListId == event.readListId) {
            val success = navigator.popUntil { it is LibraryScreen }
            if (!success && navigator.lastItem !is LibraryScreen) navigator.replaceAll(HomeScreen())
        }
    }

    override fun onDispose() {
        notificationsState.onDispose()
    }
}
