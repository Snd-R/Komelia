package io.github.snd_r.komelia.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.PlatformTitleBar
import io.github.snd_r.komelia.platform.PlatformType.DESKTOP
import io.github.snd_r.komelia.platform.PlatformType.MOBILE
import io.github.snd_r.komelia.platform.PlatformType.WEB_KOMF
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.MainScreen
import io.github.snd_r.komelia.ui.settings.SettingsScreenContainer

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val rootNavigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val platform = LocalPlatform.current
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getLoginViewModel() }

        LaunchedEffect(Unit) { vm.initialize() }
        Column {
            PlatformTitleBar { }
            when (platform) {
                MOBILE, DESKTOP ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { ScreenContent(vm, rootNavigator) }

                WEB_KOMF -> SettingsScreenContainer(title = "Komga Login") {
                    ScreenContent(vm, rootNavigator)
                }
            }
        }
    }

    @Composable
    private fun ScreenContent(
        viewModel: LoginViewModel,
        rootNavigator: Navigator
    ) {
        val state = viewModel.state.collectAsState()

        when (state.value) {
            Loading, Uninitialized -> LoginLoadingContent(viewModel::cancel)

            is Error -> LoginContent(
                url = viewModel.url,
                onUrlChange = viewModel::url::set,
                user = viewModel.user,
                onUserChange = { viewModel.user = it },
                password = viewModel.password,
                onPasswordChange = { viewModel.password = it },
                userLoginError = viewModel.userLoginError,
                autoLoginError = viewModel.autoLoginError,
                onAutoLoginRetry = viewModel::retryAutoLogin,
                onLogin = viewModel::loginWithCredentials
            )

            is Success -> rootNavigator.replaceAll(MainScreen())
        }

    }
}
