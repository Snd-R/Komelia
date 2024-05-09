package io.github.snd_r.komelia.ui.series

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.book.BookScreen
import io.github.snd_r.komelia.ui.collection.CollectionScreen
import io.github.snd_r.komelia.ui.common.ErrorContent
import io.github.snd_r.komelia.ui.library.LibraryScreen
import io.github.snd_r.komelia.ui.reader.ReaderScreen
import io.github.snd_r.komelia.ui.series.view.SeriesContent
import io.github.snd_r.komga.library.KomgaLibraryId
import io.github.snd_r.komga.series.KomgaSeriesId

class SeriesScreen(val seriesId: KomgaSeriesId) : Screen {

    @OptIn(InternalVoyagerApi::class)
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

                    onFilterClick = { filter ->
                        val series = requireNotNull(vm.series)
                        navigator.popUntilRoot()
                        navigator.dispose(navigator.lastItem)
                        navigator.replaceAll(LibraryScreen(series.libraryId, filter))
                    },

                    books = vm.books,
                    booksLoading = vm.booksLoading,
                    bookCardWidth = vm.cardWidth.collectAsState().value,
                    booksLayout = vm.booksLayout.collectAsState().value,
                    onBooksLayoutChange = vm::onBookLayoutChange,

                    booksEditMode = vm.booksEditMode,
                    onBooksEditModeChange = vm::onEditModeChange,
                    selectedBooks = vm.selectedBooks,
                    onBookSelect = vm::onBookSelect,

                    booksPageSize = vm.booksPageSize.collectAsState().value,
                    onBooksPageSizeChange = vm::onBookPageSizeChange,

                    bookMenuActions = vm.bookMenuActions(),
                    totalBookPages = vm.totalBookPages,
                    currentBookPage = vm.currentBookPage,
                    onBookClick = { navigator push BookScreen(it.id) },
                    onBookReadClick = { navigator.parent?.replace(ReaderScreen(it.id)) },

                    onBookPageNumberClick = { vm.onPageChange(it) },

                    onBackButtonClick = { onBackPress(navigator, vm.series?.libraryId) },
                )
            }
        }

        BackPressHandler { onBackPress(navigator, vm.series?.libraryId) }
    }

    private fun onBackPress(navigator: Navigator, libraryId: KomgaLibraryId?) {
        val success = navigator.popUntil {
            (it is LibraryScreen && it.libraryId == libraryId) || it is CollectionScreen
        }
        if (!success) {
            libraryId?.let { navigator replaceAll LibraryScreen(it) }
        }
    }
}
