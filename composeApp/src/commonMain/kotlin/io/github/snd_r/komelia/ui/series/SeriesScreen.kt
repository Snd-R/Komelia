package io.github.snd_r.komelia.ui.series

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.book.BookScreen
import io.github.snd_r.komelia.ui.collection.CollectionScreen
import io.github.snd_r.komelia.ui.common.ErrorContent
import io.github.snd_r.komelia.ui.library.LibraryScreen
import io.github.snd_r.komelia.ui.reader.view.ReaderScreen
import io.github.snd_r.komelia.ui.series.view.SeriesContent
import io.github.snd_r.komga.series.KomgaSeriesId

class SeriesScreen(val seriesId: KomgaSeriesId) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(seriesId.value) { viewModelFactory.getSeriesViewModel(seriesId) }

        LaunchedEffect(seriesId) { vm.initialize() }

        when (val state = vm.state.collectAsState().value) {
            is Error -> ErrorContent(
                message = state.exception.message ?: "Unknown Error",
                onReload = vm::reload
            )

            else -> {
                SeriesContent(
                    series = vm.series,
                    seriesMenuActions = vm.seriesMenuActions(),

                    books = vm.books,
                    booksLoading = vm.booksLoading,
                    bookCardWidth = vm.cardWidth.collectAsState().value,
                    booksLayout = vm.booksLayout.collectAsState().value,
                    onBooksLayoutChange = vm::onBookLayoutChange,

                    booksPageSize = vm.booksPageSize.collectAsState().value,
                    onBooksPageSizeChange = vm::onBookPageSizeChange,

                    bookMenuActions = vm.bookMenuActions(),
                    totalBookPages = vm.totalBookPages,
                    currentBookPage = vm.currentBookPage,
                    onBookClick = { navigator push BookScreen(it) },
                    onBookReadClick = { navigator.parent?.replace(ReaderScreen(it)) },

                    onBookPageNumberClick = { vm.onLoadBookPage(it) },

                    onBackButtonClick = {
                        val libraryId = vm.series?.libraryId
                        val success = navigator.popUntil {
                            (it is LibraryScreen && it.libraryId == libraryId) || it is CollectionScreen
                        }
                        if (!success) {
                            libraryId?.let { navigator replaceAll LibraryScreen(it) }
                        }

                    },
                )
            }
        }

    }
}
