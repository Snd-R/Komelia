package snd.komelia.ui.settings.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import snd.komelia.AppNotifications
import snd.komelia.KomgaAuthenticationState
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.KomgaUserApi
import snd.komelia.offline.settings.OfflineSettingsRepository
import snd.komelia.settings.SecretsRepository
import snd.komelia.ui.komf.KomfMainScreen
import snd.komelia.ui.login.LoginScreen
import snd.komelia.ui.platform.PlatformType
import snd.komelia.ui.platform.PlatformType.DESKTOP
import snd.komelia.ui.platform.PlatformType.MOBILE
import snd.komelia.ui.platform.PlatformType.WEB_KOMF
import snd.komelia.updates.AppVersion
import snd.komga.client.book.KomgaMediaStatus
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.search.allOfBooks
import snd.komga.client.user.KomgaUser

class SettingsNavigationViewModel(
    private val rootNavigator: Navigator,
    private val appNotifications: AppNotifications,
    private val userApi: KomgaUserApi,
    private val komgaSharedState: KomgaAuthenticationState,
    private val secretsRepository: SecretsRepository,
    private val offlineSettingsRepository: OfflineSettingsRepository,
    private val isOffline: StateFlow<Boolean>,
    private val currentServerUrl: Flow<String>,
    private val bookApi: KomgaBookApi,
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
            val pageResponse = bookApi.getBookList(
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
            if (isOffline.value) {
                offlineSettingsRepository.putOfflineMode(false)
            } else {
                runCatching { userApi.logout() }
            }

            secretsRepository.deleteCookie(currentServerUrl.first())
            komgaSharedState.reset()

            when (platformType) {
                MOBILE, DESKTOP -> rootNavigator.replaceAll(LoginScreen())
                WEB_KOMF -> rootNavigator.replaceAll(KomfMainScreen())
            }


        }
    }
}