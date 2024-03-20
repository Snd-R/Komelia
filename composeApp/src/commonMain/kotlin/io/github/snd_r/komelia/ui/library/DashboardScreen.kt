package io.github.snd_r.komelia.ui.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.book.BookScreen
import io.github.snd_r.komelia.ui.library.view.DashboardContent
import io.github.snd_r.komelia.ui.reader.view.ReaderScreen
import io.github.snd_r.komelia.ui.series.SeriesScreen
import io.github.snd_r.komga.library.KomgaLibraryId

class DashboardScreen(private val libraryId: KomgaLibraryId? = null) : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(libraryId?.value) { viewModelFactory.getLibraryRecommendationViewModel(libraryId) }
        val navigator = LocalNavigator.currentOrThrow
        LaunchedEffect(Unit) { vm.initialize() }

        DashboardContent(
            keepReadingBooks = vm.keepReadingBooks,
            recentlyReleasedBooks = vm.recentlyReleasedBooks,
            recentlyAddedBooks = vm.recentlyAddedBooks,
            recentlyAddedSeries = vm.recentlyAddedSeries,
            recentlyUpdatedSeries = vm.recentlyUpdatedSeries,
            cardWidth = vm.cardWidth.collectAsState().value,

            onSeriesClick = { navigator push SeriesScreen(it) },
            seriesMenuActions = vm.seriesMenuActions(),
            bookMenuActions = vm.bookMenuActions(),
            onBookClick = { navigator push BookScreen(it) },
            onBookReadClick = { navigator.parent?.replace(ReaderScreen(it)) },
        )
    }
}
