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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import snd.komelia.AppNotification
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaLibraryApi
import snd.komelia.offline.tasks.OfflineTaskEmitter
import snd.komelia.ui.LocalKomfIntegration
import snd.komelia.ui.LocalKomgaState
import snd.komelia.ui.LocalOfflineMode
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.dialogs.ConfirmationDialog
import snd.komelia.ui.dialogs.komf.reset.KomfResetLibraryMetadataDialog
import snd.komelia.ui.dialogs.libraryedit.LibraryEditDialogs
import snd.komga.client.library.KomgaLibrary

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
    var deleteOfflineLibraryDialog by remember { mutableStateOf(false) }
    if (deleteOfflineLibraryDialog)
        ConfirmationDialog(
            title = "Delete downloaded Library",
            body = "The library ${library.name} will be removed from this device only.",
            onDialogConfirm = { actions.deleteOffline(library) },
            onDialogDismiss = { deleteOfflineLibraryDialog = false },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )

    var showKomfResetDialog by remember { mutableStateOf(false) }
    if (showKomfResetDialog) {
        KomfResetLibraryMetadataDialog(
            library = library,
            onDismissRequest = {
                showKomfResetDialog = false
                onDismissRequest()
            }
        )
    }

    val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true
    val isOffline = LocalOfflineMode.current.collectAsState().value
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
        if (isAdmin && !isOffline) {
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
        }

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

        if (!isOffline && isAdmin) {
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
        if (isOffline) {
            DropdownMenuItem(
                text = { Text("Delete downloaded") },
                onClick = {
                    deleteOfflineLibraryDialog = true
                    onDismissRequest()
                },
                modifier = Modifier
                    .hoverable(deleteScanInteractionSource)
                    .then(deleteScanColor)
            )

        }
    }
}

data class LibraryMenuActions(
    val scan: (KomgaLibrary) -> Unit,
    val deepScan: (KomgaLibrary) -> Unit,
    val analyze: (KomgaLibrary) -> Unit,
    val refresh: (KomgaLibrary) -> Unit,
    val emptyTrash: (KomgaLibrary) -> Unit,
    val delete: (KomgaLibrary) -> Unit,
    val deleteOffline: (KomgaLibrary) -> Unit
) {
    constructor(
        libraryApi: KomgaLibraryApi,
        notifications: AppNotifications,
        taskEmitter: OfflineTaskEmitter,
        scope: CoroutineScope
    ) : this(
        scan = {
            notifications.runCatchingToNotifications(scope) {
                libraryApi.scan(it.id)
                notifications.add(AppNotification.Normal("Launched library scan"))
            }
        },
        deepScan = {
            notifications.runCatchingToNotifications(scope) {
                libraryApi.scan(it.id, true)
                notifications.add(AppNotification.Normal("Launched library deep scan"))
            }
        },
        analyze = {
            notifications.runCatchingToNotifications(scope) {
                libraryApi.analyze(it.id)
                notifications.add(AppNotification.Normal("Launched library analysis"))
            }
        },
        refresh = {
            notifications.runCatchingToNotifications(scope) {
                libraryApi.refreshMetadata(it.id)
                notifications.add(AppNotification.Normal("Launched library refresh"))
            }
        },
        emptyTrash = {
            notifications.runCatchingToNotifications(scope) {
                libraryApi.emptyTrash(it.id)
                notifications.add(AppNotification.Normal("Launched library trash task"))
            }
        },
        delete = {
            notifications.runCatchingToNotifications(scope) { libraryApi.deleteOne(it.id) }
        },
        deleteOffline = {
            scope.launch { taskEmitter.deleteLibrary(it.id) }
        }
    )
}
