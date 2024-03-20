package io.github.snd_r.komelia.ui.settings.server.management

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.settings.server.ServerManagementContent

class ServerManagementScreen : Screen {
    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getServerManagementViewModel() }

        ServerManagementContent(
            onScanAllLibraries = vm::onScanAllLibraries,
            onEmptyTrash = vm::onEmptyTrashForAllLibraries,
            onCancelAllTasks = vm::onCancelAllTasks,
            onShutdown = vm::onShutDown
        )

    }
}