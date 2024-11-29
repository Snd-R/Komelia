package io.github.snd_r.komelia.ui.book

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.library.LibraryScreen
import io.github.snd_r.komelia.ui.oneshot.OneshotScreen
import io.github.snd_r.komelia.ui.reader.image.ImageReaderScreen
import io.github.snd_r.komelia.ui.reader.image.readerScreen
import io.github.snd_r.komelia.ui.readlist.ReadListScreen
import io.github.snd_r.komelia.ui.series.SeriesScreen
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookId
import snd.komga.client.series.KomgaSeriesId
import kotlin.jvm.Transient

fun bookScreen(book: KomgaBook) = if (book.oneshot) OneshotScreen(book) else BookScreen(book)

class BookScreen(
    val bookId: KomgaBookId,
    @Transient
    val book: KomgaBook? = null,
) : Screen {
    constructor(book: KomgaBook) : this(book.id, book)

    override val key: ScreenKey = bookId.toString()

    @OptIn(InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm =
            rememberScreenModel(bookId.value) { viewModelFactory.getBookViewModel(bookId, book) }

        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) {
            vm.initialize()
            vm.book.value?.let { if (it.oneshot) navigator.replace(OneshotScreen(it)) }
        }
        val book = vm.book.collectAsState().value

        BookScreenContent(
            library = vm.library,
            book = book,
            bookMenuActions = vm.bookMenuActions,
            onBackButtonClick = { onBackPress(navigator, book?.seriesId) },
            onBookReadPress = { markReadProgress ->
                navigator.parent?.replace(
                    if (book != null) readerScreen(book, markReadProgress)
                    else ImageReaderScreen(bookId, markReadProgress)
                )
            },

            readLists = vm.readListsState.readLists,
            onReadListClick = { navigator.push(ReadListScreen(it.id)) },
            onBookClick = { navigator.push(bookScreen(it)) },
            onFilterClick = { filter ->
                val libraryId = requireNotNull(book?.libraryId)
                navigator.popUntilRoot()
                navigator.dispose(navigator.lastItem)
                navigator.replaceAll(LibraryScreen(libraryId, filter))
            },
            cardWidth = vm.cardWidth.collectAsState().value
        )

        BackPressHandler { onBackPress(navigator, book?.seriesId) }
    }

    private fun onBackPress(navigator: Navigator, seriesId: KomgaSeriesId?) {
        val success = navigator.popUntil {
            (it is SeriesScreen && it.seriesId == seriesId) || it is ReadListScreen
        }
        if (!success) {
            seriesId?.let { navigator push SeriesScreen(it) }
        }
    }

}