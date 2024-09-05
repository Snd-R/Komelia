package io.github.snd_r.komelia.ui.dialogs.book.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.dialogs.oneshot.OneshotEditDialog
import io.github.snd_r.komelia.ui.dialogs.tabs.TabDialog
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBook


@Composable
fun BookEditDialog(
    book: KomgaBook,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val coroutineScope = rememberCoroutineScope()
    val vm = remember { viewModelFactory.getBookEditDialogViewModel(book, onDismissRequest) }
    LaunchedEffect(book) { vm.initialize() }

    if (book.oneshot) {
        OneshotEditDialog(book.seriesId, null, book, onDismissRequest)
    } else {
        TabDialog(
            title = "Edit ${book.metadata.title}",
            currentTab = vm.currentTab,
            tabs = vm.tabs,
            confirmationText = "Save",
            onConfirm = { coroutineScope.launch { vm.saveChanges() } },
            onTabChange = { vm.currentTab = it },
            onDismissRequest = onDismissRequest
        )
    }
}