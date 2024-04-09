package io.github.snd_r.komelia.settings

import kotlinx.browser.document

class CookieStoreSecretsRepository : SecretsRepository {
    override suspend fun getCookie(url: String): String? {
        return document.cookie.ifBlank { null }
    }

    override suspend fun setCookie(url: String, cookie: String) {
        document.cookie = cookie
    }

    override suspend fun deleteCookie(url: String) {
        document.cookie = ""
    }
}