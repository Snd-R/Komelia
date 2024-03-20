package io.github.snd_r.komelia.settings


interface SecretsRepository {

    fun getCookie(url: String): String?

    fun setCookie(url: String, cookie: String)

    fun deleteCookie(url: String)

}