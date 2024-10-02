package io.github.snd_r.komelia.ui.series

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.library.SeriesScreenFilter
import io.github.snd_r.komelia.ui.series.list.SeriesListViewModel.SeriesSort
import kotlinx.coroutines.flow.StateFlow
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.referential.KomgaReferentialClient
import snd.komga.client.series.KomgaSeriesStatus

class SeriesFilterState(
    private val defaultSort: SeriesSort,
    private val library: StateFlow<KomgaLibrary?>,
    private val referentialClient: KomgaReferentialClient,
    private val appNotifications: AppNotifications,
    private val onChange: () -> Unit,
) {
    var isChanged by mutableStateOf(false)
        private set
    var searchTerm by mutableStateOf("")
        private set
    var sortOrder by mutableStateOf(defaultSort)
        private set
    var readStatus by mutableStateOf<List<KomgaReadStatus>>(emptyList())
        private set
    var publicationStatus by mutableStateOf<List<KomgaSeriesStatus>>(emptyList())
        private set
    var genres by mutableStateOf<List<String>>(emptyList())
        private set
    var genresOptions by mutableStateOf<List<String>>(emptyList())
        private set
    var tags by mutableStateOf<List<String>>(emptyList())
        private set
    var tagOptions by mutableStateOf<List<String>>(emptyList())
        private set
    var authors by mutableStateOf<List<KomgaAuthor>>(emptyList())
        private set
    var authorsOptions by mutableStateOf<List<KomgaAuthor>>(emptyList())
        private set
    var releaseDates by mutableStateOf<List<String>>(emptyList())
        private set
    var releaseDateOptions by mutableStateOf<List<String>>(emptyList())
        private set
    var ageRatings by mutableStateOf<List<String>>(emptyList())
        private set
    var ageRatingsOptions by mutableStateOf<List<String>>(emptyList())
        private set
    var publishers by mutableStateOf<List<String>>(emptyList())
        private set
    var publishersOptions by mutableStateOf<List<String>>(emptyList())
        private set
    var languages by mutableStateOf<List<String>>(emptyList())
        private set
    var languagesOptions by mutableStateOf<List<String>>(emptyList())
        private set

    var complete by mutableStateOf(Completion.ANY)
        private set

    var oneshot by mutableStateOf(Format.ANY)
        private set


    suspend fun initialize() {
        appNotifications.runCatchingToNotifications {
            genresOptions = referentialClient.getGenres(libraryId = library.value?.id)
            tagOptions = referentialClient.getSeriesTags(libraryId = library.value?.id)
            releaseDateOptions = referentialClient.getSeriesReleaseDates(libraryId = library.value?.id)
            ageRatingsOptions = referentialClient.getAgeRatings(libraryId = library.value?.id)
            publishersOptions = referentialClient.getPublishers(libraryId = library.value?.id)
            languagesOptions = referentialClient.getLanguages(libraryId = library.value?.id)
        }
    }

    fun applyFilter(filter: SeriesScreenFilter) {
        publicationStatus = filter.publicationStatus ?: publicationStatus
        ageRatings = filter.ageRating?.map { it.toString() } ?: ageRatings
        languages = filter.language ?: languages
        publishers = filter.publisher ?: publishers
        genres = filter.genres ?: genres
        tags = filter.tags ?: tags
        authors = filter.authors ?: authors
        markChanges()
    }

    fun onSortOrderChange(sortOrder: SeriesSort) {
        this.sortOrder = sortOrder
        markChanges()
        onChange()
    }

    fun onSearchTermChange(searchTerm: String) {
        if (this.searchTerm == searchTerm) return

        this.searchTerm = searchTerm
        markChanges()
        onChange()
    }

    fun onReadStatusSelect(readStatus: KomgaReadStatus) {
        if (this.readStatus.contains(readStatus)) {
            this.readStatus = this.readStatus.minus(readStatus)
        } else {
            this.readStatus = this.readStatus.plus(readStatus)
        }
        markChanges()
        onChange()
    }

    fun onPublicationStatusSelect(publicationStatus: KomgaSeriesStatus) {
        if (this.publicationStatus.contains(publicationStatus)) {
            this.publicationStatus = this.publicationStatus.minus(publicationStatus)
        } else {
            this.publicationStatus = this.publicationStatus.plus(publicationStatus)
        }

        markChanges()
        onChange()
    }


    fun onGenreSelect(genre: String) {
        this.genres =
            if (genres.contains(genre)) genres.minus(genre)
            else genres.plus(genre)
        markChanges()
        onChange()
    }

    fun onTagSelect(tag: String) {
        tags =
            if (tags.contains(tag)) tags.minus(tag)
            else tags.plus(tag)
        markChanges()
        onChange()
    }

    suspend fun onAuthorsSearch(search: String) {
        if (search.isBlank()) this.authorsOptions = emptyList()
        else this.authorsOptions = referentialClient.getAuthors(search).content
    }

    fun onAuthorSelect(author: KomgaAuthor) {
        val authorsByName = authorsOptions.filter { it.name == author.name }
        authors =
            if (authors.contains(author)) authors.filter { it.name != author.name }
            else authors.plus(authorsByName)

        markChanges()
        onChange()
    }

    fun onAgeRatingSelect(ageRating: String) {
        ageRatings = if (ageRatings.contains(ageRating)) ageRatings.minus(ageRating)
        else ageRatings.plus(ageRating)

        markChanges()
        onChange()
    }

    fun onPublisherSelect(publisher: String) {
        publishers = if (publishers.contains(publisher)) publishers.minus(publisher)
        else publishers.plus(publisher)

        markChanges()
        onChange()
    }

    fun onLanguageSelect(language: String) {
        languages = if (languages.contains(language)) languages.minus(language)
        else languages.plus(language)

        markChanges()
        onChange()
    }

    fun onReleaseDateSelect(releaseDate: String) {
        releaseDates = if (releaseDates.contains(releaseDate)) releaseDates.minus(releaseDate)
        else releaseDates.plus(releaseDate)

        markChanges()
        onChange()
    }

    fun onCompletionToggle() {
        complete = when (complete) {
            Completion.ANY -> Completion.COMPLETE
            Completion.COMPLETE -> Completion.INCOMPLETE
            Completion.INCOMPLETE -> Completion.ANY
        }
        markChanges()
        onChange()
    }

    fun onFormatToggle() {
        oneshot = when (oneshot) {
            Format.ANY -> Format.ONESHOT
            Format.ONESHOT -> Format.NOT_ONESHOT
            Format.NOT_ONESHOT -> Format.ANY
        }
        markChanges()
        onChange()
    }

    fun reset() {
        searchTerm = ""
        sortOrder = defaultSort
        readStatus = emptyList()
        publicationStatus = emptyList()
        authors = emptyList()
        resetTagFilters()
        releaseDates = emptyList()
        ageRatings = emptyList()
        ageRatings = emptyList()
        publishers = emptyList()
        languages = emptyList()
        complete = Completion.ANY
        oneshot = Format.ANY

        isChanged = false
        onChange()
    }

    fun resetTagFilters() {
        genres = emptyList()
        tags = emptyList()
    }

    private fun markChanges() {
        val hasDefaultValues = searchTerm.isBlank() &&
                sortOrder == defaultSort &&
                readStatus.isEmpty() &&
                publicationStatus.isEmpty() &&
                genres.isEmpty() &&
                tags.isEmpty() &&
                authors.isEmpty() &&
                releaseDates.isEmpty() &&
                ageRatings.isEmpty() &&
                publishers.isEmpty() &&
                languages.isEmpty() &&
                complete == Completion.ANY &&
                oneshot == Format.ANY

        isChanged = !hasDefaultValues
    }

    enum class Completion {
        ANY, COMPLETE, INCOMPLETE
    }

    enum class Format {
        ANY, ONESHOT, NOT_ONESHOT
    }
}
