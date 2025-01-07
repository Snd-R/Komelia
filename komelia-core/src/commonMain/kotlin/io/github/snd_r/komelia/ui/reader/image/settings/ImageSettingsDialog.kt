package io.github.snd_r.komelia.ui.reader.image.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.dialogs.tabs.TabDialog

@Composable
fun ImageSettingsDialog(
//    currentImage: KomeliaImage,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getImageSettingsDialogViewModel() }
    LaunchedEffect(Unit) { vm.initialize() }
    DisposableEffect(Unit) { onDispose(vm::onDispose) }

    TabDialog(
        title = "Image Settings",
        currentTab = vm.currentTab.collectAsState().value,
        tabs = vm.tabs,
        confirmationText = "Confirm",
        onConfirm = {},
        onTabChange = vm::onTabChange,
        onDismissRequest = onDismissRequest,
//        modifier = Modifier.widthIn(max = 800.dp)
    )
}