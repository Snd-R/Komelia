package io.github.snd_r.komelia.ui.home.edit

import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.home.BooleanOpState
import io.github.snd_r.komelia.ui.home.DateOpState
import io.github.snd_r.komelia.ui.home.EqualityNullableOpState
import io.github.snd_r.komelia.ui.home.EqualityOpState
import io.github.snd_r.komelia.ui.home.NumericNullableOpState
import io.github.snd_r.komelia.ui.home.NumericOpState
import io.github.snd_r.komelia.ui.home.StringOpState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaMediaStatus
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.book.MediaProfile
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.collection.KomgaCollectionClient
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.search.KomgaSearchCondition
import snd.komga.client.search.KomgaSearchCondition.AuthorMatch
import snd.komga.client.search.KomgaSearchCondition.BookCondition
import snd.komga.client.search.KomgaSearchCondition.PosterMatch
import snd.komga.client.search.KomgaSearchCondition.SeriesCondition
import snd.komga.client.search.KomgaSearchOperator
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.series.KomgaSeriesSearch
import snd.komga.client.series.KomgaSeriesStatus


sealed interface BookConditionState {
    val bookChangeFlow: Flow<BookCondition?>
    fun toBookCondition(): BookCondition?
}

sealed interface SeriesConditionState {
    val seriesChangeFlow: Flow<KomgaSearchCondition.SeriesCondition?>
    fun toSeriesCondition(): SeriesCondition?
}


class LibraryConditionState(
    val libraries: Flow<List<KomgaLibrary>>,
    val initial: KomgaSearchCondition.LibraryId?
) : EqualityOpState<KomgaLibraryId>(initial?.operator),
    SeriesConditionState,
    BookConditionState {
    override val bookChangeFlow = combine(this.operator, this.value) { this.toBookCondition() }
    override val seriesChangeFlow = combine(this.operator, this.value) { this.toSeriesCondition() }

    override fun toSeriesCondition(): SeriesCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.LibraryId(operator)
    }

    override fun toBookCondition(): BookCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.LibraryId(operator)
    }
}

@OptIn(FlowPreview::class)
class CollectionIdConditionState(
    val initial: KomgaSearchCondition.CollectionId?,
    private val collectionClient: KomgaCollectionClient,
    private val appNotifications: AppNotifications,
    coroutineScope: CoroutineScope,
) : EqualityOpState<KomgaCollectionId>(initial?.operator),
    SeriesConditionState {
    override val seriesChangeFlow = combine(this.operator, this.value) { this.toSeriesCondition() }
    private val currentCollection = MutableStateFlow<KomgaCollection?>(null)
    val collectionsSuggestions = MutableStateFlow<List<KomgaCollection>>(emptyList())
    val searchText = MutableStateFlow("")

    init {
        searchText
            .debounce { if (it.isBlank()) 0 else 500 }
            .distinctUntilChanged()
            .onEach { handleQuery(it) }
            .launchIn(coroutineScope)

        initial?.let { seriesCondition ->
            coroutineScope.launch {
                appNotifications.runCatchingToNotifications {
                    val collectionId = when (val operator = seriesCondition.operator) {
                        is KomgaSearchOperator.Is -> operator.value
                        is KomgaSearchOperator.IsNot -> operator.value
                    }
                    val collection = collectionClient.getOne(collectionId)
                    currentCollection.value = collection
                    searchText.value = collection.name
                }
            }
        }
    }

    fun onSearchTextChange(text: String) {
        searchText.value = text
    }

    fun onCollectionSelect(collection: KomgaCollection) {
        this.currentCollection.value = collection
        this.searchText.value = collection.name
        this.value.value = collection.id
    }

    private suspend fun handleQuery(query: String) {
        appNotifications.runCatchingToNotifications {
            if (query.isBlank()) collectionsSuggestions.value = emptyList()
            else {
                val readLists = collectionClient.getAll(query)
                collectionsSuggestions.value = readLists.content
            }
        }
    }

    override fun toSeriesCondition(): SeriesCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.CollectionId(operator)
    }
}

