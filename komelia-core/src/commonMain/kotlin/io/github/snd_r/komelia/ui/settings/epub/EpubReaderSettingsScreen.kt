package io.github.snd_r.komelia.ui.settings.epub

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.LocalViewModelFactory

class EpubReaderSettingsScreen(): Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel() { viewModelFactory.getEpubReaderSettingsViewModel() }
    }
}