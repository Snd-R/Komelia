package io.github.snd_r.komelia.ui.settings.account

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.settings.SettingsScreenContainer

class AccountSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getAccountViewModel() }
        SettingsScreenContainer(title = "Account Settings") {
            AccountSettingsContent(user = vm.user)
        }
    }
}