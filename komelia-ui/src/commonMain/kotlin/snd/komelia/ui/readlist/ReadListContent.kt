package snd.komelia.ui.readlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.LocalKomgaState
import snd.komelia.ui.LocalWindowWidth
import snd.komelia.ui.common.components.PageSizeSelectionDropdown
import snd.komelia.ui.common.itemlist.BookLazyCardGrid
import snd.komelia.ui.common.menus.BookMenuActions
import snd.komelia.ui.common.menus.ReadListActionsMenu
import snd.komelia.ui.common.menus.bulk.BottomPopupBulkActionsPanel
import snd.komelia.ui.common.menus.bulk.BulkActionsContainer
import snd.komelia.ui.common.menus.bulk.ReadListBulkActionsContent
import snd.komelia.ui.platform.WindowSizeClass
import snd.komga.client.readlist.KomgaReadList

@Composable
fun ReadListContent(
    readList: KomgaReadList,
    onReadListDelete: () -> Unit,

    books: List<KomeliaBook>,
    bookMenuActions: BookMenuActions,
    onBookClick: (KomeliaBook) -> Unit,
    onBookReadClick: (KomeliaBook, Boolean) -> Unit,

    selectedBooks: List<KomeliaBook>,
    onBookSelect: (KomeliaBook) -> Unit,

    editMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onReorderDragStateChange: (dragging: Boolean) -> Unit = {},

    totalPages: Int,
    currentPage: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,

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
        else {
            ReadListToolbar(
                readList = readList,
                onReadListDelete = onReadListDelete,
                onEditModeEnable = { onEditModeChange(true) },

                pageSize = pageSize,
                onPageSizeChange = onPageSizeChange,
            )
        }

        if (readList.summary.isNotBlank()) {
            Text(readList.summary)
            Spacer(Modifier.height(5.dp))
            HorizontalDivider()
        }
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
        if ((width == WindowSizeClass.COMPACT || width == WindowSizeClass.MEDIUM) && selectedBooks.isNotEmpty()) {
            BottomPopupBulkActionsPanel {
                ReadListBulkActionsContent(readList, books, true)
            }
        }
    }
}

@Composable
private fun ReadListToolbar(
    readList: KomgaReadList,
    onReadListDelete: () -> Unit,
    onEditModeEnable: () -> Unit,
    pageSize: Int,
    onPageSizeChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.padding(start = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                "read list",
                style = MaterialTheme.typography.labelMedium,
                fontStyle = FontStyle.Italic
            )
            Text(readList.name)
        }

        SuggestionChip(
            onClick = {},
            label = { Text("${readList.bookIds.size} books", style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.padding(10.dp, 0.dp),
        )

        val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true
        if (isAdmin) {
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
            IconButton(onClick = onEditModeEnable) { Icon(Icons.Default.EditNote, null) }
        }

        Spacer(Modifier.weight(1f))
        PageSizeSelectionDropdown(pageSize, onPageSizeChange)
    }
}

@Composable
private fun BulkActionsToolbar(
    onCancel: () -> Unit,
    readList: KomgaReadList,
    books: List<KomeliaBook>,
    selectedBooks: List<KomeliaBook>,
    onBookSelect: (KomeliaBook) -> Unit,
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
            WindowSizeClass.FULL -> {
                if (readList.ordered) Text("Edit mode: Click to select, drag to change order")
                else Text("Selection mode: Click on items to select or deselect them")
                if (selectedBooks.isNotEmpty()) {
                    Spacer(Modifier.weight(1f))

                    ReadListBulkActionsContent(readList, books, false)
                }
            }

            WindowSizeClass.EXPANDED -> {
                if (selectedBooks.isEmpty()) {
                    if (readList.ordered) Text("Edit mode: Click to select, drag to change order")
                    else Text("Selection mode: Click on items to select or deselect them")
                } else {
                    Spacer(Modifier.weight(1f))
                    ReadListBulkActionsContent(readList, books, false)
                }
            }

            WindowSizeClass.COMPACT, WindowSizeClass.MEDIUM -> {}
        }
    }
}