@OptIn(FlowPreview::class)
class ReadListIdConditionState(
    val initial: KomgaSearchCondition.ReadListId?,
    private val readListClient: KomgaReadListClient,
    private val appNotifications: AppNotifications,
    coroutineScope: CoroutineScope,
) : EqualityOpState<KomgaReadListId>(initial?.operator),
    BookConditionState {
    override val bookChangeFlow = combine(this.operator, this.value) { this.toBookCondition() }
    private val currentReadList = MutableStateFlow<KomgaReadList?>(null)
    val readListSuggestions = MutableStateFlow<List<KomgaReadList>>(emptyList())
    val isLoading = MutableStateFlow(false)
    val searchText = MutableStateFlow("")

    init {
        searchText
            .debounce { if (it.isBlank()) 0 else 500 }
            .distinctUntilChanged()
            .onEach { handleQuery(it) }
            .launchIn(coroutineScope)

        initial?.let { seriesCondition ->
            coroutineScope.launch {
                appNotifications.runCatchingToNotifications {
                    val readListId = when (val operator = seriesCondition.operator) {
                        is KomgaSearchOperator.Is -> operator.value
                        is KomgaSearchOperator.IsNot -> operator.value
                    }
                    val readList = readListClient.getOne(readListId)
                    currentReadList.value = readList
                    searchText.value = readList.name
                }
            }
        }
    }

    fun onSearchTextChange(text: String) {
        searchText.value = text
    }

    fun onReadListSelect(readList: KomgaReadList) {
        this.currentReadList.value = readList
        this.searchText.value = readList.name
        this.value.value = readList.id
    }

    private suspend fun handleQuery(query: String) {
        appNotifications.runCatchingToNotifications {
            isLoading.value = true
            if (query.isBlank()) readListSuggestions.value = emptyList()
            else {
                val readLists = readListClient.getAll(
                    query
                )
                readListSuggestions.value = readLists.content
            }
            isLoading.value = false
        }.onFailure { isLoading.value = false }
    }

    override fun toBookCondition(): BookCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.ReadListId(operator)
    }
}

@OptIn(FlowPreview::class)
class SeriesIdConditionState(
    val initial: KomgaSearchCondition.SeriesId?,
    private val seriesClient: KomgaSeriesClient,
    private val appNotifications: AppNotifications,
    coroutineScope: CoroutineScope,
) : EqualityOpState<KomgaSeriesId>(initial?.operator),
    BookConditionState {
    override val bookChangeFlow = combine(this.operator, this.value) { this.toBookCondition() }
    private val currentSeries = MutableStateFlow<KomgaSeries?>(null)
    val seriesSuggestions = MutableStateFlow<List<KomgaSeries>>(emptyList())
    val isLoading = MutableStateFlow(false)
    val searchText = MutableStateFlow("")

    init {
        searchText
            .debounce { if (it.isBlank()) 0 else 500 }
            .distinctUntilChanged()
            .onEach { handleQuery(it) }
            .launchIn(coroutineScope)

        initial?.let { seriesCondition ->
            coroutineScope.launch {
                appNotifications.runCatchingToNotifications {
                    val seriesId = when (val operator = seriesCondition.operator) {
                        is KomgaSearchOperator.Is -> operator.value
                        is KomgaSearchOperator.IsNot -> operator.value
                    }
                    val series = seriesClient.getOneSeries(seriesId)
                    currentSeries.value = series
                    searchText.value = series.name
                }
            }
        }
    }

    fun onSearchTextChange(text: String) {
        searchText.value = text
    }

    fun onSeriesSelect(series: KomgaSeries) {
        this.currentSeries.value = series
        this.searchText.value = series.name
        this.value.value = series.id
    }

    private suspend fun handleQuery(query: String) {
        appNotifications.runCatchingToNotifications {
            isLoading.value = true
            if (query.isBlank()) seriesSuggestions.value = emptyList()
            else {
                val seriesPage = seriesClient.getSeriesList(
                    KomgaSeriesSearch(null, query)
                )
                seriesSuggestions.value = seriesPage.content
            }
            isLoading.value = false
        }.onFailure { isLoading.value = false }
    }

    override fun toBookCondition(): BookCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.SeriesId(operator)
    }
}

