package io.github.snd_r.komelia.ui.dialogs.user

import cafe.adriel.voyager.core.model.ScreenModel
import io.github.snd_r.komelia.AppNotifications
import snd.komga.client.user.KomgaUser
import snd.komga.client.user.KomgaUserClient

class PasswordChangeDialogViewModel(
    private val appNotifications: AppNotifications,
    private val userClient: KomgaUserClient,
    val user: KomgaUser?,
) : ScreenModel {

    suspend fun changePassword(newPassword: String) {
        appNotifications.runCatchingToNotifications {
            if (user == null) userClient.updateMyPassword(newPassword)
            else userClient.updatePassword(user.id, newPassword)
        }
    }
}