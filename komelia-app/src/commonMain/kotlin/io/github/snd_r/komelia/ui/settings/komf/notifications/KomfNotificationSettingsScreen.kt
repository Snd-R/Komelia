package io.github.snd_r.komelia.ui.settings.komf.notifications

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.settings.SettingsScreenContainer

class KomfNotificationSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getKomfNotificationViewModel() }
        val vmState = vm.state.collectAsState().value
        val komfConfigLoadError = vm.komfConfig.errorFlow.collectAsState().value
        LaunchedEffect(Unit) { vm.initialize() }

        SettingsScreenContainer(title = "Discord Notifications Settings") {

            if (komfConfigLoadError != null) {
                Text("${komfConfigLoadError::class.simpleName}: ${komfConfigLoadError.message}")
                return@SettingsScreenContainer
            }

            when (vmState) {
                is LoadState.Error -> Text("${vmState.exception::class.simpleName}: ${vmState.exception.message}")
                LoadState.Loading, LoadState.Uninitialized -> LoadingMaxSizeIndicator()
                is LoadState.Success -> KomfSettingsContent(
                    discordUploadSeriesCover = StateHolder(vm.discordUploadSeriesCover, vm::onSeriesCoverChange),
                    discordWebhooks = vm.discordWebhooks,
                    onDiscordWebhookAdd = vm::onDiscordWebhookAdd,
                    onDiscordWebhookRemove = vm::onDiscordWebhookRemove,

                    titleTemplate = StateHolder(vm.titleTemplate, vm::titleTemplate::set),
                    titleUrlTemplate = StateHolder(vm.titleUrlTemplate, vm::onTitleUrlTemplateChange),
                    descriptionTemplate = StateHolder(vm.descriptionTemplate, vm::descriptionTemplate::set),
                    fieldTemplates = vm.fieldTemplates,
                    onFieldAdd = vm::onFieldAdd,
                    onFieldDelete = vm::onFieldDelete,
                    footerTemplate = StateHolder(vm.footerTemplate, vm::footerTemplate::set),

                    titlePreview = vm.titlePreview,
                    titleUrlPreview = vm.titleUrlPreview,
                    descriptionPreview = vm.descriptionPreview,
                    fieldPreviews = vm.fieldPreviews,
                    footerPreview = vm.footerPreview,

                    notificationContextState = vm.notificationContext,
                    onTemplateSave = vm::onTemplatesSave,
                    onTemplateSend = vm::onTemplatesSend,
                    onTemplateRender = vm::onTemplateRender,
                )
            }

        }
    }
}