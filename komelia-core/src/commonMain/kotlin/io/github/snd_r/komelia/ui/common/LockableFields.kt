package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dokar.chiptextfield.Chip
import com.dokar.chiptextfield.ChipTextFieldState
import com.dokar.chiptextfield.m3.ChipTextField
import com.dokar.chiptextfield.rememberChipTextFieldState

@Composable
fun LockIcon(
    lock: StateHolder<Boolean>,
) {
    LockIcon(
        locked = lock.value,
        onLockChange = { lock.setValue(it) }
    )
}

@Composable
fun LockIcon(
    locked: Boolean,
    onLockChange: (Boolean) -> Unit
) {
    IconButton(onClick = { onLockChange(!locked) }) {
        if (locked) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
        } else {
            Icon(
                Icons.Default.LockOpen,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun LockableTextField(
    text: String,
    onTextChange: (String) -> Unit,
    errorMessage: String? = null,
    label: String? = null,
    lock: StateHolder<Boolean>,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier.withTextFieldNavigation(),

    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
) {

    Row(modifier) {
        LockIcon(lock)

        TextField(
            value = text,
            onValueChange = onTextChange,
            label = label?.let { { Text(label) } },
            isError = errorMessage != null,
            minLines = minLines,
            maxLines = maxLines,
            supportingText = { if (errorMessage != null) Text(errorMessage) },
            modifier = textFieldModifier.fillMaxWidth()
        )
    }

}

@Composable
fun <T> LockableDropDown(
    selectedOption: LabeledEntry<T>,
    options: List<LabeledEntry<T>>,
    onOptionChange: (LabeledEntry<T>) -> Unit,
    label: @Composable () -> Unit,
    lock: StateHolder<Boolean>,
    inputFieldColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    inputFieldModifier: Modifier
) {

    Row(inputFieldModifier) {
        LockIcon(lock)

        DropdownChoiceMenu(
            selectedOption = selectedOption,
            options = options,
            onOptionChange = onOptionChange,
            label = label,
            inputFieldColor = inputFieldColor,
            inputFieldModifier = Modifier.fillMaxWidth(),
        )
    }

}

@Composable
fun LockableChipTextField(
    values: StateHolder<List<String>>,
    label: String,
    lock: StateHolder<Boolean>,
) {
    LockableChipTextField(
        values = values.value,
        onValuesChange = { values.setValue(it) },
        label = label,
        locked = lock.value,
        onLockChange = { lock.setValue(it) }
    )
}

@Composable
fun LockableChipTextField(
    values: List<String>,
    onValuesChange: (List<String>) -> Unit,
    label: String,
    locked: Boolean,
    onLockChange: (Boolean) -> Unit
) {

    val state = rememberStringChipTextFieldState(values, onValuesChange)
    var textFieldValue by remember { mutableStateOf("") }

    Row {
        LockIcon(locked, onLockChange)

        ChipTextField(
            state = state,
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            label = { Text(label) },
            onSubmit = { text ->
                when {
                    text.isBlank() -> {
                        textFieldValue = ""
                        null
                    }

                    state.chips.none { it.text == text } -> {
                        Chip(text)
                    }

                    else -> {
                        textFieldValue = ""
                        null
                    }
                }
            },
            readOnlyChips = true
        )
    }
}

@Composable
fun rememberStringChipTextFieldState(
    chips: List<String>,
    onChipsChange: (List<String>) -> Unit
): ChipTextFieldState<Chip> {
    val state = rememberChipTextFieldState(chips.map { Chip(it) })
    LaunchedEffect(state, chips) {
        snapshotFlow { state.chips.map { it.text } }
            .collect { onChipsChange(it) }
    }

    return state
}
