package io.github.snd_r.komelia.ui.dialogs.book.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.snd_r.komelia.ui.LocalViewModelFactory
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
    LaunchedEffect(book) { vm.initialize() }

    TabDialog(
        title = "Edit ${book.metadata.title}",
        currentTab = vm.currentTab,
        tabs = vm.tabs(),
        confirmationText = "Save Changes",
        onConfirm = { coroutineScope.launch { vm.saveChanges() } },
        onTabChange = { vm.currentTab = it },
        onDismissRequest = onDismissRequest
    )
}