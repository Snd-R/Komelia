package snd.komelia.ui.settings.account

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_account_title
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.settings.SettingsScreenContainer

class AccountSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getAccountViewModel() }
        SettingsScreenContainer(title = stringResource(Res.string.settings_account_title)) {
            AccountSettingsContent(user = vm.user)
        }
    }
}