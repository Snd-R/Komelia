package io.github.snd_r.komelia.ui.series

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.ui.BookSiblingsContext
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LocalReloadEvents
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.ReloadableScreen
import io.github.snd_r.komelia.ui.book.bookScreen
import io.github.snd_r.komelia.ui.collection.CollectionScreen
import io.github.snd_r.komelia.ui.common.ErrorContent
import io.github.snd_r.komelia.ui.common.ScreenPullToRefreshBox
import io.github.snd_r.komelia.ui.library.LibraryScreen
import io.github.snd_r.komelia.ui.oneshot.OneshotScreen
import io.github.snd_r.komelia.ui.reader.image.readerScreen
import io.github.snd_r.komelia.ui.series.SeriesViewModel.SeriesTab
import io.github.snd_r.komelia.ui.series.view.SeriesContent
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId
import kotlin.jvm.Transient

fun seriesScreen(series: KomgaSeries): Screen =
    if (series.oneshot) OneshotScreen(series, BookSiblingsContext.Series)
    else SeriesScreen(series)

class SeriesScreen(
    val seriesId: KomgaSeriesId,
    @Transient
    private val series: KomgaSeries? = null,
    @Transient
    private val startingTab: SeriesTab? = SeriesTab.BOOKS,
) : ReloadableScreen {

    constructor(series: KomgaSeries, startingTab: SeriesTab = SeriesTab.BOOKS) : this(
        series.id,
        series,
        startingTab
    )

    override val key: ScreenKey = seriesId.toString()

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(seriesId.value) {
            viewModelFactory.getSeriesViewModel(seriesId, series, startingTab)
        }
        val reloadEvents = LocalReloadEvents.current
        LaunchedEffect(seriesId) {
            vm.initialize()
            val series = vm.series.value
            if (series != null && series.oneshot) {
                navigator.replace(OneshotScreen(series, BookSiblingsContext.Series))
                return@LaunchedEffect
            }
            reloadEvents.collect { vm.reload() }
        }

        DisposableEffect(Unit) {
            vm.startKomgaEventHandler()
            onDispose { vm.stopKomgaEventHandler() }
        }

        ScreenPullToRefreshBox(screenState = vm.state, onRefresh = vm::reload) {
            when (val state = vm.state.collectAsState().value) {
                is Error -> ErrorContent(
                    message = state.exception.message ?: "Unknown Error",
                    onReload = vm::reload
                )

                else -> {
                    SeriesContent(
                        series = vm.series.collectAsState().value,
                        library = vm.library.collectAsState().value,
                        onLibraryClick = { navigator.push(LibraryScreen(it.id)) },
                        seriesMenuActions = vm.seriesMenuActions(),
                        onFilterClick = { filter ->
                            val series = requireNotNull(vm.series.value)
                            navigator.push(LibraryScreen(series.libraryId, filter))
                        },

                        currentTab = vm.currentTab,
                        onTabChange = vm::onTabChange,

                        booksState = vm.booksState,
                        onBookClick = { navigator push bookScreen(it) },
                        onBookReadClick = { book, markProgress ->
                            navigator.parent?.push(readerScreen(book, markProgress))
                        },

                        collectionsState = vm.collectionsState,
                        onCollectionClick = { collection -> navigator.push(CollectionScreen(collection.id)) },
                        onSeriesClick = { series ->
                            navigator.push(
                                if (series.oneshot) OneshotScreen(series, BookSiblingsContext.Series)
                                else SeriesScreen(series, vm.currentTab)
                            )
                        },
                    )
                }
            }

            BackPressHandler {
                vm.series.value?.let { onBackPress(navigator, it.libraryId) }
            }
        }
    }

    private fun onBackPress(navigator: Navigator, libraryId: KomgaLibraryId?) {
        if (navigator.canPop) {
            navigator.pop()
        } else {
            libraryId?.let { navigator replaceAll LibraryScreen(it) }
        }
    }
}
