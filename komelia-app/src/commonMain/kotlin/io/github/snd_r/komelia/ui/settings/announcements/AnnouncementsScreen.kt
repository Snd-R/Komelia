package io.github.snd_r.komelia.ui.settings.announcements

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.settings.SettingsScreenContainer

class AnnouncementsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getAnnouncementsViewModel() }
        val state = vm.state.collectAsState()

        SettingsScreenContainer("Announcements") {
            when (val result = state.value) {
                is Success -> AnnouncementsContent(result.value.items)
                LoadState.Uninitialized, Loading -> LoadingMaxSizeIndicator()

                is Error -> Text("Error")
            }
        }

    }
}