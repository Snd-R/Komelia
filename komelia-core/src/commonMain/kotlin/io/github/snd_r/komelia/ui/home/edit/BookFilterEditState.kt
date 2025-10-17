package io.github.snd_r.komelia.ui.home.edit

import androidx.compose.ui.unit.Dp
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.home.BooksHomeScreenFilter
import io.github.snd_r.komelia.ui.home.HomeScreenFilter
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
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookSearch
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaSort
import snd.komga.client.common.KomgaSort.Direction.ASC
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.search.KomgaSearchCondition
import snd.komga.client.series.KomgaSeriesClient

class BookFilterEditState(
    private val bookClient: KomgaBookClient,
    private val seriesClient: KomgaSeriesClient,
    private val readListClient: KomgaReadListClient,
    private val appNotifications: AppNotifications,
    private val coroutineScope: CoroutineScope,
    private val options: StateFlow<FilterSuggestionOptions>,
    val cardWidth: StateFlow<Dp>,
    initialFilter: BooksHomeScreenFilter?,
    initialBooks: List<KomgaBook>?,
) : FilterEditState {
    override val label = MutableStateFlow(initialFilter?.label ?: "Book Filter")

    val filter: MutableStateFlow<BookFilterStateType> = MutableStateFlow(
        initialFilter?.let { initial ->
            when (initial) {
                is BooksHomeScreenFilter.CustomFilter -> BookCustomFilterState(
                    bookClient = bookClient,
                    seriesClient = seriesClient,
                    readListClient = readListClient,
                    appNotifications = appNotifications,
                    options = options,
                    coroutineScope = coroutineScope,
                    initial = initial,
                    initialBooks = initialBooks,
                    initialPage = initial.pageRequest,
                )

                is BooksHomeScreenFilter.OnDeck -> BookOnDeckFilterState(
                    bookClient = bookClient,
                    appNotifications = appNotifications,
                    coroutineScope = coroutineScope,
                    initial = initial,
                    initialBooks = initialBooks,
                )
            }
        } ?: BookCustomFilterState(
            seriesClient = seriesClient,
            bookClient = bookClient,
            readListClient = readListClient,
            appNotifications = appNotifications,
            options = options,
            coroutineScope = coroutineScope,
            initial = null,
            initialBooks = null,
            null
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val books: StateFlow<List<KomgaBook>> = filter.flatMapLatest { it.books }
        .stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())

    val type = filter.map {
        when (it) {
            is BookCustomFilterState -> FilterType.Custom
            is BookOnDeckFilterState -> FilterType.OnDeck
        }
    }.stateIn(coroutineScope, SharingStarted.Eagerly, FilterType.Custom)

    override fun toFilter(order: Int): HomeScreenFilter {
        return when (val editState = filter.value) {
            is BookCustomFilterState -> BooksHomeScreenFilter.CustomFilter(
                order = order,
                label = label.value,
                filter = editState.toBookCondition(),
                textSearch = null,
                pageRequest = KomgaPageRequest(
                    size = editState.pageSize.value,
                    sort = editState.getKomgaSort()
                )
            )

            is BookOnDeckFilterState -> BooksHomeScreenFilter.OnDeck(
                order = order,
                label = label.value,
                pageSize = editState.pageSize.value
            )
        }
    }

    fun onTypeChange(type: FilterType) {
        filter.value = when (type) {
            FilterType.Custom -> BookCustomFilterState(
                seriesClient = seriesClient,
                bookClient = bookClient,
                readListClient = readListClient,
                appNotifications = appNotifications,
                options = options,
                coroutineScope = coroutineScope,
                initial = null,
                initialBooks = null,
                initialPage = null
            )

            FilterType.OnDeck -> BookOnDeckFilterState(
                bookClient = bookClient,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope,
                initial = null,
                initialBooks = null
            )
        }
    }

    enum class FilterType { Custom, OnDeck }
}

sealed interface BookFilterStateType {
    val books: StateFlow<List<KomgaBook>>
}

class BookOnDeckFilterState(
    private val bookClient: KomgaBookClient,
    private val appNotifications: AppNotifications,
    private val coroutineScope: CoroutineScope,
    initial: BooksHomeScreenFilter.OnDeck?,
    initialBooks: List<KomgaBook>?,
) : BookFilterStateType {
    val libraryIds = MutableStateFlow<List<KomgaLibraryId>>(emptyList())
    val pageSize = MutableStateFlow(initial?.pageSize ?: 20)
    override val books = MutableStateFlow(initialBooks ?: emptyList())

    init {
        val combined = combine(
            libraryIds,
            pageSize
        ) { libraryId, page -> libraryId to page }

        val dropped = if (initial != null) combined.drop(1) else combined
        dropped.onEach { (libraryId, page) ->
            books.value = getBooks(libraryId, page)
        }.launchIn(coroutineScope)
    }


    fun onPageSizeChange(pageSize: Int) {
        this.pageSize.value = pageSize
    }

    private suspend fun getBooks(libraryIds: List<KomgaLibraryId>, pageSize: Int): List<KomgaBook> {
        return appNotifications.runCatchingToNotifications {
            val page = KomgaPageRequest(size = pageSize)
            bookClient.getBooksOnDeck(libraryIds, page).content
        }.getOrDefault(emptyList())
    }

    fun close() {
        coroutineScope.cancel()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class BookCustomFilterState(
    private val seriesClient: KomgaSeriesClient,
    private val bookClient: KomgaBookClient,
    private val readListClient: KomgaReadListClient,
    private val appNotifications: AppNotifications,
    private val options: StateFlow<FilterSuggestionOptions>,
    private val coroutineScope: CoroutineScope,
    initial: BooksHomeScreenFilter.CustomFilter?,
    initialBooks: List<KomgaBook>?,
    initialPage: KomgaPageRequest?,
) : BookFilterStateType {
    val conditionState: MutableStateFlow<BookConditionState?> = MutableStateFlow(
        initial?.filter?.toBookConditionState(
            options = options,
            bookClient = bookClient,
            seriesClient = seriesClient,
            readListClient = readListClient,
            appNotifications = appNotifications,
            coroutineScope = coroutineScope

        )
    )
    val pageSize = MutableStateFlow(initialPage?.size ?: 20)
    val sort = MutableStateFlow(initialPage?.sort?.let { toBookSort(it) } ?: BookSort.Unsorted)
    val sortDirection = MutableStateFlow(initialPage?.sort?.orders?.firstOrNull()?.direction ?: ASC)


    override val books: StateFlow<List<KomgaBook>>

    init {
        books = combine(
            conditionState.flatMapLatest { it?.bookChangeFlow ?: flowOf(null) },
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
                else getBooks(condition, request)
            }.stateIn(coroutineScope, SharingStarted.Eagerly, initialBooks ?: emptyList())
    }

    fun toBookCondition(): KomgaSearchCondition.BookCondition? {
        return this.conditionState.value?.toBookCondition()
    }

    fun getKomgaSort(): KomgaSort = toKomgaSort(this.sort.value, this.sortDirection.value)
    private fun toKomgaSort(sort: BookSort, direction: KomgaSort.Direction): KomgaSort {
        return when (sort) {
            BookSort.Title -> KomgaSort.KomgaBooksSort.byTitle(direction)
            BookSort.CreatedDate -> KomgaSort.KomgaBooksSort.byCreatedDate(direction)
            BookSort.SeriesTitle -> KomgaSort.KomgaBooksSort.bySeriesTitle(direction)
            BookSort.PagesCount -> KomgaSort.KomgaBooksSort.byPagesCount(direction)
            BookSort.ReleaseDate -> KomgaSort.KomgaBooksSort.byReleaseDate(direction)
            BookSort.LastModified -> KomgaSort.KomgaBooksSort.byLastModifiedDate(direction)
            BookSort.Number -> KomgaSort.KomgaBooksSort.byNumber(direction)
            BookSort.ReadDate -> KomgaSort.KomgaBooksSort.byReadDate(direction)
            BookSort.Unsorted -> KomgaSort.Unsorted
        }
    }

    private fun toBookSort(sort: KomgaSort): BookSort {
        if (sort !is KomgaSort.KomgaBooksSort) return BookSort.Unsorted
        val komgaSort = sort.orders.firstOrNull() ?: return BookSort.Unsorted
        return when (komgaSort.property) {
            "metadata.title" -> BookSort.Title
            "series" -> BookSort.SeriesTitle
            "createdDate" -> BookSort.CreatedDate
            "metadata.pagesCount" -> BookSort.PagesCount
            "metadata.releaseDate" -> BookSort.ReleaseDate
            "lastModified" -> BookSort.LastModified
            "metadata.numberSort" -> BookSort.Number
            "readProgress.readDate" -> BookSort.ReadDate
            else -> BookSort.Unsorted
        }
    }

    fun onSortChange(sort: BookSort) {
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

    fun addCondition(conditionType: BookMatchConditionState.BookConditionType) {
        this.conditionState.value = createCondition(conditionType)
    }

    fun changeConditionType(type: BookMatchConditionState.BookConditionType) {
        this.conditionState.value = createCondition(type)
    }

    private fun createCondition(type: BookMatchConditionState.BookConditionType): BookConditionState {
        return when (type) {
            BookMatchConditionState.BookConditionType.AnyOf -> BookMatchConditionState(
                type = MatchType.Any,
                options = options,
                bookClient = bookClient,
                seriesClient = seriesClient,
                readListClient = readListClient,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope,
            )

            BookMatchConditionState.BookConditionType.AllOf -> BookMatchConditionState(
                type = MatchType.All,
                options = options,
                bookClient = bookClient,
                seriesClient = seriesClient,
                readListClient = readListClient,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope,
            )

            BookMatchConditionState.BookConditionType.Tag -> TagConditionState(options.map { it.tags }, null)
            BookMatchConditionState.BookConditionType.ReadStatus -> ReadStatusConditionState(null)
            BookMatchConditionState.BookConditionType.Library -> LibraryConditionState(
                options.map { it.libraries },
                null
            )

            BookMatchConditionState.BookConditionType.Title -> TitleConditionState(null)
            BookMatchConditionState.BookConditionType.Author -> AuthorConditionState(options.map { it.authors }, null)
            BookMatchConditionState.BookConditionType.Deleted -> DeletedConditionState()
            BookMatchConditionState.BookConditionType.ReleaseDate -> ReleaseDateConditionState(null)
            BookMatchConditionState.BookConditionType.MediaStatus -> MediaStatusConditionState(null)
            BookMatchConditionState.BookConditionType.MediaProfile -> MediaProfileConditionState(null)
            BookMatchConditionState.BookConditionType.Series -> SeriesIdConditionState(
                null,
                seriesClient = seriesClient,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope
            )

            BookMatchConditionState.BookConditionType.ReadList -> ReadListIdConditionState(
                null,
                readListClient = readListClient,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope
            )

            BookMatchConditionState.BookConditionType.NumberSort -> NumberSortConditionState(null)
            BookMatchConditionState.BookConditionType.Poster -> PosterConditionState(null)
            BookMatchConditionState.BookConditionType.Oneshot -> OneShotConditionState()
        }
    }


    private suspend fun getBooks(
        condition: KomgaSearchCondition.BookCondition?,
        page: KomgaPageRequest,
    ): List<KomgaBook> {
        return appNotifications.runCatchingToNotifications {
            val search = KomgaBookSearch(condition)
            val bookPage = bookClient.getBookList(
                search,
                page
            )
            bookPage.content
        }.getOrDefault(emptyList())
    }

    fun close() {
        coroutineScope.cancel()
    }
}

enum class BookSort {
    Title,
    CreatedDate,
    SeriesTitle,
    PagesCount,
    ReleaseDate,
    LastModified,
    Number,
    ReadDate,
    Unsorted,
}

class BookMatchConditionState(
    type: MatchType,
    private val options: StateFlow<FilterSuggestionOptions>,
    private val bookClient: KomgaBookClient,
    private val seriesClient: KomgaSeriesClient,
    private val readListClient: KomgaReadListClient,
    private val appNotifications: AppNotifications,
    private val coroutineScope: CoroutineScope,
) : BookConditionState {
    val conditions = MutableStateFlow<List<BookConditionState>>(emptyList())
    val matchType = MutableStateFlow(type)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val bookChangeFlow = conditions.flatMapLatest { conditions ->
        combine(conditions.map { it.bookChangeFlow }) { conditions ->
            toBookCondition(conditions.filterNotNull().toList())
        }
    }


    constructor(
        initial: KomgaSearchCondition.AllOfBook,
        options: StateFlow<FilterSuggestionOptions>,
        bookClient: KomgaBookClient,
        seriesClient: KomgaSeriesClient,
        readListClient: KomgaReadListClient,
        appNotifications: AppNotifications,
        coroutineScope: CoroutineScope,
    ) : this(
        type = MatchType.All,
        options = options,
        bookClient = bookClient,
        seriesClient = seriesClient,
        readListClient = readListClient,
        appNotifications = appNotifications,
        coroutineScope = coroutineScope
    ) {
        conditions.value = initial.conditions.map {
            it.toBookConditionState(
                options = options,
                seriesClient = seriesClient,
                bookClient = bookClient,
                readListClient = readListClient,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope
            )
        }
    }

    constructor(
        initial: KomgaSearchCondition.AnyOfBook,
        options: StateFlow<FilterSuggestionOptions>,
        bookClient: KomgaBookClient,
        seriesClient: KomgaSeriesClient,
        readListClient: KomgaReadListClient,
        appNotifications: AppNotifications,
        coroutineScope: CoroutineScope,
    ) : this(
        type = MatchType.Any,
        options = options,
        bookClient = bookClient,
        seriesClient = seriesClient,
        readListClient = readListClient,
        appNotifications = appNotifications,
        coroutineScope = coroutineScope
    ) {
        conditions.value = initial.conditions.map {
            it.toBookConditionState(
                options = options,
                seriesClient = seriesClient,
                bookClient = bookClient,
                readListClient = readListClient,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope
            )
        }
    }

    override fun toBookCondition(): KomgaSearchCondition.BookCondition {
        return toBookCondition(conditions.value.mapNotNull { it.toBookCondition() })
    }

    fun removeCondition(condition: BookConditionState) {
        conditions.update { current ->
            val result = ArrayList(current)
            result.remove(condition)
            result
        }
    }

    fun addCondition(conditionType: BookConditionType) {
        conditions.update { current -> current.plus(createConditionState(conditionType)) }
    }

    fun onConditionTypeChange(condition: BookConditionState, type: BookConditionType) {
        val newCondition = createConditionState(type)
        conditions.update { current ->
            val result = ArrayList(current)
            val index = result.indexOf(condition)
            result.removeAt(index)
            result.add(index, newCondition)
            result
        }

    }

    private fun createConditionState(type: BookConditionType): BookConditionState {
        return when (type) {
            BookConditionType.AnyOf -> BookMatchConditionState(
                type = MatchType.Any,
                options = options,
                bookClient = bookClient,
                seriesClient = seriesClient,
                readListClient = readListClient,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope
            )

            BookConditionType.AllOf -> BookMatchConditionState(
                type = MatchType.All,
                options = options,
                bookClient = bookClient,
                seriesClient = seriesClient,
                readListClient = readListClient,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope
            )

            BookConditionType.Tag -> TagConditionState(options.map { it.tags }, null)
            BookConditionType.Library -> LibraryConditionState(options.map { it.libraries }, null)
            BookConditionType.ReadStatus -> ReadStatusConditionState(null)
            BookConditionType.Title -> TitleConditionState(null)
            BookConditionType.Author -> AuthorConditionState(options.map { it.authors }, null)
            BookConditionType.Deleted -> DeletedConditionState()
            BookConditionType.ReleaseDate -> ReleaseDateConditionState(null)
            BookConditionType.MediaStatus -> MediaStatusConditionState(null)
            BookConditionType.MediaProfile -> MediaProfileConditionState(null)
            BookConditionType.Series -> SeriesIdConditionState(
                initial = null,
                seriesClient = seriesClient,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope
            )

            BookConditionType.ReadList -> ReadListIdConditionState(
                initial = null,
                readListClient = readListClient,
                appNotifications = appNotifications,
                coroutineScope = coroutineScope
            )

            BookConditionType.NumberSort -> NumberSortConditionState(null)
            BookConditionType.Poster -> PosterConditionState(null)
            BookConditionType.Oneshot -> OneShotConditionState()
        }
    }

    fun setMatchType(matchType: MatchType) {
        this.matchType.value = matchType
    }

    enum class BookConditionType {
        AnyOf,
        AllOf,
        Author,
        Deleted,
        Library,
        MediaProfile,
        MediaStatus,
        NumberSort,
        Oneshot,
        Poster,
        ReadList,
        ReadStatus,
        ReleaseDate,
        Series,
        Tag,
        Title,

    }


    private fun toBookCondition(conditions: List<KomgaSearchCondition.BookCondition>): KomgaSearchCondition.BookCondition {
        return when (matchType.value) {
            MatchType.Any -> KomgaSearchCondition.AnyOfBook(conditions)
            MatchType.All -> KomgaSearchCondition.AllOfBook(conditions)
        }

    }

//    private fun toBookCondition(): KomgaSearchCondition.BookCondition {
//        val conditions = conditions.value.mapNotNull { it.toBookCondition() }
//        return when (matchType.value) {
//            ANY -> KomgaSearchCondition.AnyOfBook(conditions)
//            ALL -> KomgaSearchCondition.AllOfBook(conditions)
//        }
//    }
}
