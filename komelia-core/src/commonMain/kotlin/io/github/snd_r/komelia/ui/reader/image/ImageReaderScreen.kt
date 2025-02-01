package io.github.snd_r.komelia.ui.reader.image

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.PlatformTitleBar
import io.github.snd_r.komelia.platform.canIntegrateWithSystemBar
import io.github.snd_r.komelia.ui.BookSiblingsContext
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.LocalWindowState
import io.github.snd_r.komelia.ui.MainScreen
import io.github.snd_r.komelia.ui.book.bookScreen
import io.github.snd_r.komelia.ui.color.view.ColorCorrectionScreen
import io.github.snd_r.komelia.ui.common.ErrorContent
import io.github.snd_r.komelia.ui.reader.TitleBarContent
import io.github.snd_r.komelia.ui.reader.epub.EpubScreen
import io.github.snd_r.komelia.ui.reader.image.common.ReaderContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.MediaProfile.DIVINA
import snd.komga.client.book.MediaProfile.EPUB
import snd.komga.client.book.MediaProfile.PDF

fun readerScreen(
    book: KomgaBook,
    markReadProgress: Boolean,
    bookSiblingsContext: BookSiblingsContext? = null,
): Screen {
    val context = bookSiblingsContext ?: BookSiblingsContext.Series
    return when (book.media.mediaProfile) {
        DIVINA, PDF -> ImageReaderScreen(
            bookId = book.id,
            markReadProgress = markReadProgress,
            bookSiblingsContext = context
        )

        EPUB -> EpubScreen(
            bookId = book.id,
            bookSiblingsContext = context,
            markReadProgress = markReadProgress,
            book = book
        )

        null -> error("Unsupported book format")
    }
}

class ImageReaderScreen(
    private val bookId: KomgaBookId,
    private val bookSiblingsContext: BookSiblingsContext,
    private val markReadProgress: Boolean = true,
) : Screen {

    @Composable
    override fun Content() {
        val coroutineScope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(bookId.value) {
            viewModelFactory.getBookReaderViewModel(
                navigator = navigator,
                markReadProgress = markReadProgress,
                bookSiblingsContext = bookSiblingsContext
            )
        }

        //FIXME: do outside of composition? No proper multiplatform way to do it in viewmodel
        // restore current book when app process is killed in background on Android
        var currentBookId by rememberSaveable { mutableStateOf(bookId.value) }
        LaunchedEffect(Unit) {
            val bookId = KomgaBookId(currentBookId)
            vm.initialize(bookId)
            val book = vm.readerState.booksState.value?.currentBook
            if (book != null && book.media.mediaProfile != DIVINA && book.media.mediaProfile != PDF) {
                navigator.replace(
                    readerScreen(
                        book = book,
                        bookSiblingsContext = bookSiblingsContext,
                        markReadProgress = markReadProgress
                    )
                )
            }
            vm.readerState.booksState.filterNotNull()
                .collect { currentBookId = it.currentBook.id.value }
        }

        val vmState = vm.readerState.state.collectAsState(Dispatchers.Main.immediate)
        val currentBook = vm.readerState.booksState.collectAsState().value?.currentBook

        Column {
            PlatformTitleBar(Modifier.zIndex(10f), false) {
                if (canIntegrateWithSystemBar()) {
                    val isFullscreen = LocalWindowState.current.isFullscreen.collectAsState(false)
                    if (currentBook != null && !isFullscreen.value) {
                        TitleBarContent(
                            title = currentBook.metadata.title,
                            onExit = { onExit(navigator, currentBook) }
                        )
                    }
                }
            }

            when (val result = vmState.value) {
                is LoadState.Error -> ErrorContent(
                    exception = result.exception,
                    onExit = { onExit(navigator, currentBook) },
                    onReload = { coroutineScope.launch { vm.initialize(bookId) } }
                )

                LoadState.Loading, LoadState.Uninitialized -> LoadIndicator()
                is Success -> ReaderScreenContent(vm)
            }
        }
    }

    @Composable
    fun ReaderScreenContent(vm: ReaderViewModel) {
        val navigator = LocalNavigator.currentOrThrow

        ReaderContent(
            commonReaderState = vm.readerState,
            pagedReaderState = vm.pagedReaderState,
            continuousReaderState = vm.continuousReaderState,
            screenScaleState = vm.screenScaleState,
            isColorCorrectionActive = vm.colorCorrectionIsActive.collectAsState(false).value,
            onColorCorrectionClick = {
                vm.readerState.booksState.value?.currentBook?.let { book ->
                    val page = vm.readerState.readProgressPage.value
                    navigator push ColorCorrectionScreen(book.id, page)
                }
            },
            onExit = { onExit(navigator, vm.readerState.booksState.value?.currentBook) }
        )
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
                    trackColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = .3f),
                    modifier = Modifier.scale(scaleX = 1f, scaleY = 3f).fillMaxWidth()
                )
            }

    }

    private fun onExit(navigator: Navigator, book: KomgaBook?) {
        if (navigator.canPop) {
            navigator.pop()
        } else if (book != null) {
            navigator.replace(MainScreen(bookScreen(book)))
        }
    }
}
