package io.github.snd_r.komelia.ui.book

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.platform.WindowWidth.COMPACT
import io.github.snd_r.komelia.platform.WindowWidth.EXPANDED
import io.github.snd_r.komelia.platform.WindowWidth.FULL
import io.github.snd_r.komelia.platform.WindowWidth.MEDIUM
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.common.BookReadButton
import io.github.snd_r.komelia.ui.common.ExpandableText
import io.github.snd_r.komelia.ui.common.images.BookThumbnail
import io.github.snd_r.komelia.ui.common.menus.BookActionsMenu
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.dialogs.book.edit.BookEditDialog
import io.github.snd_r.komelia.ui.readlist.BookReadListsContent
import snd.komga.client.book.KomgaBook
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.readlist.KomgaReadList

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookScreenContent(
    library: KomgaLibrary?,
    book: KomgaBook?,
    bookMenuActions: BookMenuActions,
    onBackButtonClick: () -> Unit,
    onBookReadPress: (markReadProgress: Boolean) -> Unit,

    readLists: Map<KomgaReadList, List<KomgaBook>>,
    onReadListClick: (KomgaReadList) -> Unit,
    onBookClick: (KomgaBook) -> Unit,
    cardWidth: Dp
) {

    val scrollState: ScrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize()) {
        if (book == null || library == null) return
        BookToolBar(
            book = book,
            bookMenuActions = bookMenuActions,
            onBackButtonClick = onBackButtonClick
        )

        val contentPadding = when (LocalWindowWidth.current) {
            COMPACT, MEDIUM -> Modifier.padding(5.dp)
            EXPANDED -> Modifier.padding(start = 20.dp, end = 20.dp)
            FULL -> Modifier.padding(start = 30.dp, end = 30.dp)
        }

        Box {
            Column(
                modifier = contentPadding
                    .fillMaxWidth()
                    .verticalScroll(state = scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.Start
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    BookThumbnail(
                        book.id,
                        modifier = Modifier
                            .heightIn(min = 100.dp, max = 400.dp)
                            .widthIn(min = 300.dp, max = 500.dp)
                            .animateContentSize()
                    )
                    BookMainInfo(book, onBookReadPress)
                }

                BookInfoColumn(
                    publisher = null,
                    genres = null,
                    authors = book.metadata.authors,
                    tags = book.metadata.tags,
                    links = book.metadata.links,
                    sizeInMiB = book.size,
                    mediaType = book.media.mediaType,
                    isbn = book.metadata.isbn,
                    fileUrl = book.url
                )
                BookReadListsContent(
                    readLists = readLists,
                    onReadListClick = onReadListClick,
                    onBookClick = onBookClick,
                    cardWidth = cardWidth
                )
            }
            VerticalScrollbar(scrollState, Modifier.align(Alignment.CenterEnd))
        }
    }
}

@Composable
fun BookToolBar(
    book: KomgaBook,
    bookMenuActions: BookMenuActions,
    onBackButtonClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onBackButtonClick() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
        }
        Text(
            book.metadata.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, false)
        )
        ToolbarBookActions(book, bookMenuActions)
    }
}

@Composable
private fun ToolbarBookActions(
    book: KomgaBook,
    bookMenuActions: BookMenuActions,
) {
    Row {
        Box {
            var expandActions by remember { mutableStateOf(false) }
            IconButton(onClick = { expandActions = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
            BookActionsMenu(
                book = book,
                actions = bookMenuActions,
                expanded = expandActions,
                onDismissRequest = { expandActions = false }
            )
        }

        var showEditDialog by remember { mutableStateOf(false) }
        IconButton(onClick = { showEditDialog = true }) {
            Icon(Icons.Default.Edit, null)
        }
        if (showEditDialog) {
            BookEditDialog(book = book, onDismissRequest = { showEditDialog = false })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowScope.BookMainInfo(
    book: KomgaBook,
    onBookReadPress: (markReadProgress: Boolean) -> Unit,
) {
    val maxWidth = when (LocalWindowWidth.current) {
        FULL -> 1200.dp
        else -> Dp.Unspecified
    }

    Column(
        modifier = Modifier.weight(1f, false).widthIn(min = 450.dp, max = maxWidth),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BookInfoRow(
            seriesTitle = book.seriesTitle,
            readProgress = book.readProgress,
            bookPagesCount = book.media.pagesCount,
            bookNumber = book.metadata.number,
            releaseDate = book.metadata.releaseDate
        )

        BookReadButton { onBookReadPress(true) }
        HorizontalDivider()
        ExpandableText(
            text = book.metadata.summary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

