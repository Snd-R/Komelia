package io.github.snd_r.komelia.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.book.BookScreen
import io.github.snd_r.komelia.ui.collection.CollectionScreen
import io.github.snd_r.komelia.ui.common.menus.LibraryMenuActions
import io.github.snd_r.komelia.ui.library.DashboardScreen
import io.github.snd_r.komelia.ui.library.LibraryScreen
import io.github.snd_r.komelia.ui.navigation.SearchBarState
import io.github.snd_r.komelia.ui.readlist.ReadListScreen
import io.github.snd_r.komelia.ui.series.SeriesScreen
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.library.KomgaLibraryClient
import io.github.snd_r.komga.sse.KomgaEvent
import io.github.snd_r.komga.sse.KomgaEvent.BookDeleted
import io.github.snd_r.komga.sse.KomgaEvent.CollectionDeleted
import io.github.snd_r.komga.sse.KomgaEvent.LibraryDeleted
import io.github.snd_r.komga.sse.KomgaEvent.ReadListDeleted
import io.github.snd_r.komga.sse.KomgaEvent.SeriesDeleted
import io.github.snd_r.komga.sse.KomgaEvent.TaskQueueStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainScreenViewModel(
    private val libraryClient: KomgaLibraryClient,
    private val appNotifications: AppNotifications,
    private val navigator: Navigator,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    val searchBarState: SearchBarState,
    val libraries: StateFlow<List<KomgaLibrary>>
) : ScreenModel {

    init {
        screenModelScope.launch { startEventListener() }
    }

    val komgaTaskQueueStatus = MutableStateFlow<TaskQueueStatus?>(null)
    var isNavBarOpen by mutableStateOf(true)
        private set

    fun toggleNavBar() {
        isNavBarOpen = !isNavBarOpen
    }

    fun getLibraryActions(): LibraryMenuActions {
        return LibraryMenuActions(libraryClient, appNotifications, screenModelScope)
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
        if (lastScreen is SeriesScreen && lastScreen.seriesId == event.seriesId)
            navigator.replaceAll(LibraryScreen(event.libraryId))
    }

    private fun onLibraryDeleted(event: LibraryDeleted) {
        val lastScreen = navigator.lastItem
        if (lastScreen is LibraryScreen && lastScreen.libraryId == event.libraryId)
            navigator.replaceAll(DashboardScreen())
    }

    private fun onCollectionDeleted(event: CollectionDeleted) {
        val lastScreen = navigator.lastItem
        if (lastScreen is CollectionScreen && lastScreen.collectionId == event.collectionId) {
            val success = navigator.popUntil { it is LibraryScreen }
            if (!success && navigator.lastItem !is LibraryScreen) navigator.replaceAll(DashboardScreen())
        }
    }

    private fun onReadListDeleted(event: ReadListDeleted) {
        val lastScreen = navigator.lastItem
        if (lastScreen is ReadListScreen && lastScreen.readListId == event.readListId) {
            val success = navigator.popUntil { it is LibraryScreen }
            if (!success && navigator.lastItem !is LibraryScreen) navigator.replaceAll(DashboardScreen())
        }
    }

}
