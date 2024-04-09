package io.github.snd_r.komelia.http

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.settings.SecretsRepository
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*
import kotlinx.coroutines.flow.StateFlow

private const val rememberMeCookie = "remember-me"

private val logger = KotlinLogging.logger {}

class RememberMePersistingCookieStore(
    private val serverUrl: StateFlow<String>,
    private val secretsRepository: SecretsRepository,
) : CookiesStorage {
    private val delegate = AcceptAllCookiesStorage()

    suspend fun loadRememberMeCookie() {
        logger.info { "init cookies" }
        val url = URLBuilder(serverUrl.value).build()
        secretsRepository.getCookie(serverUrl.value)
            ?.let { parseServerSetCookieHeader(it) }
            ?.let {
                logger.info { "load cookie $it" }
                delegate.addCookie(url, it)
            }
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        logger.info { "add cookie $cookie" }
        if (cookie.name == rememberMeCookie && cookie.value.isNotBlank()) {
            logger.info { "set cookie $cookie" }
            secretsRepository.setCookie(serverUrl.value, renderSetCookieHeader(cookie))
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
