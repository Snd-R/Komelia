package io.github.snd_r.komelia.ui.common.menus

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
import io.github.snd_r.komelia.ui.dialogs.ConfirmationDialog
import io.github.snd_r.komga.collection.KomgaCollection

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
            title = "Delete Collection",
            body = "Collection ${collection.name} will be removed from this server. Your media files will not be affected. This cannot be undone. Continue?",
            confirmText = "Yes, delete collection \"${collection.name}\"",
            onDialogConfirm = {
                onCollectionDelete()
                onDismissRequest()

            },
            onDialogDismiss = {
                showDeleteDialog = false
                onDismissRequest()
            },
            buttonConfirmColor = MaterialTheme.colorScheme.error
        )
    }

    val showDropdown = derivedStateOf { expanded && !showDeleteDialog }
    DropdownMenu(
        expanded = showDropdown.value,
        onDismissRequest = onDismissRequest
    ) {
        val deleteInteractionSource = remember { MutableInteractionSource() }
        val deleteIsHovered = deleteInteractionSource.collectIsHoveredAsState()
        DropdownMenuItem(
            text = { Text("Delete") },
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