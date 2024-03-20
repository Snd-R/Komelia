package io.github.snd_r.komelia.ui.settings.appearance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.settings.defaultCardWidth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppearanceViewModel(
    private val settingsRepository: SettingsRepository,
) : ScreenModel {
    var cardWidth by mutableStateOf(defaultCardWidth.dp)

    init {
        screenModelScope.launch {
            cardWidth = settingsRepository.getCardWidth().first()
        }
    }

    fun onCardWidthChange(cardWidth: Dp) {
        this.cardWidth = cardWidth
        screenModelScope.launch {
            settingsRepository.putCardWidth(cardWidth)
        }
    }
}