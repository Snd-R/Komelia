package io.github.snd_r.komelia.ui.settings.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.PlatformType.DESKTOP
import io.github.snd_r.komelia.platform.PlatformType.MOBILE
import io.github.snd_r.komelia.platform.PlatformType.WEB_KOMF
import io.github.snd_r.komelia.settings.SecretsRepository
import io.github.snd_r.komelia.ui.KomgaSharedState
import io.github.snd_r.komelia.ui.komf.KomfMainScreen
import io.github.snd_r.komelia.ui.login.LoginScreen
import io.github.snd_r.komelia.updates.AppVersion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaMediaStatus
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.search.allOfBooks
import snd.komga.client.user.KomgaUser
import snd.komga.client.user.KomgaUserClient

class SettingsNavigationViewModel(
    private val rootNavigator: Navigator,
    private val appNotifications: AppNotifications,
    private val userClient: KomgaUserClient,
    private val komgaSharedState: KomgaSharedState,
    private val secretsRepository: SecretsRepository,
    private val currentServerUrl: Flow<String>,
    private val bookClient: KomgaBookClient,
    private val latestVersion: Flow<AppVersion?>,
    private val platformType: PlatformType,
    val updatesEnabled: Boolean,
    val user: StateFlow<KomgaUser?>,
    komfEnabled: Flow<Boolean>,
) : ScreenModel {
    var hasMediaErrors by mutableStateOf(false)
        private set
    var newVersionIsAvailable by mutableStateOf(false)
        private set
    val komfEnabledFlow = komfEnabled.stateIn(screenModelScope, Eagerly, false)

    suspend fun initialize() {
        appNotifications.runCatchingToNotifications {
            val pageResponse = bookClient.getBookList(
                conditionBuilder = allOfBooks {
                    mediaStatus { isEqualTo(KomgaMediaStatus.ERROR) }
                    mediaStatus { isEqualTo(KomgaMediaStatus.UNSUPPORTED) }
                },
                pageRequest = KomgaPageRequest(size = 0)
            )
            if (pageResponse.numberOfElements > 0) {
                hasMediaErrors = true
            }
            val latestVersion = latestVersion.first()
            newVersionIsAvailable = latestVersion != null && AppVersion.current < latestVersion
        }
    }

    fun logout() {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            secretsRepository.deleteCookie(currentServerUrl.first())
            runCatching { userClient.logout() }
            komgaSharedState.reset()
            when (platformType) {
                MOBILE, DESKTOP -> rootNavigator.replaceAll(LoginScreen())
                WEB_KOMF -> rootNavigator.replaceAll(KomfMainScreen())
            }

        }
    }
}