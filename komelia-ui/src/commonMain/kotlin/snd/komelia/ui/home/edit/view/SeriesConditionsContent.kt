package snd.komelia.ui.home.edit.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import snd.komelia.ui.common.components.DropdownChoiceMenu
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.home.EqualityOpState
import snd.komelia.ui.home.NumericNullableOpState
import snd.komelia.ui.home.edit.AgeRatingConditionState
import snd.komelia.ui.home.edit.AuthorConditionState
import snd.komelia.ui.home.edit.CollectionIdConditionState
import snd.komelia.ui.home.edit.CompleteConditionState
import snd.komelia.ui.home.edit.DeletedConditionState
import snd.komelia.ui.home.edit.GenreConditionState
import snd.komelia.ui.home.edit.LanguageConditionState
import snd.komelia.ui.home.edit.LibraryConditionState
import snd.komelia.ui.home.edit.MatchType
import snd.komelia.ui.home.edit.OneShotConditionState
import snd.komelia.ui.home.edit.PublisherConditionState
import snd.komelia.ui.home.edit.ReadStatusConditionState
import snd.komelia.ui.home.edit.ReleaseDateConditionState
import snd.komelia.ui.home.edit.SeriesConditionState
import snd.komelia.ui.home.edit.SeriesCustomFilterState
import snd.komelia.ui.home.edit.SeriesMatchConditionState
import snd.komelia.ui.home.edit.SeriesMatchConditionState.SeriesConditionType
import snd.komelia.ui.home.edit.SeriesSort
import snd.komelia.ui.home.edit.SeriesStatusConditionState
import snd.komelia.ui.home.edit.SharingLabelConditionState
import snd.komelia.ui.home.edit.TagConditionState
import snd.komelia.ui.home.edit.TitleConditionState
import snd.komelia.ui.home.edit.TitleSortConditionState
import snd.komga.client.series.KomgaSeriesStatus

@Composable
fun SeriesConditionContent(
    state: SeriesCustomFilterState,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        val sort = state.sort.collectAsState().value
        PageSettingsContent(
            pageSize = state.pageSize.collectAsState().value,
            onPageSizeChange = state::onPagSizeChange,
            sort = remember(sort) { LabeledEntry(sort, sort.name) },
            sortOptions = remember { SeriesSort.entries.map { LabeledEntry(it, it.name) } },
            onSortChange = state::onSortChange,
            sortDirection = state.sortDirection.collectAsState().value,
            onSortDirectionChange = state::onSortDirectionChange
        )

        ConditionContent(
            condition = state.conditionState.collectAsState().value,
            onConditionAdd = state::addCondition,
            onConditionTypeChange = state::changeConditionType,
            onConditionRemove = state::removeCondition
        )
    }
}

@Composable
fun SeriesMatchConditionContent(
    state: SeriesMatchConditionState,
    onConditionRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .widthIn(min = 280.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.secondary,
                RoundedCornerShape(10.dp)
            ).padding(5.dp)
    ) {
        FlowRow {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val type = state.matchType.collectAsState().value
                DropdownChoiceMenu(
                    selectedOption = LabeledEntry(type, type.name),
                    options = MatchType.entries.map { LabeledEntry(it, it.name) },
                    onOptionChange = { state.setMatchType(it.value) }
                )
                IconButton(onClick = onConditionRemove) {
                    Icon(Icons.Default.Delete, null)
                }
            }

            val conditions = state.conditions.collectAsState().value
            MatchConditionChildContent(conditions, state::onConditionTypeChange, state::removeCondition)

        }
        ConditionAddButton(
            conditions = remember { SeriesConditionType.entries.map { LabeledEntry(it, it.name) } },
            onConditionAdd = state::addCondition,
        )
    }
}

@Composable
private fun MatchConditionChildContent(
    conditions: List<SeriesConditionState>,
    onChildTypeChange: (SeriesConditionState, SeriesConditionType) -> Unit,
    onChildRemove: (SeriesConditionState) -> Unit
) {
    if (conditions.isEmpty()) return
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            for (condition in conditions) {
                val removeFunction = { onChildRemove(condition) }
                val changeFunction =
                    { type: SeriesConditionType -> onChildTypeChange(condition, type) }
                ConditionContent(
                    condition = condition,
                    onConditionAdd = {},
                    onConditionTypeChange = changeFunction,
                    onConditionRemove = removeFunction
                )

                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun ConditionContent(
    condition: SeriesConditionState?,
    onConditionAdd: (SeriesConditionType) -> Unit,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit,
) {
    when (condition) {
        is SeriesMatchConditionState -> SeriesMatchConditionContent(condition, onConditionRemove)
        null -> ConditionAddButton(
            conditions = remember { SeriesConditionType.entries.map { LabeledEntry(it, it.name) } },
            onConditionAdd = onConditionAdd,
        )

        is AgeRatingConditionState -> SeriesAgeRatingConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is AuthorConditionState -> SeriesAuthorConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is CollectionIdConditionState -> CollectionIdConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is CompleteConditionState -> SeriesCompleteConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is DeletedConditionState -> SeriesDeletedConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is GenreConditionState -> SeriesGenreConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is LanguageConditionState -> SeriesLanguageConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is LibraryConditionState -> SeriesLibraryConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is OneShotConditionState -> SeriesOneShotConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is PublisherConditionState -> SeriesPublisherConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is ReadStatusConditionState -> SeriesReadStatusConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is ReleaseDateConditionState -> SeriesReleaseDateConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is SeriesStatusConditionState -> SeriesStatusConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is SharingLabelConditionState -> SeriesSharingLabelConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is TagConditionState -> SeriesTagConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is TitleConditionState -> SeriesTitleConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is TitleSortConditionState -> SeriesTitleSortConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove

        )
    }
}

@Composable
private fun SeriesConditionLayout(
    type: SeriesConditionType,
    onTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    SimpleConditionLayout(
        conditionType = remember { LabeledEntry(type, type.name) },
        options = remember { SeriesConditionType.entries.map { LabeledEntry(it, it.name) } },
        onConditionTypeChange = onTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        content()
    }
}

@Composable
fun SeriesTagConditionContent(
    state: TagConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Tag,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { TagConditionContent(state) }
}

@Composable
fun SeriesReadStatusConditionContent(
    state: ReadStatusConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.ReadStatus,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { ReadStatusConditionContent(state) }
}

@Composable
fun SeriesTitleConditionContent(
    state: TitleConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Title,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { TitleConditionContent(state) }
}

@Composable
fun SeriesTitleSortConditionContent(
    state: TitleSortConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.TitleSort,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        StringOpContent(
            operator = state.operator.collectAsState().value,
            onOperatorChange = state::setOp,
            value = state.value.collectAsState().value,
            onValueChange = state::setValue
        )
    }
}

