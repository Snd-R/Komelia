package io.github.snd_r.komelia.ui.settings.users

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator

class UsersScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getUsersViewModel() }
        LaunchedEffect(Unit) { vm.initialize() }

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