class DeletedConditionState(
) : BooleanOpState(), SeriesConditionState, BookConditionState {
    override val bookChangeFlow = combine(this.operator) { this.toBookCondition() }
    override val seriesChangeFlow = combine(this.operator) { this.toSeriesCondition() }
    override fun toSeriesCondition(): SeriesCondition {
        return KomgaSearchCondition.Deleted(toSearchOperator())
    }

    override fun toBookCondition(): BookCondition {
        return KomgaSearchCondition.Deleted(toSearchOperator())
    }
}

class CompleteConditionState() : BooleanOpState(), SeriesConditionState {
    override val seriesChangeFlow = combine(this.operator) { this.toSeriesCondition() }
    override fun toSeriesCondition(): SeriesCondition {
        return KomgaSearchCondition.Complete(toSearchOperator())
    }
}

class OneShotConditionState() : BooleanOpState(), BookConditionState, SeriesConditionState {
    override val seriesChangeFlow = combine(this.operator) { this.toSeriesCondition() }
    override val bookChangeFlow = combine(this.operator) { this.toBookCondition() }

    override fun toBookCondition(): BookCondition {
        return KomgaSearchCondition.OneShot(toSearchOperator())
    }

    override fun toSeriesCondition(): SeriesCondition {
        return KomgaSearchCondition.OneShot(toSearchOperator())
    }
}

class TitleConditionState(
    val initial: KomgaSearchCondition.Title?
) : StringOpState(), SeriesConditionState, BookConditionState {
    override val bookChangeFlow = combine(this.operator, this.value) { this.toBookCondition() }
    override val seriesChangeFlow = combine(this.operator, this.value) { this.toSeriesCondition() }
    override fun toSeriesCondition(): SeriesCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.Title(operator)
    }

    override fun toBookCondition(): BookCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.Title(operator)
    }
}

class TitleSortConditionState(
    val initial: KomgaSearchCondition.TitleSort?
) : StringOpState(initial?.operator), SeriesConditionState {
    override val seriesChangeFlow = combine(this.operator, this.value) { this.toSeriesCondition() }
    override fun toSeriesCondition(): SeriesCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.TitleSort(operator)
    }
}

class ReleaseDateConditionState(
    val initial: KomgaSearchCondition.ReleaseDate?
) : DateOpState(initial?.operator), SeriesConditionState, BookConditionState {
    override val bookChangeFlow = combine(this.operator, this.date, this.period) { this.toBookCondition() }
    override val seriesChangeFlow = combine(this.operator, this.date, this.period) { this.toSeriesCondition() }

    override fun toSeriesCondition(): SeriesCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.ReleaseDate(operator)
    }

    override fun toBookCondition(): BookCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.ReleaseDate(operator)
    }
}

class NumberSortConditionState(
    val initial: KomgaSearchCondition.NumberSort?
) : NumericOpState<Float>(initial?.operator), BookConditionState {
    override val bookChangeFlow = combine(this.operator, this.value) { this.toBookCondition() }
    override fun toBookCondition(): BookCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.NumberSort(operator)
    }
}

class TagConditionState(
    val tags: Flow<List<String>>,
    val initial: KomgaSearchCondition.Tag?
) : EqualityNullableOpState<String>(initial?.operator), SeriesConditionState, BookConditionState {
    override val bookChangeFlow = combine(this.operator, this.value) { this.toBookCondition() }
    override val seriesChangeFlow = combine(this.operator, this.value) { this.toSeriesCondition() }

    override fun toSeriesCondition(): SeriesCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.Tag(operator)
    }

    override fun toBookCondition(): BookCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.Tag(operator)
    }
}

