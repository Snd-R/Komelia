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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.WindowWidth.COMPACT
import io.github.snd_r.komelia.platform.WindowWidth.EXPANDED
import io.github.snd_r.komelia.platform.WindowWidth.FULL
import io.github.snd_r.komelia.platform.WindowWidth.MEDIUM
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.common.DescriptionChips
import io.github.snd_r.komelia.ui.common.ExpandableText
import io.github.snd_r.komelia.ui.common.LabeledEntry.Companion.stringEntry
import io.github.snd_r.komelia.ui.common.images.BookThumbnail
import io.github.snd_r.komelia.ui.common.menus.BookActionsMenu
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.dialogs.book.edit.BookEditDialog
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.common.coloristRole
import io.github.snd_r.komga.common.coverRole
import io.github.snd_r.komga.common.editorRole
import io.github.snd_r.komga.common.inkerRole
import io.github.snd_r.komga.common.lettererRole
import io.github.snd_r.komga.common.pencillerRole
import io.github.snd_r.komga.common.translatorRole
import io.github.snd_r.komga.common.writerRole
import io.github.snd_r.komga.library.KomgaLibrary


private val authorsOrder = listOf(
    writerRole,
    pencillerRole,
    inkerRole,
    coloristRole,
    lettererRole,
    coverRole,
    editorRole,
    translatorRole
)

@Composable
fun BookContent(
    library: KomgaLibrary?,
    book: KomgaBook?,
    bookMenuActions: BookMenuActions,
    onBackButtonClick: () -> Unit,
    onBookReadPress: (markReadProgress: Boolean) -> Unit,
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
            COMPACT, MEDIUM -> Modifier.padding(10.dp, 5.dp)
            EXPANDED -> Modifier.padding(20.dp, 5.dp)
            FULL -> Modifier.padding(30.dp, 5.dp)
        }

        Column(
            modifier = contentPadding
                .fillMaxWidth()
                .verticalScroll(state = scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp),
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

@Composable
private fun ToolbarBooksNavigation() {
    Row(
        horizontalArrangement = Arrangement.End,
    ) {
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
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column {
            BookThumbnail(
                book.id,
                modifier = Modifier
                    .heightIn(min = 100.dp, max = 400.dp)
                    .widthIn(min = 300.dp, max = 500.dp)
                    .animateContentSize()
            )

        }

        val contentSize =
            if (LocalWindowWidth.current == FULL) Modifier.fillMaxSize(.7f)
            else Modifier

        Column(
            modifier = contentSize.weight(1f, false).widthIn(min = 200.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "\"${book.seriesTitle}\" series",
                style = MaterialTheme.typography.titleMedium,
            )

            Column {
                val readProgress = book.readProgress
                if (readProgress != null) {
                    if (!readProgress.completed) {
                        Text(
                            "Read progress ${readProgress.page} / ${book.media.pagesCount}; ${book.media.pagesCount - readProgress.page} pages left",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        "Last read on ${readProgress.readDate}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Column {
                Text(
                    text = "book #${book.metadata.number} · ${book.media.pagesCount} pages",
                    style = MaterialTheme.typography.bodyMedium,
                )

                book.metadata.releaseDate?.let {
                    Text(
                        text = "release date: $it",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            HorizontalDivider()
            ExpandableText(book.metadata.summary, style = MaterialTheme.typography.bodyMedium)

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
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
                        Icon(
                            Icons.Default.DisabledVisible,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
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

        }
    }
}


@Composable
private fun BookLowerPanelInfo(book: KomgaBook) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        val authorEntries = remember(book) {
            book.metadata.authors
                .groupBy { it.role }
                .map { (role, authors) ->
                    role to authors.map { stringEntry(it.name) }
                }.sortedBy { (role, _) -> authorsOrder.indexOf(role) }
        }
        authorEntries.forEach { (role, authors) ->
            DescriptionChips(
                label = role,
                chipValues = authors,
            )
        }
        Spacer(Modifier.size(10.dp))

        val tagEntries = remember(book) { book.metadata.tags.map { stringEntry(it) } }
        DescriptionChips("Tags".uppercase(), tagEntries)
        val linkEntries = remember(book) { book.metadata.links.map { stringEntry(it.label) } }
        DescriptionChips("Links".uppercase(), linkEntries)


        Row {
            Text("SIZE", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(150.dp))
            Text(book.size, style = MaterialTheme.typography.bodySmall)
        }

        Row {
            Text("FORMAT", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(150.dp))
            Text(book.media.mediaType, style = MaterialTheme.typography.bodySmall)
        }
        book.metadata.isbn.ifBlank { null }?.let { isbn ->
            Row {
                Text("ISBN", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(150.dp))
                Text(isbn, style = MaterialTheme.typography.bodySmall)
            }
        }

        Row {
            Text("FILE", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(150.dp))
            Text(book.url, style = MaterialTheme.typography.bodySmall)
        }
    }
}