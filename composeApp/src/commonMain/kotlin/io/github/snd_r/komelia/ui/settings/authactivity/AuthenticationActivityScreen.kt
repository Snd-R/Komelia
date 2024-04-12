package io.github.snd_r.komelia.ui.settings.authactivity

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

class AuthenticationActivityScreen(val forMe: Boolean) : Screen {
    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(forMe.toString()) { viewModelFactory.getAuthenticationActivityViewModel(forMe) }
        LaunchedEffect(forMe) { vm.initialize() }

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