class SharingLabelConditionState(
    initial: KomgaSearchCondition.SharingLabel?,
    val sharingLabels: Flow<List<String>>,
) : EqualityNullableOpState<String>(initial?.operator), SeriesConditionState {
    override val seriesChangeFlow = combine(this.operator, this.value) { this.toSeriesCondition() }
    override fun toSeriesCondition(): SeriesCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.SharingLabel(operator)
    }
}

class PublisherConditionState(
    initial: KomgaSearchCondition.Publisher?,
    val publishers: Flow<List<String>>,
) : EqualityOpState<String>(initial?.operator), SeriesConditionState {
    override val seriesChangeFlow = combine(this.operator, this.value) { this.toSeriesCondition() }
    override fun toSeriesCondition(): SeriesCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.Publisher(operator)
    }
}

class LanguageConditionState(
    initial: KomgaSearchCondition.Language?,
    val languages: Flow<List<String>>,
) : EqualityOpState<String>(initial?.operator), SeriesConditionState {
    override val seriesChangeFlow = combine(this.operator, this.value) { this.toSeriesCondition() }
    override fun toSeriesCondition(): SeriesCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.Language(operator)
    }
}

class GenreConditionState(
    initial: KomgaSearchCondition.Genre?,
    val genres: Flow<List<String>>
) : EqualityNullableOpState<String>(initial?.operator), SeriesConditionState {
    override val seriesChangeFlow = combine(this.operator, this.value) { this.toSeriesCondition() }
    override fun toSeriesCondition(): SeriesCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.Genre(operator)
    }
}

class AgeRatingConditionState(
    initial: KomgaSearchCondition.AgeRating?
) : NumericNullableOpState<Int>(initial?.operator), SeriesConditionState {
    override val seriesChangeFlow = combine(this.operator, this.value) { this.toSeriesCondition() }
    override fun toSeriesCondition(): SeriesCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.AgeRating(operator)
    }
}

class ReadStatusConditionState(
    val initial: KomgaSearchCondition.ReadStatus?
) : EqualityOpState<KomgaReadStatus>(initial?.operator),
    SeriesConditionState,
    BookConditionState {
    override val bookChangeFlow = combine(this.operator, this.value) { this.toBookCondition() }
    override val seriesChangeFlow = combine(this.operator, this.value) { this.toSeriesCondition() }
    override fun toSeriesCondition(): SeriesCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.ReadStatus(operator)
    }

    override fun toBookCondition(): BookCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.ReadStatus(operator)
    }
}

class MediaStatusConditionState(
    initial: KomgaSearchCondition.MediaStatus?
) : EqualityOpState<KomgaMediaStatus>(initial?.operator), BookConditionState {
    override val bookChangeFlow = combine(this.operator, this.value) { this.toBookCondition() }
    override fun toBookCondition(): BookCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.MediaStatus(operator)
    }
}

class SeriesStatusConditionState(
    initial: KomgaSearchCondition.SeriesStatus?
) : EqualityOpState<KomgaSeriesStatus>(initial?.operator), SeriesConditionState {
    override val seriesChangeFlow = combine(this.operator, this.value) { this.toSeriesCondition() }
    override fun toSeriesCondition(): SeriesCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.SeriesStatus(operator)
    }
}

