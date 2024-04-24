package io.github.snd_r.komelia.ui.book

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.reader.ReaderScreen
import io.github.snd_r.komelia.ui.readlist.ReadListScreen
import io.github.snd_r.komelia.ui.series.SeriesScreen
import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.series.KomgaSeriesId

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
                onBackPress(navigator, vm.book?.seriesId)
            },
            onBookReadPress = { markReadProgress -> navigator.parent?.replace(ReaderScreen(bookId, markReadProgress)) },
        )

        BackPressHandler { onBackPress(navigator, vm.book?.seriesId) }
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