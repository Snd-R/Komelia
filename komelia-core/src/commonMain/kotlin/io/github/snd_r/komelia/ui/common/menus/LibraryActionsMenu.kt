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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LocalKomfIntegration
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.dialogs.ConfirmationDialog
import io.github.snd_r.komelia.ui.dialogs.komf.reset.KomfResetMetadataDialog
import io.github.snd_r.komelia.ui.dialogs.libraryedit.LibraryEditDialogs
import kotlinx.coroutines.CoroutineScope
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryClient

@Composable
fun LibraryActionsMenu(
    library: KomgaLibrary,
    actions: LibraryMenuActions,
    expanded: Boolean,
    onDismissRequest: () -> Unit
) {
    var showLibraryEditDialog by remember { mutableStateOf(false) }
    if (showLibraryEditDialog) {
        LibraryEditDialogs(
            library = library,
            onDismissRequest = { showLibraryEditDialog = false }
        )
    }

    var showAnalyzeDialog by remember { mutableStateOf(false) }
    if (showAnalyzeDialog)
        ConfirmationDialog(
            title = "Analyze library",
            body = "Analyzes all the media files in the library. The analysis captures information about the media. Depending on your library size, this may take a long time.",
            onDialogConfirm = { actions.analyze(library) },
            onDialogDismiss = { showAnalyzeDialog = false }
        )

    var refreshMetadataDialog by remember { mutableStateOf(false) }
    if (refreshMetadataDialog)
        ConfirmationDialog(
            title = "Refresh metadata for library",
            body = "Refreshes metadata for all the media files in the library. Depending on your library size, this may take a long time.",
            onDialogConfirm = { actions.refresh(library) },
            onDialogDismiss = { refreshMetadataDialog = false }
        )

    var emptyTrashDialog by remember { mutableStateOf(false) }
    if (emptyTrashDialog)
        ConfirmationDialog(
            title = "Empty trash for library",
            body = """
                    By default the media server doesn't remove information for media right away.
                    This helps if a drive is temporarily disconnected. 
                    When you empty the trash for a library, all information about missing media is deleted.""".trimIndent(),
            onDialogConfirm = { actions.emptyTrash(library) },
            onDialogDismiss = { emptyTrashDialog = false }
        )

    var deleteLibraryDialog by remember { mutableStateOf(false) }
    if (deleteLibraryDialog)
        ConfirmationDialog(
            title = "Delete Library",
            body = "The library ${library.name} will be removed from this server. Your media files will not be affected. This cannot be undone. Continue?",
            confirmText = "Yes, delete the library \"${library.name}\"",
            onDialogConfirm = { actions.delete(library) },
            onDialogDismiss = { deleteLibraryDialog = false },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )

    var showKomfResetDialog by remember { mutableStateOf(false) }
    if (showKomfResetDialog) {
        KomfResetMetadataDialog(
            library = library,
            onDismissRequest = {
                showKomfResetDialog = false
                onDismissRequest()
            }
        )
    }

    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
        DropdownMenuItem(
            text = { Text("Scan library files") },
            onClick = {
                actions.scan(library)
                onDismissRequest()
            }
        )

        val deepScanInteractionSource = remember { MutableInteractionSource() }
        val deepScanIsHovered = deepScanInteractionSource.collectIsHoveredAsState()
        val deepScanColor =
            if (deepScanIsHovered.value) Modifier.background(MaterialTheme.colorScheme.tertiaryContainer)
            else Modifier

        DropdownMenuItem(
            text = { Text("Scan library files (deep)") },
            onClick = {
                actions.deepScan(library)
                onDismissRequest()
            },
            modifier = Modifier
                .hoverable(deepScanInteractionSource)
                .then(deepScanColor)
        )
        DropdownMenuItem(
            text = { Text("Analyze") },
            onClick = {
                showAnalyzeDialog = true
                onDismissRequest()
            }
        )
        DropdownMenuItem(
            text = { Text("Refresh metadata") },
            onClick = {
                refreshMetadataDialog = true
                onDismissRequest()
            }
        )
        DropdownMenuItem(
            text = { Text("Empty trash") },
            onClick = {
                emptyTrashDialog = true
                onDismissRequest()
            }
        )
        DropdownMenuItem(
            text = { Text("Edit") },
            onClick = {
                showLibraryEditDialog = true
                onDismissRequest()
            }
        )

        val komfIntegration = LocalKomfIntegration.current.collectAsState(false)
        if (komfIntegration.value) {
            val vmFactory = LocalViewModelFactory.current
            val autoIdentifyVm = remember(library) {
                vmFactory.getKomfLibraryIdentifyViewModel(library)
            }
            DropdownMenuItem(
                text = { Text("Auto-Identify (Komf)") },
                onClick = {
                    autoIdentifyVm.autoIdentify()
                    onDismissRequest()
                },
            )

            DropdownMenuItem(
                text = { Text("Reset Metadata (Komf)") },
                onClick = { showKomfResetDialog = true },
            )
        }

        val deleteScanInteractionSource = remember { MutableInteractionSource() }
        val deleteScanIsHovered = deleteScanInteractionSource.collectIsHoveredAsState()
        val deleteScanColor =
            if (deleteScanIsHovered.value) Modifier.background(MaterialTheme.colorScheme.errorContainer)
            else Modifier
        DropdownMenuItem(
            text = { Text("Delete") },
            onClick = {
                deleteLibraryDialog = true
                onDismissRequest()
            },
            modifier = Modifier
                .hoverable(deleteScanInteractionSource)
                .then(deleteScanColor)
        )
    }
}

data class LibraryMenuActions(
    val scan: (KomgaLibrary) -> Unit,
    val deepScan: (KomgaLibrary) -> Unit,
    val analyze: (KomgaLibrary) -> Unit,
    val refresh: (KomgaLibrary) -> Unit,
    val emptyTrash: (KomgaLibrary) -> Unit,
    val delete: (KomgaLibrary) -> Unit,
) {
    constructor(
        libraryClient: KomgaLibraryClient,
        notifications: AppNotifications,
        scope: CoroutineScope
    ) : this(
        scan = {
            notifications.runCatchingToNotifications(scope) {
                libraryClient.scan(it.id)
                notifications.add(AppNotification.Normal("Launched library scan"))
            }
        },
        deepScan = {
            notifications.runCatchingToNotifications(scope) {
                libraryClient.scan(it.id, true)
                notifications.add(AppNotification.Normal("Launched library deep scan"))
            }
        },
        analyze = {
            notifications.runCatchingToNotifications(scope) {
                libraryClient.analyze(it.id)
                notifications.add(AppNotification.Normal("Launched library analysis"))
            }
        },
        refresh = {
            notifications.runCatchingToNotifications(scope) {
                libraryClient.refreshMetadata(it.id)
                notifications.add(AppNotification.Normal("Launched library refresh"))
            }
        },
        emptyTrash = {
            notifications.runCatchingToNotifications(scope) {
                libraryClient.emptyTrash(it.id)
                notifications.add(AppNotification.Normal("Launched library trash task"))
            }
        },
        delete = {
            notifications.runCatchingToNotifications(scope) { libraryClient.deleteOne(it.id) }
        },
    )
}
