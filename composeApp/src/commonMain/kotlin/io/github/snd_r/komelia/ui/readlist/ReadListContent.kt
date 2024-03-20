package io.github.snd_r.komelia.ui.readlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.PaginationWithSizeOptions
import io.github.snd_r.komelia.ui.common.itemlist.BookLazyCardGrid
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.common.menus.ReadListActionsMenu
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.readlist.KomgaReadList

@Composable
fun ReadListContent(
    readList: KomgaReadList,
    onReadListDelete: () -> Unit,

    books: List<KomgaBook>,
    bookMenuActions: BookMenuActions,
    onBookClick: (KomgaBookId) -> Unit,
    onBookReadClick: (KomgaBookId) -> Unit,

    totalPages: Int,
    currentPage: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,

    onBackClick: () -> Unit,
    cardMinSize: Dp,
) {

    Column {
        ReadListToolbar(
            readList = readList,
            onReadListDelete = onReadListDelete,

            totalPages = totalPages,
            currentPage = currentPage,
            pageSize = pageSize,
            onPageChange = onPageChange,
            onPageSizeChange = onPageSizeChange,
            onBackClick = onBackClick
        )

        BookLazyCardGrid(
            books = books,
            bookMenuActions = bookMenuActions,
            onBookClick = onBookClick,
            onBookReadClick = onBookReadClick,
            minSize = cardMinSize,
        )
    }

}

@Composable
private fun ReadListToolbar(
    readList: KomgaReadList,
    onReadListDelete: () -> Unit,

    totalPages: Int,
    currentPage: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,

    onBackClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        IconButton(onClick = { onBackClick() }) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
        }

        Text(readList.name)

        Box {
            var expandActions by remember { mutableStateOf(false) }
            IconButton(onClick = { expandActions = true }) {
                Icon(Icons.Rounded.MoreVert, null)
            }

            ReadListActionsMenu(
                readList = readList,
                onReadListDelete = onReadListDelete,
                expanded = expandActions,
                onDismissRequest = { expandActions = false }
            )
        }

        SuggestionChip(
            onClick = {},
            label = { Text("${readList.bookIds.size} books") },
            modifier = Modifier.padding(10.dp, 0.dp),
        )

        PaginationWithSizeOptions(
            totalPages = totalPages,
            currentPage = currentPage,
            onPageChange = onPageChange,
            navigationButtons = false,
            pageSize = pageSize,
            onPageSizeChange = onPageSizeChange,
            spacer = { Spacer(Modifier.weight(1f)) }
        )

    }
}
