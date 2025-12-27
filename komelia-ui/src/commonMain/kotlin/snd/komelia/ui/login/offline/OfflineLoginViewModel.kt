package snd.komelia.ui.login.offline

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.KomgaAuthenticationState
import snd.komelia.offline.api.OfflineLibraryApi
import snd.komelia.offline.server.actions.MediaServerDeleteAction
import snd.komelia.offline.server.model.OfflineMediaServer
import snd.komelia.offline.server.model.OfflineMediaServerId
import snd.komelia.offline.server.repository.OfflineMediaServerRepository
import snd.komelia.offline.settings.OfflineSettingsRepository
import snd.komelia.offline.user.actions.UserDeleteAction
import snd.komelia.offline.user.model.OfflineUser
import snd.komelia.offline.user.repository.OfflineUserRepository
import snd.komelia.ui.MainScreen
import snd.komga.client.user.KomgaUserId

class OfflineLoginViewModel(
    private val appNotifications: AppNotifications,
    private val offlineSettingsRepository: OfflineSettingsRepository,
    private val userRepository: OfflineUserRepository,
    private val serverRepository: OfflineMediaServerRepository,
    private val komgaAuthState: KomgaAuthenticationState,
    private val offlineLibraryApi: OfflineLibraryApi,
    private val serverDeleteAction: MediaServerDeleteAction,
    private val userDeleteAction: UserDeleteAction,
) : ScreenModel {

    val offlineUsers = MutableStateFlow<Map<OfflineMediaServer, List<OfflineUser>>>(emptyMap())
    private val navigator = MutableStateFlow<Navigator?>(null)

    suspend fun initialize(navigator: Navigator) {
        this.navigator.value = navigator
        loadServers()
    }

    private suspend fun loadServers() {
        val servers = serverRepository.findAll()
        val users = servers.associateWith { userRepository.findAllByServer(it.id) }
        offlineUsers.value = users
    }

    fun loginAs(userId: KomgaUserId) {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            val user = userRepository.get(userId)
            offlineSettingsRepository.putUserId(user.id)
            offlineSettingsRepository.putOfflineMode(true)
            komgaAuthState.setStateValues(user.toKomgaUser(), offlineLibraryApi.getLibraries())
            navigator.value?.replaceAll(MainScreen())
        }
    }

    fun onServerDelete(serverId: OfflineMediaServerId) {
        screenModelScope.launch {
            serverDeleteAction.execute(serverId)
            loadServers()
        }
    }

    fun onUserDelete(userId: KomgaUserId) {
        screenModelScope.launch {
            userDeleteAction.execute(userId)
            loadServers()
        }
    }
}