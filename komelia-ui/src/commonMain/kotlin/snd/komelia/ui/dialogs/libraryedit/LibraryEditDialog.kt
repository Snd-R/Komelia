package snd.komelia.ui.dialogs.libraryedit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_add
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_dialog_next
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.library_edit_library
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.dialogs.tabs.TabDialog
import snd.komga.client.library.KomgaLibrary

@Composable
fun LibraryEditDialogs(
    library: KomgaLibrary?,
    onDismissRequest: () -> Unit
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getLibraryEditDialogViewModel(library, onDismissRequest) }
    val coroutineScope = rememberCoroutineScope()

    val title =
        if (library != null) stringResource(Res.string.library_edit_library)
        else stringResource(Res.string.library_add)

    val libraryEditString = stringResource(Res.string.library_edit)
    val libraryAddString = stringResource(Res.string.library_add)
    val libraryNextString = stringResource(Res.string.library_edit_dialog_next)
    val confirmationText = remember(library, vm.currentTab) {
        when {
            library != null -> libraryEditString
            vm.currentTab is MetadataTab -> libraryAddString
            else -> libraryNextString
        }
    }

    TabDialog(
        title = title,
        currentTab = vm.currentTab,
        tabs = vm.tabs(),
        onTabChange = { vm.currentTab = it },
        onConfirm = { coroutineScope.launch { vm.onNextTabSwitch() } },
        confirmationText = confirmationText,
        onDismissRequest = onDismissRequest,
    )

}
