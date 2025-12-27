package snd.komelia.ui.common.cards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.offline.sync.model.DownloadEvent
import snd.komelia.ui.LocalBookDownloadEvents
import snd.komelia.ui.LocalLibraries
import snd.komelia.ui.LocalWindowWidth
import snd.komelia.ui.common.BookReadButton
import snd.komelia.ui.common.components.NoPaddingChip
import snd.komelia.ui.common.images.BookThumbnail
import snd.komelia.ui.common.menus.BookActionsMenu
import snd.komelia.ui.common.menus.BookMenuActions
import snd.komelia.ui.common.readIsSupported
import snd.komelia.ui.platform.WindowSizeClass.COMPACT
import snd.komelia.ui.platform.WindowSizeClass.MEDIUM
import snd.komelia.ui.platform.cursorForHand

@Composable
fun BookImageCard(
    book: KomeliaBook,
    bookMenuActions: BookMenuActions? = null,
    onBookClick: (() -> Unit)? = null,
    onBookReadClick: ((markProgress: Boolean) -> Unit)? = null,
    isSelected: Boolean = false,
    onSelect: (() -> Unit)? = null,
    showSeriesTitle: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val libraries = LocalLibraries.current
    val libraryIsDeleted = remember {
        libraries.value.firstOrNull { it.id == book.libraryId }?.unavailable ?: false
    }
    ItemCard(
        modifier = modifier,
        onClick = onBookClick,
        onLongClick = onSelect,
        image = {
            BookHoverOverlay(
                book = book,
                libraryIsDeleted = libraryIsDeleted,
                bookMenuActions = bookMenuActions,
                onBookReadClick = onBookReadClick,
                onSelect = onSelect,
                isSelected = isSelected,
            ) {
                BookImageOverlay(
                    book = book,
                    libraryIsDeleted = libraryIsDeleted,
                    showSeriesTitle = showSeriesTitle,
                ) {
                    BookThumbnail(
                        book.id,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    )
}

@Composable
fun BookSimpleImageCard(
    book: KomeliaBook,
    onBookClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ItemCard(
        modifier = modifier,
        onClick = onBookClick,
        image = {
            BookImageOverlay(
                book = book,
                libraryIsDeleted = false,
                showTitle = false
            ) {
                BookThumbnail(
                    book.id,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    )
}

@Composable
private fun BookImageOverlay(
    book: KomeliaBook,
    libraryIsDeleted: Boolean,
    showTitle: Boolean = true,
    showSeriesTitle: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(contentAlignment = Alignment.TopStart) {
        content()
        if (showTitle)
            CardGradientOverlay()
        Column {
            Row {
                if (book.downloaded) {
                    val tint =
                        if (book.isLocalFileOutdated || book.remoteFileUnavailable) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.secondary
                    Icon(
                        imageVector = Icons.Filled.OfflinePin,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier
                            .padding(1.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                    )
                }

                Spacer(Modifier.weight(1f))
                if (book.readProgress == null) BookUnreadTick()
            }

            Spacer(modifier = Modifier.weight(1f))
            Column(Modifier.padding(10.dp)) {
                if (showSeriesTitle && !book.oneshot) {
                    CardOutlinedText(
                        text = book.seriesTitle,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium.copy(color = Color(195, 195, 195)),
                    )
                }
                if (showTitle) {
                    CardOutlinedText(
                        text = book.metadata.title,
                        maxLines = 3
                    )
                }
                if (book.deleted || libraryIsDeleted) {
                    CardOutlinedText(
                        text = "Unavailable",
                        textColor = MaterialTheme.colorScheme.error
                    )
                }
            }

            val readProgress = book.readProgress
            if (readProgress != null && !readProgress.completed) {
                LinearProgressIndicator(
                    progress = { getReadProgressPercentage(book) },
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                    modifier = Modifier.height(6.dp).fillMaxWidth().background(Color.Black),
                    drawStopIndicator = {}
                )
            }
        }
        BookDownloadCardOverlay(book)

    }
}

@Composable
private fun BookDownloadCardOverlay(book: KomeliaBook) {
    val downloadEvents = LocalBookDownloadEvents.current
    var downloadEvent: DownloadEvent? by remember { mutableStateOf(null) }
    LaunchedEffect(downloadEvents, book) {
        downloadEvents?.filter { it.bookId == book.id }?.collect { downloadEvent = it }
    }

    when (val event = downloadEvent) {
        is DownloadEvent.BookDownloadCompleted -> {}
        is DownloadEvent.BookDownloadError -> {}
        is DownloadEvent.BookDownloadProgress -> {

            Box(
                modifier = Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .8f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.tertiary)
                CircularProgressIndicator(
                    progress = { event.completed / event.total.toFloat() },
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.primary,
                )
            }
        }

        null -> {}
    }

}

@Composable
private fun BookUnreadTick() {
    val color = MaterialTheme.colorScheme.tertiary
    Canvas(modifier = Modifier.size(30.dp)) {
        val trianglePath = Path().apply {
            moveTo(0f, 0f)
            lineTo(x = size.width, y = size.height)
            lineTo(x = size.width, y = size.height)
            lineTo(x = size.width, y = 0f)
        }

        drawPath(
            color = color,
            path = trianglePath
        )
    }
}

@Composable
private fun BookHoverOverlay(
    book: KomeliaBook,
    libraryIsDeleted: Boolean,
    bookMenuActions: BookMenuActions?,
    onBookReadClick: ((Boolean) -> Unit)?,
    isSelected: Boolean,
    onSelect: (() -> Unit)?,
    content: @Composable () -> Unit
) {
    var isActionsMenuExpanded by remember { mutableStateOf(false) }
    var isReadButtonExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState()
    val showOverlay = derivedStateOf { isHovered.value || isActionsMenuExpanded || isReadButtonExpanded || isSelected }

    val border =
        if (showOverlay.value) overlayBorderModifier() else Modifier

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hoverable(interactionSource)
            .then(border),
        contentAlignment = Alignment.Center
    ) {
        content()
        if (showOverlay.value) {
            val backgroundColor =
                if (isSelected)
                    Modifier.background(MaterialTheme.colorScheme.secondary.copy(alpha = .5f))
                else Modifier
            Column(backgroundColor.fillMaxSize()) {
                if (onSelect != null) {
                    SelectionRadioButton(
                        isSelected,
                        onSelect
                    )
                    Spacer(Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier.padding(vertical = 5.dp).fillMaxSize(),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    if (onBookReadClick != null && !book.deleted && readIsSupported(book) && !libraryIsDeleted) {
                        BookReadButton(
                            modifier = Modifier.padding(start = 5.dp, bottom = 5.dp),
                            onRead = { onBookReadClick(true) },
                            onIncognitoRead = { onBookReadClick(false) },
                            onDropdownOpenChange = { isReadButtonExpanded = it }
                        )
                    }

                    Spacer(Modifier.weight(1f))
                    if (bookMenuActions != null)
                        BookMenuActionsDropdown(
                            book = book,
                            bookMenuActions = bookMenuActions,
                            isActionsMenuExpanded = isActionsMenuExpanded,
                            onActionsMenuExpand = { isActionsMenuExpanded = it }
                        )
                }
            }
        }
    }
}

private fun getReadProgressPercentage(book: KomeliaBook): Float {
    val progress = book.readProgress ?: return 0f
    if (progress.completed) return 100f

    return progress.page / book.media.pagesCount.toFloat()
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookDetailedListCard(
    book: KomeliaBook,
    onClick: (() -> Unit)? = null,
    bookMenuActions: BookMenuActions? = null,
    onBookReadClick: ((Boolean) -> Unit)? = null,
    isSelected: Boolean = false,
    onSelect: (() -> Unit)? = null,
    downloaded: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState()
    Card(
        modifier
            .cursorForHand()
            .combinedClickable(onClick = onClick ?: {}, onLongClick = onSelect)
            .hoverable(interactionSource)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .heightIn(max = 220.dp)
                .fillMaxWidth()
                .then(
                    if (isSelected) Modifier.background(
                        MaterialTheme.colorScheme.secondary.copy(
                            alpha = .3f
                        )
                    )
                    else Modifier
                )
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                BookSimpleImageCard(book)
                if (onSelect != null && (isSelected || isHovered.value)) {
                    SelectionRadioButton(
                        isSelected,
                        onSelect
                    )
                }
            }
            BookDetailedListDetails(
                book = book,
                downloaded = downloaded,
                bookMenuActions = bookMenuActions,
                onBookReadClick = onBookReadClick,
                isSelected = isSelected,
                onSelect = onSelect,
            )
        }
    }

}

@Composable
private fun BookDetailedListDetails(
    book: KomeliaBook,
    downloaded: Boolean,
    bookMenuActions: BookMenuActions?,
    onBookReadClick: ((Boolean) -> Unit)? = null,
    isSelected: Boolean,
    onSelect: (() -> Unit)?,
) {
    val width = LocalWindowWidth.current
    Column(Modifier.padding(start = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                book.metadata.title,
                fontWeight = FontWeight.Bold,
                maxLines = when (width) {
                    COMPACT, MEDIUM -> 2
                    else -> 4
                }
            )
        }

        LazyRow(
            modifier = Modifier.padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Text(
                    "${book.media.pagesCount} pages",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            item {
                NoPaddingChip(
                    borderColor = MaterialTheme.colorScheme.surface,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        "downloaded",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            items(book.metadata.tags) {
                NoPaddingChip(
                    borderColor = MaterialTheme.colorScheme.surface,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            book.metadata.summary,
            maxLines = when (width) {
                COMPACT, MEDIUM -> 3
                else -> 4
            },
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 1500.dp)
        )

        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.Start) {
            if (onBookReadClick != null && !book.deleted && readIsSupported(book)) {
                BookReadButton(
                    modifier = Modifier.padding(start = 5.dp, bottom = 5.dp),
                    onRead = { onBookReadClick(true) },
                    onIncognitoRead = { onBookReadClick(false) }
                )
            }
            if (bookMenuActions != null) {
                Box {
                    var isMenuExpanded by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { isMenuExpanded = true },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.MoreVert, null)
                    }
                    BookActionsMenu(
                        book = book,
                        actions = bookMenuActions,
                        expanded = isMenuExpanded,
                        showEditOption = true,
                        showDownloadOption = true,
                        onDismissRequest = { isMenuExpanded = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun BookMenuActionsDropdown(
    book: KomeliaBook,
    bookMenuActions: BookMenuActions,
    isActionsMenuExpanded: Boolean,
    onActionsMenuExpand: (Boolean) -> Unit
) {
    Box {
        IconButton(
            onClick = { onActionsMenuExpand(true) },
            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Icon(Icons.Default.MoreVert, null)
        }

        BookActionsMenu(
            book = book,
            actions = bookMenuActions,
            expanded = isActionsMenuExpanded,
            showEditOption = true,
            showDownloadOption = true,
            onDismissRequest = { onActionsMenuExpand(false) },
        )
    }
}

