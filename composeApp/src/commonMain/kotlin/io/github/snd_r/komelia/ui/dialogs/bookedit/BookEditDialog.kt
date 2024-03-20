package io.github.snd_r.komelia.ui.dialogs.bookedit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogControlButtons
import io.github.snd_r.komelia.ui.dialogs.tabs.TabDialog
import io.github.snd_r.komga.book.KomgaBook
import kotlinx.coroutines.launch


@Composable
fun BookEditDialog(
    book: KomgaBook,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val coroutineScope = rememberCoroutineScope()
    val vm = remember { viewModelFactory.getBookEditDialogViewModel(book, onDismissRequest) }

    TabDialog(
        title = "Edit ${book.metadata.title}",
        dialogSize = DpSize(800.dp, Dp.Unspecified),
        currentTab = vm.currentTab,
        tabs = vm.tabs(),
        controlButtons = {
            DialogControlButtons(
                confirmationText = "Save Changes",
                onConfirmClick = {
                    coroutineScope.launch { vm.saveChanges() }
                },
                onDismissRequest = onDismissRequest
            )
        },
        onTabChange = { vm.currentTab = it },
        onDismissRequest = onDismissRequest
    )
}