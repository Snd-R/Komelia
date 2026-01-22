package snd.komelia.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.utils.io.*
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import snd.komelia.AppNotification
import snd.komelia.AppNotifications
import snd.komelia.KomgaAuthenticationState
import snd.komelia.komga.api.KomgaLibraryApi
import snd.komelia.komga.api.KomgaUserApi
import snd.komelia.offline.api.OfflineLibraryApi
import snd.komelia.offline.server.repository.OfflineMediaServerRepository
import snd.komelia.offline.settings.OfflineSettingsRepository
import snd.komelia.offline.user.model.OfflineUser
import snd.komelia.offline.user.repository.OfflineUserRepository
import snd.komelia.settings.CommonSettingsRepository
import snd.komelia.settings.SecretsRepository
import snd.komelia.ui.LoadState
import snd.komelia.ui.LoadState.Uninitialized
import snd.komelia.ui.error.formatExceptionMessage
import snd.komelia.ui.platform.PlatformType
import snd.komelia.ui.platform.PlatformType.DESKTOP
import snd.komelia.ui.platform.PlatformType.MOBILE
import snd.komelia.ui.platform.PlatformType.WEB_KOMF

class LoginViewModel(
    private val settingsRepository: CommonSettingsRepository,
    private val secretsRepository: SecretsRepository,
    private val komgaUserApi: Flow<KomgaUserApi>,
    private val komgaLibraryApi: Flow<KomgaLibraryApi>,
    private val komgaAuthState: KomgaAuthenticationState,
    private val notifications: AppNotifications,
    private val platform: PlatformType,

    private val offlineUserRepository: OfflineUserRepository,
    private val offlineServerRepository: OfflineMediaServerRepository,
    private val offlineSettingsRepository: OfflineSettingsRepository,
    private val offlineLibraryApi: OfflineLibraryApi,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    var url by mutableStateOf("")
    var user by mutableStateOf("")
    var password by mutableStateOf("")
    var userLoginError by mutableStateOf<String?>(null)
    var autoLoginError by mutableStateOf<String?>(null)
    val offlineIsAvailable = MutableStateFlow(false)
    private val offlineUser = MutableStateFlow<OfflineUser?>(null)
    val canGoOfflineAsCurrentUser = offlineUser.map { it != null }

    fun initialize() {
        if (state.value !is Uninitialized) return

        screenModelScope.launch {
            url = settingsRepository.getServerUrl().first()
            user = settingsRepository.getCurrentUser().first()
            val offlineUsers = offlineUserRepository.findAll()
            val offlineServer = offlineServerRepository.findByUrl(url)

            offlineIsAvailable.value = offlineUsers.any { it.id != OfflineUser.ROOT }
            offlineUser.value = offlineServer?.let { server -> offlineUsers.first { it.serverId == server.id } }
            val isOffline = offlineSettingsRepository.getOfflineMode().first()

            when (platform) {
                MOBILE, DESKTOP -> {
                    if (isOffline || secretsRepository.getCookie(url) != null) {
                        tryAutologin()
                    } else {
                        mutableState.value = LoadState.Error(RuntimeException("Not logged in"))
                    }
                }

                WEB_KOMF -> tryAutologin()
            }
        }
    }

    fun retryAutoLogin() {
        screenModelScope.launch {
            mutableState.value = LoadState.Loading
            tryAutologin()
        }
    }

    fun cancel() {
        screenModelScope.coroutineContext.cancelChildren()
        mutableState.value = LoadState.Error(RuntimeException("Cancelled login attempt"))
        userLoginError = "Cancelled login attempt"
    }

    fun loginWithCredentials() {
        screenModelScope.launch {
            userLoginError = null
            settingsRepository.putServerUrl(url)
            settingsRepository.putCurrentUser(user)
            tryUserLogin(user, password)
        }
    }

    fun offlineLogin() {
        notifications.runCatchingToNotifications(screenModelScope) {
            val user = offlineUser.value ?: return@runCatchingToNotifications
            offlineSettingsRepository.putOfflineMode(true)
            offlineSettingsRepository.putUserId(user.id)
            komgaAuthState.setStateValues(user.toKomgaUser(), offlineLibraryApi.getLibraries())
            mutableState.value = LoadState.Success(Unit)
        }
    }

    private suspend fun tryAutologin() {
        try {
            tryLogin()
        } catch (e: CancellationException) {
            throw e
        } catch (e: NoTransformationFoundException) {
            val message = "Unexpected response for url $url"
            autoLoginError = message
            notifications.add(AppNotification.Error(message))
            mutableState.value = LoadState.Error(e)
        } catch (e: ClientRequestException) {
            if (e.response.status == Unauthorized) {
                autoLoginError = null
            } else {
                autoLoginError = "Login error: ${e::class.simpleName} ${e.message}"
                notifications.add(AppNotification.Error(e.message))
            }
            mutableState.value = LoadState.Error(e)
        } catch (e: Error) { // wasm fetch error
            val errorMessage = "Login error: ${e::class.simpleName} ${e.message}"
            mutableState.value = LoadState.Error(e)
            notifications.add(AppNotification.Error(errorMessage))
        } catch (e: Throwable) {
            val errorMessage = "Login error: ${e::class.simpleName} ${e.message}"
            autoLoginError = errorMessage
            mutableState.value = LoadState.Error(e)
            notifications.add(AppNotification.Error(errorMessage))
        }
    }

    private suspend fun tryUserLogin(username: String, password: String) {
        try {
            tryLogin(username, password)
        } catch (e: CancellationException) {
            throw e
        } catch (e: NoTransformationFoundException) {
            val message = "Unexpected response for url $url"
            userLoginError = message
            mutableState.value = LoadState.Error(e)
        } catch (e: ClientRequestException) {
            userLoginError = if (e.response.status == Unauthorized) "Invalid credentials"
            else "Login error ${e::class.simpleName}: ${e.message}"
            mutableState.value = LoadState.Error(e)
        } catch (e: Throwable) {
            userLoginError = formatExceptionMessage(e)
            mutableState.value = LoadState.Error(e)
        }
    }

    private suspend fun tryLogin(
        username: String? = null,
        password: String? = null
    ) {
        val userApi = this.komgaUserApi.first()
        val libraryApi = this.komgaLibraryApi.first()
        val user =
            if (username != null && password != null) userApi.getMe(username, password, true)
            else userApi.getMe()

        val libraries = libraryApi.getLibraries()
        komgaAuthState.setStateValues(user, libraries)
        mutableState.value = LoadState.Success(Unit)
    }
}

sealed class LoginResult {
    data object Loading : LoginResult()
    data object Error : LoginResult()
    data object Success : LoginResult()
}