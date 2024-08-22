package io.github.snd_r.komelia.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.SecretsRepository
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.ktor.client.plugins.*
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.utils.io.*
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryClient
import snd.komga.client.user.KomgaUser
import snd.komga.client.user.KomgaUserClient

class LoginViewModel(
    private val settingsRepository: SettingsRepository,
    private val komgaUserClient: KomgaUserClient,
    private val komgaLibraryClient: KomgaLibraryClient,
    private val authenticatedUserFlow: MutableStateFlow<KomgaUser?>,
    private val availableLibrariesFlow: MutableStateFlow<List<KomgaLibrary>>,
    private val secretsRepository: SecretsRepository,
    private val notifications: AppNotifications,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    var url by mutableStateOf("")
    var user by mutableStateOf("")
    var password by mutableStateOf("")
    var userLoginError by mutableStateOf<String?>(null)

    var autoLoginError by mutableStateOf<String?>(null)

    fun initialize() {
        if (state.value !is Uninitialized) return

        screenModelScope.launch {
            url = settingsRepository.getServerUrl().first()
            user = settingsRepository.getCurrentUser().first()
            val loginCookie = secretsRepository.getCookie(url)
            tryAutologin()
//            if (!loginCookie.isNullOrBlank()) {
//                tryAutologin()
//            } else {
//                mutableState.value = LoadState.Error(RuntimeException())
//            }
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
            val withoutTrailingSlashes = url.replace("/+$".toRegex(), "")
            settingsRepository.putServerUrl(withoutTrailingSlashes)
            settingsRepository.putCurrentUser(user)
            tryUserLogin(user, password)
        }
    }

    private suspend fun tryAutologin() {
        try {
            tryLogin()
        } catch (e: CancellationException) {
            throw e
        } catch (e: ClientRequestException) {
            autoLoginError = if (e.response.status == Unauthorized) "Invalid credentials"
            else "Login error: ${e::class.simpleName} ${e.message}"
            mutableState.value = LoadState.Error(e)
            notifications.add(AppNotification.Error(e.message))
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
        } catch (e: ClientRequestException) {
            userLoginError = if (e.response.status == Unauthorized) "Invalid credentials"
            else "Login error ${e::class.simpleName}: ${e.message}"
            mutableState.value = LoadState.Error(e)
        } catch (e: Throwable) {
            userLoginError = "${e::class.simpleName} ${e.message}"
            mutableState.value = LoadState.Error(e)
        }
    }

    private suspend fun tryLogin(
        username: String? = null,
        password: String? = null
    ) {
        val user =
            if (username != null && password != null) komgaUserClient.getMe(username, password, true)
            else komgaUserClient.getMe()

        val libraries = komgaLibraryClient.getLibraries()
        authenticatedUserFlow.value = user
        availableLibrariesFlow.value = libraries
        mutableState.value = LoadState.Success(Unit)
    }
}

sealed class LoginResult {
    data object Loading : LoginResult()
    data object Error : LoginResult()
    data object Success : LoginResult()
}