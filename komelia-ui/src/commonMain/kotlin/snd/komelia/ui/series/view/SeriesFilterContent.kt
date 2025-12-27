package snd.komelia.ui.series.view

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
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.delay
import snd.komelia.ui.LocalStrings
import snd.komelia.ui.LocalWindowWidth
import snd.komelia.ui.common.components.FilterDropdownChoice
import snd.komelia.ui.common.components.FilterDropdownMultiChoice
import snd.komelia.ui.common.components.FilterDropdownMultiChoiceWithSearch
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.common.components.LabeledEntry.Companion.stringEntry
import snd.komelia.ui.common.components.NoPaddingTextField
import snd.komelia.ui.common.components.TagFiltersDropdownMenu
import snd.komelia.ui.library.LibrarySeriesTabState
import snd.komelia.ui.platform.WindowSizeClass.COMPACT
import snd.komelia.ui.platform.WindowSizeClass.EXPANDED
import snd.komelia.ui.platform.WindowSizeClass.FULL
import snd.komelia.ui.platform.WindowSizeClass.MEDIUM
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.ui.series.SeriesFilterState
import snd.komelia.ui.series.SeriesFilterState.Completion.ANY
import snd.komelia.ui.series.SeriesFilterState.Completion.COMPLETE
import snd.komelia.ui.series.SeriesFilterState.Completion.INCOMPLETE
import snd.komelia.ui.series.SeriesFilterState.Format
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
    val currentFilter = filterState.state.collectAsState().value

    Column(
        modifier = Modifier.widthIn(max = 1400.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.Center,
        ) {

            var searchTerm by remember { mutableStateOf(currentFilter.searchTerm) }
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
                    border = if (filterState.isChanged) null else ButtonDefaults.outlinedButtonBorder(true),
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
                selectedOption = LabeledEntry(currentFilter.sortOrder, strings.forSeriesSort(currentFilter.sortOrder)),
                options = LibrarySeriesTabState.SeriesSort.entries.map { LabeledEntry(it, strings.forSeriesSort(it)) },
                onOptionChange = { filterState.onSortOrderChange(it.value) },
                label = strings.sort,
                modifier = Modifier.width(width)
            )
            TagFiltersDropdownMenu(
                allTags = filterState.tagOptions,
                includeTags = currentFilter.includeTags,
                excludeTags = currentFilter.excludeTags,
                onTagSelect = filterState::onTagSelect,

                allGenres = filterState.genresOptions,
                includeGenres = currentFilter.includeGenres,
                excludeGenres = currentFilter.excludeGenres,
                onGenreSelect = filterState::onGenreSelect,

                onReset = filterState::resetTagFilters,
                inclusionMode = currentFilter.inclusionMode,
                onInclusionModeChange = filterState::onInclusionModeChange,
                exclusionMode = currentFilter.exclusionMode,
                onExclusionModeChange = filterState::onExclusionModeChange,

                contentPadding = PaddingValues(5.dp),
                label = strings.filterTagsLabel,
                inputFieldColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .width(width)
                    .clip(RoundedCornerShape(5.dp)),
                inputFieldModifier = Modifier.fillMaxWidth()
            )
            FilterDropdownMultiChoice(
                selectedOptions = currentFilter.readStatus
                    .map { LabeledEntry(it, strings.forSeriesReadStatus(it)) },
                options = KomgaReadStatus.entries.map { LabeledEntry(it, strings.forSeriesReadStatus(it)) },
                onOptionSelect = { changed -> filterState.onReadStatusSelect(changed.value) },
                label = strings.readStatus,
                modifier = Modifier.width(width),
            )

            FilterDropdownMultiChoice(
                selectedOptions = currentFilter.publicationStatus
                    .map { LabeledEntry(it, strings.forPublicationStatus(it)) },
                options = KomgaSeriesStatus.entries.map { LabeledEntry(it, strings.forPublicationStatus(it)) },
                onOptionSelect = { changed -> filterState.onPublicationStatusSelect(changed.value) },
                label = strings.publicationStatus,
                modifier = Modifier.width(width),
            )

            val authorsSelectedOptions = remember(currentFilter.authors) {
                currentFilter.authors.distinctBy { it.name }.map { LabeledEntry(it, it.name) }
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
                selectedOptions = currentFilter.publishers.map { stringEntry(it) },
                options = filterState.publishersOptions.map { stringEntry(it) },
                onOptionSelect = { changed -> filterState.onPublisherSelect(changed.value) },
                label = strings.publisher,
                modifier = Modifier.width(width),
            )

            FilterDropdownMultiChoice(
                selectedOptions = currentFilter.languages.map { stringEntry(it) },
                options = filterState.languagesOptions.map { stringEntry(it) },
                onOptionSelect = { changed -> filterState.onLanguageSelect(changed.value) },
                label = strings.language,
                modifier = Modifier.width(width),
            )
            FilterDropdownMultiChoice(
                selectedOptions = currentFilter.releaseDates.map { stringEntry(it) },
                options = filterState.releaseDateOptions.map { stringEntry(it) },
                onOptionSelect = { changed -> filterState.onReleaseDateSelect(changed.value) },
                label = strings.releaseDate,
                modifier = Modifier.width(width),
            )

            FilterDropdownMultiChoice(
                selectedOptions = currentFilter.ageRatings.map { stringEntry(it) },
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
                        when (currentFilter.complete) {
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
                        when (currentFilter.oneshot) {
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
