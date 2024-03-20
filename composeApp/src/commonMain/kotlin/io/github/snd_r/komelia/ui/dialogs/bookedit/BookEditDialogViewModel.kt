package io.github.snd_r.komelia.ui.dialogs.bookedit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.book.KomgaBookMetadataUpdateRequest
import io.github.snd_r.komga.common.KomgaAuthor
import io.github.snd_r.komga.common.KomgaWebLink
import io.github.snd_r.komga.common.patch
import io.github.snd_r.komga.common.patchLists
import java.time.LocalDate

class BookEditDialogViewModel(
    val book: KomgaBook,
    val onDialogDismiss: () -> Unit,
    private val bookClient: KomgaBookClient,
    private val notifications: AppNotifications,
) {

    var title by mutableStateOf(book.metadata.title)

    var titleLock by mutableStateOf(book.metadata.titleLock)
    var summary by mutableStateOf(book.metadata.summary)
    var summaryLock by mutableStateOf(book.metadata.summaryLock)
    var number by mutableStateOf(book.metadata.number)
    var numberLock by mutableStateOf(book.metadata.numberLock)
    var numberSort by mutableStateOf(book.metadata.numberSort)
    var numberSortLock by mutableStateOf(book.metadata.numberSortLock)
    var releaseDate by mutableStateOf(book.metadata.releaseDate?.toString() ?: "")
    var releaseDateLock by mutableStateOf(book.metadata.releaseDateLock)
    var authorsLock by mutableStateOf(book.metadata.authorsLock)
    var tags by mutableStateOf(book.metadata.tags)
    var tagsLock by mutableStateOf(book.metadata.tagsLock)
    var isbn by mutableStateOf(book.metadata.isbn)
    var isbnLock by mutableStateOf(book.metadata.isbnLock)
    var links = mutableStateListOf<KomgaWebLink>()
        .also { it.addAll(book.metadata.links) }
    var linksLock by mutableStateOf(book.metadata.linksLock)

    private val defaultRoles =
        listOf("writer", "penciller", "inker", "colorist", "letterer", "cover", "editor", "translator")

    var authors by mutableStateOf(
        defaultRoles.associateWith { emptyList<KomgaAuthor>() }
            .plus(book.metadata.authors.groupBy { it.role })
    )

    private val generalTab = GeneralTab(this)
    private val authorsTab = AuthorsTab(this)
    private val tagsTab = TagsTab(this)
    private val linksTab = LinksTab(this)
    private val posterTab = PosterTab(this)
    var currentTab by mutableStateOf<DialogTab>(generalTab)

    fun tabs(): List<DialogTab> = listOf(generalTab, authorsTab, tagsTab, linksTab, posterTab)

    suspend fun saveChanges() {

        val bookMetadata = book.metadata
        val newAuthors = authors.flatMap { (_, authorsForRole) -> authorsForRole }
        val newReleaseDate = if (releaseDate.isNotBlank()) LocalDate.parse(releaseDate) else null
        val request = KomgaBookMetadataUpdateRequest(

            title = patch(bookMetadata.title, title),
            titleLock = patch(bookMetadata.titleLock, titleLock),
            summary = patch(bookMetadata.summary, summary),
            summaryLock = patch(bookMetadata.summaryLock, summaryLock),
            number = patch(bookMetadata.number, number),
            numberLock = patch(bookMetadata.numberLock, numberLock),
            numberSort = patch(bookMetadata.numberSort, numberSort),
            numberSortLock = patch(bookMetadata.numberSortLock, numberSortLock),
            releaseDate = patch(bookMetadata.releaseDate, newReleaseDate),
            releaseDateLock = patch(bookMetadata.releaseDateLock, releaseDateLock),
            authors = patchLists(bookMetadata.authors, newAuthors),
            authorsLock = patch(bookMetadata.authorsLock, authorsLock),
            tags = patchLists(bookMetadata.tags, tags),
            tagsLock = patch(bookMetadata.tagsLock, tagsLock),
            isbn = patch(bookMetadata.isbn, isbn),
            isbnLock = patch(bookMetadata.isbnLock, isbnLock),
            links = patchLists(bookMetadata.links, links),
            linksLock = patch(bookMetadata.linksLock, linksLock),
        )

        notifications.runCatchingToNotifications {
            bookClient.updateMetadata(book.id, request)
            onDialogDismiss()
        }
    }
}