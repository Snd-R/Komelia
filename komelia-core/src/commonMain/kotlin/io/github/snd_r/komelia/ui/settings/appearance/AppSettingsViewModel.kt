package io.github.snd_r.komelia.ui.settings.appearance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.common.cards.defaultCardWidth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import io.github.snd_r.komelia.settings.CommonSettingsRepository

class AppSettingsViewModel(
    private val settingsRepository: CommonSettingsRepository,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {
    var cardWidth by mutableStateOf(defaultCardWidth.dp)
    var currentTheme by mutableStateOf(AppTheme.DARK)

    suspend fun initialize() {
        if (state.value !is LoadState.Uninitialized) return
        mutableState.value = LoadState.Loading
        cardWidth = settingsRepository.getCardWidth().map { it.dp }.first()
        currentTheme = settingsRepository.getAppTheme().first()
        mutableState.value = LoadState.Success(Unit)
    }

    fun onCardWidthChange(cardWidth: Dp) {
        this.cardWidth = cardWidth
        screenModelScope.launch { settingsRepository.putCardWidth(cardWidth.value.toInt()) }
    }

    fun onAppThemeChange(theme: AppTheme) {
        this.currentTheme = theme
        screenModelScope.launch { settingsRepository.putAppTheme(theme) }
    }

}