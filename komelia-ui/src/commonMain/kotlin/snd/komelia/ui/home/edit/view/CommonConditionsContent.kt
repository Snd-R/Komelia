package snd.komelia.ui.home.edit.view

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryEditable
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import snd.komelia.ui.common.components.DropdownChoiceMenu
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.common.components.scrollbar
import snd.komelia.ui.home.BooleanOpState
import snd.komelia.ui.home.DateOpState
import snd.komelia.ui.home.EqualityNullableOpState
import snd.komelia.ui.home.EqualityOpState
import snd.komelia.ui.home.StringOpState
import snd.komelia.ui.home.edit.AuthorConditionState
import snd.komelia.ui.home.edit.DeletedConditionState
import snd.komelia.ui.home.edit.LibraryConditionState
import snd.komelia.ui.home.edit.OneShotConditionState
import snd.komelia.ui.home.edit.ReadStatusConditionState
import snd.komelia.ui.home.edit.ReleaseDateConditionState
import snd.komelia.ui.home.edit.TagConditionState
import snd.komelia.ui.home.edit.TitleConditionState
import snd.komelia.ui.platform.cursorForHand
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.common.KomgaSort
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

val conditionInputMinWidth = 200.dp

@Composable
fun <T> SimpleConditionLayout(
    conditionType: LabeledEntry<T>,
    options: List<LabeledEntry<T>>,
    onConditionTypeChange: (T) -> Unit,
    onConditionRemove: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onConditionRemove) { Icon(Icons.Default.Delete, null) }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            DropdownChoiceMenu(
                conditionType,
                options = options,
                onOptionChange = { onConditionTypeChange(it.value) },
                label = { Text("Condition") },
                inputFieldModifier = Modifier.widthIn(conditionInputMinWidth)
            )
            content()
        }
    }
}

@Composable
fun TagConditionContent(state: TagConditionState) {
    EqualityNullableOpDropdownSearchContent(
        state,
        state.tags.collectAsState(emptyList()).value,
        "Tag"
    )
}

@Composable
fun RowScope.ReadStatusConditionContent(state: ReadStatusConditionState) {
    val value = state.value.collectAsState().value
    EqualityOpDropDownContent(
        operator = state.operator.collectAsState().value,
        onOpChange = state::setOp,
        selectedValue = remember(value) { value?.let { LabeledEntry(it, it.name) } },
        valueOptions = remember { KomgaReadStatus.entries.map { LabeledEntry(it, it.name) } },
        onValueChange = state::setValue
    )
}

@Composable
fun RowScope.TitleConditionContent(state: TitleConditionState) {
    StringOpContent(
        operator = state.operator.collectAsState().value,
        onOperatorChange = state::setOp,
        value = state.value.collectAsState().value,
        onValueChange = state::setValue
    )
}

@Composable
fun RowScope.LibraryConditionContent(
    state: LibraryConditionState,
) {
    val value = state.value.collectAsState().value
    val libraries = state.libraries.collectAsState(emptyList()).value
    EqualityOpDropDownContent(
        operator = state.operator.collectAsState().value,
        onOpChange = state::setOp,
        selectedValue = remember(value, libraries) {
            value?.let { libraryId ->
                LabeledEntry(
                    libraryId,
                    libraries.firstOrNull { it.id == libraryId }?.name ?: libraryId.value
                )
            }
        },
        valueOptions = remember(libraries) { libraries.map { LabeledEntry(it.id, it.name) } },
        onValueChange = state::setValue
    )
}

@Composable
fun RowScope.AuthorConditionContent(
    state: AuthorConditionState,
) {
    val operator = state.operator.collectAsState().value
    val roleOptions = state.roleOptions.collectAsState(emptyList()).value
    val nameOptions = state.nameOptions.collectAsState(emptyList()).value
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
            LabeledEntry(currentValue?.role, currentValue?.role ?: "Any")
        },
        options = remember(roleOptions) {
            listOf(LabeledEntry<String?>(null, "Any"))
                .plus(roleOptions.map { LabeledEntry(it, it) })
        },
        onOptionChange = { state.setRoleValue(it.value) },
        inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
        label = { Text("Role") }
    )

    SearchableOptionSelectionField(
        searchText = state.searchText.collectAsState().value,
        onSearchTextChange = state::setSearchText,
        options = nameOptions.map { LabeledEntry.stringEntry(it) },
        onValueChange = { state.setNameValue(it) },
        label = "Author"
    )
}

