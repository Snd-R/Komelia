package snd.komelia.ui.reader

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.BookSiblingsContext
import snd.komelia.ui.LoadState
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.LocalWindowState
import snd.komelia.ui.MainScreen
import snd.komelia.ui.book.BookScreen
import snd.komelia.ui.book.bookScreen
import snd.komelia.ui.common.components.ErrorContent
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.platform.PlatformTitleBar
import snd.komelia.ui.platform.canIntegrateWithSystemBar
import snd.komelia.ui.reader.epub.EpubContent
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.MediaProfile
import kotlin.jvm.Transient

class EpubScreen(
    private val bookId: KomgaBookId,
    private val bookSiblingsContext: BookSiblingsContext,
    private val markReadProgress: Boolean = true,
    @Transient
    private val book: KomeliaBook? = null,
) : Screen {

    override val key: ScreenKey = bookId.value

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(bookId.value) {
            viewModelFactory.getEpubReaderViewModel(
                bookId = bookId,
                bookSiblingsContext = bookSiblingsContext,
                book = book,
                markReadProgress = markReadProgress
            )
        }
        LaunchedEffect(bookId) {
            vm.initialize(navigator)
            val state = vm.state.value
            if (state is LoadState.Success) {
                val book = state.value.book.value
                if (book != null && book.media.mediaProfile != MediaProfile.EPUB) {
                    navigator.replace(readerScreen(book, markReadProgress))
                }
            }
        }

        val state = vm.state.collectAsState().value
        Column {
            PlatformTitleBar(applyInsets = false) {
                if (canIntegrateWithSystemBar()) {
                    val isFullscreen = LocalWindowState.current.isFullscreen.collectAsState(false)
                    if (state is LoadState.Success && !isFullscreen.value) {
                        val book = state.value.book.collectAsState().value
                        TitleBarContent(
                            title = book?.metadata?.title ?: "",
                            onExit = { state.value.closeWebview() }
                        )
                    }
                }
            }
            when (state) {
                LoadState.Loading, LoadState.Uninitialized -> LoadingMaxSizeIndicator()
                is LoadState.Error -> ErrorContent(
                    message = state.exception.message ?: state.exception.stackTraceToString(),
                    onExit = {
                        val screen = book?.let { bookScreen(book = it, bookSiblingsContext = bookSiblingsContext) }
                            ?: BookScreen(bookId = bookId, bookSiblingsContext = bookSiblingsContext)

                        navigator.replaceAll(MainScreen(screen))
                    }
                )

                is LoadState.Success -> EpubContent(
                    onWebviewCreated = { state.value.onWebviewCreated(it) },
                    onBackButtonPress = state.value::onBackButtonPress
                )
            }
        }
    }

}