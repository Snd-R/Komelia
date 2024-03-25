package io.github.snd_r.komelia.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.MainScreen
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getLoginViewModel() }

        val state = vm.state.collectAsState()
        LaunchedEffect(Unit) { vm.initialize() }
        when (state.value) {
            Loading, Uninitialized -> LoadingMaxSizeIndicator()
            is Error -> LoginContent(
                url = vm.url,
                onUrlChange = vm::url::set,
                user = vm.user,
                onUserChange = { vm.user = it },
                password = vm.password,
                onPasswordChange = { vm.password = it },
                errorMessage = vm.error,
                onLogin = vm::loginWithCredentials
            )

            is Success -> navigator.replaceAll(MainScreen())
        }
    }
}