@Composable
fun RowScope.ReleaseDateConditionContent(
    state: ReleaseDateConditionState,
) {
    DateOpContent(
        operator = state.operator.collectAsState().value,
        onOperatorChange = state::setOp,
        date = state.date.collectAsState().value,
        onDateChange = state::setDate,
        duration = state.period.collectAsState().value,
        onDurationChange = state::setPeriod
    )
}

@Composable
fun RowScope.DeletedConditionContent(
    state: DeletedConditionState,
) {
    BooleanOpContent(
        operator = state.operator.collectAsState().value,
        onOperatorChange = state::setOp
    )
}

@Composable
fun RowScope.OneShotConditionContent(
    state: OneShotConditionState,
) {
    BooleanOpContent(
        operator = state.operator.collectAsState().value,
        onOperatorChange = state::setOp
    )
}

@Composable
fun RowScope.EqualityNullableOpContent(
    state: EqualityNullableOpState<String>,
    options: List<String>,
) {
    val operator = state.operator.collectAsState().value
    var value by remember { mutableStateOf(state.value.value ?: "") }
    DropdownChoiceMenu(
        selectedOption = LabeledEntry(operator, operator.name),
        options = EqualityNullableOpState.Op.entries.map { LabeledEntry(it, it.name) },
        onOptionChange = { state.setOp(it.value) },
        inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
        label = { Text("Operator") }
    )

    if (operator != EqualityNullableOpState.Op.IsNull && operator != EqualityNullableOpState.Op.IsNotNull)
        StringSelectionField(
            value = value,
            onValueChange = {
                value = it
                state.setValue(it)
            },
            options = options,
        )
}

@Composable
fun RowScope.BooleanOpContent(
    operator: BooleanOpState.Op,
    onOperatorChange: (BooleanOpState.Op) -> Unit,
) {
    DropdownChoiceMenu(
        selectedOption = LabeledEntry(operator, operator.name),
        options = BooleanOpState.Op.entries.map { LabeledEntry(it, it.name) },
        onOptionChange = { onOperatorChange(it.value) },
        inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
        label = { Text("Operator") }
    )
}

@Composable
fun <T> RowScope.EqualityOpDropDownContent(
    operator: EqualityOpState.Op,
    onOpChange: (EqualityOpState.Op) -> Unit,
    selectedValue: LabeledEntry<T>?,
    valueOptions: List<LabeledEntry<T>>,
    onValueChange: (T) -> Unit,
) {
    DropdownChoiceMenu(
        selectedOption = LabeledEntry(operator, operator.name),
        options = EqualityOpState.Op.entries.map { LabeledEntry(it, it.name) },
        onOptionChange = { onOpChange(it.value) },
        inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
        label = { Text("Operator") }
    )

    DropdownChoiceMenu(
        selectedOption = selectedValue,
        options = valueOptions,
        onOptionChange = { onValueChange(it.value) },
        inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
        label = { Text("Value") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StringSelectionField(
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
) {
    val suggestedOptions = derivedStateOf {
        options
            .filter { it.lowercase().contains(value.lowercase()) }
            .take(50)
    }

    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val focused = interactionSource.collectIsFocusedAsState().value
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        var isExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it },
        ) {
            TextField(
                value = value,
                onValueChange = { newText ->
                    onValueChange(newText)
                    options.find { it == newText }?.let { onValueChange(it) }
                },
                modifier = Modifier
                    .widthIn(min = conditionInputMinWidth)
                    .menuAnchor(PrimaryEditable),

                interactionSource = interactionSource
            )

            ExposedDropdownMenu(
                expanded = isExpanded || focused,
                onDismissRequest = { isExpanded = false }
            ) {
                suggestedOptions.value.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            isExpanded = false
                            focusManager.clearFocus()
                            onValueChange(it)
                        }
                    )
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableOptionSelectionField(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    options: List<LabeledEntry<T>>,
    onValueChange: (T) -> Unit,
    label: String,
) {
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val focused = interactionSource.collectIsFocusedAsState().value
    var textFieldText by remember { mutableStateOf(searchText) }
    val suggestedOptions = remember(options, textFieldText) {
        options.filter { it.label.lowercase().contains(searchText) }.take(50)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        var isExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it },
        ) {
            TextField(
                value = textFieldText,
                onValueChange = { newText ->
                    textFieldText = newText
                    onSearchTextChange(newText)
                },
                modifier = Modifier
                    .widthIn(min = conditionInputMinWidth)
                    .menuAnchor(PrimaryEditable),
                interactionSource = interactionSource,
                label = { Text(label) }
            )

            ExposedDropdownMenu(
                expanded = isExpanded || focused,
                onDismissRequest = { isExpanded = false }
            ) {
                suggestedOptions.forEach {
                    DropdownMenuItem(
                        text = { Text(it.label) },
                        onClick = {
                            isExpanded = false
                            focusManager.clearFocus()
                            textFieldText = it.label
                            onValueChange(it.value)
                        }
                    )
                }

            }
        }
    }
}

