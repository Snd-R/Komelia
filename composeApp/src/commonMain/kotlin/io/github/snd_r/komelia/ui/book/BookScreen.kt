package io.github.snd_r.komelia.ui.book

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.reader.view.ReaderScreen
import io.github.snd_r.komelia.ui.readlist.ReadListScreen
import io.github.snd_r.komelia.ui.series.SeriesScreen
import io.github.snd_r.komga.book.KomgaBookId

class BookScreen(val bookId: KomgaBookId) : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(bookId.value) { viewModelFactory.getBookViewModel(bookId) }

        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) { vm.initialize() }

        BookContent(
            library = vm.library,
            book = vm.book,
            bookMenuActions = vm.bookMenuActions(),
            onBackButtonClick = {
                val seriesId = vm.book?.seriesId
                val success = navigator.popUntil {
                    (it is SeriesScreen && it.seriesId == seriesId) || it is ReadListScreen
                }
                if (!success) {
                    seriesId?.let { navigator push SeriesScreen(it) }
                }
            },
            onBookReadPress = { markReadProgress -> navigator.parent?.replace(ReaderScreen(bookId, markReadProgress)) },
        )
    }
}