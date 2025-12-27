package snd.komelia.ui.settings.komf

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import snd.komelia.ui.common.components.HttpTextField
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.common.components.PasswordTextField

@OptIn(ExperimentalLayoutApi::class)
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
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SavableHttpTextField(
    label: String,
    currentValue: String,
    onValueSave: (String) -> Unit,
    confirmationText: String = "Save",
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
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

val komfLanguageTagsSuggestions = listOf(
    LabeledEntry("en", "English (en)"),
    LabeledEntry("ja", "Japanese (ja)"),
    LabeledEntry("ja-ro", "Japanese romanized (ja-ro)"),
    LabeledEntry("ko", "Korean (ko)"),
    LabeledEntry("ko-ro", "Korean romanized (ko-ro)"),
    LabeledEntry("zh", "Simplified Chinese (zh)"),
    LabeledEntry("zh-hk", "Traditional Chinese (zh-hk)"),
    LabeledEntry("zh-ro", "Chinese romanized (zh-ro)"),
    LabeledEntry("pt-br", "Brazilian Portugese (pt-br)"),
    LabeledEntry("es", "Castilian Spanish (es)"),
    LabeledEntry("es-la", "Latin American Spanish (es-la)"),
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
        komfLanguageTagsSuggestions.filter { (_, label) -> label.lowercase().contains(languageValue.lowercase()) }
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
                        text = { Text(it.label) },
                        onClick = {
                            isExpanded = false
                            focusManager.clearFocus()
                            onLanguageValueChange(it.value)
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
