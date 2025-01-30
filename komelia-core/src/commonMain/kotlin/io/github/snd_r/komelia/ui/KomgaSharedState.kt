package io.github.snd_r.komelia.ui

import io.github.snd_r.komelia.ui.KomgaSharedState.DataState.AuthenticationRequired
import io.github.snd_r.komelia.ui.KomgaSharedState.DataState.Loaded
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeout
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryClient
import snd.komga.client.user.KomgaUser
import snd.komga.client.user.KomgaUserClient

class KomgaSharedState(
    private val userClient: KomgaUserClient,
    private val libraryClient: KomgaLibraryClient,
) {
    private val _authenticatedUser = MutableStateFlow<KomgaUser?>(null)
    private val _libraries = MutableStateFlow<List<KomgaLibrary>>(emptyList())
    private val _state = MutableStateFlow<DataState>(AuthenticationRequired)

    val authenticatedUser = _authenticatedUser.asStateFlow()
    val libraries = _libraries.asStateFlow()
    val state = _state.asStateFlow()

    fun setStateValues(user: KomgaUser, libraries: List<KomgaLibrary>) {
        _authenticatedUser.value = user
        _libraries.value = libraries
        _state.value = Loaded
    }

    fun updateLibraries(libraries: List<KomgaLibrary>) {
        _libraries.value = libraries
    }

    fun reset() {
        _authenticatedUser.value = null
        _libraries.value = emptyList()
        _state.value = AuthenticationRequired
    }

    suspend fun tryReloadState() {
        runCatching {
            withTimeout(3000) {
                val user = userClient.getMe()
                val libraries = libraryClient.getLibraries()
                setStateValues(user, libraries)
            }
        }
    }

    sealed interface DataState {
        data object Loaded : DataState
        data object AuthenticationRequired : DataState
    }
}

