package io.github.snd_r.komelia.ui.book

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DisabledVisible
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.DescriptionChips
import io.github.snd_r.komelia.ui.common.ExpandableText
import io.github.snd_r.komelia.ui.common.LocalWindowSize
import io.github.snd_r.komelia.ui.common.ScrollableItemsRow
import io.github.snd_r.komelia.ui.common.WindowSize
import io.github.snd_r.komelia.ui.common.images.BookThumbnail
import io.github.snd_r.komelia.ui.common.menus.BookActionsMenu
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.dialogs.bookedit.BookEditDialog
import io.github.snd_r.komelia.ui.platform.ScrollBarConfig
import io.github.snd_r.komelia.ui.platform.cursorForHand
import io.github.snd_r.komelia.ui.platform.verticalScrollWithScrollbar
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.library.KomgaLibrary


@Composable
fun BookContent(
    library: KomgaLibrary?,
    book: KomgaBook?,
    bookMenuActions: BookMenuActions,
    onBackButtonClick: () -> Unit,
    onBookReadPress: (markReadProgress: Boolean) -> Unit,
) {

    val scrollState: ScrollState = rememberScrollState()
    val contentPadding = when (LocalWindowSize.current) {
        WindowSize.COMPACT, WindowSize.MEDIUM -> Modifier.padding(5.dp)
        WindowSize.EXPANDED -> Modifier.padding(20.dp)
        WindowSize.FULL -> Modifier.padding(30.dp)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (book == null || library == null) return
        BookToolBar(
            book = book,
            bookMenuActions = bookMenuActions,
            onBackButtonClick = onBackButtonClick
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScrollWithScrollbar(
                    state = scrollState,
                    scrollbarConfig = ScrollBarConfig(
                        indicatorColor = MaterialTheme.colorScheme.onSurface,
                        alpha = .8f
                    )
                ).then(contentPadding),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.Start
        ) {
            BookInfoUpperPanel(library, book, onBookReadPress)
            BookLowerPanelInfo(book)
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
        IconButton(
            onClick = { onBackButtonClick() }
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = null,
            )
        }

        Box {
            var expandActions by remember { mutableStateOf(false) }
            IconButton(
                onClick = { expandActions = true }
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = null,
                )
            }
            BookActionsMenu(
                book = book,
                actions = bookMenuActions,
                expanded = expandActions,
                onDismissRequest = { expandActions = false }
            )
        }

        var showEditDialog by remember { mutableStateOf(false) }
        IconButton(
            onClick = { showEditDialog = true }
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
            )
        }
        if (showEditDialog) {
            BookEditDialog(book = book, onDismissRequest = { showEditDialog = false })
        }

        Spacer(modifier = Modifier.weight(1.0f))



        IconButton(
            onClick = {}
        ) {
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = null,
            )
        }

        IconButton(
            onClick = {}
        ) {
            Icon(
                Icons.Default.Menu,
                contentDescription = null,
            )
        }

        IconButton(
            onClick = {}
        ) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
            )
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookInfoUpperPanel(
    library: KomgaLibrary,
    book: KomgaBook,
    onBookReadPress: (markReadProgress: Boolean) -> Unit,
) {
    FlowRow {
        Column {
            BookThumbnail(
                book.id,
                modifier = Modifier
                    .heightIn(min = 100.dp, max = 400.dp)
                    .widthIn(min = 300.dp, max = 500.dp)
                    .animateContentSize()
            )
            val readProgress = book.readProgress
            if (readProgress != null) {
                if (!readProgress.completed) {
                    Text(
                        "Read progress ${readProgress.page} / ${book.media.pagesCount}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        "${book.media.pagesCount - readProgress.page} pages left",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(5.dp))
                }
                Text(
                    "Last read on ${readProgress.readDate}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        val contentSize = when (LocalWindowSize.current) {
            WindowSize.COMPACT, WindowSize.MEDIUM -> Modifier.padding(10.dp, 0.dp)
            WindowSize.EXPANDED -> Modifier.padding(20.dp, 0.dp).fillMaxSize(0.8f)
            WindowSize.FULL -> Modifier.padding(30.dp, 0.dp).fillMaxSize(0.7f)
        }
        Column(
            modifier = contentSize.weight(1f, false).widthIn(min = 500.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    book.seriesTitle,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f, false)
                )
            }

            Text(book.metadata.title, style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "book #${book.metadata.number} · ${book.media.pagesCount} pages",
                    style = MaterialTheme.typography.labelSmall,
                )

                book.metadata.releaseDate?.let {
                    Text(
                        text = "release date: $it",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(
                    onClick = { onBookReadPress(true) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.cursorForHand(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                ) {
                    Row {
                        Icon(Icons.Rounded.Book, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text("READ", style = MaterialTheme.typography.labelMedium)
                    }
                }

                FilledTonalButton(
                    onClick = { onBookReadPress(false) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.cursorForHand(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                ) {
                    Row {
                        Icon(Icons.Default.DisabledVisible, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text("READ Incognito", style = MaterialTheme.typography.labelMedium)
                    }
                }

                FilledTonalButton(
                    onClick = {},
                    enabled = false,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Row {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text("DOWNLOAD", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }


            ExpandableText(book.metadata.summary, style = MaterialTheme.typography.bodyMedium)
        }
    }

}

@Composable
private fun BookLowerPanelInfo(book: KomgaBook) {
    Column(
        modifier = Modifier.padding(0.dp, 30.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        book.metadata.authors.groupBy { it.role }.forEach { (role, authors) ->
            DescriptionChips(role.uppercase(), authors.map { it.name })
        }
        Spacer(Modifier.size(10.dp))

        DescriptionChips("Tags".uppercase(), book.metadata.tags)

        DescriptionChips("Links".uppercase(), book.metadata.links.map { it.label })

        ScrollableItemsRow("SIZE") {
            Text(book.size, style = MaterialTheme.typography.bodySmall)
        }
        ScrollableItemsRow("FORMAT") { Text(book.media.mediaType, style = MaterialTheme.typography.bodySmall) }

        book.metadata.isbn.ifBlank { null }?.let {
            ScrollableItemsRow("ISBN") { Text(it, style = MaterialTheme.typography.bodySmall) }
        }

        ScrollableItemsRow("FILE") { Text(book.url, style = MaterialTheme.typography.bodySmall) }
    }
}