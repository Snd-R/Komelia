package snd.komelia.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import snd.komelia.LocalKomfViewModelFactory
import snd.komelia.ui.LoadState
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabItem
import snd.komelia.ui.error.formatExceptionMessage
import snd.komelia.ui.settings.komf.notifications.view.KomfSettingsContent
import snd.komelia.ui.strings.AppStrings

class NotificationsTab : DialogTab {

    override fun options() = TabItem(
        title = AppStrings.komfNotifications,
        icon = Icons.Default.Notifications
    )

    @Composable
    override fun Content() {
        val viewModelFactory = LocalKomfViewModelFactory.current
        val vm = remember { viewModelFactory.getKomfNotificationViewModel() }
        val vmState = vm.state.collectAsState().value
        val komfConfigLoadError = vm.komfConfig.configError.collectAsState().value
        LaunchedEffect(Unit) { vm.initialize() }

        if (komfConfigLoadError != null) {
            Text(formatExceptionMessage(komfConfigLoadError))
            return
        }

        when (vmState) {
            is LoadState.Error -> Text(formatExceptionMessage(vmState.exception))
            LoadState.Loading, LoadState.Uninitialized -> LoadingMaxSizeIndicator()
            is LoadState.Success -> KomfSettingsContent(vm.discordState, vm.appriseState)
        }

    }
}
