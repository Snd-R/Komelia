package io.github.snd_r.komelia.ui.oneshot

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
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.book.bookScreen
import io.github.snd_r.komelia.ui.collection.CollectionScreen
import io.github.snd_r.komelia.ui.common.ErrorContent
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.library.LibraryScreen
import io.github.snd_r.komelia.ui.reader.image.readerScreen
import io.github.snd_r.komelia.ui.readlist.ReadListScreen
import io.github.snd_r.komelia.ui.search.SearchScreen
import io.github.snd_r.komelia.ui.series.seriesScreen
import snd.komga.client.book.KomgaBook
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId
import kotlin.jvm.Transient

class OneshotScreen(
    val seriesId: KomgaSeriesId,
    @Transient private val series: KomgaSeries? = null,
    @Transient private val book: KomgaBook? = null,
) : Screen {
    constructor(series: KomgaSeries) : this(series.id, series, null)
    constructor(book: KomgaBook) : this(book.seriesId, null, book)

    override val key: ScreenKey = seriesId.toString()

    @OptIn(InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(seriesId.value) {
            viewModelFactory.getOneshotViewModel(seriesId, series, book)
        }
        LaunchedEffect(seriesId) { vm.initialize() }

        val state = vm.state.collectAsState().value
        val book = vm.book.collectAsState().value
        val series = vm.series.collectAsState().value
        when {
            state is LoadState.Error -> ErrorContent(
                message = state.exception.message ?: "Unknown Error",
                onReload = vm::reload
            )

            book == null || series == null -> LoadingMaxSizeIndicator()
            else -> OneshotScreenContent(
                series = series,
                book = book,
                libraryIsDeleted = vm.libraryIsDeleted.collectAsState().value,
                oneshotMenuActions = vm.bookMenuActions,
                onBackButtonClick = {
                    vm.series.value?.let { onBackPress(navigator, it.libraryId) }
                },
                onBookReadPress = { markReadProgress ->
                    navigator.parent?.replace(readerScreen(book, markReadProgress))
                },
                collections = vm.collectionsState.collections,
                onCollectionClick = { collection -> navigator.push(CollectionScreen(collection.id)) },
                onSeriesClick = { navigator.push(seriesScreen(it)) },

                readLists = vm.readListsState.readLists,
                onReadListClick = { navigator.push(ReadListScreen(it.id)) },
                onBookClick = { navigator push bookScreen(it) },
                onFilterClick = { filter ->
                    navigator.popUntilRoot()
                    navigator.dispose(navigator.lastItem)
                    navigator.replaceAll(LibraryScreen(book.libraryId, filter))
                },

                cardWidth = vm.cardWidth.collectAsState().value,
            )
        }
        BackPressHandler {
            vm.series.value?.let { onBackPress(navigator, it.libraryId) }
        }
    }

    private fun onBackPress(navigator: Navigator, libraryId: KomgaLibraryId?) {
        val success = navigator.popUntil {
            (it is LibraryScreen && it.libraryId == libraryId)
                    || it is CollectionScreen || it is ReadListScreen || it is SearchScreen
        }
        if (!success) {
            libraryId?.let { navigator replaceAll LibraryScreen(it) }
        }
    }
}