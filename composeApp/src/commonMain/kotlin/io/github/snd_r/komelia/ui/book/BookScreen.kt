package io.github.snd_r.komelia.ui.book

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.reader.ReaderScreen
import io.github.snd_r.komelia.ui.readlist.ReadListScreen
import io.github.snd_r.komelia.ui.series.SeriesScreen
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.series.KomgaSeriesId
import kotlin.jvm.Transient

class BookScreen(
    val bookId: KomgaBookId,
    @Transient
    val book: KomgaBook? = null,
) : Screen {
    constructor(book: KomgaBook) : this(book.id, book)

    override val key: ScreenKey = bookId.toString()

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(bookId.value) { viewModelFactory.getBookViewModel(bookId, book) }

        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) { vm.initialize() }
        val book = vm.book.collectAsState().value

        BookContent(
            library = vm.library,
            book = book,
            bookMenuActions = vm.bookMenuActions(),
            onBackButtonClick = { onBackPress(navigator, book?.seriesId) },
            onBookReadPress = { markReadProgress -> navigator.parent?.replace(ReaderScreen(bookId, markReadProgress)) },

            readLists = vm.readListsState.readLists,
            onReadListClick = { navigator.push(ReadListScreen(it.id)) },
            onBookClick = { navigator.push(BookScreen(it)) },
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