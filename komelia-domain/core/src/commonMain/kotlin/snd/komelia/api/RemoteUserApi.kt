package snd.komelia.api

import snd.komelia.komga.api.KomgaUserApi
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.user.KomgaUserClient
import snd.komga.client.user.KomgaUserCreateRequest
import snd.komga.client.user.KomgaUserId
import snd.komga.client.user.KomgaUserUpdateRequest

class RemoteUserApi(private val userClient: KomgaUserClient) : KomgaUserApi {
    override suspend fun logout() = userClient.logout()

    override suspend fun getMe() = userClient.getMe()

    override suspend fun getMe(
        username: String,
        password: String,
        rememberMe: Boolean
    ) = userClient.getMe(username, password, rememberMe)

    override suspend fun updateMyPassword(newPassword: String) = userClient.updateMyPassword(newPassword)

    override suspend fun getAllUsers() = userClient.getAllUsers()

    override suspend fun addUser(user: KomgaUserCreateRequest) = userClient.addUser(user)

    override suspend fun deleteUser(userId: KomgaUserId) = userClient.deleteUser(userId)

    override suspend fun updateUser(
        userId: KomgaUserId,
        request: KomgaUserUpdateRequest
    ) = userClient.updateUser(userId, request)

    override suspend fun updatePassword(userId: KomgaUserId, password: String) =
        userClient.updatePassword(userId, password)

    override suspend fun getMeAuthenticationActivity(
        pageRequest: KomgaPageRequest?,
        unpaged: Boolean
    ) = userClient.getMeAuthenticationActivity(pageRequest, unpaged)

    override suspend fun getAuthenticationActivity(
        pageRequest: KomgaPageRequest?,
        unpaged: Boolean
    ) = userClient.getAuthenticationActivity(pageRequest, unpaged)

    override suspend fun getLatestAuthenticationActivityForUser(userId: KomgaUserId) =
        userClient.getLatestAuthenticationActivityForUser(userId)
}