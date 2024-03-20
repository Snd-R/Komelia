package io.github.snd_r.komelia.ui.book

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.library.KomgaLibraryClient
import io.github.snd_r.komga.sse.KomgaEvent
import io.github.snd_r.komga.sse.KomgaEvent.BookChanged
import io.github.snd_r.komga.sse.KomgaEvent.ReadListEvent
import io.github.snd_r.komga.sse.KomgaEvent.ReadProgressChanged
import io.github.snd_r.komga.sse.KomgaEvent.ReadProgressDeleted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class BookViewModel(
    private val libraryClient: KomgaLibraryClient,
    private val bookClient: KomgaBookClient,
    private val bookId: KomgaBookId,
    private val notifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {
    var book by mutableStateOf<KomgaBook?>(null)
    var library by mutableStateOf<KomgaLibrary?>(null)

    fun initialize() {
        if (state.value != Uninitialized) return

        screenModelScope.launch { startEventListener() }
        screenModelScope.launch { loadBook() }
    }

    private suspend fun loadBook() {
        mutableState.value = Loading
        val loadedBook = bookClient.getBook(bookId)
        library = libraryClient.getLibrary(loadedBook.libraryId)
        book = loadedBook
        mutableState.value = Success(Unit)
    }

    fun bookMenuActions() = BookMenuActions(bookClient, notifications, screenModelScope)

    private suspend fun startEventListener() {
        komgaEvents.collect { event ->
            when (event) {
                is BookChanged -> if (event.bookId == bookId) loadBook()
                is ReadProgressChanged -> if (event.bookId == bookId) loadBook()
                is ReadProgressDeleted -> if (event.bookId == bookId) loadBook()
                is ReadListEvent -> if (event.bookIds.any { it == bookId }) loadBook()

                else -> {}
            }
        }
    }

}