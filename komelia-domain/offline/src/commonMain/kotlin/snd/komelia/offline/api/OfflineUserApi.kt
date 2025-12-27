package snd.komelia.offline.api

import kotlinx.coroutines.flow.StateFlow
import snd.komelia.komga.api.KomgaUserApi
import snd.komelia.offline.user.repository.OfflineUserRepository
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.Page
import snd.komga.client.user.KomgaAuthenticationActivity
import snd.komga.client.user.KomgaUser
import snd.komga.client.user.KomgaUserCreateRequest
import snd.komga.client.user.KomgaUserId
import snd.komga.client.user.KomgaUserUpdateRequest

class OfflineUserApi(
    private val offlineUserId: StateFlow<KomgaUserId>,
    private val userRepository: OfflineUserRepository,
) : KomgaUserApi {

    private val userId
        get() = offlineUserId.value

    override suspend fun logout() {
    }

    override suspend fun getMe(): KomgaUser {
        return userRepository.get(userId).toKomgaUser()
    }

    override suspend fun getMe(
        username: String,
        password: String,
        rememberMe: Boolean
    ): KomgaUser {
        return userRepository.get(userId).toKomgaUser()
    }

    override suspend fun updateMyPassword(newPassword: String) {
    }

    override suspend fun getAllUsers(): List<KomgaUser> {
        return listOf(userRepository.get(userId).toKomgaUser())
    }

    override suspend fun addUser(user: KomgaUserCreateRequest): KomgaUser {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUser(userId: KomgaUserId) {
    }

    override suspend fun updateUser(
        userId: KomgaUserId,
        request: KomgaUserUpdateRequest
    ) {
    }

    override suspend fun updatePassword(userId: KomgaUserId, password: String) {
    }

    override suspend fun getMeAuthenticationActivity(
        pageRequest: KomgaPageRequest?,
        unpaged: Boolean
    ): Page<KomgaAuthenticationActivity> {
        return Page.empty()
    }

    override suspend fun getAuthenticationActivity(
        pageRequest: KomgaPageRequest?,
        unpaged: Boolean
    ): Page<KomgaAuthenticationActivity> {
        return Page.empty()
    }

    override suspend fun getLatestAuthenticationActivityForUser(userId: KomgaUserId): KomgaAuthenticationActivity? {
        return null
    }
}