@Composable
fun RowScope.StringOpContent(
    operator: StringOpState.Op,
    onOperatorChange: (StringOpState.Op) -> Unit,
    value: String?,
    onValueChange: (String) -> Unit,
) {
    DropdownChoiceMenu(
        selectedOption = LabeledEntry(operator, operator.name),
        options = StringOpState.Op.entries.map { LabeledEntry(it, it.name) },
        onOptionChange = { onOperatorChange(it.value) },
        inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
        label = { Text("Operator") }
    )

    var textValue by remember { mutableStateOf(value ?: "") }
    TextField(
        value = textValue,
        onValueChange = {
            textValue = it
            onValueChange(it)
        },
        modifier = Modifier.widthIn(min = conditionInputMinWidth)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.DateOpContent(
    operator: DateOpState.Op,
    onOperatorChange: (DateOpState.Op) -> Unit,

    date: Instant?,
    onDateChange: (Instant) -> Unit,

    duration: Duration?,
    onDurationChange: (Duration?) -> Unit,
) {
    DropdownChoiceMenu(
        selectedOption = LabeledEntry(operator, operator.name),
        options = DateOpState.Op.entries.map { LabeledEntry(it, it.name) },
        onOptionChange = { onOperatorChange(it.value) },
        inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
        label = { Text("Operator") }
    )

    when (operator) {
        DateOpState.Op.IsBefore, DateOpState.Op.IsAfter -> DatePickerField(date, onDateChange)
        DateOpState.Op.IsInLast, DateOpState.Op.IsNotInLast -> PeriodPickerField(duration, onDurationChange)
        DateOpState.Op.IsNull, DateOpState.Op.IsNotNull -> {}
    }
}


@Composable
fun IntTextField(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    label: String
) {
    var valueText by remember { mutableStateOf(value?.toString() ?: "") }
    TextField(
        value = valueText,
        onValueChange = { newText ->
            if (newText.isBlank()) {
                valueText = ""
                onValueChange(null)
            } else {
                newText.toIntOrNull()?.let { number ->
                    valueText = newText
                    onValueChange(number)
                }
            }

        },
        label = { Text(label) },
        modifier = Modifier.width(100.dp),
    )
}

@Composable
fun FloatTextField(
    value: Float?,
    onValueChange: (Float?) -> Unit,
    label: String
) {
    var valueText by remember { mutableStateOf(value?.toString() ?: "") }
    TextField(
        value = valueText,
        onValueChange = { newText ->
            if (newText.isBlank()) {
                valueText = ""
                onValueChange(null)
            } else {
                newText.toFloatOrNull()?.let { number ->
                    valueText = newText
                    onValueChange(number)
                }
            }

        },
        label = { Text(label) },
        modifier = Modifier.width(100.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    currentDate: Instant?,
    onDateChange: (Instant) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentDate?.toEpochMilliseconds())


    TextField(
        value = currentDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.toString() ?: "",
        onValueChange = { },
        placeholder = { Text("MM/DD/YYYY") },
        trailingIcon = {
            Icon(Icons.Default.DateRange, null)
        },
        modifier = Modifier
            .pointerInput(currentDate) {
                awaitEachGesture {
                    // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
                    // in the Initial pass to observe events before the text field consumes them
                    // in the Main pass.
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        showDatePicker = true
                    }
                }
            }
    )
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateChange(Instant.fromEpochMilliseconds(it))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodPickerField(
    duration: Duration?,
    onPeriodChange: (Duration?) -> Unit,
) {
    var durationText by remember { mutableStateOf(duration?.inWholeDays?.toString() ?: "") }

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        TextField(
            value = durationText,
            onValueChange = { newText ->
                if (newText.isBlank()) {
                    durationText = ""
                    onPeriodChange(null)
                } else {
                    newText.toIntOrNull()?.let { number ->
                        durationText = newText
                        onPeriodChange(number.days)
                    }
                }

            },
            label = { Text("Days") },
            modifier = Modifier.width(100.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ConditionAddButton(
    conditions: List<LabeledEntry<T>>,
    onConditionAdd: (T) -> Unit,
) {
    var dropDownExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = dropDownExpanded,
        onExpandedChange = { dropDownExpanded = it },
    ) {
        FilledTonalButton(
            onClick = { dropDownExpanded = true },
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier
                .cursorForHand()
                .menuAnchor(PrimaryNotEditable)
        ) {
            Text("Add condition")
        }

        val scrollState = rememberScrollState()
        ExposedDropdownMenu(
            expanded = dropDownExpanded,
            onDismissRequest = { dropDownExpanded = false },
            scrollState = scrollState,
            modifier = Modifier
                .widthIn(min = 200.dp)
                .scrollbar(scrollState, Orientation.Vertical)
        ) {
            conditions.forEach {
                DropdownMenuItem(
                    text = { Text(it.label) },
                    onClick = {
                        dropDownExpanded = false
                        onConditionAdd(it.value)
                    },
                    modifier = Modifier.cursorForHand()
                )
            }
        }
    }
}

@Composable
fun EqualityNullableOpDropdownSearchContent(
    state: EqualityNullableOpState<String>,
    options: List<String>,
    label: String
) {
    var suggestedOptions by remember(options) { mutableStateOf(options.take(50)) }
    val operator = state.operator.collectAsState().value
    val value = state.value.collectAsState().value
    DropdownChoiceMenu(
        selectedOption = LabeledEntry(operator, operator.name),
        options = EqualityNullableOpState.Op.entries.map { LabeledEntry(it, it.name) },
        onOptionChange = { state.setOp(it.value) },
        inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
        label = { Text("Operator") }
    )

    if (operator != EqualityNullableOpState.Op.IsNull && operator != EqualityNullableOpState.Op.IsNotNull) {
        var searchText by remember { mutableStateOf(value ?: "") }
        SearchableOptionSelectionField(
            searchText = searchText,
            onSearchTextChange = {
                searchText = it
                suggestedOptions = options.filter { it.contains(searchText, true) }
            },
            options = remember(suggestedOptions) { suggestedOptions.map { LabeledEntry.stringEntry(it) } },
            onValueChange = { state.setValue(it) },
            label = label,
        )
    }
}

@Composable
fun EqualityOpDropdownSearchContent(
    state: EqualityOpState<String>,
    options: List<String>,
    label: String
) {
    var suggestedOptions by remember(options) { mutableStateOf(options.take(50)) }
    val operator = state.operator.collectAsState().value
    val value = state.value.collectAsState().value
    DropdownChoiceMenu(
        selectedOption = LabeledEntry(operator, operator.name),
        options = EqualityOpState.Op.entries.map { LabeledEntry(it, it.name) },
        onOptionChange = { state.setOp(it.value) },
        inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
        label = { Text("Operator") }
    )

    var searchText by remember { mutableStateOf(value ?: "") }
    SearchableOptionSelectionField(
        searchText = searchText,
        onSearchTextChange = {
            searchText = it
            suggestedOptions = options.filter { it.contains(searchText, true) }
        },
        options = remember(suggestedOptions) { suggestedOptions.map { LabeledEntry.stringEntry(it) } },
        onValueChange = { state.setValue(it) },
        label = label
    )
}

@Composable
fun <T> PageSettingsContent(
    pageSize: Int,
    onPageSizeChange: (Int) -> Unit,
    sort: LabeledEntry<T>,
    sortOptions: List<LabeledEntry<T>>,
    onSortChange: (T) -> Unit,
    sortDirection: KomgaSort.Direction,
    onSortDirectionChange: (KomgaSort.Direction) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        DropdownChoiceMenu(
            selectedOption = sort,
            options = sortOptions,
            onOptionChange = { onSortChange(it.value) },
            label = { Text("Sort") },
            inputFieldModifier = Modifier.widthIn(min = 150.dp)
        )
        DropdownChoiceMenu(
            selectedOption = remember(sortDirection) { LabeledEntry(sortDirection, sortDirection.name) },
            options = remember { KomgaSort.Direction.entries.map { LabeledEntry(it, it.name) } },
            onOptionChange = { onSortDirectionChange(it.value) },
            label = { Text("Direction") },
            inputFieldModifier = Modifier.widthIn(min = 60.dp)
        )
        PageSizeSettingsContent(pageSize, onPageSizeChange)
    }

}

@Composable
fun PageSizeSettingsContent(pageSize: Int, onPageSizeChange: (Int) -> Unit) {
    var pageSizeText by remember { mutableStateOf(pageSize.toString()) }
    TextField(
        value = pageSizeText,
        onValueChange = { newText ->
            if (newText.isBlank()) {
                pageSizeText = ""
                onPageSizeChange(0)
            } else if (newText.length < 4) {
                newText.toIntOrNull()?.let { number ->
                    pageSizeText = newText
                    onPageSizeChange(number)
                }
            }
        },
        label = { Text("Limit") },
        modifier = Modifier.width(70.dp),
    )

}