class AuthorConditionState(
    authorOptions: Flow<List<KomgaAuthor>>,
    initial: KomgaSearchCondition.Author?,
) : EqualityOpState<AuthorMatch>(initial?.operator), SeriesConditionState, BookConditionState {
    override val bookChangeFlow = combine(this.operator, this.value) { this.toBookCondition() }
    override val seriesChangeFlow = combine(this.operator, this.value) { this.toSeriesCondition() }

    val roleOptions = authorOptions.map { authors -> authors.map { it.role }.distinct() }
    val searchText = MutableStateFlow("")
    val nameOptions = combine(authorOptions, searchText, this.value) { options, search, currentValue ->
        val role = currentValue?.role
        val roleFiltered = if (role != null) options.filter { it.role == role } else options

        val searchFiltered = if (search.isNotBlank()) roleFiltered.filter { it.name.contains(search, true) }
        else roleFiltered

        searchFiltered.map { it.name }.distinct().take(50)
    }

    fun setRoleValue(role: String?) {
        this.value.update { it?.copy(role = role) ?: AuthorMatch(role = role) }
    }

    fun setNameValue(name: String?) {
        this.value.update { it?.copy(name = name) ?: AuthorMatch(name = name) }
    }

    fun setSearchText(search: String) {
        this.searchText.value = search
    }

    override fun toSeriesCondition(): SeriesCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.Author(operator)
    }

    override fun toBookCondition(): BookCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.Author(operator)
    }
}

class MediaProfileConditionState(
    initial: KomgaSearchCondition.MediaProfile?
) : EqualityOpState<MediaProfile>(initial?.operator), BookConditionState {
    override val bookChangeFlow = combine(this.operator, this.value) { this.toBookCondition() }
    override fun toBookCondition(): BookCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.MediaProfile(operator)
    }
}

class PosterConditionState(
    initial: KomgaSearchCondition.Poster?
) : EqualityOpState<PosterMatch>(initial?.operator),
    BookConditionState {
    override val bookChangeFlow = combine(this.operator, this.value) { this.toBookCondition() }

    fun setType(type: PosterMatch.Type?) {
        this.value.update { it?.copy(type = type) ?: PosterMatch(type = type) }
    }

    fun setSelected(selected: Boolean?) {
        this.value.update { it?.copy(selected = selected) ?: PosterMatch(selected = selected) }
    }

    override fun toBookCondition(): BookCondition? {
        val operator = this.toSearchOperator() ?: return null
        return KomgaSearchCondition.Poster(operator)
    }
}


enum class MatchType() { Any, All }

fun BookCondition.toBookConditionState(
    options: StateFlow<FilterSuggestionOptions>,
    bookClient: KomgaBookClient,
    seriesClient: KomgaSeriesClient,
    readListClient: KomgaReadListClient,
    appNotifications: AppNotifications,
    coroutineScope: CoroutineScope
): BookConditionState {
    return when (this) {
        is KomgaSearchCondition.AllOfBook -> BookMatchConditionState(
            initial = this,
            options = options,
            bookClient = bookClient,
            seriesClient = seriesClient,
            readListClient = readListClient,
            appNotifications = appNotifications,
            coroutineScope = coroutineScope
        )

        is KomgaSearchCondition.AnyOfBook -> BookMatchConditionState(
            initial = this,
            options = options,
            bookClient = bookClient,
            seriesClient = seriesClient,
            readListClient = readListClient,
            appNotifications = appNotifications,
            coroutineScope = coroutineScope
        )

        KomgaSearchCondition.NoopCondition -> BookMatchConditionState(
            type = MatchType.Any,
            options = options,
            bookClient = bookClient,
            seriesClient = seriesClient,
            readListClient = readListClient,
            appNotifications = appNotifications,
            coroutineScope = coroutineScope
        )

        is KomgaSearchCondition.Tag -> TagConditionState(options.map { it.tags }, this)
        is KomgaSearchCondition.LibraryId -> LibraryConditionState(options.map { it.libraries }, this)
        is KomgaSearchCondition.ReadStatus -> ReadStatusConditionState(this)
        is KomgaSearchCondition.Author -> AuthorConditionState(options.map { it.authors }, this)
        is KomgaSearchCondition.Deleted -> DeletedConditionState()
        is KomgaSearchCondition.MediaProfile -> MediaProfileConditionState(this)
        is KomgaSearchCondition.MediaStatus -> MediaStatusConditionState(this)
        is KomgaSearchCondition.NumberSort -> NumberSortConditionState(this)
        is KomgaSearchCondition.Poster -> PosterConditionState(this)
        is KomgaSearchCondition.ReadListId -> ReadListIdConditionState(
            initial = this,
            readListClient = readListClient,
            appNotifications = appNotifications,
            coroutineScope = coroutineScope
        )

        is KomgaSearchCondition.ReleaseDate -> ReleaseDateConditionState(this)
        is KomgaSearchCondition.SeriesId -> SeriesIdConditionState(
            initial = this,
            seriesClient = seriesClient,
            appNotifications = appNotifications,
            coroutineScope = coroutineScope
        )

        is KomgaSearchCondition.Title -> TitleConditionState(this)
        is KomgaSearchCondition.OneShot -> OneShotConditionState()
    }
}

