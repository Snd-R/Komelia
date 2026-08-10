package snd.komelia.ui.dialogs.book.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.dialog_edit
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.dialog_save
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.dialogs.oneshot.OneshotEditDialog
import snd.komelia.ui.dialogs.tabs.TabDialog


@Composable
fun BookEditDialog(
    book: KomeliaBook,
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
            title = stringResource(Res.string.dialog_edit, book.metadata.title),
            currentTab = vm.currentTab,
            tabs = vm.tabs,
            confirmationText = stringResource(Res.string.dialog_save),
            onConfirm = { coroutineScope.launch { vm.saveChanges() } },
            onTabChange = { vm.currentTab = it },
            onDismissRequest = onDismissRequest
        )
    }
}