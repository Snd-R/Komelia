package snd.komelia.ui.dialogs.readlistedit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.dialogs.tabs.TabDialog
import snd.komga.client.readlist.KomgaReadList

@Composable
fun ReadListEditDialog(
    readList: KomgaReadList,
    onDismissRequest: () -> Unit
) {

    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getReadListEditDialogViewModel(readList, onDismissRequest) }
    LaunchedEffect(readList) { vm.initialize() }

    val coroutineScope = rememberCoroutineScope()
    TabDialog(
        title = "Edit ${readList.name}",
        currentTab = vm.currentTab,
        tabs = vm.tabs(),
        confirmationText = "Save Changes",
        confirmEnabled = vm.canSave(),
        onConfirm = { coroutineScope.launch { vm.saveChanges() } },
        onTabChange = { vm.currentTab = it },
        onDismissRequest = { onDismissRequest() }
    )
}