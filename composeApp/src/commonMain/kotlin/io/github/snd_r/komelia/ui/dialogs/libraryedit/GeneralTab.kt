package io.github.snd_r.komelia.ui.dialogs.libraryedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.dialogs.filebrowser.FileBrowserDialogContent
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem

class GeneralTab(
    private val vm: LibraryEditDialogViewModel,
) : DialogTab {
    override fun options() = TabItem(
        title = "GENERAL",
        icon = Icons.Default.Category
    )

    @Composable
    override fun Content() {
        GeneralTabContent(
            name = StateHolder(vm.libraryName.value, vm::setLibraryName, vm.libraryNameError),
            rootFolder = StateHolder(vm.rootFolder.value, vm::setRootFolder, vm.rootFolderError),
        )
    }
}

@Composable
private fun GeneralTabContent(
    name: StateHolder<String>,
    rootFolder: StateHolder<String>,
) {
    var showFileBrowserDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TextField(
            value = name.value,
            onValueChange = name.setValue,
            label = { Text("Name") },
            isError = name.errorMessage != null,
            supportingText = { if (name.errorMessage != null) Text(name.errorMessage) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = rootFolder.value,
                onValueChange = rootFolder.setValue,
                label = { Text("Root folder") },
                isError = rootFolder.errorMessage != null,
                supportingText = { if (rootFolder.errorMessage != null) Text(rootFolder.errorMessage) },
                modifier = Modifier.weight(7f)
            )

            ElevatedButton(
                onClick = { showFileBrowserDialog = true },
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                Text("Browse")
            }
        }
    }

    if (showFileBrowserDialog) {
        FileBrowserDialogContent(
            onDismissRequest = { showFileBrowserDialog = false },
            onDirectoryChoice = rootFolder.setValue
        )
    }

}