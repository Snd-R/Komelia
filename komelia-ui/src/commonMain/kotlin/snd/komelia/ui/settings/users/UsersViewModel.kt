package snd.komelia.ui.settings.users

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.komga.api.KomgaUserApi
import snd.komelia.ui.LoadState
import snd.komelia.ui.LoadState.Error
import snd.komelia.ui.LoadState.Loading
import snd.komelia.ui.LoadState.Success
import snd.komelia.ui.LoadState.Uninitialized
import snd.komga.client.user.KomgaAuthenticationActivity
import snd.komga.client.user.KomgaUser
import snd.komga.client.user.KomgaUserId

class UsersViewModel(
    private val appNotifications: AppNotifications,
    private val userApi: KomgaUserApi,
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
                users = userApi.getAllUsers().associateWith { user -> getLatestActivityFor(user.id) }
                mutableState.value = Success(Unit)
            }.onFailure { mutableState.value = Error(it) }
        }
    }

    fun onUserDelete(userId: KomgaUserId) {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            userApi.deleteUser(userId)
            loadUserList()
        }

    }

    private suspend fun getLatestActivityFor(userId: KomgaUserId): KomgaAuthenticationActivity? {
        return try {
            userApi.getLatestAuthenticationActivityForUser(userId)
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) null
            else throw e
        }
    }
}