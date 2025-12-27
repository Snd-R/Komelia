package snd.komelia.ui.dialogs.book.editbulk

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.dialogs.tabs.TabDialog


@Composable
fun BookBulkEditDialog(
    books: List<KomeliaBook>,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val coroutineScope = rememberCoroutineScope()
    val vm = remember { viewModelFactory.getBookBulkEditDialogViewModel(books, onDismissRequest) }
    LaunchedEffect(books) { vm.initialize() }

    TabDialog(
        title = "Edit ${books.size} books",
        currentTab = vm.currentTab,
        tabs = vm.tabs(),
        confirmationText = "Save Changes",
        onConfirm = { coroutineScope.launch { vm.saveChanges() } },
        onTabChange = { vm.currentTab = it },
        onDismissRequest = onDismissRequest
    )
}