package io.github.snd_r.komga.user

import io.github.snd_r.komga.common.KomgaPageRequest
import io.github.snd_r.komga.common.Page
import io.github.snd_r.komga.common.toParams
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*

class KomgaUserClient(private val ktor: HttpClient) {

    suspend fun logout() {
        ktor.post("api/logout")
    }

    suspend fun getMe(): KomgaUser {
        return ktor.get("api/v2/users/me").body()
    }

    suspend fun getMe(username: String, password: String, rememberMe: Boolean): KomgaUser {
        val usernameAndPassword = "$username:$password"
        val encoded = usernameAndPassword.encodeBase64()

        return ktor.get("api/v2/users/me") {
            header("Authorization", "Basic $encoded")
            header("Cache-Control", "no-cache, no-store, max-age=0")
            parameter("remember-me", rememberMe)
        }.body()
    }

    suspend fun updateMyPassword(newPassword: String) {
        ktor.patch("api/v2/users/me/password") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("password" to newPassword))
        }
    }

    suspend fun getAllUsers(): List<KomgaUser> {
        return ktor.get("api/v2/users").body()
    }

    suspend fun addUser(user: KomgaUserCreateRequest): KomgaUser {
        return ktor.post("api/v2/users") {
            contentType(ContentType.Application.Json)
            setBody(user)
        }.body()
    }

    suspend fun deleteUser(userId: KomgaUserId) {
        ktor.delete("api/v2/users/$userId")
    }

    suspend fun updateUser(userId: KomgaUserId, request: KomgaUserUpdateRequest) {
        ktor.patch("api/v2/users/$userId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun updatePassword(userId: KomgaUserId, password: String) {
        ktor.patch("api/v2/users/$userId/password") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("password" to password))
        }
    }

    suspend fun getMeAuthenticationActivity(
        pageRequest: KomgaPageRequest? = null,
        unpaged: Boolean = false,
    ): Page<KomgaAuthenticationActivity> {
        return ktor.get("api/v2/users/me/authentication-activity") {
            pageRequest?.let { url.parameters.appendAll(it.toParams()) }
            url.parameters.append("unpaged", unpaged.toString())
        }.body()
    }

    suspend fun getAuthenticationActivity(
        pageRequest: KomgaPageRequest? = null,
        unpaged: Boolean = false,
    ): Page<KomgaAuthenticationActivity> {
        return ktor.get("api/v2/users/authentication-activity") {
            pageRequest?.let { url.parameters.appendAll(it.toParams()) }
            url.parameters.append("unpaged", unpaged.toString())
        }.body()
    }

    suspend fun getLatestAuthenticationActivityForUser(userId: KomgaUserId): KomgaAuthenticationActivity {
        return ktor.get("api/v2/users/$userId/authentication-activity/latest").body()
    }

}