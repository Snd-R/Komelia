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
import io.github.snd_r.komelia.platform.WindowWidth
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.common.PageSizeSelectionDropdown
import io.github.snd_r.komelia.ui.common.itemlist.BookLazyCardGrid
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.common.menus.ReadListActionsMenu
import io.github.snd_r.komelia.ui.common.menus.bulk.BooksBulkActionsContent
import io.github.snd_r.komelia.ui.common.menus.bulk.BottomPopupBulkActionsPanel
import io.github.snd_r.komelia.ui.common.menus.bulk.BulkActionsContainer
import io.github.snd_r.komelia.ui.common.menus.bulk.ReadListBulkActionsContent
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.readlist.KomgaReadList

@Composable
fun ReadListContent(
    readList: KomgaReadList,
    onReadListDelete: () -> Unit,

    books: List<KomgaBook>,
    bookMenuActions: BookMenuActions,
    onBookClick: (KomgaBook) -> Unit,
    onBookReadClick: (KomgaBook) -> Unit,

    selectedBooks: List<KomgaBook>,
    onBookSelect: (KomgaBook) -> Unit,

    editMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onReorderDragStateChange: (dragging: Boolean) -> Unit = {},

    totalPages: Int,
    currentPage: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,

    onBackClick: () -> Unit,
    cardMinSize: Dp,
) {

    Column {
        if (editMode)
            BulkActionsToolbar(
                onCancel = { onEditModeChange(false) },
                readList = readList,
                books = books,
                selectedBooks = selectedBooks,
                onBookSelect = onBookSelect
            )
        else
            ReadListToolbar(
                readList = readList,
                onReadListDelete = onReadListDelete,

                pageSize = pageSize,
                onPageSizeChange = onPageSizeChange,
                onBackClick = onBackClick
            )

        BookLazyCardGrid(
            books = books,
            onBookClick = if (editMode) onBookSelect else onBookClick,
            onBookReadClick = if (editMode) null else onBookReadClick,
            bookMenuActions = if (editMode) null else bookMenuActions,

            selectedBooks = selectedBooks,
            onBookSelect = onBookSelect,

            reorderable = readList.ordered && editMode,
            onReorder = onReorder,
            onReorderDragStateChange = onReorderDragStateChange,

            totalPages = totalPages,
            currentPage = currentPage,
            onPageChange = onPageChange,

            minSize = cardMinSize,
        )

        val width = LocalWindowWidth.current
        if ((width == WindowWidth.COMPACT || width == WindowWidth.MEDIUM) && selectedBooks.isNotEmpty()) {
            BottomPopupBulkActionsPanel {
                ReadListBulkActionsContent(readList, books, false)
                BooksBulkActionsContent(books, false)
            }
        }
    }
}

@Composable
private fun ReadListToolbar(
    readList: KomgaReadList,
    onReadListDelete: () -> Unit,
    pageSize: Int,
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

        Spacer(Modifier.weight(1f))
        PageSizeSelectionDropdown(pageSize, onPageSizeChange)
    }
}

@Composable
private fun BulkActionsToolbar(
    onCancel: () -> Unit,
    readList: KomgaReadList,
    books: List<KomgaBook>,
    selectedBooks: List<KomgaBook>,
    onBookSelect: (KomgaBook) -> Unit,
) {
    BulkActionsContainer(
        onCancel = onCancel,
        selectedCount = selectedBooks.size,
        allSelected = books.size == selectedBooks.size,
        onSelectAll = {
            if (books.size == selectedBooks.size) books.forEach { onBookSelect(it) }
            else books.filter { it !in selectedBooks }.forEach { onBookSelect(it) }
        }
    ) {
        when (LocalWindowWidth.current) {
            WindowWidth.FULL -> {
                if (readList.ordered) Text("Edit mode: Click to select, drag to change order")
                else Text("Selection mode: Click on items to select or deselect them")
                if (selectedBooks.isNotEmpty()) {
                    Spacer(Modifier.weight(1f))

                    ReadListBulkActionsContent(readList, books, true)
                    BooksBulkActionsContent(selectedBooks, true)
                }
            }

            WindowWidth.EXPANDED -> {
                if (selectedBooks.isEmpty()) {
                    if (readList.ordered) Text("Edit mode: Click to select, drag to change order")
                    else Text("Selection mode: Click on items to select or deselect them")
                } else {
                    Spacer(Modifier.weight(1f))
                    ReadListBulkActionsContent(readList, books, true)
                    BooksBulkActionsContent(selectedBooks, true)
                }
            }

            WindowWidth.COMPACT, WindowWidth.MEDIUM -> {}
        }
    }
}
