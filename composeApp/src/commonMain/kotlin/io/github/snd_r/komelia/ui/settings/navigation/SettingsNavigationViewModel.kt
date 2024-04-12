package io.github.snd_r.komelia.ui.settings.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.SecretsRepository
import io.github.snd_r.komelia.ui.login.LoginScreen
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.book.KomgaBookQuery
import io.github.snd_r.komga.book.KomgaMediaStatus
import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.user.KomgaUser
import io.github.snd_r.komga.user.KomgaUserClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsNavigationViewModel(
    private val rootNavigator: Navigator,
    private val appNotifications: AppNotifications,
    private val userClient: KomgaUserClient,
    private val authenticatedUser: MutableStateFlow<KomgaUser?>,
    private val secretsRepository: SecretsRepository,
    private val currentServerUrl: Flow<String>,
    private val bookClient: KomgaBookClient,
) : ScreenModel {
    var hasMediaErrors by mutableStateOf(false)
        private set

    fun initialize() {
        screenModelScope.launch {
            val pageResponse = bookClient.getAllBooks(
                KomgaBookQuery(mediaStatus = listOf(KomgaMediaStatus.ERROR, KomgaMediaStatus.UNSUPPORTED)),
                KomgaPageRequest(size = 0)
            )
            if (pageResponse.numberOfElements > 0) {
                hasMediaErrors = true
            }
        }
    }

    fun logout() {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            authenticatedUser.value = null
            userClient.logout()
            secretsRepository.deleteCookie(currentServerUrl.first())
            rootNavigator.replaceAll(LoginScreen())
        }
    }
}