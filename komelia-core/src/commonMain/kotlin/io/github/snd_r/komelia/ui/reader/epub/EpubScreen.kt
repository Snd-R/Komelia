package io.github.snd_r.komelia.ui.reader.epub

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.PlatformTitleBar
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.ErrorContent
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.reader.readerScreen
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.MediaProfile.EPUB

class EpubScreen(
    private val bookId: KomgaBookId,
    private val markReadProgress: Boolean = true,
    @Transient
    private val book: KomgaBook? = null,
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(bookId.value) {
            viewModelFactory.getEpubViewModel(
                bookId = bookId,
                navigator = navigator,
                book = book
            )
        }
        LaunchedEffect(bookId) {
            vm.initialize()
            val book = vm.book.value
            if (book != null && book.media.mediaProfile != EPUB) {
                navigator.replace(readerScreen(book, markReadProgress))
            }
        }

        Column {
            PlatformTitleBar()
            when (val result = vm.state.collectAsState().value) {
                Loading, Uninitialized -> LoadingMaxSizeIndicator()
                is Error -> ErrorContent(result.exception.message ?: result.exception.stackTraceToString())
                is Success -> EpubContent(vm::onWebviewCreated)
            }

        }
    }
}