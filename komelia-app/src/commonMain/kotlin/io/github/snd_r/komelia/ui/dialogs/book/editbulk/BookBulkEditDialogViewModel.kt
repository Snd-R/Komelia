package io.github.snd_r.komelia.ui.dialogs.book.editbulk

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.book.KomgaBookMetadataUpdateRequest
import io.github.snd_r.komga.common.KomgaAuthor
import io.github.snd_r.komga.common.patch
import io.github.snd_r.komga.common.patchLists

class BookBulkEditDialogViewModel(
    val books: List<KomgaBook>,
    val onDialogDismiss: () -> Unit,
    private val bookClient: KomgaBookClient,
    private val notifications: AppNotifications,
) {
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

                bookClient.updateMetadata(book.id, request)

            }
            onDialogDismiss()
        }
    }

    private fun <T, R> distinctOrDefault(elements: List<T>, default: R, selector: (T) -> R): R {
        return if (elements.all { selector(it) == selector(elements[0]) }) selector(elements[0])
        else default
    }
}