package io.github.snd_r.komelia.ui.series.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
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
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.WindowSizeClass.COMPACT
import io.github.snd_r.komelia.platform.WindowSizeClass.EXPANDED
import io.github.snd_r.komelia.platform.WindowSizeClass.FULL
import io.github.snd_r.komelia.platform.WindowSizeClass.MEDIUM
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.common.FilterDropdownChoice
import io.github.snd_r.komelia.ui.common.FilterDropdownMultiChoice
import io.github.snd_r.komelia.ui.common.FilterDropdownMultiChoiceWithSearch
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.LabeledEntry.Companion.stringEntry
import io.github.snd_r.komelia.ui.common.NoPaddingTextField
import io.github.snd_r.komelia.ui.common.TagFiltersDropdownMenu
import io.github.snd_r.komelia.ui.library.LibrarySeriesTabState
import io.github.snd_r.komelia.ui.series.SeriesFilterState
import io.github.snd_r.komelia.ui.series.SeriesFilterState.Completion.ANY
import io.github.snd_r.komelia.ui.series.SeriesFilterState.Completion.COMPLETE
import io.github.snd_r.komelia.ui.series.SeriesFilterState.Completion.INCOMPLETE
import io.github.snd_r.komelia.ui.series.SeriesFilterState.Format
import kotlinx.coroutines.delay
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.series.KomgaSeriesStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeriesFilterContent(
    filterState: SeriesFilterState,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current.seriesFilter
    val widthClass = LocalWindowWidth.current

    val spacing = remember(widthClass) {
        when (widthClass) {
            COMPACT, MEDIUM, EXPANDED -> 10.dp
            FULL -> 20.dp
        }
    }
    val width = remember(widthClass) {
        when (widthClass) {
            COMPACT -> 400.dp
            MEDIUM -> 220.dp
            else -> 250.dp
        }
    }

    Column(
        modifier = Modifier.widthIn(max = 1400.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.Center,
        ) {

            var searchTerm by remember { mutableStateOf(filterState.searchTerm) }
            LaunchedEffect(searchTerm) {
                delay(200)
                filterState.onSearchTermChange(searchTerm)
            }
            NoPaddingTextField(
                text = searchTerm,
                placeholder = strings.search,
                onTextChange = { searchTerm = it },
                modifier = Modifier.weight(1f).height(40.dp).widthIn(min = 340.dp),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = filterState::reset,
                    enabled = filterState.isChanged,
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (filterState.isChanged) MaterialTheme.colorScheme.tertiaryContainer else Color.Unspecified,
                    ),
                    border = if (filterState.isChanged) null else ButtonDefaults.outlinedButtonBorder,
                    modifier = Modifier.height(40.dp).cursorForHand()
                ) {
                    Text(strings.resetFilters, style = MaterialTheme.typography.bodyLarge)
                }

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.height(40.dp).cursorForHand()
                ) {
                    Text(strings.hideFilters, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            FilterDropdownChoice(
                selectedOption = LabeledEntry(filterState.sortOrder, strings.forSeriesSort(filterState.sortOrder)),
                options = LibrarySeriesTabState.SeriesSort.entries.map { LabeledEntry(it, strings.forSeriesSort(it)) },
                onOptionChange = { filterState.onSortOrderChange(it.value) },
                label = strings.sort,
                modifier = Modifier.width(width)
            )
            TagFiltersDropdownMenu(
                selectedGenres = filterState.genres,
                genreOptions = filterState.genresOptions,
                onGenreSelect = filterState::onGenreSelect,
                selectedTags = filterState.tags,
                tagOptions = filterState.tagOptions,
                onTagSelect = filterState::onTagSelect,
                onReset = filterState::resetTagFilters,
                contentPadding = PaddingValues(5.dp),
                label = strings.filterTagsLabel,
                inputFieldColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .width(width)
                    .clip(RoundedCornerShape(5.dp)),
                inputFieldModifier = Modifier.fillMaxWidth()
            )
            FilterDropdownMultiChoice(
                selectedOptions = filterState.readStatus
                    .map { LabeledEntry(it, strings.forSeriesReadStatus(it)) },
                options = KomgaReadStatus.entries.map { LabeledEntry(it, strings.forSeriesReadStatus(it)) },
                onOptionSelect = { changed -> filterState.onReadStatusSelect(changed.value) },
                label = strings.readStatus,
                modifier = Modifier.width(width),
            )

            FilterDropdownMultiChoice(
                selectedOptions = filterState.publicationStatus
                    .map { LabeledEntry(it, strings.forPublicationStatus(it)) },
                options = KomgaSeriesStatus.entries.map { LabeledEntry(it, strings.forPublicationStatus(it)) },
                onOptionSelect = { changed -> filterState.onPublicationStatusSelect(changed.value) },
                label = strings.publicationStatus,
                modifier = Modifier.width(width),
            )

            val authorsSelectedOptions = remember(filterState.authors) {
                filterState.authors.distinctBy { it.name }.map { LabeledEntry(it, it.name) }
            }
            val authorOptions = remember(filterState.authorsOptions) {
                filterState.authorsOptions.distinctBy { it.name }.map { LabeledEntry(it, it.name) }
            }
            FilterDropdownMultiChoiceWithSearch(
                selectedOptions = authorsSelectedOptions,
                options = authorOptions,
                onOptionSelect = { author -> filterState.onAuthorSelect(author.value) },
                onSearch = filterState::onAuthorsSearch,
                label = strings.authors,
                modifier = Modifier.width(width),
            )

            FilterDropdownMultiChoice(
                selectedOptions = filterState.publishers.map { stringEntry(it) },
                options = filterState.publishersOptions.map { stringEntry(it) },
                onOptionSelect = { changed -> filterState.onPublisherSelect(changed.value) },
                label = strings.publisher,
                modifier = Modifier.width(width),
            )

            FilterDropdownMultiChoice(
                selectedOptions = filterState.languages.map { stringEntry(it) },
                options = filterState.languagesOptions.map { stringEntry(it) },
                onOptionSelect = { changed -> filterState.onLanguageSelect(changed.value) },
                label = strings.language,
                modifier = Modifier.width(width),
            )
            FilterDropdownMultiChoice(
                selectedOptions = filterState.releaseDates.map { stringEntry(it) },
                options = filterState.releaseDateOptions.map { stringEntry(it) },
                onOptionSelect = { changed -> filterState.onReleaseDateSelect(changed.value) },
                label = strings.releaseDate,
                modifier = Modifier.width(width),
            )

            FilterDropdownMultiChoice(
                selectedOptions = filterState.ageRatings.map { stringEntry(it) },
                options = filterState.ageRatingsOptions.map { stringEntry(it) },
                onOptionSelect = { changed -> filterState.onAgeRatingSelect(changed.value) },
                label = strings.ageRating,
                modifier = Modifier.width(width),
            )

            Row(
                modifier = Modifier.width(width),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable { filterState.onCompletionToggle() }
                        .cursorForHand()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clip(RoundedCornerShape(5.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val checkboxState by derivedStateOf {
                        when (filterState.complete) {
                            ANY -> ToggleableState.Off
                            COMPLETE -> ToggleableState.On
                            INCOMPLETE -> ToggleableState.Indeterminate
                        }

                    }
                    TriStateCheckbox(
                        state = checkboxState,
                        onClick = filterState::onCompletionToggle,
                        modifier = Modifier.size(30.dp)
                    )
                    Text(strings.complete, style = MaterialTheme.typography.labelLarge, maxLines = 2)
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable { filterState.onFormatToggle() }
                        .cursorForHand()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clip(RoundedCornerShape(5.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val checkboxState by derivedStateOf {
                        when (filterState.oneshot) {
                            Format.ANY -> ToggleableState.Off
                            Format.ONESHOT -> ToggleableState.On
                            Format.NOT_ONESHOT -> ToggleableState.Indeterminate
                        }

                    }
                    TriStateCheckbox(
                        state = checkboxState,
                        onClick = filterState::onFormatToggle,
                        modifier = Modifier.size(30.dp)
                    )
                    Text(strings.oneshot, style = MaterialTheme.typography.labelLarge, maxLines = 2)
                }
            }
        }

    }
}
