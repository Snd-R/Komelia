package io.github.snd_r.komelia.ui.series

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
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
import io.github.snd_r.komelia.ui.series.SeriesViewModel.SeriesTab
import io.github.snd_r.komelia.ui.series.view.SeriesContent
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId
import kotlin.jvm.Transient

class SeriesScreen(
    val seriesId: KomgaSeriesId,
    @Transient
    private val series: KomgaSeries? = null,
    @Transient
    private val startingTab: SeriesTab = SeriesTab.BOOKS
) : Screen {
    constructor(series: KomgaSeries, startingTab: SeriesTab = SeriesTab.BOOKS) : this(series.id, series, startingTab)

    override val key: ScreenKey = seriesId.toString()

    @OptIn(InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(seriesId.value) {
            viewModelFactory.getSeriesViewModel(seriesId, series, startingTab)
        }
        LaunchedEffect(seriesId) { vm.initialize() }

        when (val state = vm.state.collectAsState().value) {
            is Error -> ErrorContent(
                message = state.exception.message ?: "Unknown Error",
                onReload = vm::reload
            )

            else -> {
                SeriesContent(
                    series = vm.series.collectAsState().value,
                    seriesMenuActions = vm.seriesMenuActions(),
                    onFilterClick = { filter ->
                        val series = requireNotNull(vm.series.value)
                        navigator.popUntilRoot()
                        navigator.dispose(navigator.lastItem)
                        navigator.replaceAll(LibraryScreen(series.libraryId, filter))
                    },

                    currentTab = vm.currentTab,
                    onTabChange = vm::onTabChange,

                    booksState = vm.booksState,
                    onBookClick = { navigator push BookScreen(it) },
                    onBookReadClick = { navigator.parent?.replace(ReaderScreen(it.id)) },

                    collectionsState = vm.collectionsState,
                    onCollectionClick = { collection -> navigator.push(CollectionScreen(collection.id)) },
                    onSeriesClick = { series -> navigator.push(SeriesScreen(series, vm.currentTab)) },

                    onBackButtonClick = {
                        vm.series.value?.let { onBackPress(navigator, it.libraryId) }
                    },
                )
            }
        }

        BackPressHandler {
            vm.series.value?.let { onBackPress(navigator, it.libraryId) }
        }
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
