package snd.komelia.ui.color.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import snd.komelia.color.Preset
import snd.komelia.ui.color.PresetsState
import snd.komelia.ui.common.components.CheckboxWithLabel
import snd.komelia.ui.common.components.DropdownChoiceMenu
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.dialogs.AppDialog
import snd.komelia.ui.dialogs.DialogConfirmCancelButtons

@Composable
fun <T : Preset> PresetsContent(
    state: PresetsState<T>,
    modifier: Modifier = Modifier
) {
    val selectedPreset = state.selectedPreset.collectAsState().value
    val availablePresets = state.presets.collectAsState().value
    var showNameDialog by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        DropdownChoiceMenu(
            selectedOption = remember(selectedPreset) { LabeledEntry(selectedPreset, selectedPreset?.name ?: "") },
            options = remember(availablePresets) { availablePresets.map { LabeledEntry(it, it.name) } },
            onOptionChange = { preset -> preset.value?.let { state.onPresetSelect(it) } },
            inputFieldModifier = Modifier.fillMaxWidth(),
            label = { Text("Presets") },
            modifier = Modifier.weight(1f, false).widthIn(max = 400.dp).fillMaxWidth(),
        )
        if (selectedPreset == null)
            Tooltip("Save Preset") {
                IconButton(onClick = { showNameDialog = true }) {
                    Icon(Icons.Default.Add, null)
                }
            }
        else {
            Tooltip("Delete Preset") {
                IconButton(
                    onClick = { state.onPresetDelete(selectedPreset) },
                ) {
                    Icon(Icons.Default.Delete, null)
                }
            }
        }

    }
    var newPresetName by remember { mutableStateOf("") }
    val isValidName by derivedStateOf { availablePresets.none { it.name == newPresetName } }
    var confirmOverride by remember { mutableStateOf(false) }
    if (showNameDialog) {
        AppDialog(
            modifier = Modifier.width(400.dp),
            onDismissRequest = {
                showNameDialog = false
                newPresetName = ""
            },
            header = {
                Text(
                    "Enter a name for the preset",
                    modifier = Modifier.padding(20.dp)
                )
            },
            content = {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    TextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Saved Settings") },
                        supportingText = {
                            if (!isValidName) Text(
                                "Preset with that name already exists",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    )

                    AnimatedVisibility(!isValidName) {
                        CheckboxWithLabel(
                            checked = confirmOverride,
                            onCheckedChange = { confirmOverride = it },
                            label = { Text("Override existing preset") }
                        )
                    }

                }
            },
            controlButtons = {
                DialogConfirmCancelButtons(
                    modifier = Modifier.padding(10.dp),
                    onConfirm = {
                        state.onPresetAdd(newPresetName, confirmOverride)
                        showNameDialog = false
                        newPresetName = ""
                    },
                    onCancel = {
                        showNameDialog = false
                        newPresetName = ""
                    },
                    confirmEnabled = newPresetName != "" && (isValidName || confirmOverride)
                )
            }

        )
    }
}
