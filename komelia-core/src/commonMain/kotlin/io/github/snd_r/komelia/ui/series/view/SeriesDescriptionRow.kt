package io.github.snd_r.komelia.ui.series.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.ExpandableText
import io.github.snd_r.komelia.ui.library.SeriesScreenFilter
import kotlinx.datetime.LocalDate
import snd.komga.client.common.KomgaReadingDirection
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.series.KomgaAlternativeTitle
import snd.komga.client.series.KomgaSeriesStatus
import snd.komga.client.series.KomgaSeriesStatus.ABANDONED
import snd.komga.client.series.KomgaSeriesStatus.ENDED
import snd.komga.client.series.KomgaSeriesStatus.HIATUS
import snd.komga.client.series.KomgaSeriesStatus.ONGOING

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeriesDescriptionRow(
    library: KomgaLibrary,
    onLibraryClick: (KomgaLibrary) -> Unit,
    releaseDate: LocalDate?,
    status: KomgaSeriesStatus?,
    ageRating: Int?,
    language: String,
    readingDirection: KomgaReadingDirection?,
    deleted: Boolean,
    alternateTitles: List<KomgaAlternativeTitle>,
    onFilterClick: (SeriesScreenFilter) -> Unit,
    modifier: Modifier
) {
    val strings = LocalStrings.current.seriesView
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.Start
    ) {

        if (releaseDate != null)
            Text("Release Year: ${releaseDate.year}", fontSize = 10.sp)

        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ElevatedButton(
                onClick = { onLibraryClick(library) },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Icon(Icons.AutoMirrored.Outlined.MenuBook, null)
                Spacer(Modifier.width(3.dp))
                Text(text = library.name, textDecoration = TextDecoration.Underline)
            }
            if (status != null) {
                SuggestionChip(
                    onClick = { onFilterClick(SeriesScreenFilter(publicationStatus = listOf(status))) },
                    label = { Text(strings.forSeriesStatus(status)) },
                    border = null,
                    colors =
                        when (status) {
                            ENDED -> SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                labelColor = MaterialTheme.colorScheme.onSecondary
                            )

                            ONGOING -> SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            ABANDONED -> SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                labelColor = MaterialTheme.colorScheme.onErrorContainer
                            )

                            HIATUS -> SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        },
                )
            }

            ageRating?.let { age ->
                SuggestionChip(
                    onClick = { onFilterClick(SeriesScreenFilter(ageRating = listOf(age))) },
                    label = { Text("$age+") }
                )
            }

            if (language.isNotBlank())
                SuggestionChip(
                    onClick = { onFilterClick(SeriesScreenFilter(language = listOf(language))) },
                    label = { Text(language) }
                )

            if (readingDirection != null) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(strings.forReadingDirection(readingDirection)) }
                )
            }

            if (deleted) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("Unavailable") },
                    border = null,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                )
            }
        }

        if (alternateTitles.isNotEmpty()) {
            SelectionContainer {
                Column {
                    Text("Alternative titles", fontWeight = FontWeight.Bold)
                    alternateTitles.forEach {
                        Row {
                            Text(
                                it.label,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.widthIn(min = 100.dp, max = 200.dp)
                            )
                            Text(
                                it.title,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeriesSummary(
    seriesSummary: String,
    bookSummary: String,
    bookSummaryNumber: String,
) {
    val summaryText = remember(seriesSummary) {
        if (seriesSummary.isNotBlank()) {
            seriesSummary
        } else if (bookSummary.isNotBlank()) {
            "Summary from book ${bookSummaryNumber}:\n" + bookSummary
        } else null
    }
    if (summaryText != null) {
        ExpandableText(
            text = summaryText,
            style = MaterialTheme.typography.bodyMedium,
        )
    }

}