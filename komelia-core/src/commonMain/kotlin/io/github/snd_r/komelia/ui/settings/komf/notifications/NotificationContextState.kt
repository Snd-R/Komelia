package io.github.snd_r.komelia.ui.settings.komf.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import snd.komf.api.notifications.KomfAlternativeTitleContext
import snd.komf.api.notifications.KomfAuthorContext
import snd.komf.api.notifications.KomfBookContext
import snd.komf.api.notifications.KomfBookMetadataContext
import snd.komf.api.notifications.KomfLibraryContext
import snd.komf.api.notifications.KomfNotificationContext
import snd.komf.api.notifications.KomfSeriesContext
import snd.komf.api.notifications.KomfSeriesMetadataContext
import snd.komf.api.notifications.KomfWebLinkContext

class NotificationContextState {
    var libraryId by mutableStateOf("1")
    var libraryName by mutableStateOf("Test library")

    var seriesId by mutableStateOf("2")
    var seriesName by mutableStateOf("TestSeries")
    var seriesBookCount by mutableStateOf<Int?>(1)
    var seriesStatus by mutableStateOf("ONGOING")
    var seriesTitle by mutableStateOf("Series Title")
    var seriesTitleSort by mutableStateOf("Series Title")
    var seriesAlternativeTitles by mutableStateOf<List<AlternativeTitleContext>>(emptyList())
    var seriesSummary by mutableStateOf("Series Summary")
    var seriesReadingDirection by mutableStateOf("LEFT_TO_RIGHT")
    var seriesPublisher by mutableStateOf("Series Publisher")
    var seriesAlternativePublishers by mutableStateOf<List<String>>(emptyList())
    var seriesAgeRating by mutableStateOf<Int?>(18)
    var seriesLanguage by mutableStateOf("")
    var seriesGenres by mutableStateOf(listOf("genre1", "genre2"))
    var seriesTags by mutableStateOf(listOf("tag1", "tag2"))
    var seriesTotalBookCount by mutableStateOf<Int?>(2)
    var seriesAuthors by mutableStateOf(
        listOf(
            AuthorContext("Author1", "Writer"),
            AuthorContext("Author2", "Artist")
        )
    )
    var seriesReleaseYer by mutableStateOf<Int?>(1970)
    var seriesLinks by mutableStateOf(listOf(WebLinkContext("Example link", "http://example.com")))
    var books by mutableStateOf(listOf(BookContextState()))

    fun onSeriesAlternativeTitleAdd() {
        seriesAlternativeTitles += AlternativeTitleContext()

    }

    fun onSeriesAlternativeTitleDelete(title: AlternativeTitleContext) {
        seriesAlternativeTitles -= title
    }

    fun onSeriesAuthorAdd() {
        seriesAuthors += AuthorContext()

    }

    fun onSeriesAuthorDelete(author: AuthorContext) {
        seriesAuthors -= author
    }

    fun onSeriesLinkAdd() {
        seriesLinks += WebLinkContext()

    }

    fun onSeriesLinkDelete(link: WebLinkContext) {
        seriesLinks -= link
    }

    fun onBookAdd() {
        books += BookContextState()
    }

    fun onBookDelete(book: BookContextState) {
        books -= book
    }

     fun getKomfNotificationContext() = KomfNotificationContext(
        library = KomfLibraryContext(
            id = libraryId,
            name = libraryName
        ),
        series = KomfSeriesContext(
            id = seriesId,
            name = seriesName,
            bookCount = seriesBookCount ?: 0,
            metadata = KomfSeriesMetadataContext(
                status = seriesStatus,
                title = seriesTitle,
                titleSort = seriesTitleSort,
                alternativeTitles = seriesAlternativeTitles.map {
                    KomfAlternativeTitleContext(it.label, it.title)
                },
                summary = seriesSummary,
                readingDirection = seriesReadingDirection,
                publisher = seriesPublisher,
                alternativePublishers = seriesAlternativePublishers.toSet(),
                ageRating = seriesAgeRating,
                language = seriesLanguage,
                genres = seriesGenres,
                tags = seriesTags,
                totalBookCount = seriesTotalBookCount,
                authors = seriesAuthors.map { KomfAuthorContext(it.name, it.role) },
                releaseYear = seriesReleaseYer,
                links = seriesLinks.map { KomfWebLinkContext(it.label, it.url) }
            )
        ),
        books = books.map { book ->
            KomfBookContext(
                id = book.id,
                name = book.name,
                number = book.number,
                metadata = KomfBookMetadataContext(
                    title = book.title,
                    summary = book.summary,
                    number = book.metadataNumber,
                    numberSort = book.metadataNumberSort,
                    releaseDate = book.releaseDate,
                    authors = book.authors.map { KomfAuthorContext(it.name, it.role) },
                    tags = book.tags,
                    isbn = book.isbn,
                    links = book.links.map { KomfWebLinkContext(it.label, it.url) }
                )
            )
        }
    )

    class BookContextState {
        var id by mutableStateOf("3")
        var name by mutableStateOf("TestBook")
        var number by mutableStateOf(1)
        var title by mutableStateOf("Book Metadata Title")
        var summary by mutableStateOf("Book summary")
        var metadataNumber by mutableStateOf("1")
        var metadataNumberSort by mutableStateOf("1")
        var releaseDate by mutableStateOf("1970-01-01")
        var authors by mutableStateOf(
            listOf(
                AuthorContext("Author1", "Writer"),
                AuthorContext("Author2", "Artist")
            )
        )
        var tags by mutableStateOf(listOf("bookTag1", "bookTag2"))
        var isbn by mutableStateOf("9780000000000")
        var links by mutableStateOf<List<WebLinkContext>>(emptyList())

        fun onAuthorAdd() {
            authors += AuthorContext()

        }

        fun onAuthorDelete(author: AuthorContext) {
            authors -= author
        }

        fun onLinkAdd() {
            links += WebLinkContext()

        }

        fun onLinkDelete(link: WebLinkContext) {
            links -= link
        }
    }

    class AlternativeTitleContext(label: String = "En", title: String = "Alternative Title") {
        var label by mutableStateOf(label)
        var title by mutableStateOf(title)
    }

    class AuthorContext(name: String = "Author", role: String = "Author Role") {
        var name by mutableStateOf(name)
        var role by mutableStateOf(role)
    }

    class WebLinkContext(label: String = "Example Link", url: String = "http://example.com") {
        var label by mutableStateOf(label)
        var url by mutableStateOf(url)
    }
}
