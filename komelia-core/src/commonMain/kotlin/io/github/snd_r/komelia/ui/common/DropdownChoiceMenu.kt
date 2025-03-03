package io.github.snd_r.komelia.ui.common

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.platform.scrollbar
import io.github.snd_r.komelia.ui.LocalStrings
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownChoiceMenu(
    selectedOption: LabeledEntry<T>,
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
            value = selectedOption.label,
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

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
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

        DropdownMenu(
            modifier = modifier,
            scrollState = rememberScrollState(),
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagFiltersDropdownMenu(
    selectedTags: List<String>,
    tagOptions: List<String>,
    onTagSelect: (String) -> Unit,

    selectedGenres: List<String> = emptyList(),
    genreOptions: List<String> = emptyList(),
    onGenreSelect: (String) -> Unit = {},

    onReset: () -> Unit,

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
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
    ) {
        InputField(
            value = selectedGenres.plus(selectedTags).joinToString()
                .ifBlank { placeholder ?: strings.anyValue },
            modifier = Modifier
                .menuAnchor(PrimaryNotEditable)
                .then(inputFieldModifier),
            label = label?.let {
                {
                    FilterLabelAndCount(
                        label,
                        selectedGenres.size + selectedTags.size
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
        ) {
            TagFilterDropdownContent(
                selectedGenres = selectedGenres,
                genreOptions = genreOptions,
                onGenreSelect = onGenreSelect,
                selectedTags = selectedTags,
                tagOptions = tagOptions,
                onTagSelect = onTagSelect,
                onReset = onReset,
            )

        }
    }
}

@Composable
private fun TagFilterDropdownContent(
    selectedGenres: List<String>,
    genreOptions: List<String>,
    onGenreSelect: (String) -> Unit,

    selectedTags: List<String>,
    tagOptions: List<String>,
    onTagSelect: (String) -> Unit,

    onReset: () -> Unit,
) {
    val strings = LocalStrings.current.filters
    var tagsFilter by remember { mutableStateOf("") }
    var filteredGenreOptions by remember { mutableStateOf(genreOptions) }
    var filteredTagsOptions by remember { mutableStateOf(tagOptions) }
    LaunchedEffect(tagsFilter) {
        filteredGenreOptions = genreOptions.filter { genre -> genre.contains(tagsFilter) }
        filteredTagsOptions = tagOptions.filter { tag -> tag.contains(tagsFilter) }
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
                enabled = selectedTags.isNotEmpty() || selectedGenres.isNotEmpty(),
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.cursorForHand()
            ) {
                Text(strings.filterTagsReset, style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (genreOptions.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(strings.filterTagsGenreLabel)
                HorizontalDivider(Modifier.padding(start = 10.dp))
            }
            TagsRow(filteredGenreOptions, selectedGenres, onGenreSelect)
        }

        if (tagOptions.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(strings.filterTagsTagsLabel)
                HorizontalDivider(Modifier.padding(start = 10.dp))
            }
            TagsRow(filteredTagsOptions, selectedTags, onTagSelect)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsRow(
    tags: List<String>,
    selectedTags: List<String>,
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
            TagFilterChip(
                tag = tag,
                selected = selectedTags.contains(tag),
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

@Composable
private fun TagFilterChip(
    tag: String,
    selected: Boolean,
    onSelect: (String) -> Unit,
) {
    NoPaddingChip(
        onClick = { onSelect(tag) },
        color = MaterialTheme.colorScheme.surface,
        borderColor = if (selected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            tag,
            style = MaterialTheme.typography.labelLarge.copy(
                color = if (selected) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.primary
            )
        )
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
private fun FilterLabelAndCount(label: String, count: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        if (count > 0) {
            Text(
                " + $count",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.tertiary
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
        fun stringEntry(value: String) = LabeledEntry(value, value)

    }
}

