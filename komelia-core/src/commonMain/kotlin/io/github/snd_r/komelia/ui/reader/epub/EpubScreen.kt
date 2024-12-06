package io.github.snd_r.komelia.ui.reader.epub

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.PlatformTitleBar
import io.github.snd_r.komelia.platform.canIntegrateWithSystemBar
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.MainScreen
import io.github.snd_r.komelia.ui.book.BookScreen
import io.github.snd_r.komelia.ui.book.bookScreen
import io.github.snd_r.komelia.ui.common.ErrorContent
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.reader.image.readerScreen
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.MediaProfile.EPUB

class EpubScreen(
    private val bookId: KomgaBookId,
    private val markReadProgress: Boolean = true,
    @Transient
    private val book: KomgaBook? = null,
) : Screen {

    override val key: ScreenKey = bookId.value

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(bookId.value) {
            viewModelFactory.getEpubReaderViewModel(
                bookId = bookId,
                book = book,
                markReadProgress = markReadProgress
            )
        }
        LaunchedEffect(bookId) {
            vm.initialize(navigator)
            val state = vm.state.value
            if (state is Success) {
                val book = state.value.book.value
                if (book != null && book.media.mediaProfile != EPUB) {
                    navigator.replace(readerScreen(book, markReadProgress))
                }
            }
        }

        val state = vm.state.collectAsState().value
        Column {
            PlatformTitleBar(applyInsets = false) {
                if (canIntegrateWithSystemBar() && state is Success) {
                    val book = state.value.book.collectAsState().value
                    TitleBarContent(
                        title = book?.metadata?.title ?: "",
                        onExit = { state.value.closeWebview() }
                    )
                }
            }
            when (state) {
                Loading, Uninitialized -> LoadingMaxSizeIndicator()
                is Error -> ErrorContent(
                    message = state.exception.message ?: state.exception.stackTraceToString(),
                    onExit = { navigator.replaceAll(MainScreen(book?.let { bookScreen(it) } ?: BookScreen(bookId))) }
                )

                is Success -> EpubContent(onWebviewCreated = { state.value.onWebviewCreated(it) })
            }
        }
    }

}