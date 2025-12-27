package snd.komelia

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeout
import snd.komelia.komga.api.KomgaLibraryApi
import snd.komelia.komga.api.KomgaUserApi
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.user.KomgaUser

class KomgaAuthenticationState(
    private val userApi: StateFlow<KomgaUserApi>,
    private val libraryApi: StateFlow<KomgaLibraryApi>,
    private val currentUserFlow: MutableStateFlow<KomgaUser?>,
    val serverUrl: StateFlow<String>
) {
    private val _libraries = MutableStateFlow<List<KomgaLibrary>>(emptyList())
    private val _authenticationState = MutableStateFlow<DataState>(DataState.AuthenticationRequired)

    val authenticatedUser = currentUserFlow.asStateFlow()
    val authenticationState = _authenticationState.asStateFlow()
    val libraries = _libraries.asStateFlow()

    fun setStateValues(user: KomgaUser, libraries: List<KomgaLibrary>) {
        currentUserFlow.value = user
        _libraries.value = libraries
        _authenticationState.value = DataState.Loaded
    }

    fun updateLibraries(libraries: List<KomgaLibrary>) {
        _libraries.value = libraries
    }

    fun reset() {
        currentUserFlow.value = null
        _libraries.value = emptyList()
        _authenticationState.value = DataState.AuthenticationRequired
    }

    suspend fun tryReloadState() {
        runCatching {
            withTimeout(3000) {
                val user = userApi.value.getMe()
                val libraries = libraryApi.value.getLibraries()
                setStateValues(user, libraries)
            }
        }
    }

    sealed interface DataState {
        data object Loaded : DataState
        data object AuthenticationRequired : DataState
    }
}

