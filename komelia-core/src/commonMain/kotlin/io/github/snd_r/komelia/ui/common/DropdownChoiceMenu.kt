package io.github.snd_r.komelia.ui.common

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.platform.scrollbar
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.series.SeriesFilterState.TagExclusionMode
import io.github.snd_r.komelia.ui.series.SeriesFilterState.TagInclusionMode
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownChoiceMenu(
    selectedOption: LabeledEntry<T>?,
    options: List<LabeledEntry<T>>,
    onOptionChange: (LabeledEntry<T>) -> Unit,
    inputFieldModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    inputFieldColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentPadding: PaddingValues = PaddingValues(10.dp)
) {
    var isExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
    ) {
        InputField(
            value = selectedOption?.label ?: "",
            modifier = Modifier
                .menuAnchor(PrimaryNotEditable)
                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                .then(inputFieldModifier),
            label = label,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            color = inputFieldColor,
            contentPadding = contentPadding
        )

        val scrollState = rememberScrollState()
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            scrollState = scrollState,
            modifier = Modifier.scrollbar(scrollState, Orientation.Vertical)
        ) {

            options.forEach {
                DropdownMenuItem(
                    text = { Text(it.label) },
                    onClick = {
                        onOptionChange(it)
                        isExpanded = false
                    }, modifier = Modifier.cursorForHand()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownMultiChoiceMenu(
    selectedOptions: List<LabeledEntry<T>>,
    options: List<LabeledEntry<T>>,
    onOptionSelect: (LabeledEntry<T>) -> Unit,
    inputFieldModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: String? = null,
    inputFieldColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentPadding: PaddingValues = PaddingValues(10.dp)
) {
    var isExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
    ) {
        InputField(
            value = selectedOptions.joinToString { it.label }.ifBlank { placeholder ?: "Any" },
            modifier = Modifier
                .menuAnchor(PrimaryNotEditable)
                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                .then(inputFieldModifier),
            label = label,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            color = inputFieldColor,
            contentPadding = contentPadding
        )

        val scrollState = rememberScrollState()
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            scrollState = scrollState,
            modifier = Modifier.scrollbar(scrollState, Orientation.Vertical)
        ) {

            options.forEach { option ->
                val isSelected = selectedOptions.any { it.value == option.value }
                DropdownMultiChoiceItem(
                    option = option,
                    onOptionSelect = onOptionSelect,
                    selected = isSelected
                )
            }
        }
    }
}

@Composable
private fun InputField(
    value: String,
    modifier: Modifier,
    label: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit),
    color: Color,
    contentPadding: PaddingValues = PaddingValues(10.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        shadowElevation = 1.dp,
        color = color,
        modifier = Modifier
            .cursorForHand()
            .indication(interactionSource, LocalIndication.current)
            .hoverable(interactionSource)
            .then(modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                horizontalAlignment = Alignment.Start
            ) {

                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelMedium) {
                    label?.let { it() }
                }
                Text(value, maxLines = 1)
            }

            Spacer(Modifier.weight(1f))
            trailingIcon()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownChoiceMenuWithSearch(
    selectedOptions: List<LabeledEntry<T>>,
    options: List<LabeledEntry<T>>,
    onOptionSelect: (LabeledEntry<T>) -> Unit,
    onSearch: suspend (String) -> Unit,
    textFieldModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: String? = null,
    inputFieldColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(10.dp)
) {
    var searchText by remember { mutableStateOf("") }
    LaunchedEffect(searchText) {
        delay(200)
        onSearch(searchText)
    }
    var isExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
    ) {
        InputField(
            value = selectedOptions.joinToString { it.label }.ifBlank { placeholder ?: "Any" },
            modifier = Modifier
                .menuAnchor(PrimaryNotEditable)
                .then(textFieldModifier),
            label = label,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            color = inputFieldColor,
            contentPadding = contentPadding
        )

        val scrollState = rememberScrollState()
        DropdownMenu(
            modifier = modifier.scrollbar(scrollState, Orientation.Vertical),
            scrollState = scrollState,
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            val focusRequester = remember { FocusRequester() }
            NoPaddingTextField(
                text = searchText,
                placeholder = "Search",
                onTextChange = { searchText = it },
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .height(40.dp)
                    .focusRequester(focusRequester),
            )
            LaunchedEffect(Unit) { focusRequester.requestFocus() }

            Column(
                modifier = Modifier.padding(5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                selectedOptions.forEach {
                    NoPaddingChip(
                        color = MaterialTheme.colorScheme.surface,
                        onClick = { onOptionSelect(it) }
                    ) {
                        Icon(Icons.Default.Close, null)
                        Text(it.label, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            if (options.isNotEmpty()) {
                HorizontalDivider()
                options.forEach { option ->
                    DropdownMultiChoiceItem(
                        option = option,
                        onOptionSelect = { onOptionSelect(it) },
                        selected = selectedOptions.contains(option)

                    )
                }
            }
        }
    }
}

@Composable
fun <T> FilterDropdownChoice(
    selectedOption: LabeledEntry<T>,
    options: List<LabeledEntry<T>>,
    onOptionChange: (LabeledEntry<T>) -> Unit,
    label: String?,
    modifier: Modifier
) {
    DropdownChoiceMenu(
        selectedOption = selectedOption,
        options = options,
        onOptionChange = onOptionChange,
        contentPadding = PaddingValues(5.dp),
        label = label?.let { { Text(it) } },
        inputFieldColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.clip(RoundedCornerShape(5.dp)),
        inputFieldModifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun <T> FilterDropdownMultiChoice(
    selectedOptions: List<LabeledEntry<T>>,
    options: List<LabeledEntry<T>>,
    onOptionSelect: (LabeledEntry<T>) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    modifier: Modifier = Modifier
) {
    DropdownMultiChoiceMenu(
        selectedOptions = selectedOptions,
        options = options,
        onOptionSelect = onOptionSelect,
        contentPadding = PaddingValues(5.dp),
        label = label?.let { { FilterLabelAndCount(label, selectedOptions.size) } },
        placeholder = placeholder,
        inputFieldColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.clip(RoundedCornerShape(5.dp)),
        inputFieldModifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun <T> FilterDropdownMultiChoiceWithSearch(
    selectedOptions: List<LabeledEntry<T>>,
    options: List<LabeledEntry<T>>,
    onOptionSelect: (LabeledEntry<T>) -> Unit,
    onSearch: suspend (String) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    modifier: Modifier = Modifier
) {
    DropdownChoiceMenuWithSearch(
        selectedOptions = selectedOptions,
        options = options,
        onOptionSelect = onOptionSelect,
        onSearch = onSearch,
        contentPadding = PaddingValues(5.dp),
        label = label?.let { { FilterLabelAndCount(label, selectedOptions.size) } },
        placeholder = placeholder,
        inputFieldColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.clip(RoundedCornerShape(5.dp)),
        textFieldModifier = Modifier.fillMaxWidth()
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TagFiltersDropdownMenu(
    allTags: List<String>,
    includeTags: List<String>,
    excludeTags: List<String>,
    onTagSelect: (String) -> Unit,

    allGenres: List<String> = emptyList(),
    includeGenres: List<String> = emptyList(),
    excludeGenres: List<String> = emptyList(),
    onGenreSelect: (String) -> Unit = {},

    onReset: () -> Unit,

    inclusionMode: TagInclusionMode,
    onInclusionModeChange: (TagInclusionMode) -> Unit,
    exclusionMode: TagExclusionMode,
    onExclusionModeChange: (TagExclusionMode) -> Unit,

    label: String? = null,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
    inputFieldModifier: Modifier = Modifier,
    inputFieldColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(10.dp)
) {
    val strings = LocalStrings.current.filters
    var isExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val inputValue = remember(includeGenres, includeTags, excludeGenres, excludeTags) {
        val include = includeGenres + includeTags
        val exclude = excludeGenres + excludeTags

        val value = buildString {
            if (include.isNotEmpty() && exclude.isNotEmpty()) {
                append("Include ")
                append(include.joinToString())
                append(" and exclude ")
                append(exclude.joinToString())
            } else if (include.isNotEmpty()) {
                append("Include ")
                append(include.joinToString())
            } else if (exclude.isNotEmpty()) {
                append("Exclude ")
                append(exclude.joinToString())
            }
        }
        value.ifBlank { placeholder ?: strings.anyValue }
    }

    BasicTooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
        tooltip = {
            Card(
                border = BorderStroke(Dp.Hairline, MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = inputValue,
                    modifier = Modifier.padding(10.dp),
                )
            }
        },
        state = rememberBasicTooltipState(),
    ) {
        ExposedDropdownMenuBox(
            modifier = modifier,
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it },
        ) {
            InputField(
                value = inputValue,
                modifier = Modifier
                    .menuAnchor(PrimaryNotEditable)
                    .then(inputFieldModifier),
                label = label?.let {
                    {
                        FilterLabelAndCount(
                            label,
                            includeGenres.size + includeTags.size,
                            excludeGenres.size + excludeTags.size
                        )
                    }
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                color = inputFieldColor,
                contentPadding = contentPadding
            )

            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                scrollState = scrollState,
                modifier = Modifier
                    .widthIn(min = 400.dp, max = 800.dp)
                    .fillMaxWidth()
                    .scrollbar(scrollState, Orientation.Vertical)
            ) {
                TagFilterDropdownContent(
                    allTags = allTags,
                    includeTags = includeTags,
                    excludeTags = excludeTags,
                    onTagSelect = onTagSelect,
                    allGenres = allGenres,
                    includeGenres = includeGenres,
                    excludeGenres = excludeGenres,
                    onGenreSelect = onGenreSelect,
                    onReset = onReset,
                    inclusionMode = inclusionMode,
                    onInclusionModeChange = onInclusionModeChange,
                    exclusionMode = exclusionMode,
                    onExclusionModeChange = onExclusionModeChange
                )

            }
        }
    }

}

@Composable
private fun TagFilterDropdownContent(
    allTags: List<String>,
    includeTags: List<String>,
    excludeTags: List<String>,
    onTagSelect: (String) -> Unit,

    allGenres: List<String> = emptyList(),
    includeGenres: List<String> = emptyList(),
    excludeGenres: List<String> = emptyList(),
    onGenreSelect: (String) -> Unit = {},

    onReset: () -> Unit,

    inclusionMode: TagInclusionMode,
    onInclusionModeChange: (TagInclusionMode) -> Unit,
    exclusionMode: TagExclusionMode,
    onExclusionModeChange: (TagExclusionMode) -> Unit,
) {
    val strings = LocalStrings.current.filters
    var tagsFilter by remember { mutableStateOf("") }
    var filteredGenreOptions by remember { mutableStateOf(allGenres) }
    var filteredTagsOptions by remember { mutableStateOf(allTags) }
    LaunchedEffect(tagsFilter) {
        filteredGenreOptions = allGenres.filter { genre -> genre.contains(tagsFilter) }
        filteredTagsOptions = allTags.filter { tag -> tag.contains(tagsFilter) }
    }

    Column(Modifier.padding(15.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NoPaddingTextField(
                text = tagsFilter,
                placeholder = strings.filterTagsSearch,
                onTextChange = { tagsFilter = it },
                modifier = Modifier.weight(1f).height(40.dp),
            )
            OutlinedButton(
                onClick = {
                    tagsFilter = ""
                    onReset()
                },
                enabled = includeTags.isNotEmpty() || excludeTags.isNotEmpty() || includeGenres.isNotEmpty() || excludeGenres.isNotEmpty(),
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.cursorForHand()
            ) {
                Text(strings.filterTagsReset, style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (allGenres.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(strings.filterTagsGenreLabel)
                HorizontalDivider(Modifier.padding(start = 10.dp))
            }
            TagsRow(filteredGenreOptions, includeGenres, excludeGenres, onGenreSelect)
        }

        if (allTags.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(strings.filterTagsTagsLabel)
                HorizontalDivider(Modifier.padding(start = 10.dp))
            }
            TagsRow(filteredTagsOptions, includeTags, excludeTags, onTagSelect)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Text("Other Options")
            HorizontalDivider(Modifier.padding(start = 10.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            DropdownChoiceMenu(
                selectedOption = remember(inclusionMode) {
                    LabeledEntry(inclusionMode, strings.forInclusionMode(inclusionMode))
                },
                options = remember {
                    TagInclusionMode.entries.map {
                        LabeledEntry(it, strings.forInclusionMode(it))
                    }
                },
                onOptionChange = { onInclusionModeChange(it.value) },
                label = { Text("Inclusion mode") }
            )

            DropdownChoiceMenu(
                selectedOption = remember(exclusionMode) {
                    LabeledEntry(exclusionMode, strings.forExclusionMode(exclusionMode))
                },
                options = remember {
                    TagExclusionMode.entries.map {
                        LabeledEntry(it, strings.forExclusionMode(it))
                    }
                },
                onOptionChange = { onExclusionModeChange(it.value) },
                label = { Text("Exclusion mode") }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsRow(
    tags: List<String>,
    includeTags: List<String>,
    excludeTags: List<String>,
    onTagSelect: (String) -> Unit
) {
    val strings = LocalStrings.current.filters
    val maxTagNum = 30
    var isExpanded by remember { mutableStateOf(false) }
    val tagsToTake = if (isExpanded) tags else tags.take(maxTagNum)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.animateContentSize()
    ) {
        tagsToTake.forEach { tag ->
            val includeType = when {
                includeTags.contains(tag) -> IncludeType.INCLUDE
                excludeTags.contains(tag) -> IncludeType.EXCLUDE
                else -> IncludeType.NONE
            }
            TagFilterChip(
                tag = tag,
                includeType = includeType,
                onSelect = onTagSelect
            )
        }

        if (tags.size > maxTagNum) {
            TextButton(
                onClick = { isExpanded = !isExpanded },
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.cursorForHand()
            ) {
                Text(
                    if (isExpanded) strings.filterTagsShowLess else strings.filterTagsShowMore,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private enum class IncludeType {
    INCLUDE, EXCLUDE, NONE
}

@Composable
private fun TagFilterChip(
    tag: String,
    includeType: IncludeType,
    onSelect: (String) -> Unit,
) {

    val (borderColor, textColor) = when (includeType) {
        IncludeType.INCLUDE -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary
        IncludeType.EXCLUDE -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        IncludeType.NONE -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.primary
    }

    NoPaddingChip(
        onClick = { onSelect(tag) },
        color = MaterialTheme.colorScheme.surface,
        borderColor = borderColor
    ) {
        Text(tag, style = MaterialTheme.typography.labelLarge.copy(color = textColor))
    }
}

@Composable
private fun <T> DropdownMultiChoiceItem(
    option: LabeledEntry<T>,
    onOptionSelect: (LabeledEntry<T>) -> Unit,
    selected: Boolean,
) {
    val color = if (selected) MaterialTheme.colorScheme.tertiary else Color.Unspecified
    DropdownMenuItem(
        text = { Text(text = option.label, color = color) },
        onClick = { onOptionSelect(option) },
        modifier = Modifier.cursorForHand(),
        leadingIcon = {
            if (selected) Icon(Icons.Default.RadioButtonChecked, null, tint = color)
            else Icon(Icons.Default.RadioButtonUnchecked, null)
        }
    )
}

@Composable
private fun FilterLabelAndCount(label: String, includeCount: Int, excludeCount: Int = 0) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        if (includeCount > 0) {
            Text(
                " + $includeCount",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.secondary
                ),
            )
        }

        if (excludeCount > 0) {
            Text(
                " - $excludeCount",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.error
                ),
            )
        }
    }
}

data class LabeledEntry<T>(
    val value: T,
    val label: String
) {

    companion object {
        fun intEntry(value: Int) = LabeledEntry(value, value.toString())
        fun stringEntry(value: String): LabeledEntry<String> = LabeledEntry(value, value)

    }
}

