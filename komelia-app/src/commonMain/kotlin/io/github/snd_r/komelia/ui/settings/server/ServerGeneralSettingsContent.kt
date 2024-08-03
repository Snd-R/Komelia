package io.github.snd_r.komelia.ui.settings.server

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.CheckboxWithLabel
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.OptionsStateHolder
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.common.withTextFieldNavigation
import io.github.snd_r.komelia.ui.dialogs.ConfirmationDialog
import io.github.snd_r.komga.settings.KomgaThumbnailSize

@Composable
fun ServerSettingsContent(
    deleteEmptyCollections: StateHolder<Boolean>,
    deleteEmptyReadLists: StateHolder<Boolean>,
    taskPoolSize: StateHolder<Int?>,
    rememberMeDurationDays: StateHolder<Int?>,
    renewRememberMeKey: StateHolder<Boolean>,
    serverPort: StateHolder<Int?>,
    configServerPort: Int,
    serverContextPath: StateHolder<String?>,

    thumbnailSize: OptionsStateHolder<KomgaThumbnailSize>,
) {
    val strings = LocalStrings.current.settings

    Column(verticalArrangement = Arrangement.spacedBy(20.dp),) {

        Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(thumbnailSize.value, strings.forThumbnailSize(thumbnailSize.value)),
                options = thumbnailSize.options.map { LabeledEntry(it, strings.forThumbnailSize(it)) },
                onOptionChange = { thumbnailSize.onValueChange(it.value) },
                label = { Text(strings.thumbnailSize) }
            )
        }

        Column {
            CheckboxWithLabel(
                checked = deleteEmptyCollections.value,
                onCheckedChange = deleteEmptyCollections.setValue,
                label = { Text(strings.deleteEmptyCollections) },
            )

            CheckboxWithLabel(
                checked = deleteEmptyReadLists.value,
                onCheckedChange = deleteEmptyReadLists.setValue,
                label = { Text(strings.deleteEmptyReadLists) },
            )
        }

        TextField(
            value = taskPoolSize.value?.toString() ?: "",
            onValueChange = { newValue ->
                if (newValue.isBlank()) taskPoolSize.setValue(null)
                else newValue.toIntOrNull()?.let { taskPoolSize.setValue(it) }
            },
            label = { Text(strings.taskPoolSize) },
            supportingText = {
                if (taskPoolSize.errorMessage != null)
                    Text(text = taskPoolSize.errorMessage, color = MaterialTheme.colorScheme.error)
            },
            modifier = Modifier.fillMaxWidth().withTextFieldNavigation(),
        )

        Column {
            TextField(
                value = rememberMeDurationDays.value?.toString() ?: "",
                onValueChange = { newValue ->
                    if (newValue.isBlank()) rememberMeDurationDays.setValue(null)
                    else newValue.toIntOrNull()?.let { rememberMeDurationDays.setValue(it) }

                },
                label = { Text(strings.rememberMeDurationDays) },
                supportingText = {
                    if (rememberMeDurationDays.errorMessage != null)
                        Text(text = rememberMeDurationDays.errorMessage, color = MaterialTheme.colorScheme.error)
                    else Text(strings.requiresRestart)
                },
                modifier = Modifier.fillMaxWidth().withTextFieldNavigation(),
            )

            CheckboxWithLabel(
                checked = renewRememberMeKey.value,
                onCheckedChange = renewRememberMeKey.setValue,
                label = { Text(strings.renewRememberMeKey) },
            )
        }

        TextField(
            value = serverPort.value?.toString() ?: "",
            onValueChange = { newValue ->
                if (newValue.isBlank()) serverPort.setValue(null)
                else newValue.toIntOrNull()?.let { serverPort.setValue(it) }

            },
            placeholder = { Text(configServerPort.toString()) },
            label = { Text(strings.serverPort) },
            supportingText = { Text(strings.requiresRestart) },
            modifier = Modifier.fillMaxWidth().withTextFieldNavigation(),
        )

        TextField(
            value = serverContextPath.value ?: "",
            onValueChange = { serverContextPath.setValue(it) },
            label = { Text(strings.serverContextPath) },
            supportingText = { Text(strings.requiresRestart) },
            modifier = Modifier.fillMaxWidth().withTextFieldNavigation(),
        )

    }
}


@Composable
fun ChangesConfirmationButton(
    thumbnailSizeChanged: Boolean,
    onThumbnailRegenerate: (forBiggerResultOnly: Boolean) -> Unit,

    isChanged: Boolean,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
) {
    val strings = LocalStrings.current.settings

    var showThumbnailRegenerateDialog by remember { mutableStateOf(false) }
    if (showThumbnailRegenerateDialog) {
        ThumbRegenerationDialog(
            onThumbnailRegenerate = onThumbnailRegenerate,
            onDismiss = { showThumbnailRegenerateDialog = false }
        )
    }

    Row(
        modifier = Modifier.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = onDiscard,
            enabled = isChanged
        ) { Text(strings.serverSettingsDiscard) }
        Spacer(Modifier.width(20.dp))

        FilledTonalButton(
            onClick = {
                if (thumbnailSizeChanged) showThumbnailRegenerateDialog = true
                onSave()
            },
            shape = RoundedCornerShape(5.dp),
            enabled = isChanged
        ) {
            Text(strings.serverSettingsSave)
        }
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChangesConfirmationPopup(
    thumbnailSizeChanged: Boolean,
    onThumbnailRegenerate: (forBiggerResultOnly: Boolean) -> Unit,

    isChanged: Boolean,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
) {

    var showThumbnailRegenerateDialog by remember { mutableStateOf(false) }
    if (showThumbnailRegenerateDialog) {
        ThumbRegenerationDialog(
            onThumbnailRegenerate = onThumbnailRegenerate,
            onDismiss = { showThumbnailRegenerateDialog = false }
        )
    }

    if (isChanged) {
        Popup(
            alignment = Alignment.BottomCenter,
        ) {
            Surface(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier
                    .width(600.dp)
                    .padding(20.dp)
            ) {
                FlowRow(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("You have unsaved changes")
                    Spacer(Modifier.weight(1f))

                    TextButton(onClick = onDiscard) {
                        Text("Discard")
                    }
                    Spacer(Modifier.width(20.dp))

                    FilledTonalButton(
                        onClick = {
                            if (thumbnailSizeChanged) showThumbnailRegenerateDialog = true
                            onSave()
                        },
                        shape = RoundedCornerShape(5.dp),
                    ) {
                        Text("Save Changes")

                    }
                }
            }
        }
    }

}

@Composable
private fun ThumbRegenerationDialog(
    onThumbnailRegenerate: (forBiggerResultOnly: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current.settings
    ConfirmationDialog(
        title = strings.thumbnailRegenTitle,
        body = strings.thumbnailRegenBody,
        buttonConfirm = strings.thumbnailRegenIfBigger,
        buttonAlternate = strings.thumbnailRegenAllBooks,
        buttonCancel = strings.thumbnailRegenNo,
        onDialogConfirm = { onThumbnailRegenerate(true) },
        onDialogConfirmAlternate = { onThumbnailRegenerate(false) },
        onDialogDismiss = onDismiss
    )

}