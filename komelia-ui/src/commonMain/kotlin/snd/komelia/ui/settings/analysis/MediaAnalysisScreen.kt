package snd.komelia.ui.settings.analysis

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import snd.komelia.ui.LoadState.Error
import snd.komelia.ui.LoadState.Loading
import snd.komelia.ui.LoadState.Success
import snd.komelia.ui.LoadState.Uninitialized
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.MainScreen
import snd.komelia.ui.book.bookScreen
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.settings.SettingsScreenContainer

class MediaAnalysisScreen : Screen {
    @Composable
    @OptIn(InternalVoyagerApi::class)
    override fun Content() {
        val rootNavigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getMediaAnalysisViewModel() }
        LaunchedEffect(Unit) { vm.initialize() }

        SettingsScreenContainer("Media Analysis") {
            when (val state = vm.state.collectAsState().value) {
                Uninitialized, Loading -> LoadingMaxSizeIndicator()
                is Error -> Text(state.exception.message ?: "Error")
                is Success -> MediaAnalysisContent(
                    books = vm.books,
                    onBookClick = {
                        rootNavigator.pop()
                        rootNavigator.dispose(rootNavigator.lastItem)
                        rootNavigator.replaceAll(MainScreen(bookScreen(it)))
                    },
                    currentPage = vm.currentPage,
                    totalPages = vm.totalPages,
                    onPageChange = vm::loadPage
                )
            }
        }
    }
}