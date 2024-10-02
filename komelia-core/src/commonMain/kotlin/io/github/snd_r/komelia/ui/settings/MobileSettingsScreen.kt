package io.github.snd_r.komelia.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.platform.PlatformTitleBar
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.settings.navigation.SettingsNavigationMenu

class MobileSettingsScreen : Screen {
    @Composable
    override fun Content() {
        val currentNavigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getSettingsNavigationViewModel(currentNavigator) }
        LaunchedEffect(Unit) { vm.initialize() }

        Surface(
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                PlatformTitleBar()
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentNavigator.pop() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                    }
                    Text("Settings", style = MaterialTheme.typography.titleLarge)
                }

                HorizontalDivider()

                SettingsNavigationMenu(
                    currentScreen = currentNavigator.lastItem,
                    onNavigation = { currentNavigator.push(it) },
                    hasMediaErrors = vm.hasMediaErrors,
                    komfEnabled = vm.komfEnabledFlow.collectAsState().value,
                    newVersionIsAvailable = vm.newVersionIsAvailable,
                    onLogout = vm::logout,
                    contentColor = MaterialTheme.colorScheme.surface
                )

                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
            }
        }
        BackPressHandler { currentNavigator.pop() }
    }
}
