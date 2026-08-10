package snd.komelia.ui.settings.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_context_path
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_delete_empty_collections
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_delete_empty_read_lists
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_discard
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_port
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_remember_me_duration
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_remember_me_key
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_requires_restart
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_save
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_task_pool_size
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_thumbnail_regen_all_books
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_thumbnail_regen_body
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_thumbnail_regen_if_bigger
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_thumbnail_regen_no
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_thumbnail_regen_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_server_thumbnail_size
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.OptionsStateHolder
import snd.komelia.ui.StateHolder
import snd.komelia.ui.common.components.CheckboxWithLabel
import snd.komelia.ui.common.components.DropdownChoiceMenu
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.common.components.withTextFieldNavigation
import snd.komelia.ui.dialogs.ConfirmationDialog
import snd.komelia.ui.strings.AppStrings
import snd.komelia.ui.strings.stringLabels
import snd.komga.client.settings.KomgaThumbnailSize

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
    thumbnailSizeChanged: Boolean,
    onThumbnailRegenerate: (forBiggerResultOnly: Boolean) -> Unit,
    generalSettingsChanged: Boolean,
    onGeneralSettingsSave: () -> Unit,
    onGeneralSettingsDiscard: () -> Unit,

    onScanAllLibraries: (deep: Boolean) -> Unit,
    onEmptyTrash: () -> Unit,
    onCancelAllTasks: () -> Unit,
    onShutdown: () -> Unit
) {
    GeneralSettingsContent(
        deleteEmptyCollections = deleteEmptyCollections,
        deleteEmptyReadLists = deleteEmptyReadLists,
        taskPoolSize = taskPoolSize,

        rememberMeDurationDays = rememberMeDurationDays,
        renewRememberMeKey = renewRememberMeKey,
        serverPort = serverPort,
        configServerPort = configServerPort,
        serverContextPath = serverContextPath,
        thumbnailSize = thumbnailSize,
    )

    ChangesConfirmationButton(
        thumbnailSizeChanged = thumbnailSizeChanged,
        onThumbnailRegenerate = onThumbnailRegenerate,

        isChanged = generalSettingsChanged,
        onSave = onGeneralSettingsSave,
        onDiscard = onGeneralSettingsDiscard,
    )


    ServerManagementContent(
        onScanAllLibraries = onScanAllLibraries,
        onEmptyTrash = onEmptyTrash,
        onCancelAllTasks = onCancelAllTasks,
        onShutdown = onShutdown
    )

    Spacer(Modifier.height(100.dp))
}

@Composable
fun GeneralSettingsContent(
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

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

        Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(
                    thumbnailSize.value,
                    stringResource(AppStrings.forThumbnailSize(thumbnailSize.value))
                ),
                options = stringLabels(thumbnailSize.options) { AppStrings.forThumbnailSize(it) },
                onOptionChange = { thumbnailSize.onValueChange(it.value) },
                label = { Text(stringResource(Res.string.settings_server_thumbnail_size)) }
            )
        }

        Column {
            CheckboxWithLabel(
                checked = deleteEmptyCollections.value,
                onCheckedChange = deleteEmptyCollections.setValue,
                label = { Text(stringResource(Res.string.settings_server_delete_empty_collections)) },
            )

            CheckboxWithLabel(
                checked = deleteEmptyReadLists.value,
                onCheckedChange = deleteEmptyReadLists.setValue,
                label = { Text(stringResource(Res.string.settings_server_delete_empty_read_lists)) },
            )
        }

        TextField(
            value = taskPoolSize.value?.toString() ?: "",
            onValueChange = { newValue ->
                if (newValue.isBlank()) taskPoolSize.setValue(null)
                else newValue.toIntOrNull()?.let { taskPoolSize.setValue(it) }
            },
            label = { Text(stringResource(Res.string.settings_server_task_pool_size)) },
            supportingText = {
                val error = taskPoolSize.errorMessage
                if (error != null)
                    Text(text = error, color = MaterialTheme.colorScheme.error)
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
                label = { Text(stringResource(Res.string.settings_server_remember_me_duration)) },
                supportingText = {
                    val error = rememberMeDurationDays.errorMessage
                    if (error != null)
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    else Text(stringResource(Res.string.settings_server_requires_restart))
                },
                modifier = Modifier.fillMaxWidth().withTextFieldNavigation(),
            )

            CheckboxWithLabel(
                checked = renewRememberMeKey.value,
                onCheckedChange = renewRememberMeKey.setValue,
                label = { Text(stringResource(Res.string.settings_server_remember_me_key)) },
            )
        }

        TextField(
            value = serverPort.value?.toString() ?: "",
            onValueChange = { newValue ->
                if (newValue.isBlank()) serverPort.setValue(null)
                else newValue.toIntOrNull()?.let { serverPort.setValue(it) }

            },
            placeholder = { Text(configServerPort.toString()) },
            label = { Text(stringResource(Res.string.settings_server_port)) },
            supportingText = { Text(stringResource(Res.string.settings_server_requires_restart)) },
            modifier = Modifier.fillMaxWidth().withTextFieldNavigation(),
        )

        TextField(
            value = serverContextPath.value ?: "",
            onValueChange = { serverContextPath.setValue(it) },
            label = { Text(stringResource(Res.string.settings_server_context_path)) },
            supportingText = { Text(stringResource(Res.string.settings_server_requires_restart)) },
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

        ElevatedButton(
            onClick = onDiscard,
            enabled = isChanged,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            Text(stringResource(Res.string.settings_server_discard))
        }
        Spacer(Modifier.width(20.dp))

        FilledTonalButton(
            onClick = {
                if (thumbnailSizeChanged) showThumbnailRegenerateDialog = true
                onSave()
            },
            enabled = isChanged,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            Text(stringResource(Res.string.settings_server_save))
        }
    }

}

@Composable
private fun ThumbRegenerationDialog(
    onThumbnailRegenerate: (forBiggerResultOnly: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        title = stringResource(Res.string.settings_server_thumbnail_regen_title),
        body = stringResource(Res.string.settings_server_thumbnail_regen_body),
        buttonConfirm = stringResource(Res.string.settings_server_thumbnail_regen_if_bigger),
        buttonAlternate = stringResource(Res.string.settings_server_thumbnail_regen_all_books),
        buttonCancel = stringResource(Res.string.settings_server_thumbnail_regen_no),
        onDialogConfirm = { onThumbnailRegenerate(true) },
        onDialogConfirmAlternate = { onThumbnailRegenerate(false) },
        onDialogDismiss = onDismiss
    )

}
