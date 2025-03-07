package io.github.snd_r.komelia.http

import io.github.snd_r.komelia.settings.SecretsRepository
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*
import kotlinx.coroutines.flow.StateFlow

@Deprecated("changed to komga-remember-me since komga 1.21.0")
private const val deprecatedRememberMeCookie = "remember-me"
private const val rememberMeCookie = "komga-remember-me"

class RememberMePersistingCookieStore(
    private val komgaUrl: StateFlow<Url>,
    private val secretsRepository: SecretsRepository,
) : CookiesStorage {
    private val delegate = AcceptAllCookiesStorage()

    suspend fun loadRememberMeCookie() {
        val url = komgaUrl.value
        secretsRepository.getCookie(url.toString())
            ?.let { parseServerSetCookieHeader(it) }
            ?.let { delegate.addCookie(url, it) }
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        if (
            (cookie.name == rememberMeCookie || cookie.name == deprecatedRememberMeCookie)
            && cookie.value.isNotBlank()
            && komgaUrl.value.host == requestUrl.host
        ) {
            secretsRepository.setCookie(komgaUrl.value.toString(), renderSetCookieHeader(cookie))
        }

        delegate.addCookie(requestUrl, cookie)
    }

    override fun close() {
        delegate.close()
    }

    override suspend fun get(requestUrl: Url): List<Cookie> {
        return delegate.get(requestUrl)
    }
}
