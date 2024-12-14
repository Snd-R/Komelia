package io.github.snd_r.komelia.ui.settings.server

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.common.OptionsStateHolder
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.settings.SettingsScreenContainer
import kotlinx.coroutines.Dispatchers
import snd.komga.client.settings.KomgaThumbnailSize

class ServerSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getServerSettingsViewModel() }
        LaunchedEffect(Unit) { vm.initialize() }
        val state = vm.state.collectAsState().value

        val strings = LocalStrings.current.settings
        SettingsScreenContainer(strings.serverSettings) {
            when (state) {
                is LoadState.Error -> Text("Error ${state.exception.message}")
                LoadState.Loading, LoadState.Uninitialized -> LoadingMaxSizeIndicator()
                is LoadState.Success -> {
                    val thumbnailSize = vm.thumbnailSize.collectAsState()
                    val currentSettings = vm.currentSettings.collectAsState().value

                    ServerSettingsContent(
                        deleteEmptyCollections = StateHolder(
                            vm.deleteEmptyCollections.collectAsState().value,
                            vm::onDeleteEmptyCollectionsChange
                        ),
                        deleteEmptyReadLists = StateHolder(
                            vm.deleteEmptyReadLists.collectAsState().value,
                            vm::onDeleteEmptyReadListsChange
                        ),
                        taskPoolSize = StateHolder(
                            value = vm.taskPoolSize.collectAsState(Dispatchers.Main.immediate).value,
                            setValue = vm::onTaskPoolSizeChange,
                            errorMessage = vm.taskPoolSizeValidationMessage.collectAsState().value
                        ),

                        rememberMeDurationDays = StateHolder(
                            value = vm.rememberMeDurationDays.collectAsState(Dispatchers.Main.immediate).value,
                            setValue = vm::onRememberMeDurationDaysChange,
                            errorMessage = vm.rememberMeDurationDaysValidationMessage.collectAsState().value
                        ),
                        renewRememberMeKey = StateHolder(
                            vm.renewRememberMeKey.collectAsState(Dispatchers.Main.immediate).value,
                            vm::onRenewRememberMeKeyChange
                        ),
                        serverPort = StateHolder(
                            vm.serverPort.collectAsState(Dispatchers.Main.immediate).value,
                            vm::onServerPortChange
                        ),
                        configServerPort = vm.configServerPort.collectAsState(Dispatchers.Main.immediate).value
                            ?: 25600,
                        serverContextPath = StateHolder(
                            vm.serverContextPath.collectAsState(Dispatchers.Main.immediate).value,
                            vm::onServerContextPathChange
                        ),

                        thumbnailSize = OptionsStateHolder(
                            value = thumbnailSize.value,
                            options = KomgaThumbnailSize.entries,
                            onValueChange = vm::onThumbnailSizeChange
                        ),
                        thumbnailSizeChanged = currentSettings.thumbnailSize != thumbnailSize.value,
                        onThumbnailRegenerate = vm::regenerateThumbnails,
                        generalSettingsChanged = vm.isChanged.collectAsState().value,
                        onGeneralSettingsSave = vm::updateSettings,
                        onGeneralSettingsDiscard = vm::resetChanges,
                        onScanAllLibraries = vm::onScanAllLibraries,
                        onEmptyTrash = vm::onEmptyTrashForAllLibraries,
                        onCancelAllTasks = vm::onCancelAllTasks,
                        onShutdown = vm::onShutDown,
                    )
                }
            }

        }
    }
}
