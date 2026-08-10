package snd.komelia.ui.settings.komf.notifications

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.komf_settings_notifications_title
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.LoadState
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.error.formatExceptionMessage
import snd.komelia.ui.settings.SettingsScreenContainer
import snd.komelia.ui.settings.komf.notifications.view.KomfSettingsContent

class KomfNotificationSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getKomfNotificationViewModel() }
        val vmState = vm.state.collectAsState().value
        val komfConfigLoadError = vm.komfConfig.configError.collectAsState().value
        LaunchedEffect(Unit) { vm.initialize() }

        SettingsScreenContainer(title = stringResource(Res.string.komf_settings_notifications_title)) {

            if (komfConfigLoadError != null) {
                Text(formatExceptionMessage(komfConfigLoadError))
                return@SettingsScreenContainer
            }

            when (vmState) {
                is LoadState.Error -> Text(formatExceptionMessage(vmState.exception))
                LoadState.Loading, LoadState.Uninitialized -> LoadingMaxSizeIndicator()
                is LoadState.Success -> KomfSettingsContent(vm.discordState, vm.appriseState)
            }

        }
    }
}