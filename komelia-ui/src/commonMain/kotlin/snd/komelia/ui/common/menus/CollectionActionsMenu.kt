package snd.komelia.ui.common.menus

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.collection_delete
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.collection_delete_confirm_body
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.collection_delete_confirm_confirm
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.collection_delete_confirm_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.collection_edit
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.dialogs.ConfirmationDialog
import snd.komelia.ui.dialogs.collectionedit.CollectionEditDialog
import snd.komga.client.collection.KomgaCollection

@Composable
fun CollectionActionsMenu(
    collection: KomgaCollection,
    onCollectionDelete: () -> Unit,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        ConfirmationDialog(
            title = stringResource(Res.string.collection_delete_confirm_title),
            body = stringResource(Res.string.collection_delete_confirm_body, collection.name),
            confirmText = stringResource(Res.string.collection_delete_confirm_confirm, collection.name),
            onDialogConfirm = {
                onCollectionDelete()
                onDismissRequest()

            },
            onDialogDismiss = {
                showDeleteDialog = false
                onDismissRequest()
            },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    }

    var showEditDialog by remember { mutableStateOf(false) }
    if (showEditDialog) {
        CollectionEditDialog(collection = collection, onDismissRequest = {
            showEditDialog = false
            onDismissRequest()
        })
    }

    val showDropdown = derivedStateOf { expanded && !showDeleteDialog && !showEditDialog }
    DropdownMenu(
        expanded = showDropdown.value,
        onDismissRequest = onDismissRequest
    ) {
        val deleteInteractionSource = remember { MutableInteractionSource() }
        val deleteIsHovered = deleteInteractionSource.collectIsHoveredAsState()
        DropdownMenuItem(
            text = { Text(stringResource(Res.string.collection_edit)) },
            onClick = { showEditDialog = true },
        )

        DropdownMenuItem(
            text = { Text(stringResource(Res.string.collection_delete)) },
            onClick = { showDeleteDialog = true },
            modifier = Modifier
                .hoverable(deleteInteractionSource)
                .then(
                    if (deleteIsHovered.value) Modifier.background(MaterialTheme.colorScheme.errorContainer)
                    else Modifier
                )
        )

    }
}