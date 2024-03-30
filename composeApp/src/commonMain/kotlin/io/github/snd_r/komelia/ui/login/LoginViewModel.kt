package io.github.snd_r.komelia.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.library.KomgaLibraryClient
import io.github.snd_r.komga.user.KomgaUser
import io.github.snd_r.komga.user.KomgaUserClient
import io.ktor.client.plugins.*
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginViewModel(
    private val settingsRepository: SettingsRepository,
    private val komgaUserClient: KomgaUserClient,
    private val komgaLibraryClient: KomgaLibraryClient,
    private val authenticatedUserFlow: MutableStateFlow<KomgaUser?>,
    private val availableLibrariesFlow: MutableStateFlow<List<KomgaLibrary>>,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    var url by mutableStateOf("")
    var user by mutableStateOf("")
    var password by mutableStateOf("")
    var error by mutableStateOf<String?>(null)

    fun initialize() {
        if (state.value !is Uninitialized) return

        screenModelScope.launch {
            url = settingsRepository.getServerUrl().first()
            user = settingsRepository.getCurrentUser().first()
            tryLogin()
        }
    }

    suspend fun loginWithCredentials() {
        error = null
        settingsRepository.putServerUrl(url)
        settingsRepository.putCurrentUser(user)
        tryLogin(user, password)
    }

    private suspend fun tryLogin(
        username: String? = null,
        password: String? = null
    ) {
        try {
            val user =
                if (username != null && password != null) komgaUserClient.getMe(username, password, true)
                else komgaUserClient.getMe()

            val libraries = komgaLibraryClient.getLibraries()
            authenticatedUserFlow.value = user
            availableLibrariesFlow.value = libraries
            mutableState.value = LoadState.Success(Unit)
        } catch (e: Exception) {
            error = when (e) {
                is ClientRequestException -> {
                    if (e.response.status == Unauthorized) "Invalid credentials"
                    else "Failed to login ${e.message}"
                }

                else -> e.message ?: "Failed to login"
            }
            mutableState.value = LoadState.Error(e)
        }
    }
}

sealed class LoginResult {
    data object Loading : LoginResult()
    data object Error : LoginResult()
    data object Success : LoginResult()
}