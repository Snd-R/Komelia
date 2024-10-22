package io.github.snd_r.komelia.ui.readlist

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.book.bookScreen
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.reader.readerScreen
import snd.komga.client.readlist.KomgaReadListId

class ReadListScreen(val readListId: KomgaReadListId) : Screen {

    override val key: ScreenKey = readListId.toString()

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm =
            rememberScreenModel(readListId.value) { viewModelFactory.getReadListViewModel(readListId) }
        LaunchedEffect(readListId) { vm.initialize() }

        val navigator = LocalNavigator.currentOrThrow

        when (vm.state.collectAsState().value) {
            Uninitialized -> LoadingMaxSizeIndicator()
            is Error -> Text("Error")
            is LoadState.Success, Loading -> {
                val readList = vm.readList
                if (readList == null) LoadingMaxSizeIndicator()
                else
                    ReadListContent(
                        readList = readList,
                        onReadListDelete = vm::onReadListDelete,

                        books = vm.books,
                        bookMenuActions = vm.bookMenuActions(),
                        onBookClick = { navigator push bookScreen(it) },
                        onBookReadClick = { book, markProgress ->
                            navigator.parent?.replace(readerScreen(book, markProgress))
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

                        onBackClick = { navigator.pop() },
                        cardMinSize = vm.cardWidth.collectAsState().value,
                    )
            }
        }

        BackPressHandler { navigator.pop() }

    }
}