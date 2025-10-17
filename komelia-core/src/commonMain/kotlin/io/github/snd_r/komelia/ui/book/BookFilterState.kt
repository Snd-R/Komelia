package io.github.snd_r.komelia.ui.book

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.book.BooksFilterState.BooksSort
import io.github.snd_r.komelia.ui.series.SeriesFilter.Companion.DEFAULT
import io.github.snd_r.komelia.ui.series.SeriesFilterState.TagExclusionMode
import io.github.snd_r.komelia.ui.series.SeriesFilterState.TagInclusionMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaSort.KomgaBooksSort
import snd.komga.client.referential.KomgaReferentialClient
import snd.komga.client.search.BookConditionBuilder
import snd.komga.client.search.KomgaSearchCondition
import snd.komga.client.series.KomgaSeries

data class BookFilter(
    val sortOrder: BooksSort = BooksSort.NUMBER_ASC,
    val readStatus: List<KomgaReadStatus> = emptyList(),
    val includeTags: List<String> = emptyList(),
    val excludeTags: List<String> = emptyList(),
    val inclusionMode: TagInclusionMode = TagInclusionMode.INCLUDE_IF_ALL_MATCH,
    val exclusionMode: TagExclusionMode = TagExclusionMode.EXCLUDE_IF_ANY_MATCH,
    val authors: List<KomgaAuthor> = emptyList()
) {
    companion object {
        val DEFAULT = BookFilter()
    }

    fun addConditionTo(builder: BookConditionBuilder) {

        if (readStatus.isNotEmpty()) {
            builder.anyOf {
                readStatus.forEach { readStatus { isEqualTo(it) } }
            }
        }

        if (includeTags.isNotEmpty()) {
            when (inclusionMode) {
                TagInclusionMode.INCLUDE_IF_ALL_MATCH -> builder.allOf {
                    includeTags.forEach { tag { isEqualTo(it) } }
                }

                TagInclusionMode.INCLUDE_IF_ANY_MATCH -> builder.anyOf {
                    includeTags.forEach { tag { isEqualTo(it) } }
                }
            }
        }
        if (excludeTags.isNotEmpty()) {
            when (exclusionMode) {
                TagExclusionMode.EXCLUDE_IF_ANY_MATCH -> builder.allOf {
                    excludeTags.forEach { tag { isNotEqualTo(it) } }
                }

                TagExclusionMode.EXCLUDE_IF_ALL_MATCH -> builder.anyOf {
                    excludeTags.forEach { tag { isNotEqualTo(it) } }
                }
            }
        }

        authors.forEach {
            builder.author { isEqualTo(KomgaSearchCondition.AuthorMatch(it.name, null)) }
        }
    }
}

class BooksFilterState(
    private val series: StateFlow<KomgaSeries?>,
    private val referentialClient: KomgaReferentialClient,
    private val appNotifications: AppNotifications,
) {

    private val mutableFilterState = MutableStateFlow(BookFilter())
    val state = mutableFilterState.asStateFlow()

    var isChanged by mutableStateOf(false)
        private set
    var tagOptions by mutableStateOf<List<String>>(emptyList())
        private set
    var authorsOptions by mutableStateOf<List<KomgaAuthor>>(emptyList())
        private set

    suspend fun initialize() {

        appNotifications.runCatchingToNotifications {
            val series = series.filterNotNull().first()
            tagOptions = referentialClient.getBookTags(seriesId = series.id)
            authorsOptions = referentialClient
                .getAuthors(seriesId = series.id, pageRequest = KomgaPageRequest(unpaged = true)).content
                .distinctBy { it.name }
        }
    }

    fun onSortOrderChange(sortOrder: BooksSort) {
        mutableFilterState.update { current ->
            current.copy(sortOrder = sortOrder)
        }
        checkIfAllDefault()
    }

    fun onReadStatusSelect(readStatus: KomgaReadStatus) {
        mutableFilterState.update { current ->
            current.copy(
                readStatus = if (current.readStatus.contains(readStatus)) {
                    current.readStatus.minus(readStatus)
                } else {
                    current.readStatus.plus(readStatus)
                }
            )

        }
        checkIfAllDefault()
    }

    fun onAuthorSelect(author: KomgaAuthor) {
        mutableFilterState.update { current ->
            val authorsByName = authorsOptions.filter { it.name == author.name }
            current.copy(
                authors =
                    if (current.authors.contains(author)) current.authors.filter { it.name != author.name }
                    else current.authors.plus(authorsByName)
            )
        }

        checkIfAllDefault()
    }

    fun onTagSelect(tag: String) {
        mutableFilterState.update { current ->
            if (current.includeTags.contains(tag)) {
                current.copy(
                    includeTags = current.includeTags.minus(tag),
                    excludeTags = current.excludeTags.plus(tag)
                )
            } else if (current.excludeTags.contains(tag)) {
                current.copy(
                    excludeTags = current.excludeTags.minus(tag)
                )
            } else current.copy(
                includeTags = current.includeTags.plus(tag)
            )
        }
        checkIfAllDefault()
    }

    fun onInclusionModeChange(mode: TagInclusionMode) {
        mutableFilterState.update { current -> current.copy(inclusionMode = mode) }
        checkIfAllDefault()
    }

    fun onExclusionModeChange(mode: TagExclusionMode) {
        mutableFilterState.update { current -> current.copy(exclusionMode = mode) }
        checkIfAllDefault()
    }

    fun resetTagFilters() {
        mutableFilterState.update {
            it.copy(
                includeTags = DEFAULT.includeTags,
                excludeTags = DEFAULT.excludeTags,
                inclusionMode = DEFAULT.inclusionMode,
                exclusionMode = DEFAULT.exclusionMode
            )
        }
        checkIfAllDefault()
    }

    private fun checkIfAllDefault() {
        isChanged = state.value != DEFAULT
    }

    enum class BooksSort(val komgaSort: KomgaBooksSort) {
        NUMBER_ASC(KomgaBooksSort.byNumberAsc()),
        NUMBER_DESC(KomgaBooksSort.byNumberDesc()),
//        FILENAME_ASC(KomgaBooksSort.byFileNameAsc()),
//        FILENAME_DESC(KomgaBooksSort.byFileNameDesc()),
//        RELEASE_DATE_ASC(KomgaBooksSort.byReleaseDateAsc()),
//        RELEASE_DATE_DESC(KomgaBooksSort.byReleaseDateDesc()),
    }
}
