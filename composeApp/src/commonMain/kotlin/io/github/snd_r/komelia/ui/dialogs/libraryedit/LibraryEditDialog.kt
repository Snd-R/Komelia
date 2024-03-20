package io.github.snd_r.komelia.ui.dialogs.libraryedit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.dialogs.tabs.TabDialog
import io.github.snd_r.komga.library.KomgaLibrary

@Composable
fun LibraryAddDialog(
    onDismissRequest: () -> Unit
) {
    LibraryEditDialogs(null, onDismissRequest)
}

@Composable
fun LibraryEditDialogs(
    library: KomgaLibrary?,
    onDismissRequest: () -> Unit
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getLibraryEditDialogViewModel(library, onDismissRequest) }

    val title = if (library != null) "Edit Library" else "Add Library"

    TabDialog(
        title = title,
        currentTab = vm.currentTab,
        tabs = vm.tabs(),
        onTabChange = { vm.currentTab = it },
        onDismissRequest = onDismissRequest,
    )

}
