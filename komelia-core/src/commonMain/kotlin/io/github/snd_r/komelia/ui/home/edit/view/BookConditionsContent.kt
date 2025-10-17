package io.github.snd_r.komelia.ui.home.edit.view

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
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.home.EqualityOpState
import io.github.snd_r.komelia.ui.home.NumericOpState
import io.github.snd_r.komelia.ui.home.edit.AuthorConditionState
import io.github.snd_r.komelia.ui.home.edit.BookConditionState
import io.github.snd_r.komelia.ui.home.edit.BookCustomFilterState
import io.github.snd_r.komelia.ui.home.edit.BookMatchConditionState
import io.github.snd_r.komelia.ui.home.edit.BookMatchConditionState.BookConditionType
import io.github.snd_r.komelia.ui.home.edit.BookSort
import io.github.snd_r.komelia.ui.home.edit.DeletedConditionState
import io.github.snd_r.komelia.ui.home.edit.LibraryConditionState
import io.github.snd_r.komelia.ui.home.edit.MatchType
import io.github.snd_r.komelia.ui.home.edit.MediaProfileConditionState
import io.github.snd_r.komelia.ui.home.edit.MediaStatusConditionState
import io.github.snd_r.komelia.ui.home.edit.NumberSortConditionState
import io.github.snd_r.komelia.ui.home.edit.OneShotConditionState
import io.github.snd_r.komelia.ui.home.edit.PosterConditionState
import io.github.snd_r.komelia.ui.home.edit.ReadListIdConditionState
import io.github.snd_r.komelia.ui.home.edit.ReadStatusConditionState
import io.github.snd_r.komelia.ui.home.edit.ReleaseDateConditionState
import io.github.snd_r.komelia.ui.home.edit.SeriesIdConditionState
import io.github.snd_r.komelia.ui.home.edit.TagConditionState
import io.github.snd_r.komelia.ui.home.edit.TitleConditionState
import snd.komga.client.book.KomgaMediaStatus
import snd.komga.client.book.MediaProfile
import snd.komga.client.search.KomgaSearchCondition.PosterMatch

@Composable
fun BookConditionContent(
    state: BookCustomFilterState,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        val sort = state.sort.collectAsState().value
        PageSettingsContent(
            pageSize = state.pageSize.collectAsState().value,
            onPageSizeChange = state::onPagSizeChange,
            sort = remember(sort) { LabeledEntry(sort, sort.name) },
            sortOptions = remember { BookSort.entries.map { LabeledEntry(it, it.name) } },
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
fun BookMatchConditionContent(
    state: BookMatchConditionState,
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
            conditions = remember { BookConditionType.entries.map { LabeledEntry(it, it.name) } },
            onConditionAdd = state::addCondition,
        )
    }
}

@Composable
private fun MatchConditionChildContent(
    conditions: List<BookConditionState>,
    onChildTypeChange: (BookConditionState, BookConditionType) -> Unit,
    onChildRemove: (BookConditionState) -> Unit
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
                    { type: BookConditionType -> onChildTypeChange(condition, type) }
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
    condition: BookConditionState?,
    onConditionAdd: (BookConditionType) -> Unit,
    onConditionTypeChange: (BookConditionType) -> Unit,
    onConditionRemove: () -> Unit,
) {
    when (condition) {
        is BookMatchConditionState -> BookMatchConditionContent(
            state = condition,
            onConditionRemove = onConditionRemove
        )

        is TagConditionState -> BookTagConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is ReadStatusConditionState -> BookReadStatusConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is TitleConditionState -> BookTitleConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is LibraryConditionState -> BookLibraryConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is SeriesIdConditionState -> SeriesIdConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is AuthorConditionState -> BookAuthorConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is DeletedConditionState -> BookDeletedConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is MediaProfileConditionState -> MediaProfileConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is MediaStatusConditionState -> MediaStatusConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is NumberSortConditionState -> NumberSortConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is PosterConditionState -> PosterConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is ReadListIdConditionState -> ReadListConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is ReleaseDateConditionState -> BookReleaseDateConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is OneShotConditionState -> BookOneShotConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        null -> {
            ConditionAddButton(
                conditions = remember { BookConditionType.entries.map { LabeledEntry(it, it.name) } },
                onConditionAdd = onConditionAdd,
            )
        }

    }
}

@Composable
private fun BookConditionLayout(
    type: BookConditionType,
    onTypeChange: (BookConditionType) -> Unit,
    onConditionRemove: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    SimpleConditionLayout(
        conditionType = remember { LabeledEntry(type, type.name) },
        options = remember { BookConditionType.entries.map { LabeledEntry(it, it.name) } },
        onConditionTypeChange = onTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        content()
    }

}

