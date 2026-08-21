package snd.komelia.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
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
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.login_title
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.LoadState.Error
import snd.komelia.ui.LoadState.Loading
import snd.komelia.ui.LoadState.Success
import snd.komelia.ui.LoadState.Uninitialized
import snd.komelia.ui.LocalOfflineMode
import snd.komelia.ui.LocalPlatform
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.MainScreen
import snd.komelia.ui.login.offline.OfflineLoginScreen
import snd.komelia.ui.platform.PlatformTitleBar
import snd.komelia.ui.platform.PlatformType.DESKTOP
import snd.komelia.ui.platform.PlatformType.MOBILE
import snd.komelia.ui.platform.PlatformType.WEB_KOMF
import snd.komelia.ui.platform.hasLanPermission
import snd.komelia.ui.settings.SettingsScreenContainer

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val rootNavigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val platform = LocalPlatform.current
        val viewModelFactory = LocalViewModelFactory.current
        val isOffline = LocalOfflineMode.current
        val vm = rememberScreenModel(isOffline.value.toString()) { viewModelFactory.getLoginViewModel() }
        val hasLanPermission = hasLanPermission()

        LaunchedEffect(Unit) { vm.initialize(hasLanPermission) }
        Column {
            PlatformTitleBar { }
            when (platform) {
                MOBILE, DESKTOP ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { ScreenContent(vm, rootNavigator) }

                WEB_KOMF -> SettingsScreenContainer(title = stringResource(Res.string.login_title)) {
                    ScreenContent(vm, rootNavigator)
                }
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
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
                url = viewModel.url.collectAsState().value,
                onUrlChange = { viewModel.url.value = it },
                user = viewModel.user.collectAsState().value,
                onUserChange = { viewModel.user.value = it },
                password = viewModel.password.collectAsState().value,
                onPasswordChange = { viewModel.password.value = it },
                userLoginError = viewModel.userLoginError.collectAsState().value,
                autoLoginError = viewModel.autoLoginError.collectAsState().value,
                onAutoLoginRetry = viewModel::retryAutoLogin,
                onLogin = viewModel::loginWithCredentials,
                offlineIsAvailable = viewModel.offlineIsAvailable.collectAsState().value,
                onOfflineSelect = { rootNavigator.replaceAll(OfflineLoginScreen()) },
                canGoOfflineAsCurrentUser = viewModel.canGoOfflineAsCurrentUser.collectAsState(false).value,
                goOfflineAsCurrentUser = viewModel::offlineLogin
            )

            is Success -> rootNavigator.replaceAll(MainScreen())
        }

    }
}
