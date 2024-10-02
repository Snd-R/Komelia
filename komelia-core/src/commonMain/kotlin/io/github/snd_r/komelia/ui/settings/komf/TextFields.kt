package io.github.snd_r.komelia.ui.settings.komf

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryEditable
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.dokar.chiptextfield.Chip
import com.dokar.chiptextfield.m3.ChipTextField
import com.dokar.chiptextfield.rememberChipTextFieldState
import io.github.snd_r.komelia.ui.common.HttpTextField
import io.github.snd_r.komelia.ui.common.PasswordTextField
import kotlinx.coroutines.flow.drop

@Composable
fun SavableTextField(
    currentValue: String,
    onValueSave: (String) -> Unit,
    label: @Composable () -> Unit,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    valueChangePolicy: (String) -> Boolean = { true },
    useEditButton: Boolean = false,
    isPassword: Boolean = false,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        var isChanged by remember { mutableStateOf(false) }
        var editable by remember { mutableStateOf(!useEditButton) }
        var textFieldValue by remember(currentValue) { mutableStateOf(currentValue) }

        if (isPassword) {
            PasswordTextField(
                value = textFieldValue,
                enabled = editable,
                onValueChange = {
                    if (valueChangePolicy(it)) {
                        textFieldValue = it
                        isChanged = true
                    }
                },
                supportingText = supportingText,
                isError = isError,
                label = label,
                modifier = Modifier.weight(1f).animateContentSize()
            )
        } else {
            TextField(
                value = textFieldValue,
                enabled = editable,
                onValueChange = {
                    if (valueChangePolicy(it)) {
                        textFieldValue = it
                        isChanged = true
                    }
                },
                label = label,
                supportingText = supportingText,
                isError = isError,
                singleLine = true,
                modifier = Modifier.weight(1f).animateContentSize()
            )
        }

        if (!editable) {
            ElevatedButton(
                onClick = {
                    editable = true
                    textFieldValue = ""
                },
                shape = RoundedCornerShape(5.dp),
            ) {
                Text("Edit")
            }
        }

        AnimatedVisibility(editable) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ElevatedButton(
                    enabled = useEditButton || isChanged,
                    onClick = {
                        if (useEditButton) editable = false
                        isChanged = false
                        textFieldValue = currentValue
                    },
                    shape = RoundedCornerShape(5.dp),
                ) {
                    Text("Discard")
                }
                FilledTonalButton(
                    onClick = {
                        onValueSave(textFieldValue)
                        if (useEditButton) editable = false
                        isChanged = false
                    },
                    enabled = isChanged,
                    shape = RoundedCornerShape(5.dp),
                ) {
                    Text("Save")
                }
            }
        }

    }
}

@Composable
fun SavableHttpTextField(
    label: String,
    currentValue: String,
    onValueSave: (String) -> Unit,
    confirmationText: String = "Save",
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        var isChanged by remember { mutableStateOf(false) }
        var textFieldValue by remember(currentValue) { mutableStateOf(currentValue) }
        HttpTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                isChanged = true
            },
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            isError = isError,
            supportingText = supportingText
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ElevatedButton(
                enabled = isChanged,
                onClick = {
                    isChanged = false
                    textFieldValue = currentValue
                },
                shape = RoundedCornerShape(5.dp),
            ) {
                Text("Discard")
            }
            FilledTonalButton(
                onClick = {
                    onValueSave(textFieldValue)
                    isChanged = false
                },
                enabled = isChanged || isError,
                shape = RoundedCornerShape(5.dp),
            ) {
                Text(confirmationText)
            }
        }
    }

}

val komfLanguageTagsSuggestions = mapOf(
    "en" to "English (en)",
    "ja" to "Japanese (ja)",
    "ja-ro" to "Japanese romanized (ja-ro)",
    "ko" to "Korean (ko)",
    "ko-ro" to "Korean romanized (ko-ro)",
    "zh" to "Simplified Chinese (zh)",
    "zh-hk" to "Traditional Chinese (zh-hk)",
    "zh-ro" to "Chinese romanized (zh-ro)",
    "pt-br" to "Brazilian Portugese (pt-br)",
    "es" to "Castilian Spanish (es)",
    "es-la" to "Latin American Spanish (es-la)",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionField(
    label: String,
    languageValue: String,
    onLanguageValueChange: (String) -> Unit,
    onLanguageValueSave: () -> Unit,
) {
    var isChanged by remember { mutableStateOf(false) }
    val suggestedOptions = derivedStateOf {
        komfLanguageTagsSuggestions.filter { (key, _) -> key.startsWith(languageValue) }
    }
    val focusManager = LocalFocusManager.current
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        var isExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it },
            modifier = Modifier.weight(1f)
        ) {
            TextField(
                value = languageValue,
                onValueChange = {
                    onLanguageValueChange(it)
                    isChanged = true
                },
                label = { Text(label) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(PrimaryEditable)
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                suggestedOptions.value.forEach {
                    DropdownMenuItem(
                        text = { Text(it.value) },
                        onClick = {
                            isExpanded = false
                            focusManager.clearFocus()
                            onLanguageValueChange(it.key)
                            onLanguageValueSave()
                            isChanged = false
                        }
                    )
                }

            }
        }
        FilledTonalButton(
            onClick = {
                onLanguageValueSave()
                isChanged = false
            },
            enabled = isChanged,
            shape = RoundedCornerShape(5.dp),
        ) {
            Text("Save")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageChipsField(
    currentLanguages: List<String>,
    onLanguagesChange: (List<String>) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val chipState = rememberChipTextFieldState(currentLanguages.map { Chip(it) })
    LaunchedEffect(Unit) {
        snapshotFlow { chipState.chips.map { it.text } }
            .drop(1)
            .collect { onLanguagesChange(it) }
    }

    var value by remember { mutableStateOf(TextFieldValue()) }
    val suggestedOptions = derivedStateOf {
        komfLanguageTagsSuggestions.filter { (key, _) -> key.startsWith(value.text) }
    }

    var isExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
    ) {
        ChipTextField(
            state = chipState,
            value = value,
            onValueChange = { value = it },
            label = { Text("Alternative title languages (ISO 639)") },
            onSubmit = { value ->
                if (chipState.chips.none { it.text == value.text }) Chip(value.text)
                else null
            },
            readOnlyChips = true,
            innerModifier = Modifier
                .fillMaxWidth()
                .menuAnchor(PrimaryEditable)
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            suggestedOptions.value.forEach { (key, value) ->
                DropdownMenuItem(
                    text = { Text(value) },
                    onClick = {
                        isExpanded = false
                        focusManager.clearFocus()
                        if (chipState.chips.none { it.text == key }) {
                            chipState.addChip(Chip(key))
                        }
                    }
                )
            }

        }
    }

}
