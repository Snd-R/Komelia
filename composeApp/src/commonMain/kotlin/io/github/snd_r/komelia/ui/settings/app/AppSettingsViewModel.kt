package io.github.snd_r.komelia.ui.settings.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.ui.common.cards.defaultCardWidth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppSettingsViewModel(
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