@Composable
fun BookTagConditionContent(
    state: TagConditionState,
    onConditionTypeChange: (BookConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    BookConditionLayout(
        type = BookConditionType.Tag,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { TagConditionContent(state) }
}

@Composable
fun BookReadStatusConditionContent(
    state: ReadStatusConditionState,
    onConditionTypeChange: (BookConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    BookConditionLayout(
        type = BookConditionType.ReadStatus,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { ReadStatusConditionContent(state) }
}

@Composable
fun BookTitleConditionContent(
    state: TitleConditionState,
    onConditionTypeChange: (BookConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    BookConditionLayout(
        type = BookConditionType.Title,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { TitleConditionContent(state) }
}

@Composable
fun BookLibraryConditionContent(
    state: LibraryConditionState,
    onConditionTypeChange: (BookConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    BookConditionLayout(
        type = BookConditionType.Library,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { LibraryConditionContent(state) }
}

@Composable
fun SeriesIdConditionContent(
    state: SeriesIdConditionState,
    onConditionTypeChange: (BookConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    BookConditionLayout(
        type = BookConditionType.Series,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        val options = state.seriesSuggestions.collectAsState(emptyList()).value
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
            onValueChange = state::onSeriesSelect,
            label = "Series"
        )
    }
}

@Composable
private fun BookAuthorConditionContent(
    state: AuthorConditionState,
    onConditionTypeChange: (BookConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    BookConditionLayout(
        type = BookConditionType.Author,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { AuthorConditionContent(state) }
}

@Composable
fun MediaProfileConditionContent(
    state: MediaProfileConditionState,
    onConditionTypeChange: (BookConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    BookConditionLayout(
        type = BookConditionType.MediaProfile,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        val value = state.value.collectAsState().value
        EqualityOpDropDownContent(
            operator = state.operator.collectAsState().value,
            onOpChange = state::setOp,
            selectedValue = remember(value) { value?.let { LabeledEntry(it, it.name) } },
            valueOptions = remember { MediaProfile.entries.map { LabeledEntry(it, it.name) } },
            onValueChange = state::setValue
        )
    }
}

@Composable
fun MediaStatusConditionContent(
    state: MediaStatusConditionState,
    onConditionTypeChange: (BookConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    BookConditionLayout(
        type = BookConditionType.MediaStatus,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        val value = state.value.collectAsState().value
        EqualityOpDropDownContent(
            operator = state.operator.collectAsState().value,
            onOpChange = state::setOp,
            selectedValue = remember(value) { value?.let { LabeledEntry(it, it.name) } },
            valueOptions = remember { KomgaMediaStatus.entries.map { LabeledEntry(it, it.name) } },
            onValueChange = state::setValue
        )
    }
}

@Composable
fun NumberSortConditionContent(
    state: NumberSortConditionState,
    onConditionTypeChange: (BookConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    BookConditionLayout(
        type = BookConditionType.NumberSort,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        val operator = state.operator.collectAsState().value
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(operator, operator.name),
            options = NumericOpState.Op.entries.map { LabeledEntry(it, it.name) },
            onOptionChange = { state.setOp(it.value) },
            inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
            label = { Text("Operator") }
        )
        FloatTextField(
            value = state.value.collectAsState().value,
            onValueChange = state::setValue,
            label = "Number",
        )
    }
}

@Composable
fun PosterConditionContent(
    state: PosterConditionState,
    onConditionTypeChange: (BookConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    BookConditionLayout(
        type = BookConditionType.Poster,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        val operator = state.operator.collectAsState().value
        val currentValue = state.value.collectAsState().value
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(operator, operator.name),
            options = EqualityOpState.Op.entries.map { LabeledEntry(it, it.name) },
            onOptionChange = { state.setOp(it.value) },
            inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
            label = { Text("Operator") }
        )

        DropdownChoiceMenu(
            selectedOption = remember(currentValue) {
                LabeledEntry(
                    currentValue?.type,
                    currentValue?.type?.name ?: "Any"
                )
            },
            options = remember {
                listOf(
                    LabeledEntry<PosterMatch.Type?>(
                        null,
                        "Any"
                    )
                ).plus(PosterMatch.Type.entries.map { LabeledEntry(it, it.name) }
                )
            },
            onOptionChange = { state.setType(it.value) },
            inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
            label = { Text("Type") }
        )

        DropdownChoiceMenu(
            selectedOption = remember(currentValue) {
                LabeledEntry(
                    currentValue?.selected,
                    currentValue?.selected?.toString() ?: "Any"
                )
            },
            options = remember {
                listOf(
                    LabeledEntry(null, "Any"),
                    LabeledEntry(true, "True"),
                    LabeledEntry(false, "False"),
                )
            },
            onOptionChange = { state.setSelected(it.value) },
            inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
            label = { Text("Selected") }
        )

    }
}

@Composable
fun ReadListConditionContent(
    state: ReadListIdConditionState,
    onConditionTypeChange: (BookConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    BookConditionLayout(
        type = BookConditionType.ReadList,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        val options = state.readListSuggestions.collectAsState(emptyList()).value
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
            onValueChange = state::onReadListSelect,
            label = "Read List"
        )

    }
}

@Composable
private fun BookReleaseDateConditionContent(
    state: ReleaseDateConditionState,
    onConditionTypeChange: (BookConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    BookConditionLayout(
        type = BookConditionType.ReleaseDate,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { ReleaseDateConditionContent(state) }
}

@Composable
private fun BookDeletedConditionContent(
    state: DeletedConditionState,
    onConditionTypeChange: (BookConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    BookConditionLayout(
        type = BookConditionType.Deleted,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { DeletedConditionContent(state) }
}

@Composable
private fun BookOneShotConditionContent(
    state: OneShotConditionState,
    onConditionTypeChange: (BookConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    BookConditionLayout(
        type = BookConditionType.Oneshot,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { OneShotConditionContent(state) }
}
