package io.github.snd_r.komelia.ui.common.cards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.common.images.BookThumbnail
import io.github.snd_r.komelia.ui.common.menus.BookActionsMenu
import io.github.snd_r.komelia.ui.common.menus.BookMenuActions
import io.github.snd_r.komelia.ui.platform.cursorForHand
import io.github.snd_r.komga.book.KomgaBook

@Composable
fun BookImageCard(
    book: KomgaBook,
    onBookClick: () -> Unit,
    bookMenuActions: BookMenuActions?,
    onBookReadClick: () -> Unit,
    modifier: Modifier,
) {

    ItemCard(modifier, onBookClick) {
        BookHoverOverlay(
            book = book,
            bookMenuActions = bookMenuActions,
            onBookReadClick = onBookReadClick
        ) {
            BookImageOverlay(book) {
                BookThumbnail(book.id, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
        }
    }
}

@Composable
fun BookSimpleImageCard(
    book: KomgaBook,
    onBookClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ItemCard(modifier, onBookClick) {
        BookImageOverlay(book, false) {
            BookThumbnail(book.id, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }

    }

}

@Composable
private fun BookImageOverlay(
    book: KomgaBook,
    showTitle: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        content()
        if (showTitle)
            CardGradientOverlay()
        Column {
            if (book.readProgress == null)
                Row {
                    Spacer(Modifier.weight(1f))
                    BookUnreadTick()
                }

            Spacer(modifier = Modifier.weight(1f))
            Column(Modifier.padding(10.dp)) {
                if (showTitle) {
                    Text(
                        text = book.metadata.title,
                        maxLines = 4,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(-1f, -1f),
                                blurRadius = 0f
                            ),
                        ),
                    )
                }
                if (book.deleted) {
                    Text(
                        text = "Unavailable",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.error,
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(-1f, -1f),
                                blurRadius = 0f
                            ),
                        ),
                    )

                }
            }

            val readProgress = book.readProgress
            if (readProgress != null && !readProgress.completed) {
                LinearProgressIndicator(
                    progress = {getReadProgressPercentage(book)},
                    color = AppTheme.colors.material.tertiary,
                    trackColor = AppTheme.colors.material.tertiary.copy(alpha = 0.5f),
                    modifier = Modifier.height(6.dp).fillMaxWidth().background(Color.Black),
                )
            }
        }

    }
}

@Composable
private fun BookUnreadTick() {
    val color = AppTheme.colors.material.tertiary
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
    book: KomgaBook,
    bookMenuActions: BookMenuActions?,
    onBookReadClick: () -> Unit,
    content: @Composable () -> Unit
) {
    var isActionsMenuExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState()
    val showOverlay = derivedStateOf { isHovered.value || isActionsMenuExpanded }

    val border = if (showOverlay.value) overlayBorderModifier() else Modifier

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hoverable(interactionSource)
            .then(border),
        contentAlignment = Alignment.Center
    ) {
        content()
        if (showOverlay.value) {
            if (bookMenuActions != null) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .fillMaxSize(),
                    verticalAlignment = Alignment.Bottom,
                ) {

                    FilledTonalButton(
                        modifier = Modifier.padding(horizontal = 5.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        onClick = { onBookReadClick() },
                        contentPadding = PaddingValues(vertical = 5.dp, horizontal = 15.dp)

                    ) {
                        Icon(Icons.AutoMirrored.Rounded.MenuBook, null)
                        Spacer(Modifier.width(10.dp))

                        Text("Read")
                    }

                    Spacer(Modifier.weight(1f))

                    Box {
                        IconButton(
                            onClick = { isActionsMenuExpanded = true },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = AppTheme.colors.material.surface)
                        ) {
                            Icon(Icons.Default.MoreVert, null)
                        }

                        BookActionsMenu(
                            book = book,
                            actions = bookMenuActions,
                            expanded = isActionsMenuExpanded,
                            onDismissRequest = { isActionsMenuExpanded = false },
                        )
                    }

                }
            }
        }

    }
}

private fun getReadProgressPercentage(book: KomgaBook): Float {
    val progress = book.readProgress ?: return 0f
    if (progress.completed) return 100f

    return progress.page / book.media.pagesCount.toFloat()
}


@Composable
fun BookDetailedListCard(
    book: KomgaBook,
    onClick: () -> Unit,
    bookMenuActions: BookMenuActions? = null,
    onBookReadClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {

    Card(modifier
        .cursorForHand()
        .clickable { onClick() }
        .fillMaxWidth()
    ) {
        Row(
            Modifier
//                .fillMaxWidth()
                .heightIn(max = 220.dp)
                .padding(10.dp)
        ) {
            BookSimpleImageCard(book)
            BookDetailedListDetails(
                book = book,
                bookMenuActions = bookMenuActions,
                onBookReadClick = onBookReadClick
            )
        }
    }

}

@Composable
private fun BookDetailedListDetails(
    book: KomgaBook,
    bookMenuActions: BookMenuActions?,
    onBookReadClick: (() -> Unit)? = null,
) {

    Column(Modifier.padding(start = 10.dp)) {
        BookDetailedListTitle(book, bookMenuActions)
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("${book.media.pagesCount} pages", style = MaterialTheme.typography.bodySmall)
        }

        Text(
            book.metadata.summary,
            maxLines = 4,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.widthIn(max = 1500.dp)
        )

        if (onBookReadClick != null) {
            Spacer(Modifier.weight(1f))
            FilledTonalButton(
                modifier = Modifier.padding(horizontal = 5.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                onClick = { onBookReadClick() },
                contentPadding = PaddingValues(vertical = 5.dp, horizontal = 15.dp)

            ) {
                Icon(Icons.AutoMirrored.Rounded.MenuBook, null)
                Spacer(Modifier.width(10.dp))

                Text("Read")
            }

        }

    }

}

@Composable
private fun BookDetailedListTitle(
    book: KomgaBook,
    bookMenuActions: BookMenuActions?,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        if (bookMenuActions != null) {
            Box {
                var isMenuExpanded by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { isMenuExpanded = true },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = AppTheme.colors.material.surfaceVariant)
                ) {
                    Icon(Icons.Default.MoreVert, null)
                }
                BookActionsMenu(
                    book = book,
                    actions = bookMenuActions,
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                )
            }
        }

        Text(book.metadata.title, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
    }
}