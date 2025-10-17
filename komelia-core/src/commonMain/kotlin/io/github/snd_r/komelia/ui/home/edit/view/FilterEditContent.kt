package io.github.snd_r.komelia.ui.home.edit.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.PlatformType.MOBILE
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.platform.cursorForMove
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.cards.BookImageCard
import io.github.snd_r.komelia.ui.common.cards.SeriesImageCard
import io.github.snd_r.komelia.ui.dialogs.ConfirmationDialog
import io.github.snd_r.komelia.ui.home.edit.BookCustomFilterState
import io.github.snd_r.komelia.ui.home.edit.BookFilterEditState
import io.github.snd_r.komelia.ui.home.edit.BookOnDeckFilterState
import io.github.snd_r.komelia.ui.home.edit.FilterEditState
import io.github.snd_r.komelia.ui.home.edit.FilterEditViewModel
import io.github.snd_r.komelia.ui.home.edit.SeriesCustomFilterState
import io.github.snd_r.komelia.ui.home.edit.SeriesFilterEditState
import io.github.snd_r.komelia.ui.home.edit.SeriesRecentlyAddedFilterState
import io.github.snd_r.komelia.ui.home.edit.SeriesRecentlyUpdatedFilterState
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun FilterEditContent(
    filters: List<FilterEditState>,
    onFilterMove: (Int, Int) -> Unit,
    onEditEnd: () -> Unit,
    onFilterAdd: (FilterEditViewModel.FilterType) -> Unit,
    onFilterRemove: (FilterEditState) -> Unit,
    onFiltersReset: () -> Unit,
) {
    Column {
        Toolbar(onEditEnd, onFiltersReset)
        EditContent(
            filters = filters,
            onFilterAdd = onFilterAdd,
            onFilterRemove = onFilterRemove,
            onFilterMove = onFilterMove,
        )
    }
}

@Composable
private fun Toolbar(
    onEditEnd: () -> Unit,
    onReset: () -> Unit,
) {
    Row(
        modifier = Modifier.animateContentSize(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(20.dp))
        FilterChip(
            onClick = {},
            selected = true,
            label = {
                Icon(Icons.Default.Tune, null)
            },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            ),
            border = null,
        )

        ElevatedButton(
            onClick = { onEditEnd() },
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            Text("Done")
            Icon(Icons.Default.Check, null)
        }

        var showResetDialog by remember { mutableStateOf(false) }
        ElevatedButton(
            onClick = { showResetDialog = true },
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            Text("Reset to default")
            Icon(Icons.Default.Restore, null)
        }
        if (showResetDialog) {
            ConfirmationDialog(
                body = "Reset homescreen filters to default?",
                onDialogConfirm = onReset,
                onDialogDismiss = { showResetDialog = false }
            )
        }
    }
}

@Composable
private fun EditContent(
    filters: List<FilterEditState>,
    onFilterAdd: (FilterEditViewModel.FilterType) -> Unit,
    onFilterRemove: (FilterEditState) -> Unit,
    onFilterMove: (Int, Int) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onFilterMove(from.index, to.index)
    }

    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(bottom = 50.dp),
        modifier = Modifier.imePadding()
    ) {
        items(filters, key = { it }) { data ->
            ReorderableItem(reorderableLazyListState, key = data) { isDragging ->
                FilterContent(
                    filterState = data,
                    isDragging = isDragging,
                    onFilterRemove = { onFilterRemove(data) }
                )
            }
        }
        item {
            AddConditionButton(onFilterAdd, modifier = Modifier.animateItem())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddConditionButton(
    onConditionAdd: (FilterEditViewModel.FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    var dropDownExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = dropDownExpanded,
        onExpandedChange = { dropDownExpanded = it },
        modifier = modifier
    ) {
        FilledTonalButton(
            onClick = { dropDownExpanded = true },
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier
                .cursorForHand()
                .menuAnchor(PrimaryNotEditable)
        ) {
            Text("Add Filter")
        }

        ExposedDropdownMenu(
            expanded = dropDownExpanded,
            onDismissRequest = { dropDownExpanded = false },
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
            FilterEditViewModel.FilterType.entries.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        dropDownExpanded = false
                        onConditionAdd(it)
                    },
                    modifier = Modifier.cursorForHand()
                )
            }
        }
    }
}

