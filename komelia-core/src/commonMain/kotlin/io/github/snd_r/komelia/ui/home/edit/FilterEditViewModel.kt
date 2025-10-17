package io.github.snd_r.komelia.ui.home.edit

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.common.cards.defaultCardWidth
import io.github.snd_r.komelia.ui.home.BookFilterData
import io.github.snd_r.komelia.ui.home.BooksHomeScreenFilter
import io.github.snd_r.komelia.ui.home.HomeFilterData
import io.github.snd_r.komelia.ui.home.HomeScreenFilter
import io.github.snd_r.komelia.ui.home.HomeScreenFilterRepository
import io.github.snd_r.komelia.ui.home.SeriesFilterData
import io.github.snd_r.komelia.ui.home.SeriesHomeScreenFilter
import io.github.snd_r.komelia.ui.home.homeScreenDefaultFilters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.collection.KomgaCollectionClient
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.referential.KomgaReferentialClient
import snd.komga.client.series.KomgaSeriesClient

class FilterEditViewModel(
    homeFilters: List<HomeFilterData>?,
    private val appNotifications: AppNotifications,
    private val bookClient: KomgaBookClient,
    private val seriesClient: KomgaSeriesClient,
    private val readListClient: KomgaReadListClient,
    private val collectionClient: KomgaCollectionClient,
    private val referentialClient: KomgaReferentialClient,
    private val filterRepository: HomeScreenFilterRepository,
    private val libraries: Flow<List<KomgaLibrary>>,
    cardWidthFlow: Flow<Dp>,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {
    private val filterSuggestionOptions = MutableStateFlow(
        FilterSuggestionOptions(
            tags = emptyList(),
            genres = emptyList(),
            authors = emptyList(),
            publishers = emptyList(),
            languages = emptyList(),
            libraries = emptyList(),
            sharingLabels = emptyList()
        )
    )

    val cardWidth = cardWidthFlow.stateIn(screenModelScope, Eagerly, defaultCardWidth.dp)
    val filters = MutableStateFlow(

        homeFilters?.map {
            when (it) {
                is BookFilterData -> BookFilterEditState(
                    seriesClient = seriesClient,
                    bookClient = bookClient,
                    readListClient = readListClient,
                    appNotifications = appNotifications,
                    coroutineScope = screenModelScope,
                    options = filterSuggestionOptions,
                    cardWidth = cardWidth,
                    initialFilter = it.filter,
                    initialBooks = it.books,
                )

                is SeriesFilterData -> SeriesFilterEditState(
                    seriesClient = seriesClient,
                    collectionClient = collectionClient,
                    appNotifications = appNotifications,
                    coroutineScope = screenModelScope,
                    options = filterSuggestionOptions,
                    cardWidth = cardWidth,
                    initialFilter = it.filter,
                    initialSeries = it.series,
                )
            }
        } ?: emptyList()
    )


    suspend fun initialize() {
        appNotifications.runCatchingToNotifications {
            val tags = referentialClient.getBookTags()
            val genres = referentialClient.getGenres()
            val authors = referentialClient.getAuthors(pageRequest = KomgaPageRequest(unpaged = true)).content
            val sharingLabels = referentialClient.getSharingLabels()
            val languages = referentialClient.getLanguages()
            val publishers = referentialClient.getPublishers()
            filterSuggestionOptions.value = FilterSuggestionOptions(
                tags = tags,
                genres = genres,
                libraries = libraries.first(),
                authors = authors,
                sharingLabels = sharingLabels,
                languages = languages,
                publishers = publishers
            )
        }
    }

    fun onFilterReorder(from: Int, to: Int) {
        filters.update { current ->
            val updated = current.toMutableList()
            val moved = updated.removeAt(from)
            updated.add(to, moved)
            updated
        }
    }

    fun onFilterAdd(type: FilterType) {
        val newFilter = when (type) {
            FilterType.Series -> SeriesFilterEditState(
                seriesClient = seriesClient,
                collectionClient = collectionClient,
                appNotifications = appNotifications,
                coroutineScope = screenModelScope,
                options = filterSuggestionOptions,
                cardWidth = cardWidth,
                initialFilter = null,
                initialSeries = null,
            )

            FilterType.Book -> BookFilterEditState(
                seriesClient = seriesClient,
                bookClient = bookClient,
                readListClient = readListClient,
                appNotifications = appNotifications,
                coroutineScope = screenModelScope,
                options = filterSuggestionOptions,
                cardWidth = cardWidth,
                initialFilter = null,
                initialBooks = null,
            )
        }
        filters.update { current -> current.plus(newFilter) }
    }

    fun onFilterRemove(filter: FilterEditState) {
        filters.update { current -> current.minus(filter) }

    }

    fun onResetFiltersToDefault() {
        this.filters.value = homeScreenDefaultFilters.map {
            when (it) {
                is BooksHomeScreenFilter -> BookFilterEditState(
                    seriesClient = seriesClient,
                    bookClient = bookClient,
                    readListClient = readListClient,
                    appNotifications = appNotifications,
                    coroutineScope = screenModelScope,
                    options = filterSuggestionOptions,
                    cardWidth = cardWidth,
                    initialFilter = it,
                    initialBooks = null,
                )

                is SeriesHomeScreenFilter -> SeriesFilterEditState(
                    seriesClient = seriesClient,
                    collectionClient = collectionClient,
                    appNotifications = appNotifications,
                    coroutineScope = screenModelScope,
                    options = filterSuggestionOptions,
                    cardWidth = cardWidth,
                    initialFilter = it,
                    initialSeries = null,
                )
            }
        }
        screenModelScope.launch {
            filterRepository.putFilters(homeScreenDefaultFilters)
        }
    }

    suspend fun onEditEnd() {
        filterRepository.putFilters(
            filters.value.mapIndexed { index, state -> state.toFilter(index + 1) }
        )
    }

    enum class FilterType {
        Series, Book
    }
}