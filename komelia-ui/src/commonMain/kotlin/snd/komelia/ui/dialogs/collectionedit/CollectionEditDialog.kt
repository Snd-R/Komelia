package snd.komelia.ui.dialogs.collectionedit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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
        title = "Edit ${collection.name}",
        currentTab = vm.currentTab,
        tabs = vm.tabs(),
        confirmationText = "Save Changes",
        confirmEnabled = vm.canSave(),
        onConfirm = { coroutineScope.launch { vm.saveChanges() } },
        onTabChange = { vm.currentTab = it },
        onDismissRequest = { onDismissRequest() }
    )
}