package io.github.snd_r.komelia.ui.settings.server

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.common.OptionsStateHolder
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.settings.SettingsScreenContainer
import io.github.snd_r.komga.settings.KomgaThumbnailSize
import kotlinx.coroutines.Dispatchers

class ServerSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getServerSettingsViewModel() }

        val deleteEmptyCollections = vm.deleteEmptyCollections.collectAsState()
        val deleteEmptyReadLists = vm.deleteEmptyReadLists.collectAsState()
        val rememberMeDurationDays = vm.rememberMeDurationDays.collectAsState(Dispatchers.Main.immediate)
        val rememberMeDurationDaysValidationMessage = vm.rememberMeDurationDaysValidationMessage.collectAsState()
        val renewRememberMeKey = vm.renewRememberMeKey.collectAsState(Dispatchers.Main.immediate)
        val taskPoolSize = vm.taskPoolSize.collectAsState(Dispatchers.Main.immediate)
        val taskPoolSizeValidationMessage = vm.taskPoolSizeValidationMessage.collectAsState()
        val serverPort = vm.serverPort.collectAsState(Dispatchers.Main.immediate)
        val configServerPort = vm.configServerPort.collectAsState(Dispatchers.Main.immediate)
        val serverContextPath = vm.serverContextPath.collectAsState(Dispatchers.Main.immediate)
        val thumbnailSize = vm.thumbnailSize.collectAsState()
        val isChanged = vm.isChanged.collectAsState()
        val currentSettings = vm.currentSettings.collectAsState().value

        if (currentSettings == null) {
            LoadingMaxSizeIndicator()
            return
        }

        val strings = LocalStrings.current.settings
        SettingsScreenContainer(strings.serverSettings) {
            ServerSettingsContent(
                deleteEmptyCollections = StateHolder(
                    deleteEmptyCollections.value,
                    vm::onDeleteEmptyCollectionsChange
                ),
                deleteEmptyReadLists = StateHolder(
                    deleteEmptyReadLists.value,
                    vm::onDeleteEmptyReadListsChange
                ),
                taskPoolSize = StateHolder(
                    value = taskPoolSize.value,
                    setValue = vm::onTaskPoolSizeChange,
                    errorMessage = taskPoolSizeValidationMessage.value
                ),

                rememberMeDurationDays = StateHolder(
                    value = rememberMeDurationDays.value,
                    setValue = vm::onRememberMeDurationDaysChange,
                    errorMessage = rememberMeDurationDaysValidationMessage.value
                ),
                renewRememberMeKey = StateHolder(
                    renewRememberMeKey.value,
                    vm::onRenewRememberMeKeyChange
                ),
                serverPort = StateHolder(
                    serverPort.value,
                    vm::onServerPortChange
                ),
                configServerPort = configServerPort.value ?: 25600,
                serverContextPath = StateHolder(
                    serverContextPath.value,
                    vm::onServerContextPathChange
                ),

                thumbnailSize = OptionsStateHolder(
                    value = thumbnailSize.value,
                    options = KomgaThumbnailSize.entries,
                    onValueChange = vm::onThumbnailSizeChange
                ),
            )

            ChangesConfirmationButton(
                thumbnailSizeChanged = currentSettings.thumbnailSize != thumbnailSize.value,
                onThumbnailRegenerate = vm::regenerateThumbnails,

                isChanged = isChanged.value,
                onSave = vm::updateSettings,
                onDiscard = vm::resetChanges,
            )


            HorizontalDivider(Modifier.padding(top = 40.dp, bottom = 20.dp))

            ServerManagementContent(
                onScanAllLibraries = vm::onScanAllLibraries,
                onEmptyTrash = vm::onEmptyTrashForAllLibraries,
                onCancelAllTasks = vm::onCancelAllTasks,
                onShutdown = vm::onShutDown
            )

            Spacer(Modifier.height(100.dp))

        }
    }
}
