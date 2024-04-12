package io.github.snd_r.komelia.ui.settings.analysis

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.MainScreen
import io.github.snd_r.komelia.ui.book.BookScreen
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator

class MediaAnalysisScreen : Screen {
    @OptIn(InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        val rootNavigator = requireNotNull(LocalNavigator.currentOrThrow.parent)
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getMediaAnalysisViewModel() }
        LaunchedEffect(Unit) { vm.initialize() }

        when (val state = vm.state.collectAsState().value) {
            Uninitialized, Loading -> LoadingMaxSizeIndicator()
            is Error -> Text(state.exception.message ?: "Error")
            is Success -> MediaAnalysisContent(
                books = vm.books,
                onBookClick = {
                    rootNavigator.popUntilRoot()
                    rootNavigator.dispose(rootNavigator.lastItem)
                    rootNavigator.replaceAll(MainScreen(BookScreen(it)))
                },
                currentPage = vm.currentPage,
                totalPages = vm.totalPages,
                onPageChange = vm::loadPage
            )
        }

    }
}