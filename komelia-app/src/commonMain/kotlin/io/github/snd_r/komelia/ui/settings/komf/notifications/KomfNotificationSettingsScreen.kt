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
import io.github.snd_r.komelia.ui.settings.SettingsScreenContainer
import io.github.snd_r.komelia.ui.settings.komf.notifications.view.KomfSettingsContent

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
                is LoadState.Success -> KomfSettingsContent(vm.discordState, vm.appriseState)
            }

        }
    }
}