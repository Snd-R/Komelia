package io.github.snd_r.komelia.ui.reader.view

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.MainScreen
import io.github.snd_r.komelia.ui.book.BookScreen
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.reader.HorizontalPagesReaderViewModel
import io.github.snd_r.komelia.ui.series.SeriesScreen
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookId
import kotlinx.coroutines.Dispatchers

class ReaderScreen(
    private val bookId: KomgaBookId,
    private val markReadProgress: Boolean = true,
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(bookId.value) {
            viewModelFactory.getBookReaderViewModel(navigator, markReadProgress)
        }

        LaunchedEffect(bookId) { vm.initialize(bookId) }

        val vmState = vm.state.collectAsState(Dispatchers.Main.immediate)

        when (val result = vmState.value) {
            is Error -> Text(result.exception.message ?: "Error")
            Loading, Uninitialized -> LoadingMaxSizeIndicator()
            is Success -> ReaderScreenContent(vm, result.value)
        }
    }

    @Composable
    fun ReaderScreenContent(vm: HorizontalPagesReaderViewModel, book: KomgaBook) {
        val navigator = LocalNavigator.currentOrThrow
        PagedReaderContent(
            book = book,
            pageState = vm,
            zoomState = vm,
            settingsState = vm,
            onSeriesBackClick = {
                navigator replace MainScreen(
                    SeriesScreen(book.seriesId)
                )
            },
            onBookBackClick = {
                navigator replace MainScreen(
                    BookScreen(book.id)
                )

            }
        )

        BackPressHandler { navigator replace MainScreen(SeriesScreen(book.seriesId)) }

    }
}
