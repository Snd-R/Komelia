package snd.webview

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*

fun interface RequestInterceptor {
    suspend fun run(request: ResourceRequest): ResourceLoadResult?
}

data class ResourceRequest(
    val url: Url,
    val method: HttpMethod,
    val requestHeaders: Headers
)

suspend fun HttpClient.runRequest(request: ResourceRequest): ResourceLoadResult {
    val response = this.request {
        method = request.method
        url(request.url)
        headers.appendAll(
            request.requestHeaders.filter { headerName, _ -> headerName.lowercase() != "origin" })
    }
    val contentType = response.contentType()?.let { "${it.contentType}/${it.contentSubtype}" }

    return ResourceLoadResult(
        data = response.bodyAsBytes(),
        contentType = contentType
    )
}