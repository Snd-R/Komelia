package io.github.snd_r.komelia.ui.readlist

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.book.BookScreen
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.reader.view.ReaderScreen
import io.github.snd_r.komga.readlist.KomgaReadListId

class ReadListScreen(val readListId: KomgaReadListId) : Screen {
    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(readListId.value) { viewModelFactory.getReadListViewModel(readListId) }
        LaunchedEffect(readListId) { vm.initialize() }

        val navigator = LocalNavigator.currentOrThrow

        when (vm.state.collectAsState().value) {
            Uninitialized -> LoadingMaxSizeIndicator()
            is Error -> Text("Error")
            is LoadState.Success, Loading -> ReadListContent(
                readList = vm.readList,
                onReadListDelete = vm::onReadListDelete,

                books = vm.books,
                bookMenuActions = vm.bookMenuActions(),
                onBookClick = { navigator push BookScreen(it) },
                onBookReadClick = { navigator.parent?.replace(ReaderScreen(it)) },

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
}