package io.github.snd_r.komga.sse

//import io.ktor.client.plugins.cookies.*
//import io.ktor.http.*
//import kotlinx.coroutines.runBlocking
//import okhttp3.Cookie
//import okhttp3.CookieJar
//import okhttp3.HttpUrl
//
//class KtorCookieJarWrapper(
//    private val ktorCookies: CookiesStorage
//) : CookieJar {
//    override fun loadForRequest(url: HttpUrl): List<Cookie> {
//        var cookies = emptyList<Cookie>()
//        runBlocking {
//            cookies = ktorCookies.get(toKtorUrl(url))
//                .mapNotNull { Cookie.parse(url, renderSetCookieHeader(it)) }
//        }
//        return cookies
//    }
//
//    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
//    }
//
//    private fun toKtorUrl(url: HttpUrl): Url {
//        return URLBuilder().apply {
//            protocol = if (url.isHttps) URLProtocol.HTTPS else URLProtocol.HTTP
//            host = url.host
//            port = url.port
//        }.build()
//    }
//}