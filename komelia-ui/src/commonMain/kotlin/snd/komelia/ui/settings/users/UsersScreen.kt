package snd.komelia.ui.settings.users

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import snd.komelia.ui.LoadState.Error
import snd.komelia.ui.LoadState.Loading
import snd.komelia.ui.LoadState.Success
import snd.komelia.ui.LoadState.Uninitialized
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.settings.SettingsScreenContainer

class UsersScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getUsersViewModel() }
        LaunchedEffect(Unit) { vm.initialize() }

        SettingsScreenContainer("Users") {
            when (vm.state.collectAsState().value) {
                is Error -> Text("Error")
                Uninitialized, Loading -> LoadingMaxSizeIndicator()

                is Success -> UsersContent(
                    currentUser = vm.currentUser,
                    users = vm.users,
                    onUserReloadRequest = vm::loadUserList,
                    onUserDelete = vm::onUserDelete
                )
            }
        }
    }
}