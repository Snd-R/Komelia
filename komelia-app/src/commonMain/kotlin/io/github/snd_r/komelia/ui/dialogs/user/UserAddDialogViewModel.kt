package io.github.snd_r.komelia.ui.dialogs.user

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komga.user.KomgaUserClient
import io.github.snd_r.komga.user.KomgaUserCreateRequest
import io.github.snd_r.komga.user.ROLE_ADMIN
import io.github.snd_r.komga.user.ROLE_FILE_DOWNLOAD
import io.github.snd_r.komga.user.ROLE_PAGE_STREAMING

class UserAddDialogViewModel(
    private val appNotifications: AppNotifications,
    private val userClient: KomgaUserClient,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    var email by mutableStateOf("")
        private set
    var emailValidationError by mutableStateOf<String?>(null)
        private set
    var password by mutableStateOf("")
        private set
    var passwordValidationError by mutableStateOf<String?>(null)
        private set

    var administratorRole by mutableStateOf(false)
    var pageStreamingRole by mutableStateOf(true)
    var fileDownloadRole by mutableStateOf(true)

    val isValid by derivedStateOf {
        email.isNotBlank() && emailValidationError == null
                && password.isNotBlank() && passwordValidationError == null
    }

    fun onEmailChange(email: String) {
        this.email = email
    }

    fun onPasswordChange(password: String) {
        passwordValidationError = if (password.isBlank()) "Required" else null
        this.password = password
    }

    suspend fun addUser() {
        mutableState.value = LoadState.Loading
        val request = KomgaUserCreateRequest(
            email = email,
            password = password,
            roles = buildSet {
                if (administratorRole) add(ROLE_ADMIN)
                if (pageStreamingRole) add(ROLE_PAGE_STREAMING)
                if (fileDownloadRole) add(ROLE_FILE_DOWNLOAD)
            }
        )


        appNotifications.runCatchingToNotifications {
            userClient.addUser(request)
            mutableState.value = LoadState.Success(Unit)
        }.onFailure { mutableState.value = LoadState.Error(it) }
    }
}