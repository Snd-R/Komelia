package io.github.snd_r.komelia.ui.settings.users

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import snd.komga.client.user.KomgaAuthenticationActivity
import snd.komga.client.user.KomgaUser
import snd.komga.client.user.KomgaUserClient
import snd.komga.client.user.KomgaUserId

class UsersViewModel(
    private val appNotifications: AppNotifications,
    private val userClient: KomgaUserClient,
    val currentUser: KomgaUser,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {
    var users: Map<KomgaUser, KomgaAuthenticationActivity?> by mutableStateOf(emptyMap())


    fun initialize() {
        if (state.value !is Uninitialized) return
        loadUserList()

    }

    fun loadUserList() {
        mutableState.value = Loading

        screenModelScope.launch {
            appNotifications.runCatchingToNotifications {
                users = userClient.getAllUsers().associateWith { user -> getLatestActivityFor(user.id) }
                mutableState.value = Success(Unit)
            }.onFailure { mutableState.value = Error(it) }
        }
    }

    fun onUserDelete(userId: KomgaUserId) {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            userClient.deleteUser(userId)
            loadUserList()
        }

    }

    private suspend fun getLatestActivityFor(userId: KomgaUserId): KomgaAuthenticationActivity? {
        return try {
            userClient.getLatestAuthenticationActivityForUser(userId)
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) null
            else throw e
        }
    }
}