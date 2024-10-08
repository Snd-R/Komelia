package io.github.snd_r.komelia.offline.client

import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.Page
import snd.komga.client.user.KomgaAuthenticationActivity
import snd.komga.client.user.KomgaUser
import snd.komga.client.user.KomgaUserClient
import snd.komga.client.user.KomgaUserCreateRequest
import snd.komga.client.user.KomgaUserId
import snd.komga.client.user.KomgaUserUpdateRequest

class OfflineUserClient : KomgaUserClient {
    override suspend fun addUser(user: KomgaUserCreateRequest): KomgaUser {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUser(userId: KomgaUserId) {
        TODO("Not yet implemented")
    }

    override suspend fun getAllUsers(): List<KomgaUser> {
        TODO("Not yet implemented")
    }

    override suspend fun getAuthenticationActivity(
        pageRequest: KomgaPageRequest?,
        unpaged: Boolean
    ): Page<KomgaAuthenticationActivity> {
        TODO("Not yet implemented")
    }

    override suspend fun getLatestAuthenticationActivityForUser(userId: KomgaUserId): KomgaAuthenticationActivity {
        TODO("Not yet implemented")
    }

    override suspend fun getMe(): KomgaUser {
        TODO("Not yet implemented")
    }

    override suspend fun getMe(username: String, password: String, rememberMe: Boolean): KomgaUser {
        TODO("Not yet implemented")
    }

    override suspend fun getMeAuthenticationActivity(
        pageRequest: KomgaPageRequest?,
        unpaged: Boolean
    ): Page<KomgaAuthenticationActivity> {
        TODO("Not yet implemented")
    }

    override suspend fun logout() {
        TODO("Not yet implemented")
    }

    override suspend fun updateMyPassword(newPassword: String) {
        TODO("Not yet implemented")
    }

    override suspend fun updatePassword(userId: KomgaUserId, password: String) {
        TODO("Not yet implemented")
    }

    override suspend fun updateUser(userId: KomgaUserId, request: KomgaUserUpdateRequest) {
        TODO("Not yet implemented")
    }
}