@Composable
fun SeriesLibraryConditionContent(
    state: LibraryConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Library,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { LibraryConditionContent(state) }
}

@Composable
private fun SeriesAuthorConditionContent(
    state: AuthorConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Author,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { AuthorConditionContent(state) }
}

@Composable
private fun SeriesReleaseDateConditionContent(
    state: ReleaseDateConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.ReleaseDate,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { ReleaseDateConditionContent(state) }
}

@Composable
private fun SeriesDeletedConditionContent(
    state: DeletedConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Deleted,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { DeletedConditionContent(state) }
}

@Composable
private fun SeriesOneShotConditionContent(
    state: OneShotConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Oneshot,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { OneShotConditionContent(state) }
}

@Composable
private fun SeriesSharingLabelConditionContent(
    state: SharingLabelConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.SharingLabel,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        EqualityNullableOpDropdownSearchContent(
            state = state,
            options = state.sharingLabels.collectAsState(emptyList()).value,
            label = "Sharing Label"
        )
    }
}

@Composable
private fun SeriesCompleteConditionContent(
    state: CompleteConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Complete,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        BooleanOpContent(
            operator = state.operator.collectAsState().value,
            onOperatorChange = state::setOp
        )
    }
}

@Composable
private fun SeriesGenreConditionContent(
    state: GenreConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Genre,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        EqualityNullableOpDropdownSearchContent(
            state = state,
            options = state.genres.collectAsState(emptyList()).value,
            label = "Genre"
        )
    }
}

@Composable
private fun SeriesLanguageConditionContent(
    state: LanguageConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Language,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        EqualityOpDropdownSearchContent(
            state = state,
            options = state.languages.collectAsState(emptyList()).value,
            label = "Language"
        )
    }
}

@Composable
private fun SeriesPublisherConditionContent(
    state: PublisherConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Publisher,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        EqualityOpDropdownSearchContent(
            state = state,
            options = state.publishers.collectAsState(emptyList()).value,
            label = "Publisher"
        )
    }
}

@Composable
fun SeriesStatusConditionContent(
    state: SeriesStatusConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Status,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        val value = state.value.collectAsState().value
        EqualityOpDropDownContent(
            operator = state.operator.collectAsState().value,
            onOpChange = state::setOp,
            selectedValue = remember(value) { value?.let { LabeledEntry(it, it.name) } },
            valueOptions = remember { KomgaSeriesStatus.entries.map { LabeledEntry(it, it.name) } },
            onValueChange = state::setValue
        )
    }
}


@Composable
fun SeriesAgeRatingConditionContent(
    state: AgeRatingConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.AgeRating,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        val operator = state.operator.collectAsState().value
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(operator, operator.name),
            options = NumericNullableOpState.Op.entries.map { LabeledEntry(it, it.name) },
            onOptionChange = { state.setOp(it.value) },
            inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
            label = { Text("Operator") }
        )
        if (operator != NumericNullableOpState.Op.IsNull && operator != NumericNullableOpState.Op.IsNotNull)
            IntTextField(
                value = state.value.collectAsState().value,
                onValueChange = state::setValue,
                label = "Age",
            )
    }
}

@Composable
fun CollectionIdConditionContent(
    state: CollectionIdConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Collection,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        val options = state.collectionsSuggestions.collectAsState(emptyList()).value
        val operator = state.operator.collectAsState().value
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(operator, operator.name),
            options = EqualityOpState.Op.entries.map { LabeledEntry(it, it.name) },
            onOptionChange = { state.setOp(it.value) },
            inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
            label = { Text("Operator") }
        )
        SearchableOptionSelectionField(
            searchText = state.searchText.collectAsState().value,
            onSearchTextChange = state::onSearchTextChange,
            options = remember(options) { options.map { LabeledEntry(it, it.name) } },
            onValueChange = state::onCollectionSelect,
            label = "Collection"
        )
    }
}
