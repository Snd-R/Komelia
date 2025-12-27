package snd.komelia.ui.settings.authactivity

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import snd.komelia.ui.LoadState.Error
import snd.komelia.ui.LoadState.Loading
import snd.komelia.ui.LoadState.Success
import snd.komelia.ui.LoadState.Uninitialized
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.settings.SettingsScreenContainer

class AuthenticationActivityScreen(val forMe: Boolean) : Screen {
    override val key: ScreenKey = "SettingsAuthActivityScreen$forMe"

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(forMe.toString()) { viewModelFactory.getAuthenticationActivityViewModel(forMe) }
        LaunchedEffect(forMe) { vm.initialize() }

        SettingsScreenContainer("Authentication Activity") {
            when (val state = vm.state.collectAsState().value) {
                Uninitialized, Loading -> LoadingMaxSizeIndicator()
                is Error -> Text(state.exception.message ?: "Error")
                is Success -> AuthenticationActivityContent(
                    activity = vm.activity,
                    forMe = forMe,
                    totalPages = vm.totalPages,
                    currentPage = vm.currentPage,
                    onPageChange = vm::loadPage
                )
            }
        }
    }
}