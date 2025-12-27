package snd.komelia.ui.readlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import snd.komelia.ui.BookSiblingsContext
import snd.komelia.ui.LoadState
import snd.komelia.ui.LoadState.Error
import snd.komelia.ui.LoadState.Loading
import snd.komelia.ui.LoadState.Uninitialized
import snd.komelia.ui.LocalReloadEvents
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.ReloadableScreen
import snd.komelia.ui.book.bookScreen
import snd.komelia.ui.common.components.ErrorContent
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.platform.BackPressHandler
import snd.komelia.ui.platform.ScreenPullToRefreshBox
import snd.komelia.ui.reader.readerScreen
import snd.komga.client.readlist.KomgaReadListId

class ReadListScreen(val readListId: KomgaReadListId) : ReloadableScreen {

    override val key: ScreenKey = readListId.toString()

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(readListId.value) { viewModelFactory.getReadListViewModel(readListId) }
        val reloadEvents = LocalReloadEvents.current
        LaunchedEffect(readListId) {
            vm.initialize()
            reloadEvents.collect { vm.reload() }
        }
        DisposableEffect(Unit) {
            vm.startKomgaEventHandler()
            onDispose { vm.stopKomgaEventHandler() }
        }

        val navigator = LocalNavigator.currentOrThrow

        ScreenPullToRefreshBox(screenState = vm.state, onRefresh = vm::reload) {
            when (val state = vm.state.collectAsState().value) {
                Uninitialized -> LoadingMaxSizeIndicator()
                is Error -> ErrorContent(
                    message = state.exception.message ?: "Unknown Error",
                    onReload = vm::reload
                )

                is LoadState.Success, Loading -> {
                    val readList = vm.readList
                    if (readList == null) LoadingMaxSizeIndicator()
                    else
                        ReadListContent(
                            readList = readList,
                            onReadListDelete = vm::onReadListDelete,

                            books = vm.books,
                            bookMenuActions = vm.bookMenuActions(),
                            onBookClick = { navigator push bookScreen(it, BookSiblingsContext.ReadList(readListId)) },
                            onBookReadClick = { book, markProgress ->
                                navigator.parent?.push(
                                    readerScreen(
                                        book = book,
                                        markReadProgress = markProgress,
                                        bookSiblingsContext = BookSiblingsContext.ReadList(readListId)
                                    )
                                )
                            },

                            selectedBooks = vm.selectedBooks,
                            onBookSelect = vm::onBookSelect,

                            editMode = vm.isInEditMode,
                            onEditModeChange = vm::setEditMode,
                            onReorder = vm::onBookReorder,
                            onReorderDragStateChange = vm::onSeriesReorderDragStateChange,

                            totalPages = vm.totalBookPages,
                            currentPage = vm.currentBookPage,
                            pageSize = vm.pageLoadSize,
                            onPageChange = vm::onPageChange,
                            onPageSizeChange = vm::onPageSizeChange,

                            cardMinSize = vm.cardWidth.collectAsState().value,
                        )
                }
            }

            BackPressHandler { navigator.pop() }

        }
    }
}