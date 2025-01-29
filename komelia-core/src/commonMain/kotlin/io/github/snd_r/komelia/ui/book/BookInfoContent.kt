package io.github.snd_r.komelia.ui.book

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.DefaultDateTimeFormats.localDateTimeFormat
import io.github.snd_r.komelia.ui.common.DescriptionChips
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.LabeledEntry.Companion.stringEntry
import io.github.snd_r.komelia.ui.common.TagList
import io.github.snd_r.komelia.ui.library.SeriesScreenFilter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import snd.komga.client.book.ReadProgress
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.common.KomgaWebLink
import snd.komga.client.common.coloristRole
import snd.komga.client.common.coverRole
import snd.komga.client.common.editorRole
import snd.komga.client.common.inkerRole
import snd.komga.client.common.lettererRole
import snd.komga.client.common.pencillerRole
import snd.komga.client.common.translatorRole
import snd.komga.client.common.writerRole
import kotlin.math.roundToInt

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
fun BookInfoColumn(
    publisher: String?,
    genres: List<String>?,
    authors: List<KomgaAuthor>,
    tags: List<String>,
    links: List<KomgaWebLink>,
    sizeInMiB: String,
    mediaType: String?,
    isbn: String,
    fileUrl: String,
    onFilterClick: (SeriesScreenFilter) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (!publisher.isNullOrBlank()) {
            DescriptionChips(
                label = "Publisher",
                chipValue = stringEntry(publisher),
                onClick = { onFilterClick(SeriesScreenFilter(publisher = listOf(it))) },
            )
        }

        val genreEntries = remember(genres) { genres?.map { stringEntry(it) } }
        if (genreEntries != null) {
            DescriptionChips(
                label = "Genres",
                chipValues = genreEntries,
                onChipClick = { onFilterClick(SeriesScreenFilter(genres = listOf(it))) },
            )
        }

        TagList(
            tags = tags,
            secondaryTags = null,
            onTagClick = { onFilterClick(SeriesScreenFilter(tags = listOf(it))) },
        )

        val uriHandler = LocalUriHandler.current
        val linkEntries = remember(links) { links.map { LabeledEntry(it, it.label) } }
        DescriptionChips(
            label = "Links",
            chipValues = linkEntries,
            onChipClick = { entry -> uriHandler.openUri(entry.url) },
            icon = Icons.Default.Link,
        )

        Spacer(Modifier.size(0.dp))
        val authorEntries = remember(authors) {
            authors
                .groupBy { it.role }
                .map { (role, authors) ->
                    role.replaceFirstChar { it.uppercase() } to authors.map { LabeledEntry(it, it.name) }
                }
                .sortedBy { (role, _) -> authorsOrder.indexOf(role.lowercase()) }
        }
        authorEntries.forEach { (role, authors) ->
            DescriptionChips(
                label = role,
                chipValues = authors,
                onChipClick = { onFilterClick(SeriesScreenFilter(authors = listOf(it))) },
            )
        }

        Spacer(Modifier.size(0.dp))
        Row {
            Text(
                "Size",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.width(120.dp)
            )
            SelectionContainer { Text(sizeInMiB, style = MaterialTheme.typography.labelLarge) }
        }

        Row {
            Text(
                "Format",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.width(120.dp)
            )
            if (mediaType != null) {
                SelectionContainer { Text(mediaType, style = MaterialTheme.typography.labelLarge) }
            }
        }

        isbn.ifBlank { null }?.let { isbn ->
            Row {
                Text(
                    "ISBN",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.width(120.dp)
                )
                SelectionContainer { Text(isbn, style = MaterialTheme.typography.labelLarge) }
            }
        }

        Row {
            Text(
                "File",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.width(120.dp)
            )
            SelectionContainer { Text(fileUrl, style = MaterialTheme.typography.labelLarge) }
        }
    }
}

@Composable
fun BookInfoRow(
    modifier: Modifier = Modifier,
    seriesTitle: String?,
    readProgress: ReadProgress?,
    bookPagesCount: Int,
    bookNumber: String,
    releaseDate: LocalDate?,
    onSeriesParentSeriesPress: (() -> Unit)? = null,
) {

    Column(
        modifier = modifier,
    ) {
        if (seriesTitle != null) {
            ElevatedButton(
                onClick = { onSeriesParentSeriesPress?.let { it() } },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Icon(Icons.AutoMirrored.Outlined.LibraryBooks, null)
                Spacer(Modifier.width(3.dp))
                Text(text = seriesTitle, textDecoration = TextDecoration.Underline)
            }
        }
        SelectionContainer {
            Text(text = "Book #${bookNumber} · $bookPagesCount pages")
        }

        Spacer(Modifier.heightIn(5.dp))
        SelectionContainer {
            Column {
                releaseDate?.let {
                    Row {
                        Text(
                            text = "Release date:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(120.dp)
                        )

                        Text(
                            it.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                if (readProgress != null) {
                    if (!readProgress.completed) {
                        val readProgressText = remember(bookPagesCount, readProgress) {
                            buildString {
                                val pagesLeft = bookPagesCount - readProgress.page
                                val percentage =
                                    (readProgress.page.toFloat() / bookPagesCount * 100)
                                        .roundToInt()
                                append(percentage)
                                append("%, ")
                                append(pagesLeft)
                                if (pagesLeft == 1) append(" page left")
                                else append(" pages left")
                            }
                        }

                        Row {
                            Text(
                                "Read progress:",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(120.dp)
                            )
                            Text(readProgressText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Row {
                        val readDate = remember(readProgress) {
                            readProgress.readDate
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .format(localDateTimeFormat)
                        }
                        Text(
                            "Last read:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            readDate,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