fun SeriesCondition.toSeriesConditionState(
    options: StateFlow<FilterSuggestionOptions>,
    seriesClient: KomgaSeriesClient,
    collectionClient: KomgaCollectionClient,
    appNotifications: AppNotifications,
    coroutineScope: CoroutineScope
): SeriesConditionState {
    return when (this) {
        is KomgaSearchCondition.AllOfSeries -> SeriesMatchConditionState(
            initial = this,
            options = options,
            seriesClient = seriesClient,
            collectionClient = collectionClient,
            appNotifications = appNotifications,
            coroutineScope = coroutineScope
        )

        is KomgaSearchCondition.AnyOfSeries -> SeriesMatchConditionState(
            initial = this,
            options = options,
            seriesClient = seriesClient,
            collectionClient = collectionClient,
            appNotifications = appNotifications,
            coroutineScope = coroutineScope
        )

        KomgaSearchCondition.NoopCondition -> SeriesMatchConditionState(
            type = MatchType.Any,
            options = options,
            seriesClient = seriesClient,
            collectionClient = collectionClient,
            appNotifications = appNotifications,
            coroutineScope = coroutineScope
        )

        is KomgaSearchCondition.Tag -> TagConditionState(options.map { it.tags }, this)
        is KomgaSearchCondition.LibraryId -> LibraryConditionState(options.map { it.libraries }, this)
        is KomgaSearchCondition.ReadStatus -> ReadStatusConditionState(this)
        is KomgaSearchCondition.Author -> AuthorConditionState(options.map { it.authors }, this)
        is KomgaSearchCondition.Deleted -> DeletedConditionState()
        is KomgaSearchCondition.ReleaseDate -> ReleaseDateConditionState(this)
        is KomgaSearchCondition.Title -> TitleConditionState(this)
        is KomgaSearchCondition.OneShot -> OneShotConditionState()
        is KomgaSearchCondition.AgeRating -> AgeRatingConditionState(this)
        is KomgaSearchCondition.CollectionId -> CollectionIdConditionState(
            initial = this,
            collectionClient = collectionClient,
            appNotifications = appNotifications,
            coroutineScope = coroutineScope
        )

        is KomgaSearchCondition.Complete -> CompleteConditionState()
        is KomgaSearchCondition.Genre -> GenreConditionState(this, options.map { it.genres })
        is KomgaSearchCondition.Language -> LanguageConditionState(this, options.map { it.languages })
        is KomgaSearchCondition.Publisher -> PublisherConditionState(this, options.map { it.publishers })
        is KomgaSearchCondition.SeriesStatus -> SeriesStatusConditionState(this)
        is KomgaSearchCondition.SharingLabel -> SharingLabelConditionState(this, options.map { it.sharingLabels })
        is KomgaSearchCondition.TitleSort -> TitleSortConditionState(this)
    }
}

data class FilterSuggestionOptions(
    val tags: List<String>,
    val genres: List<String>,
    val authors: List<KomgaAuthor>,
    val publishers: List<String>,
    val languages: List<String>,
    val libraries: List<KomgaLibrary>,
    val sharingLabels: List<String>,
)