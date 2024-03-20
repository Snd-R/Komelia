package io.github.snd_r.komelia.ui.settings.authactivity

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.LocalViewModelFactory

class AuthenticationActivityScreen(val forMe: Boolean) : Screen {
    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(forMe.toString()) { viewModelFactory.getAuthenticationActivityViewModel(forMe) }

        if (vm.activity.isNotEmpty())
            AuthenticationActivityContent(
                activity = vm.activity,
                forMe = forMe,
                loadMoreEntries = vm::loadMoreEntries
            )
    }
}