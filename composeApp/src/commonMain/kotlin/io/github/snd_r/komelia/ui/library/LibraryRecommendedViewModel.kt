package io.github.snd_r.komelia.ui.library

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.defaultCardWidth
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.common.menus.SeriesMenuActions
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.book.KomgaBookQuery
import io.github.snd_r.komga.book.KomgaReadStatus
import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.series.KomgaSeries
import io.github.snd_r.komga.series.KomgaSeriesClient
import io.github.snd_r.komga.sse.KomgaEvent
import io.github.snd_r.komga.sse.KomgaEvent.BookEvent
import io.github.snd_r.komga.sse.KomgaEvent.ReadProgressEvent
import io.github.snd_r.komga.sse.KomgaEvent.ReadProgressSeriesEvent
import io.github.snd_r.komga.sse.KomgaEvent.SeriesEvent
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class LibraryRecommendedViewModel(
    private val library: StateFlow<KomgaLibrary>?,
    private val seriesClient: KomgaSeriesClient,
    private val bookClient: KomgaBookClient,
    private val appNotifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    cardWidthFlow: Flow<Dp>,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    val cardWidth = cardWidthFlow.stateIn(screenModelScope, Eagerly, defaultCardWidth.dp)

    var keepReadingBooks = mutableStateListOf<KomgaBook>()
    var recentlyReleasedBooks = mutableStateListOf<KomgaBook>()
    var recentlyAddedBooks = mutableStateListOf<KomgaBook>()

    var recentlyAddedSeries = mutableStateListOf<KomgaSeries>()
    var recentlyUpdatedSeries = mutableStateListOf<KomgaSeries>()

    private val reloadJobsFlow = MutableSharedFlow<Unit>(0, 1, DROP_OLDEST)

    fun initialize() {
        if (state.value !is Uninitialized) return

        screenModelScope.launch { startEventListener() }

        reloadJobsFlow.onEach {
            load()
            delay(5000)
        }.launchIn(screenModelScope)

        screenModelScope.launch { load() }
    }

    fun reload() {
        screenModelScope.launch { load() }
    }

    private suspend fun load() {
        appNotifications.runCatchingToNotifications {

            mutableState.value = LoadState.Loading
            loadKeepReadingBooks()
            loadRecentlyReleasedBooks()
            loadRecentlyAddedBooks()
            loadRecentlyAddedSeries()
            loadRecentlyUpdatedSeries()
            mutableState.value = LoadState.Success(Unit)

        }.onFailure { mutableState.value = LoadState.Error(it) }
    }

    fun seriesMenuActions() = SeriesMenuActions(seriesClient, appNotifications, screenModelScope)
    fun bookMenuActions() = BookMenuActions(bookClient, appNotifications, screenModelScope)

    private suspend fun loadKeepReadingBooks() {
        val pageRequest = KomgaPageRequest(
            sort = listOf("readProgress.readDate,desc"),
        )

        val books = bookClient.getAllBooks(
            query = KomgaBookQuery(
                readStatus = listOf(KomgaReadStatus.IN_PROGRESS),
                libraryIds = library?.value?.let { listOf(it.id) } ?: emptyList()
            ),
            pageRequest = pageRequest
        ).content


        keepReadingBooks.clear()
        keepReadingBooks.addAll(books)
    }

    private suspend fun loadRecentlyReleasedBooks() {
        val pageRequest = KomgaPageRequest(
            sort = listOf("metadata.releaseDate,desc"),
        )

        val books = bookClient.getAllBooks(
            pageRequest = pageRequest,
            query = KomgaBookQuery(
                libraryIds = library?.value?.let { listOf(it.id) } ?: emptyList(),
                releasedAfter = LocalDate.now().minusMonths(1)
            ),
        ).content

        recentlyReleasedBooks.clear()
        recentlyReleasedBooks.addAll(books)
    }

    private suspend fun loadRecentlyAddedBooks() {
        val pageRequest = KomgaPageRequest(
            sort = listOf("createdDate,desc"),
        )

        val books = bookClient.getAllBooks(
            query = KomgaBookQuery(
                libraryIds = library?.value?.let { listOf(it.id) } ?: emptyList(),
            ),
            pageRequest = pageRequest,
        ).content

        recentlyAddedBooks.clear()
        recentlyAddedBooks.addAll(books)
    }

    private suspend fun loadRecentlyAddedSeries() {
        val series = seriesClient.getNewSeries(libraryId = library?.value?.id, oneshot = false).content
        recentlyAddedSeries.clear()
        recentlyAddedSeries.addAll(series)
    }

    private suspend fun loadRecentlyUpdatedSeries() {
        val series = seriesClient.getUpdatedSeries(libraryId = library?.value?.id, oneshot = false).content
        recentlyUpdatedSeries.clear()
        recentlyUpdatedSeries.addAll(series)
    }

    private suspend fun startEventListener() {
        komgaEvents.collect { event ->
            when (event) {
                is BookEvent -> {
                    if (library == null || event.libraryId == library.value.id) {
                        reloadJobsFlow.tryEmit(Unit)
                    }
                }

                is SeriesEvent -> {
                    if (library == null || event.libraryId == library.value.id) {
                        reloadJobsFlow.tryEmit(Unit)
                    }
                }

                is ReadProgressEvent -> {
                    val matches = keepReadingBooks.any { event.bookId == it.id }
                            || recentlyAddedBooks.any { event.bookId == it.id }
                            || recentlyAddedBooks.any { event.bookId == it.id }

                    if (matches) reloadJobsFlow.tryEmit(Unit)

                }

                is ReadProgressSeriesEvent -> {
                    val matches = recentlyAddedSeries.any { event.seriesId == it.id }
                            || recentlyUpdatedSeries.any { event.seriesId == it.id }

                    if (matches) reloadJobsFlow.tryEmit(Unit)
                }

                else -> {}
            }
        }
    }
}