@Composable
private fun ReorderableCollectionItemScope.FilterContent(
    filterState: FilterEditState,
    isDragging: Boolean,
    onFilterRemove: () -> Unit,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    val label = filterState.label.collectAsState().value
    var labelText by remember { mutableStateOf(label) }
    Column(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isDragging) MaterialTheme.colorScheme.surfaceBright
                else MaterialTheme.colorScheme.surface
            )
            .then(
                if (isDragging) Modifier.border(
                    4.dp,
                    MaterialTheme.colorScheme.secondary,
                    RoundedCornerShape(10.dp)
                )
                else Modifier
            )
    ) {
        HorizontalDivider()
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()

        ) {
            val platform = LocalPlatform.current
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.heightIn(min = 46.dp).then(
                    if (platform != MOBILE) Modifier.draggableHandle().cursorForMove()
                    else Modifier
                )
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 15.dp).size(32.dp)
                        .then(if (platform == MOBILE) Modifier.draggableHandle() else Modifier)
                )
                if (showEdit) {
                    OutlinedTextField(
                        value = labelText,
                        label = { Text("Label") },
                        onValueChange = {
                            labelText = it
                            filterState.label.value = it
                        },
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                } else {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(horizontal = 14.dp)
                            .widthIn(min = 280.dp)
                    )
                }
            }

            ElevatedButton(
                onClick = { showEdit = !showEdit },
                modifier = Modifier.cursorForHand()
            ) {
                Text("Edit")
                Icon(
                    imageVector = if (showEdit) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                )
            }

            ElevatedButton(
                onClick = {
                    showDeleteConfirmation = true
                },
                modifier = Modifier.cursorForHand()
            ) {
                Text("Delete")
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                )
            }
        }


        AnimatedVisibility(showEdit, modifier = Modifier.padding(5.dp)) {
            when (filterState) {
                is BookFilterEditState -> BookFilterEditContent(filterState)
                is SeriesFilterEditState -> SeriesFilterEditContent(filterState)
            }
        }
    }

    if (showDeleteConfirmation) {
        ConfirmationDialog(
            body = "Delete ${label}?",
            onDialogConfirm = onFilterRemove,
            onDialogDismiss = { showDeleteConfirmation = false })
    }
}

@Composable
private fun BookFilterEditContent(state: BookFilterEditState) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        val filter = state.filter.collectAsState().value
        val type = state.type.collectAsState().value
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(type, type.name),
            options = remember { BookFilterEditState.FilterType.entries.map { LabeledEntry(it, it.name) } },
            onOptionChange = { state.onTypeChange(it.value) },
        )

        when (filter) {
            is BookCustomFilterState -> BookConditionContent(filter)
            is BookOnDeckFilterState -> PageSizeSettingsContent(
                pageSize = filter.pageSize.collectAsState().value,
                onPageSizeChange = filter::onPageSizeChange
            )
        }

        val books = state.books.collectAsState().value
        val cardWidth = state.cardWidth.collectAsState().value
        LazyRow(
            contentPadding = PaddingValues(15.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            items(books) {
                BookImageCard(book = it, modifier = Modifier.width(cardWidth))
            }
        }
    }
}

@Composable
private fun SeriesFilterEditContent(state: SeriesFilterEditState) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        val filter = state.filter.collectAsState().value
        val type = state.type.collectAsState().value
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(type, type.name),
            options = remember { SeriesFilterEditState.FilterType.entries.map { LabeledEntry(it, it.name) } },
            onOptionChange = { state.onTypeChange(it.value) },
        )

        when (filter) {
            is SeriesCustomFilterState -> SeriesConditionContent(filter)
            is SeriesRecentlyAddedFilterState -> PageSizeSettingsContent(
                pageSize = filter.pageSize.collectAsState().value,
                onPageSizeChange = filter::onPageSizeChange
            )

            is SeriesRecentlyUpdatedFilterState -> PageSizeSettingsContent(
                pageSize = filter.pageSize.collectAsState().value,
                onPageSizeChange = filter::onPageSizeChange
            )
        }

        val books = state.series.collectAsState().value
        val cardWidth = state.cardWidth.collectAsState().value
        LazyRow(
            contentPadding = PaddingValues(15.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            items(books) {
                SeriesImageCard(series = it, modifier = Modifier.width(cardWidth))
            }
        }
    }
}
