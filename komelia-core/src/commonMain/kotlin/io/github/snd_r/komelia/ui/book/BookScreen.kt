package io.github.snd_r.komelia.ui.book

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.ui.LocalReloadEvents
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.ReloadableScreen
import io.github.snd_r.komelia.ui.common.ScreenPullToRefreshBox
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
) : ReloadableScreen {
    constructor(book: KomgaBook) : this(book.id, book)

    override val key: ScreenKey = bookId.toString()

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(bookId.value) { viewModelFactory.getBookViewModel(bookId, book) }
        val navigator = LocalNavigator.currentOrThrow
        val reloadEvents = LocalReloadEvents.current

        LaunchedEffect(Unit) {
            vm.initialize()
            vm.book.value?.let { if (it.oneshot) navigator.replace(OneshotScreen(it)) }
            reloadEvents.collect { vm.reload() }
        }
        val book = vm.book.collectAsState().value

        ScreenPullToRefreshBox(screenState = vm.state, onRefresh = vm::reload) {
            BookScreenContent(
                library = vm.library,
                book = book,
                bookMenuActions = vm.bookMenuActions,
                onBookReadPress = { markReadProgress ->
                    navigator.parent?.push(
                        if (book != null) readerScreen(book, markReadProgress)
                        else ImageReaderScreen(bookId, markReadProgress)
                    )
                },

                readLists = vm.readListsState.readLists,
                onReadListClick = { navigator.push(ReadListScreen(it.id)) },
                onBookPress = { if (it.id != bookId) navigator.push(bookScreen(it)) },
                onParentSeriesPress = { book?.seriesId?.let { seriesId -> navigator.push(SeriesScreen(seriesId)) } },
                onFilterClick = { filter ->
                    navigator.push(LibraryScreen(requireNotNull(book?.libraryId), filter))
                },
                cardWidth = vm.cardWidth.collectAsState().value
            )

            BackPressHandler { onBackPress(navigator, book?.seriesId) }
        }
    }

    private fun onBackPress(navigator: Navigator, seriesId: KomgaSeriesId?) {
        if (navigator.canPop) {
            navigator.pop()
        } else {
            seriesId?.let { navigator push SeriesScreen(it) }
        }
    }

}