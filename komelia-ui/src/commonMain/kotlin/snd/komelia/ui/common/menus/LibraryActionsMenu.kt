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
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_analyze
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_analyze_confirm_body
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_analyze_confirm_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_auto_identify
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_delete
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_delete_confirm_body
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_delete_confirm_confirm
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_delete_confirm_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_delete_downloaded
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_delete_downloaded_confirm_body
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_delete_downloaded_confirm_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_empty_trash
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_empty_trash_confirm_body
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_empty_trash_confirm_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_refresh_metadata
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_reresh_metada_confirm_body
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_reresh_metada_confirm_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_reset_metadata
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_scan_files
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_scan_files_deep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
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
            title = stringResource(Res.string.library_analyze_confirm_title),
            body = stringResource(Res.string.library_analyze_confirm_body),
            onDialogConfirm = { actions.analyze(library) },
            onDialogDismiss = { showAnalyzeDialog = false }
        )

    var refreshMetadataDialog by remember { mutableStateOf(false) }
    if (refreshMetadataDialog)
        ConfirmationDialog(
            title = stringResource(Res.string.library_reresh_metada_confirm_title),
            body = stringResource(Res.string.library_reresh_metada_confirm_body),
            onDialogConfirm = { actions.refresh(library) },
            onDialogDismiss = { refreshMetadataDialog = false }
        )

    var emptyTrashDialog by remember { mutableStateOf(false) }
    if (emptyTrashDialog)
        ConfirmationDialog(
            title = stringResource(Res.string.library_empty_trash_confirm_title),
            body = stringResource(Res.string.library_empty_trash_confirm_body),
            onDialogConfirm = { actions.emptyTrash(library) },
            onDialogDismiss = { emptyTrashDialog = false }
        )

    var deleteLibraryDialog by remember { mutableStateOf(false) }
    if (deleteLibraryDialog)
        ConfirmationDialog(
            title = stringResource(Res.string.library_delete_confirm_title),
            body = stringResource(Res.string.library_delete_confirm_body, library.name),
            confirmText = stringResource(Res.string.library_delete_confirm_confirm, library.name),
            onDialogConfirm = { actions.delete(library) },
            onDialogDismiss = { deleteLibraryDialog = false },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    var deleteOfflineLibraryDialog by remember { mutableStateOf(false) }
    if (deleteOfflineLibraryDialog)
        ConfirmationDialog(
            title = stringResource(Res.string.library_delete_downloaded_confirm_title),
            body = stringResource(Res.string.library_delete_downloaded_confirm_body, library.name),
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
                text = { Text(stringResource(Res.string.library_scan_files)) },
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
                text = { Text(stringResource(Res.string.library_scan_files_deep)) },
                onClick = {
                    actions.deepScan(library)
                    onDismissRequest()
                },
                modifier = Modifier
                    .hoverable(deepScanInteractionSource)
                    .then(deepScanColor)
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.library_analyze)) },
                onClick = {
                    showAnalyzeDialog = true
                    onDismissRequest()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.library_refresh_metadata)) },
                onClick = {
                    refreshMetadataDialog = true
                    onDismissRequest()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.library_empty_trash)) },
                onClick = {
                    emptyTrashDialog = true
                    onDismissRequest()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.library_edit)) },
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
                text = { Text(stringResource(Res.string.library_auto_identify)) },
                onClick = {
                    autoIdentifyVm.autoIdentify()
                    onDismissRequest()
                },
            )

            DropdownMenuItem(
                text = { Text(stringResource(Res.string.library_reset_metadata)) },
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
                text = { Text(stringResource(Res.string.library_delete)) },
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
                text = { Text(stringResource(Res.string.library_delete_downloaded)) },
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
