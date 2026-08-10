package snd.komelia.ui.dialogs.collectionedit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.collection_edit_dialog_save
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.collection_edit_dialog_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.dialogs.tabs.TabDialog
import snd.komga.client.collection.KomgaCollection

@Composable
fun CollectionEditDialog(
    collection: KomgaCollection,
    onDismissRequest: () -> Unit
) {

    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getCollectionEditDialogViewModel(collection, onDismissRequest) }
    LaunchedEffect(collection) { vm.initialize() }

    val coroutineScope = rememberCoroutineScope()
    TabDialog(
        title = stringResource(Res.string.collection_edit_dialog_title, collection.name),
        currentTab = vm.currentTab,
        tabs = vm.tabs(),
        confirmationText = stringResource(Res.string.collection_edit_dialog_save),
        confirmEnabled = vm.canSave(),
        onConfirm = { coroutineScope.launch { vm.saveChanges() } },
        onTabChange = { vm.currentTab = it },
        onDismissRequest = { onDismissRequest() }
    )
}