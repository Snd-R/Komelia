package snd.komelia.ui.settings.announcements

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_announcements_title
import org.jetbrains.compose.resources.stringResource
import snd.komelia.ui.LoadState
import snd.komelia.ui.LoadState.Error
import snd.komelia.ui.LoadState.Loading
import snd.komelia.ui.LoadState.Success
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.settings.SettingsScreenContainer

class AnnouncementsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getAnnouncementsViewModel() }
        val state = vm.state.collectAsState()

        SettingsScreenContainer(stringResource(Res.string.settings_announcements_title)) {
            when (val result = state.value) {
                is Success -> AnnouncementsContent(result.value.items)
                LoadState.Uninitialized, Loading -> LoadingMaxSizeIndicator()

                is Error -> Text("Error")
            }
        }

    }
}