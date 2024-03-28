package io.github.snd_r.komelia.ui.settings.navigation

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.SecretsRepository
import io.github.snd_r.komelia.ui.login.LoginScreen
import io.github.snd_r.komga.user.KomgaUser
import io.github.snd_r.komga.user.KomgaUserClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

class SettingsNavigationViewModel(
    private val rootNavigator: Navigator,
    private val appNotifications: AppNotifications,
    private val userClient: KomgaUserClient,
    private val authenticatedUser: MutableStateFlow<KomgaUser?>,
    private val secretsRepository: SecretsRepository,
    private val currentServerUrl: Flow<String>,
) : ScreenModel {

    fun logout() {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            authenticatedUser.value = null
            userClient.logout()
            secretsRepository.deleteCookie(currentServerUrl.first())
            rootNavigator.replaceAll(LoginScreen())
        }
    }
}