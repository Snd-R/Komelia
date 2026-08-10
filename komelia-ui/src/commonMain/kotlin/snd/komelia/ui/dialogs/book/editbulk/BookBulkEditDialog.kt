package snd.komelia.ui.dialogs.book.editbulk

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.book_edit_bulk_dialog_title
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.dialog_save
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
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
        title = pluralStringResource(
            Res.plurals.book_edit_bulk_dialog_title,
            books.size,
            if (books.size == 1) books.first().metadata.title else books.size
        ),
        currentTab = vm.currentTab,
        tabs = vm.tabs(),
        confirmationText = stringResource(Res.string.dialog_save),
        onConfirm = { coroutineScope.launch { vm.saveChanges() } },
        onTabChange = { vm.currentTab = it },
        onDismissRequest = onDismissRequest
    )
}