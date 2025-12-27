package snd.komelia.ui.home.edit

import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import snd.komelia.AppNotifications
import snd.komelia.homefilters.HomeScreenFilter
import snd.komelia.homefilters.SeriesHomeScreenFilter
import snd.komelia.komga.api.KomgaCollectionsApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.ui.home.edit.SeriesMatchConditionState.SeriesConditionType
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaSort
import snd.komga.client.common.KomgaSort.Direction.ASC
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.search.KomgaSearchCondition
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesSearch

class SeriesFilterEditState(
    private val seriesApi: KomgaSeriesApi,
    private val collectionApi: KomgaCollectionsApi,
    private val appNotifications: AppNotifications,
    private val coroutineScope: CoroutineScope,
    private val options: StateFlow<FilterSuggestionOptions>,
    val cardWidth: StateFlow<Dp>,
    initialFilter: SeriesHomeScreenFilter?,
    initialSeries: List<KomgaSeries>?
) : FilterEditState {
    override val label = MutableStateFlow(initialFilter?.label ?: "Series Filter")

    val filter: MutableStateFlow<SeriesFilterStateType> = MutableStateFlow(
        initialFilter?.let { initial ->
            when (initial) {
                is SeriesHomeScreenFilter.CustomFilter -> SeriesCustomFilterState(
                    seriesApi = seriesApi,
                    collectionApi = collectionApi,
                    appNotifications = appNotifications,
                    options = options,
                    coroutineScope = coroutineScope,
                    initial = initial,
                    initialSeries = initialSeries,
                    initialPage = initial.pageRequest,
                )

                is SeriesHomeScreenFilter.RecentlyAdded -> SeriesRecentlyAddedFilterState(
                    seriesApi = seriesApi,
                    appNotifications = appNotifications,
                    coroutineScope = coroutineScope,
                    initial = initial,
                    initialSeries = initialSeries
                )

                is SeriesHomeScreenFilter.RecentlyUpdated -> SeriesRecentlyUpdatedFilterState(
                    seriesApi = seriesApi,
                    appNotifications = appNotifications,
                    coroutineScope = coroutineScope,
                    initial = initial,
                    initialSeries = initialSeries
                )
            }
        } ?: SeriesCustomFilterState(
            seriesApi = seriesApi,
            collectionApi = collectionApi,
            appNotifications = appNotifications,
            options = options,
            coroutineScope = coroutineScope,
            initial = null,
            initialSeries = null,
            null
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val series: StateFlow<List<KomgaSeries>> = filter.flatMapLatest { it.series }
        .stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())

    val type = filter.map {
        when (it) {
            is SeriesCustomFilterState -> FilterType.Custom
            is SeriesRecentlyAddedFilterState -> FilterType.RecentlyAdded
            is SeriesRecentlyUpdatedFilterState -> FilterType.RecentlyUpdated
        }
    }.stateIn(coroutineScope, SharingStarted.Eagerly, FilterType.Custom)

    override fun toFilter(order: Int): HomeScreenFilter {
        return when (val editState = filter.value) {
            is SeriesCustomFilterState -> SeriesHomeScreenFilter.CustomFilter(
                order = order,
                label = label.value,
                filter = editState.toSeriesCondition(),
                textSearch = null,
                pageRequest = KomgaPageRequest(
                    size = editState.pageSize.value,
                    sort = editState.getKomgaSort()
                )
            )

            is SeriesRecentlyAddedFilterState -> SeriesHomeScreenFilter.RecentlyAdded(
                order = order,
                label = label.value,
                pageSize = editState.pageSize.value
            )

            is SeriesRecentlyUpdatedFilterState -> SeriesHomeScreenFilter.RecentlyUpdated(
                order = order,
                label = label.value,
                pageSize = editState.pageSize.value
            )
        }
    }

    fun onTypeChange(type: FilterType) {
        filter.value = when (type) {
            FilterType.Custom -> SeriesCustomFilterState(
                seriesApi = seriesApi,
                collectionApi = collectionApi,
                appNotifications = appNotifications,
                options = options,
                coroutineScope = coroutineScope,
                initial = null,
                initialSeries = null,
                initialPage = null
            )

            FilterType.RecentlyAdded -> SeriesRecentlyAddedFilterState(
                seriesApi, appNotifications, coroutineScope, null, null
            )

            FilterType.RecentlyUpdated -> SeriesRecentlyUpdatedFilterState(
                seriesApi, appNotifications, coroutineScope, null, null
            )
        }
    }


    enum class FilterType { Custom, RecentlyAdded, RecentlyUpdated }
}

sealed interface SeriesFilterStateType {
    val series: StateFlow<List<KomgaSeries>>
}

class SeriesRecentlyAddedFilterState(
    private val seriesApi: KomgaSeriesApi,
    private val appNotifications: AppNotifications,
    private val coroutineScope: CoroutineScope,
    initial: SeriesHomeScreenFilter.RecentlyAdded?,
    initialSeries: List<KomgaSeries>?,
) : SeriesFilterStateType {
    val libraryIds = MutableStateFlow<List<KomgaLibraryId>>(emptyList())
    val pageSize = MutableStateFlow(initial?.pageSize ?: 20)
    override val series = MutableStateFlow(initialSeries ?: emptyList())

    init {
        val combined = combine(
            libraryIds,
            pageSize
        ) { libraryId, page -> libraryId to page }

        val dropped = if (initial != null) combined.drop(1) else combined
        dropped.onEach { (libraryId, page) ->
            series.value = getSeries(libraryId, page)
        }.launchIn(coroutineScope)
    }


    fun onPageSizeChange(pageSize: Int) {
        this.pageSize.value = pageSize
    }

    private suspend fun getSeries(libraryIds: List<KomgaLibraryId>, pageSize: Int): List<KomgaSeries> {
        return appNotifications.runCatchingToNotifications {
            val page = KomgaPageRequest(size = pageSize)
            seriesApi.getNewSeries(
                libraryIds = libraryIds,
                oneshot = false,
                deleted = false,
                pageRequest = page
            ).content
        }.getOrDefault(emptyList())
    }

    fun close() {
        coroutineScope.cancel()
    }
}

class SeriesRecentlyUpdatedFilterState(
    private val seriesApi: KomgaSeriesApi,
    private val appNotifications: AppNotifications,
    private val coroutineScope: CoroutineScope,
    initial: SeriesHomeScreenFilter.RecentlyUpdated?,
    initialSeries: List<KomgaSeries>?,
) : SeriesFilterStateType {
    val libraryIds = MutableStateFlow<List<KomgaLibraryId>>(emptyList())
    val pageSize = MutableStateFlow(initial?.pageSize ?: 20)
    override val series = MutableStateFlow(initialSeries ?: emptyList())

    init {
        val combined = combine(
            libraryIds,
            pageSize
        ) { libraryId, page -> libraryId to page }

        val dropped = if (initial != null) combined.drop(1) else combined
        dropped.onEach { (libraryId, page) ->
            series.value = getSeries(libraryId, page)
        }.launchIn(coroutineScope)
    }

    fun onPageSizeChange(pageSize: Int) {
        this.pageSize.value = pageSize
    }

    private suspend fun getSeries(libraryIds: List<KomgaLibraryId>, pageSize: Int): List<KomgaSeries> {
        return appNotifications.runCatchingToNotifications {
            val page = KomgaPageRequest(size = pageSize)
            seriesApi.getUpdatedSeries(
                libraryIds = libraryIds,
                oneshot = false,
                deleted = false,
                pageRequest = page
            ).content
        }.getOrDefault(emptyList())
    }

    fun close() {
        coroutineScope.cancel()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SeriesCustomFilterState(
    private val seriesApi: KomgaSeriesApi,
    private val collectionApi: KomgaCollectionsApi,
    private val appNotifications: AppNotifications,
    private val options: StateFlow<FilterSuggestionOptions>,
    private val coroutineScope: CoroutineScope,
    initial: SeriesHomeScreenFilter.CustomFilter?,
    initialSeries: List<KomgaSeries>?,
    initialPage: KomgaPageRequest?,
) : SeriesFilterStateType {
    val conditionState: MutableStateFlow<SeriesConditionState?> = MutableStateFlow(
        initial?.filter?.toSeriesConditionState(
            options = options,
            seriesApi = seriesApi,
            collectionApi = collectionApi,
            appNotifications = appNotifications,
            coroutineScope = coroutineScope

        )
    )
    val pageSize = MutableStateFlow(initialPage?.size ?: 20)
    val sort = MutableStateFlow(initialPage?.sort?.let { toSeriesSort(it) } ?: SeriesSort.Unsorted)
    val sortDirection = MutableStateFlow(initialPage?.sort?.orders?.firstOrNull()?.direction ?: ASC)


    override val series: StateFlow<List<KomgaSeries>>

    init {
        series = combine(
            conditionState.flatMapLatest { it?.seriesChangeFlow ?: flowOf(null) },
            pageSize,
            sort,
            sortDirection,
        ) { condition, pageSize, sort, sortDirection ->
            if (condition == null) null to KomgaPageRequest()
            else condition to KomgaPageRequest(size = pageSize, sort = toKomgaSort(sort, sortDirection))
        }
            .drop(1)
            .map { (condition, request) ->
                if (condition == null) emptyList()
                else getSeries(condition, request)
            }.stateIn(coroutineScope, SharingStarted.Eagerly, initialSeries ?: emptyList())
    }

    fun toSeriesCondition(): KomgaSearchCondition.SeriesCondition? {
        return this.conditionState.value?.toSeriesCondition()
    }

    fun getKomgaSort(): KomgaSort = toKomgaSort(this.sort.value, this.sortDirection.value)

    private fun toKomgaSort(sort: SeriesSort, direction: KomgaSort.Direction): KomgaSort {
        return when (sort) {
            SeriesSort.Title -> KomgaSort.KomgaSeriesSort.byTitle(direction)
            SeriesSort.CreatedDate -> KomgaSort.KomgaSeriesSort.byCreatedDate(direction)
            SeriesSort.LastModifiedDate -> KomgaSort.KomgaSeriesSort.byLastModifiedDate(direction)
            SeriesSort.ReleaseDate -> KomgaSort.KomgaSeriesSort.byReleaseDate(direction)
            SeriesSort.BookCount -> KomgaSort.KomgaSeriesSort.byBooksCount(direction)
            SeriesSort.Unsorted -> KomgaSort.Unsorted
        }
    }

    private fun toSeriesSort(sort: KomgaSort): SeriesSort {
        if (sort !is KomgaSort.KomgaBooksSort) return SeriesSort.Unsorted
        val komgaSort = sort.orders.firstOrNull() ?: return SeriesSort.Unsorted
        return when (komgaSort.property) {
            "metadata.titleSort" -> SeriesSort.Title
            "created" -> SeriesSort.CreatedDate
            "lastModified" -> SeriesSort.LastModifiedDate
            "booksMetadata.releaseDate" -> SeriesSort.ReleaseDate
            "booksCount" -> SeriesSort.BookCount
            else -> SeriesSort.Unsorted
        }
    }

    fun onSortChange(sort: SeriesSort) {
        this.sort.value = sort
    }

    fun onSortDirectionChange(direction: KomgaSort.Direction) {
        this.sortDirection.value = direction
    }

    fun onPagSizeChange(pageSize: Int) {
        this.pageSize.value = pageSize
    }

    fun removeCondition() {
        this.conditionState.value = null
    }

    fun addCondition(conditionType: SeriesConditionType) {
        this.conditionState.value = createCondition(conditionType)
    }

    fun changeConditionType(type: SeriesConditionType) {
        this.conditionState.value = createCondition(type)
    }

    private fun createCondition(type: SeriesConditionType): SeriesConditionState {
        return when (type) {
            SeriesConditionType.AnyOf -> SeriesMatchConditionState(
                type = MatchType.Any,
                options = options,
                seriesApi = seriesApi,
                collectionApi = collectionApi,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope,
            )

            SeriesConditionType.AllOf -> SeriesMatchConditionState(
                type = MatchType.All,
                options = options,
                seriesApi = seriesApi,
                collectionApi = collectionApi,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope,
            )

            SeriesConditionType.Tag -> TagConditionState(options.map { it.tags }, null)
            SeriesConditionType.ReadStatus -> ReadStatusConditionState(null)
            SeriesConditionType.Library -> LibraryConditionState(
                options.map { it.libraries },
                null
            )

            SeriesConditionType.Title -> TitleConditionState(null)
            SeriesConditionType.Author -> AuthorConditionState(
                options.map { it.authors },
                null
            )

            SeriesConditionType.Deleted -> DeletedConditionState()
            SeriesConditionType.ReleaseDate -> ReleaseDateConditionState(null)
            SeriesConditionType.Oneshot -> OneShotConditionState()
            SeriesConditionType.Collection -> CollectionIdConditionState(
                initial = null,
                collectionApi = collectionApi,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope,
            )

            SeriesConditionType.Complete -> CompleteConditionState()
            SeriesConditionType.Genre -> GenreConditionState(null, options.map { it.genres })
            SeriesConditionType.Language -> LanguageConditionState(null, options.map { it.languages })
            SeriesConditionType.Publisher -> PublisherConditionState(null, options.map { it.publishers })
            SeriesConditionType.Status -> SeriesStatusConditionState(null)
            SeriesConditionType.SharingLabel -> SharingLabelConditionState(null, options.map { it.sharingLabels })
            SeriesConditionType.TitleSort -> TitleSortConditionState(null)
            SeriesConditionType.AgeRating -> AgeRatingConditionState(null)
        }
    }


    private suspend fun getSeries(
        condition: KomgaSearchCondition.SeriesCondition?,
        page: KomgaPageRequest,
    ): List<KomgaSeries> {
        return appNotifications.runCatchingToNotifications {
            val search = KomgaSeriesSearch(condition)
            val seriesPage = seriesApi.getSeriesList(
                search,
                page
            )
            seriesPage.content
        }.getOrDefault(emptyList())
    }

    fun close() {
        coroutineScope.cancel()
    }
}

enum class SeriesSort {
    Title,
    CreatedDate,
    LastModifiedDate,
    ReleaseDate,
    BookCount,
    Unsorted,
}

class SeriesMatchConditionState(
    type: MatchType,
    private val options: StateFlow<FilterSuggestionOptions>,
    private val seriesApi: KomgaSeriesApi,
    private val collectionApi: KomgaCollectionsApi,
    private val appNotifications: AppNotifications,
    private val coroutineScope: CoroutineScope,
) : SeriesConditionState {
    val conditions = MutableStateFlow<List<SeriesConditionState>>(emptyList())
    val matchType = MutableStateFlow(type)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val seriesChangeFlow = conditions.flatMapLatest { conditions ->
        combine(conditions.map { it.seriesChangeFlow }) { conditions ->
            toSeriesCondition(conditions.filterNotNull().toList())
        }
    }

    constructor(
        initial: KomgaSearchCondition.AllOfSeries,
        options: StateFlow<FilterSuggestionOptions>,
        seriesApi: KomgaSeriesApi,
        collectionApi: KomgaCollectionsApi,
        appNotifications: AppNotifications,
        coroutineScope: CoroutineScope,
    ) : this(
        type = MatchType.All,
        options = options,
        seriesApi = seriesApi,
        collectionApi = collectionApi,
        appNotifications = appNotifications,
        coroutineScope = coroutineScope
    ) {
        conditions.value = initial.conditions.map {
            it.toSeriesConditionState(
                options = options,
                seriesApi = seriesApi,
                collectionApi = collectionApi,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope
            )
        }
    }

    constructor(
        initial: KomgaSearchCondition.AnyOfSeries,
        options: StateFlow<FilterSuggestionOptions>,
        seriesApi: KomgaSeriesApi,
        collectionApi: KomgaCollectionsApi,
        appNotifications: AppNotifications,
        coroutineScope: CoroutineScope,
    ) : this(
        type = MatchType.Any,
        options = options,
        seriesApi = seriesApi,
        collectionApi = collectionApi,
        appNotifications = appNotifications,
        coroutineScope = coroutineScope
    ) {
        conditions.value = initial.conditions.map {
            it.toSeriesConditionState(
                options = options,
                seriesApi = seriesApi,
                collectionApi = collectionApi,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope
            )
        }
    }

    override fun toSeriesCondition(): KomgaSearchCondition.SeriesCondition {
        return toSeriesCondition(conditions.value.mapNotNull { it.toSeriesCondition() })
    }

    fun removeCondition(condition: SeriesConditionState) {
        conditions.update { current ->
            val result = ArrayList(current)
            result.remove(condition)
            result
        }
    }

    fun addCondition(conditionType: SeriesConditionType) {
        conditions.update { current -> current.plus(createConditionState(conditionType)) }
    }

    fun onConditionTypeChange(condition: SeriesConditionState, type: SeriesConditionType) {
        val newCondition = createConditionState(type)
        conditions.update { current ->
            val result = ArrayList(current)
            val index = result.indexOf(condition)
            result.removeAt(index)
            result.add(index, newCondition)
            result
        }

    }

    private fun createConditionState(type: SeriesConditionType): SeriesConditionState {
        return when (type) {
            SeriesConditionType.AnyOf -> SeriesMatchConditionState(
                type = MatchType.Any,
                options = options,
                seriesApi = seriesApi,
                collectionApi = collectionApi,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope
            )

            SeriesConditionType.AllOf -> SeriesMatchConditionState(
                type = MatchType.All,
                options = options,
                seriesApi = seriesApi,
                collectionApi = collectionApi,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope
            )

            SeriesConditionType.Tag -> TagConditionState(options.map { it.tags }, null)
            SeriesConditionType.Library -> LibraryConditionState(options.map { it.libraries }, null)
            SeriesConditionType.ReadStatus -> ReadStatusConditionState(null)
            SeriesConditionType.Title -> TitleConditionState(null)
            SeriesConditionType.Author -> AuthorConditionState(options.map { it.authors }, null)
            SeriesConditionType.Deleted -> DeletedConditionState()
            SeriesConditionType.ReleaseDate -> ReleaseDateConditionState(null)
            SeriesConditionType.Oneshot -> OneShotConditionState()

            SeriesConditionType.Collection -> CollectionIdConditionState(
                initial = null,
                collectionApi = collectionApi,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope
            )

            SeriesConditionType.Complete -> CompleteConditionState()
            SeriesConditionType.Genre -> GenreConditionState(null, options.map { it.genres })
            SeriesConditionType.Language -> LanguageConditionState(null, options.map { it.languages })
            SeriesConditionType.Publisher -> PublisherConditionState(null, options.map { it.publishers })
            SeriesConditionType.Status -> SeriesStatusConditionState(null)
            SeriesConditionType.SharingLabel -> SharingLabelConditionState(null, options.map { it.sharingLabels })
            SeriesConditionType.TitleSort -> TitleSortConditionState(null)
            SeriesConditionType.AgeRating -> AgeRatingConditionState(null)
        }
    }

    fun setMatchType(matchType: MatchType) {
        this.matchType.value = matchType
    }

    enum class SeriesConditionType {
        AnyOf,
        AllOf,
        AgeRating,
        Author,
        Collection,
        Complete,
        Deleted,
        Genre,
        Language,
        Library,
        Oneshot,
        Publisher,
        ReadStatus,
        ReleaseDate,
        SharingLabel,
        Status,
        Tag,
        Title,
        TitleSort,

    }


    private fun toSeriesCondition(conditions: List<KomgaSearchCondition.SeriesCondition>): KomgaSearchCondition.SeriesCondition {
        return when (matchType.value) {
            MatchType.Any -> KomgaSearchCondition.AnyOfSeries(conditions)
            MatchType.All -> KomgaSearchCondition.AllOfSeries(conditions)
        }

    }
}
