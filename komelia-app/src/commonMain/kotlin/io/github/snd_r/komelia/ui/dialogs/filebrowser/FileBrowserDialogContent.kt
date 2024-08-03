package io.github.snd_r.komelia.ui.dialogs.filebrowser

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import kotlinx.coroutines.launch
import snd.komga.client.filesystem.DirectoryListing


@Composable
fun FileBrowserDialogContent(
    onDismissRequest: () -> Unit,
    onDirectoryChoice: (String) -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val viewmodel = remember { viewModelFactory.getFileBrowserDialogViewModel() }
    LaunchedEffect(Unit) {
        viewmodel.selectDirectory("")
    }

    val coroutineScope = rememberCoroutineScope()
    val directoryListing = viewmodel.directoryListing
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                if (directoryListing == null) return@Column

                Text("Library's root folder", fontSize = 20.sp, modifier = Modifier.padding(vertical = 10.dp))

                TextField(
                    value = viewmodel.selectedPath,
                    readOnly = true,
                    onValueChange = {},
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                val scrollState = rememberScrollState()
                Box(
                    Modifier
                        .heightIn(min = 100.dp, max = 400.dp)
                ) {
                    DirectoryListing(
                        listing = directoryListing,
                        onDirectoryClick = { coroutineScope.launch { viewmodel.selectDirectory(it) } },
                        scrollState = scrollState
                    )

                    VerticalScrollbar(scrollState, Modifier.align(Alignment.CenterEnd))
                }

                DialogControlButtons(viewmodel.selectedPath, onDismissRequest, onDirectoryChoice)
            }
        }
    }
}

@Composable
private fun DirectoryListing(
    listing: DirectoryListing,
    onDirectoryClick: (String) -> Unit,
    scrollState: ScrollState
) {
    Column(
        Modifier
            .shadow(1.dp)
            .verticalScroll(scrollState)
    ) {
        val parent = listing.parent
        if (parent != null) {
            DirectoryListingItem(
                icon = Icons.Default.ChevronLeft,
                title = "Parent",
                onClick = { onDirectoryClick(parent) }
            )
        }

        listing.directories.forEach {
            if (it.type == "directory") {
                DirectoryListingItem(
                    icon = Icons.Default.Folder,
                    title = it.name,
                    onClick = { onDirectoryClick(it.path) })
            }
        }
    }
}

@Composable
private fun DirectoryListingItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Column(Modifier
        .clickable { onClick() }
        .cursorForHand()
        .fillMaxWidth()
    ) {
        Row(Modifier.padding(10.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.padding(horizontal = 10.dp))
            Text(title)
        }
        HorizontalDivider()
    }
}

@Composable
private fun DialogControlButtons(
    currentPath: String,
    onDismissRequest: () -> Unit,
    onDirectoryChoice: (String) -> Unit,
) {
    Row(modifier = Modifier.padding(top = 20.dp)) {

        Spacer(Modifier.weight(1f))
        TextButton(onClick = onDismissRequest) {
            Text("CANCEL")
        }
        Spacer(Modifier.size(10.dp))

        FilledTonalButton(onClick = {
            onDirectoryChoice(currentPath)
            onDismissRequest()
        }) {
            Text("CHOOSE")
        }
    }
}