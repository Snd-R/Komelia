package snd.komelia.ui.dialogs.book.editbulk

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.oshai.kotlinlogging.KotlinLogging
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.KomgaReferentialApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komga.client.book.KomgaBookMetadataUpdateRequest
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.common.patch
import snd.komga.client.common.patchLists

private val logger = KotlinLogging.logger { }

class BookBulkEditDialogViewModel(
    val books: List<KomeliaBook>,
    val onDialogDismiss: () -> Unit,
    private val bookApi: KomgaBookApi,
    private val referentialApi: KomgaReferentialApi,
    private val notifications: AppNotifications,
) {
    var allTags by mutableStateOf<List<String>>(emptyList())
        private set
    var tags by mutableStateOf<List<String>>(emptyList())
    var tagsLock by mutableStateOf(
        distinctOrDefault(books, false) { it.metadata.tagsLock }
    )
    var authorsLock by mutableStateOf(
        distinctOrDefault(books, false) { it.metadata.authorsLock }
    )

    private val defaultRoles =
        listOf("writer", "penciller", "inker", "colorist", "letterer", "cover", "editor", "translator")

    var authors by mutableStateOf(
        (defaultRoles.plus(books.flatMap { it.metadata.authors }.map { it.role }))
            .associateWith { emptyList<KomgaAuthor>() }
    )

    private val authorsTab = AuthorsTab(this)
    private val tagsTab = TagsTab(this)
    var currentTab by mutableStateOf<DialogTab>(authorsTab)

    suspend fun initialize() {
        notifications.runCatchingToNotifications {
            allTags = referentialApi.getTags()
        }.onFailure { logger.catching(it) }
    }

    fun tabs(): List<DialogTab> = listOf(authorsTab, tagsTab)

    suspend fun saveChanges() {
        notifications.runCatchingToNotifications {
            books.forEach { book ->
                val bookMetadata = book.metadata
                val newAuthors = authors.flatMap { (_, authorsForRole) -> authorsForRole }
                val request = KomgaBookMetadataUpdateRequest(
                    authors = patchLists(bookMetadata.authors, newAuthors),
                    authorsLock = patch(bookMetadata.authorsLock, authorsLock),
                    tags = patchLists(bookMetadata.tags, tags),
                    tagsLock = patch(bookMetadata.tagsLock, tagsLock),
                )

                bookApi.updateMetadata(book.id, request)

            }
            onDialogDismiss()
        }
    }

    private fun <T, R> distinctOrDefault(elements: List<T>, default: R, selector: (T) -> R): R {
        return if (elements.all { selector(it) == selector(elements[0]) }) selector(elements[0])
        else default
    }
}