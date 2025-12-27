package snd.komelia.ui.login.offline

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.login.LoginScreen
import snd.komelia.ui.platform.PlatformTitleBar

class OfflineLoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getOfflineLoginViewModel() }
        LaunchedEffect(Unit) { vm.initialize(navigator) }

        Column {
            PlatformTitleBar { }
            Box(
                modifier = Modifier.fillMaxSize().padding(30.dp),
                contentAlignment = Alignment.Center
            ) {

                OfflineLoginContent(
                    serverUsers = vm.offlineUsers.collectAsState().value,
                    loginAs = vm::loginAs,
                    onServerDelete = vm::onServerDelete,
                    onUserDelete = vm::onUserDelete,
                    onReturnToLogin = { navigator.replaceAll(LoginScreen()) },
                )
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
        }
    }
}