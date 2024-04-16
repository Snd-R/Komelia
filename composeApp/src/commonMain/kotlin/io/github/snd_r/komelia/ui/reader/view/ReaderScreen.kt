package io.github.snd_r.komelia.ui.reader.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import io.github.snd_r.komelia.ui.reader.ReaderViewModel
import io.github.snd_r.komelia.ui.series.SeriesScreen
import io.github.snd_r.komga.book.KomgaBookId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

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

        val vmState = vm.readerState.state.collectAsState(Dispatchers.Main.immediate)

        when (val result = vmState.value) {
            is Error -> Text(result.exception.message ?: "Error")
            Loading, Uninitialized -> LoadIndicator()
            is Success -> ReaderScreenContent(vm)
        }
    }

    @Composable
    fun ReaderScreenContent(vm: ReaderViewModel) {
        val navigator = LocalNavigator.currentOrThrow
        PagedReaderContent(
            pageState = vm.pagedReaderState,
//            zoomState = vm.zoomState,
            settingsState = vm.readerState,
            onSeriesBackClick = {
                vm.readerState.bookState.value?.book?.let { book ->
                    navigator replace MainScreen(
                        SeriesScreen(book.seriesId)
                    )
                }
            },
            onBookBackClick = {
                vm.readerState.bookState.value?.book?.let { book ->
                    navigator replace MainScreen(
                        BookScreen(book.id)
                    )
                }
            }
        )
        BackPressHandler {
            vm.readerState.bookState.value?.book?.let { book ->
                navigator replace MainScreen(SeriesScreen(book.seriesId))
            }
        }
    }

    @Composable
    private fun LoadIndicator() {
        var showLoadIndicator by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(100)
            showLoadIndicator = true
        }

        if (showLoadIndicator)
            Box(
                contentAlignment = Alignment.TopStart,
                modifier = Modifier.fillMaxSize()
            ) {
                LinearProgressIndicator(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.scale(scaleX = 1f, scaleY = 3f).fillMaxWidth()
                )
            }

    }
}
